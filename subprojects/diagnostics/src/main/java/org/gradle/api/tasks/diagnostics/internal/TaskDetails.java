/*
 * Copyright 2010 the original author or authors.
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
package org.gradle.api.tasks.diagnostics.internal;

import org.gradle.api.Task;
import org.gradle.api.internal.plugins.DslObject;
import org.gradle.api.reflect.TypeOf;
import org.gradle.util.Path;

import javax.annotation.Nullable;
import java.util.Objects;

public interface TaskDetails {
    Path getPath();

    default String getName() {
        return Objects.requireNonNull(getPath().getName());
    }

    @Nullable
    String getDescription();

    TypeOf<?> getType();

    static TaskDetails of(Path path, Task task) {
        return new DefaultTaskDetails(path, new DslObject(task).getPublicType(), task.getDescription());
    }

    static TaskDetails of(Path path, TypeOf<?> type, @Nullable String description) {
        return new DefaultTaskDetails(path, type, description);
    }

    final class DefaultTaskDetails implements TaskDetails {
        private final Path path;
        private final TypeOf<?> taskType;
        @Nullable private final String description;

        private DefaultTaskDetails(Path path, TypeOf<?> taskType, @Nullable String description) {
            this.path = path;
            this.taskType = taskType;
            this.description = description;
        }

        @Override
        public Path getPath() {
            return path;
        }

        @Override
        public TypeOf<?> getType() {
            return taskType;
        }

        @Nullable
        @Override
        public String getDescription() {
            return description;
        }

    }
}
