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

package org.gradle.internal.isolated.models;

import org.gradle.api.isolated.models.ProjectScopeModelRequest;
import org.gradle.api.provider.Provider;

import java.util.List;

public class AllModelsRequest implements ProjectScopeModelRequest<Object> {
    private final ProjectModelController projectModelController;
    private final ProjectModelScopeIdentifier consumerScope;

    public AllModelsRequest(
        ProjectModelController projectModelController,
        ProjectModelScopeIdentifier consumerScope
    ) {
        this.projectModelController = projectModelController;
        this.consumerScope = consumerScope;
    }

    @Override
    public Provider<List<Object>> getAll() {
        return projectModelController.request(getRequest());
    }

    @Override
    public Provider<List<Object>> getPresent() {
        return projectModelController.request(getRequest());
    }

    private ProjectScopeModelBatchRequest<Object> getRequest() {
        return ProjectScopeModelBatchRequest.allModels(consumerScope);
    }
}
