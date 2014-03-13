/*
 * Copyright 2012 the original author or authors.
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
package org.gradle.api.internal.artifacts.ivyservice;

import org.gradle.api.artifacts.resolution.SoftwareArtifact;
import org.gradle.api.internal.artifacts.metadata.ModuleVersionArtifactMetaData;
import org.gradle.api.internal.artifacts.metadata.ModuleVersionMetaData;

public interface ArtifactResolver {
    /**
     * Resolves the given artifact. Any failures are packaged up in the result.
     */
    void resolve(ModuleVersionArtifactMetaData artifact, BuildableArtifactResolveResult result);

    /**
     * Resolves artifacts of the given type and belonging to the given module. Any failures are packaged up in the result.
     */
    void resolve(ModuleVersionMetaData moduleMetadata, Class<? extends SoftwareArtifact> artifactType, BuildableMultipleArtifactResolveResult result);
}
