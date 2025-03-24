/*
 * Copyright 2021 the original author or authors.
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

package org.gradle.plugins.ide.idea;

import org.gradle.integtests.fixtures.AbstractIntegrationSpec
import org.gradle.integtests.fixtures.ToBeFixedForConfigurationCache
import org.gradle.util.GradleVersion

import static org.gradle.plugins.ide.fixtures.IdeaFixtures.parseIml

class IdeaSourceDirTypesIntegrationTests extends AbstractIntegrationSpec {
    @ToBeFixedForConfigurationCache
    def "properly marks additional source sets created by test suites as test source"() {
        given:
        settingsFile << """
            rootProject.name = 'root'
        """

        buildFile << """
            plugins {
                id 'java'
                id 'idea'
            }

            ${mavenCentralRepository()}

            testing {
                suites {
                    integTest(JvmTestSuite)
                }
            }
        """

        file('src/integTest/java').createDir()

        when:
        executer.expectDeprecationWarning("The IdeaModule.testSourceDirs property has been deprecated. This is scheduled to be removed in Gradle 9.0. Please use the testSources property instead. For more information, please refer to https://docs.gradle.org/${GradleVersion.current().version}/dsl/org.gradle.plugins.ide.idea.model.IdeaModule.html#org.gradle.plugins.ide.idea.model.IdeaModule:testSourceDirs in the Gradle documentation.")
        executer.expectDeprecationWarning("The IdeaModule.testResourceDirs property has been deprecated. This is scheduled to be removed in Gradle 9.0. Please use the testResources property instead. For more information, please refer to https://docs.gradle.org/${GradleVersion.current().version}/dsl/org.gradle.plugins.ide.idea.model.IdeaModule.html#org.gradle.plugins.ide.idea.model.IdeaModule:testResourceDirs in the Gradle documentation.")
        succeeds ":idea"

        then:
        result.assertTasksExecuted(":ideaModule", ":ideaProject", ":ideaWorkspace", ":idea")

        def moduleFixture = parseIml(file ( 'root.iml'))
        def sources = moduleFixture.getContent().getProperty("sources")
        def integTestSource = sources.find { s -> s.url == 'file://$MODULE_DIR$/src/integTest/java' }
        assert integTestSource.isTestSource
    }
}
