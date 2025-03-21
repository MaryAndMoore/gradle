/*
 * Copyright 2016 the original author or authors.
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

package org.gradle.composite.internal;

import org.gradle.api.artifacts.component.ComponentArtifactIdentifier;
import org.gradle.api.artifacts.component.ComponentIdentifier;
import org.gradle.api.tasks.TaskDependency;
import org.gradle.internal.component.local.model.LocalComponentArtifactMetadata;
import org.gradle.internal.component.model.IvyArtifactName;

import java.io.File;

/**
 * Formerly used to represent an artifact that belongs to a different build than the current build.
 * <p>
 * This class is no longer used as of Gradle 9.0, but remains as it is still referenced by KGP.
 *
 * @see <a href="https://github.com/JetBrains/kotlin/blame/c42ea0396c9e9dbcd504dae4d308ce5ad7522771/libraries/tools/kotlin-gradle-plugin/src/common/kotlin/org/jetbrains/kotlin/gradle/targets/js/npm/resolver/KotlinCompilationNpmResolver.kt#L254">Link</a>
 *
 * @deprecated This class is no longer in use as of Gradle 9.0.
 */
@Deprecated
public abstract class CompositeProjectComponentArtifactMetadata implements LocalComponentArtifactMetadata, ComponentArtifactIdentifier {

    @Override
    public String toString() {
        throw new UnsupportedOperationException();
    }

    public LocalComponentArtifactMetadata getDelegate() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ComponentIdentifier getComponentId() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ComponentArtifactIdentifier getId() {
        throw new UnsupportedOperationException();
    }

    @Override
    public IvyArtifactName getName() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ComponentIdentifier getComponentIdentifier() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getDisplayName() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getCapitalizedDisplayName() {
        throw new UnsupportedOperationException();
    }

    @Override
    public File getFile() {
        throw new UnsupportedOperationException();
    }

    @Override
    public TaskDependency getBuildDependencies() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean equals(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int hashCode() {
        throw new UnsupportedOperationException();
    }

}
