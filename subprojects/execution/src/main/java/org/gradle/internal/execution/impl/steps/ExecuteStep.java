/*
 * Copyright 2018 the original author or authors.
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

package org.gradle.internal.execution.impl.steps;

import org.gradle.api.BuildCancelledException;
import org.gradle.api.GradleException;
import org.gradle.caching.internal.origin.OriginMetadata;
import org.gradle.initialization.BuildCancellationToken;
import org.gradle.internal.execution.ExecutionException;
import org.gradle.internal.execution.ExecutionOutcome;
import org.gradle.internal.execution.OutputChangeListener;
import org.gradle.internal.execution.Result;
import org.gradle.internal.execution.UnitOfWork;
import org.gradle.internal.id.UniqueId;

import javax.annotation.Nullable;

import static org.gradle.internal.execution.ExecutionOutcome.EXECUTED;
import static org.gradle.internal.execution.ExecutionOutcome.UP_TO_DATE;

public class ExecuteStep implements Step<Context, Result> {

    private final BuildCancellationToken cancellationToken;
    private final OutputChangeListener outputChangeListener;
    private final UniqueId buildInvocationScopeId;

    public ExecuteStep(
        UniqueId buildInvocationScopeId,
        BuildCancellationToken cancellationToken,
        OutputChangeListener outputChangeListener
    ) {
        this.buildInvocationScopeId = buildInvocationScopeId;
        this.cancellationToken = cancellationToken;
        this.outputChangeListener = outputChangeListener;
    }

    @Override
    public Result execute(Context context) {
        UnitOfWork work = context.getWork();
        boolean didWork = true;
        GradleException failure;
        try {
            outputChangeListener.beforeOutputChange();

            didWork = work.execute();
            if (cancellationToken.isCancellationRequested()) {
                failure = new BuildCancelledException("Build cancelled during executing " + work.getDisplayName());
            } else {
                failure = null;
            }
        } catch (Throwable t) {
            // TODO Should we catch Exception here?
            failure = new ExecutionException(work, t);
        }

        ExecutionOutcome outcome = (failure != null || didWork)
            ? EXECUTED
            : UP_TO_DATE;
        OriginMetadata originMetadata = OriginMetadata.fromCurrentBuild(buildInvocationScopeId, work.markExecutionTime());
        Throwable finalFailure = failure;

        return new Result() {
            @Override
            public ExecutionOutcome getOutcome() {
                return outcome;
            }

            @Override
            public OriginMetadata getOriginMetadata() {
                return originMetadata;
            }

            @Nullable
            @Override
            public Throwable getFailure() {
                return finalFailure;
            }
        };
    }
}
