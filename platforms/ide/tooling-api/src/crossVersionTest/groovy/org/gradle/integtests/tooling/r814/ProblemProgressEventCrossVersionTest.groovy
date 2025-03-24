/*
 * Copyright 2023 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradle.integtests.tooling.r814


import org.gradle.integtests.tooling.fixture.TargetGradleVersion
import org.gradle.integtests.tooling.fixture.ToolingApiSpecification
import org.gradle.integtests.tooling.fixture.ToolingApiVersion
import org.gradle.tooling.events.ProgressEvent
import org.gradle.tooling.events.ProgressListener
import org.gradle.tooling.events.problems.Problem
import org.gradle.tooling.events.problems.SingleProblemEvent
import org.gradle.workers.fixtures.WorkerExecutorFixture

@ToolingApiVersion(">=8.14")
@TargetGradleVersion(">=8.14")
class ProblemProgressEventCrossVersionTest extends ToolingApiSpecification {

    def setupBuild() {
        file('buildSrc/build.gradle') << """
            plugins {
                id 'java'
            }

            dependencies {
                implementation(gradleApi())
            }
        """
        file('buildSrc/src/main/java/org/gradle/test/ProblemsWorkerTaskParameter.java') << """
            package org.gradle.test;

            import org.gradle.workers.WorkParameters;

            public interface ProblemsWorkerTaskParameter extends WorkParameters { }
        """
        file('buildSrc/src/main/java/org/gradle/test/SomeOtherData.java') << """
            package org.gradle.test;

            import org.gradle.api.provider.Property;

            public interface SomeOtherData{
                String getOtherName();
                void setOtherName(String name);
            }

        """
        file('buildSrc/src/main/java/org/gradle/test/SomeData.java') << """
            package org.gradle.test;

            import org.gradle.api.problems.AdditionalData;
            import org.gradle.api.provider.Property;

            import java.util.List;

            public interface SomeData extends AdditionalData {
                  Property<String> getSome();
                  String getName();
                  void setName(String name);

                  List<String> getNames();
                  void setNames(List<String> names);

                  SomeOtherData getOtherData();
                  void setOtherData(SomeOtherData otherData);
            }
        """
        file('buildSrc/src/main/java/org/gradle/test/ProblemWorkerAction.java') << """
            package org.gradle.test;

            import java.io.File;
            import java.util.Collections;
            import java.io.FileWriter;
            import org.gradle.api.problems.Problems;
            import org.gradle.api.problems.internal.InternalProblems;
            import org.gradle.api.problems.ProblemId;
            import org.gradle.api.problems.ProblemGroup;
            import org.gradle.api.model.ObjectFactory;

            import org.gradle.workers.WorkAction;

            import javax.inject.Inject;

            public abstract class ProblemWorkerAction implements WorkAction<ProblemsWorkerTaskParameter> {

                @Inject
                public abstract InternalProblems getProblems();

                @Inject
                public abstract ObjectFactory getObjectFactory();

                @Override
                public void execute() {
                    try {
                        ProblemId problemId = ProblemId.create("type", "label", ProblemGroup.create("generic", "Generic"));
                        getProblems().getReporter().report(
                            problemId,
                            problem -> problem
                                .additionalData(SomeData.class, d -> {
                                    d.getSome().set("some");
                                    d.setName("someData");
                                    d.setNames(Collections.singletonList("someMoreData"));
                                    SomeOtherData sod = getObjectFactory().newInstance(SomeOtherData.class);
                                    sod.setOtherName("otherName");
                                    d.setOtherData(sod);
                                })
                            );
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        """
    }

    interface SomeOtherDataView {
        String getOtherName();
    }

    interface SomeDataView {
        String getSome();

        String getName();

        List<String> getNames();

        SomeOtherDataView getOtherData();
    }

    def "problems are emitted correctly from a worker when using #isolationMode"() {
        setupBuild()

        given:
        buildFile << """
            import javax.inject.Inject
            import org.gradle.test.ProblemWorkerAction

            abstract class ProblemTask extends DefaultTask {
                @Inject
                abstract WorkerExecutor getWorkerExecutor();

                @TaskAction
                void executeTask() {
                    getWorkerExecutor().${isolationMode}().submit(ProblemWorkerAction.class) {}
                }
            }

            tasks.register("reportProblem", ProblemTask)
        """

        when:
        def listener = new ProblemProgressListener()
        withConnection { connection ->
            connection.newBuild()
                .forTasks("reportProblem")
                .addProgressListener(listener)
                .setStandardError(System.err)
                .setStandardOutput(System.out)
                .addArguments("--info")
                .run()
        }

        then:
        // TODO: Remove, only for testing
        listener.problems.forEach {
            println "Problem: ${it.definition.id.name} (${it.definition}"
            println "  " + it.contextualLabel
            println "  " + it.details
        }
        listener.problems.size() == 1

        def someDataView = listener.problems[0].additionalData.get(SomeDataView)
        someDataView.name == "someData"
        someDataView.some == "some"
        someDataView.names == ["someMoreData"]
        someDataView.otherData.otherName == "otherName"

        where:
        isolationMode << WorkerExecutorFixture.ISOLATION_MODES
    }

    def "deprecations with their own additional data types are reported correctly"() {
        buildFile """
            import org.gradle.api.problems.Severity
            import org.gradle.api.problems.AdditionalData
            import org.gradle.api.problems.deprecation.source.ReportSource

            abstract class DeprecatingTask extends DefaultTask {
                @Inject
                protected abstract Problems getProblems();

                @TaskAction
                void run() {
                    getProblems()
                        .getDeprecationReporter()
                        .deprecate(${reportSource}, "test deprecation", feature -> feature
                            .removedInVersion("x.y.z")
                            .replacedBy("replacement")
                            .details("reasoning")
                        )
                }
            }

            tasks.register("reportDeprecation", DeprecatingTask)
        """

        when:
        def listener = new ProblemProgressListener()
        withConnection { connection ->
            connection
                .newBuild()
                .forTasks("reportDeprecation")
                .addProgressListener(listener)
                .run()
        }

        then:
        def problems = listener.problems
        problems.size() == 1

        def deprecation = problems[0]
        deprecation.contextualLabel.contextualLabel == "test deprecation"
        deprecation.details.details == "reasoning"
        def additionalData = deprecation.additionalData.asMap
        additionalData.size() == 3
        additionalData['removedIn'] == "x.y.z"
        additionalData['replacedBy'] == "replacement"
        additionalData['source'] == expectedSourceFields

        where:
        reportSource                         | expectedSourceFields
        "ReportSource.gradle()"              | ["name": "gradle"]
        "ReportSource.plugin(\"plugin.id\")" | ["name": "plugin", "id": "plugin.id"]
    }

    class ProblemProgressListener implements ProgressListener {
        List<Problem> problems = []

        @Override
        void statusChanged(ProgressEvent event) {
            if (event instanceof SingleProblemEvent) {
                this.problems.add(event.problem)
            }
        }
    }
}
