/*
 * Copyright 2025 the original author or authors.
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

package org.gradle.internal.component.resolution.failure.describer;

import org.gradle.StartParameter;
import org.gradle.api.artifacts.result.ComponentSelectionCause;
import org.gradle.internal.component.resolution.failure.SelectionReasonAssessor.AssessedSelection.AssessedSelectionReason;
import org.gradle.internal.component.resolution.failure.exception.ConflictingConstraintsException;
import org.gradle.internal.component.resolution.failure.type.ModuleRejectedFailure;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * A {@link ResolutionFailureDescriber} that describes a {@link ModuleRejectedFailure} where
 * there were multiple constraints involved in a selection failure that each require different versions.
 * <p>
 * This describer will <strong>NOT</strong> be used during the dependencyInsight task, to leave that task
 * as a means of providing more detailed information about the conflicting constraints.
 */
public abstract class ModuleRejectedIncompatibleConstraintsFailureDescriber extends AbstractResolutionFailureDescriber<ModuleRejectedFailure> {
    private static final String DEPENDENCY_INSIGHT_TASK_NAME = "dependencyInsight";
    private static final String DEBUGGING_WITH_DEPENDENCY_INSIGHT_PREFIX = "Debugging using the " + DEPENDENCY_INSIGHT_TASK_NAME + " report is described in more detail at: ";
    private static final String DEBUGGING_WITH_DEPENDENCY_INSIGHT_ID = "viewing_debugging_dependencies";
    private static final String DEBUGGING_WITH_DEPENDENCY_INSIGHT_SECTION = "sec:identifying-reason-dependency-selection";

    @Inject
    public abstract StartParameter getStartParameter();

    @Override
    public boolean canDescribeFailure(ModuleRejectedFailure failure) {
        boolean dependencyInsightRunning = getStartParameter().getTaskRequests().stream().anyMatch(request -> request.getArgs().get(0).contains(DEPENDENCY_INSIGHT_TASK_NAME));
        return !dependencyInsightRunning && findConflictingConstraints(failure).size() > 1;
    }

    private List<AssessedSelectionReason> findConflictingConstraints(ModuleRejectedFailure failure) {
        Map<String, List<AssessedSelectionReason>> versionsByReasons = failure.getAssessedSelection().getReasons().stream()
            .filter(reason -> reason.getCause() == ComponentSelectionCause.CONSTRAINT)
            .collect(Collectors.groupingBy(AssessedSelectionReason::getRequiredVersion));
        return versionsByReasons.values().stream()
            .map(reasons -> reasons.iterator().next())
            .collect(Collectors.toList());
    }

    @Override
    public ConflictingConstraintsException describeFailure(ModuleRejectedFailure failure) {
        return new ConflictingConstraintsException(summarizeFailure(failure), failure, buildResolutions(failure));
    }

    private List<String> buildResolutions(ModuleRejectedFailure failure) {
        List<String> resolutions = new ArrayList<>(failure.getResolutions().size() + 1);
        resolutions.addAll(failure.getResolutions());
        resolutions.add(DEBUGGING_WITH_DEPENDENCY_INSIGHT_PREFIX + getDocumentationRegistry().getDocumentationFor(DEBUGGING_WITH_DEPENDENCY_INSIGHT_ID, DEBUGGING_WITH_DEPENDENCY_INSIGHT_SECTION) + ".");
        return resolutions;
    }

    private String summarizeFailure(ModuleRejectedFailure failure) {
        StringBuilder sb = new StringBuilder("Component is the target of multiple version constraints with conflicting requirements:\n");
        findConflictingConstraints(failure).forEach(reason -> sb.append(summarizeReason(reason)).append("\n"));
        return sb.toString();
    }

    private String summarizeReason(AssessedSelectionReason reason) {
        StringBuilder sb = new StringBuilder(reason.getRequiredVersion());

        if (reason.getSegmentedSelectionPath().size() > 2) {
            String lastSegment = reason.getSegmentedSelectionPath().get(reason.getSegmentedSelectionPath().size() - 2);
            sb.append(" - via ")
                .append(lastSegment);
        } else if (reason.isFromLock()) {
            sb.append(" - via Dependency Locking");
        }

        return sb.toString();
    }
}
