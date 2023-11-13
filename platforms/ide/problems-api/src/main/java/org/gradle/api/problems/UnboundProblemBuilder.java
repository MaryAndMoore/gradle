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

package org.gradle.api.problems;

import org.gradle.api.Incubating;
import org.gradle.util.Path;

import javax.annotation.Nullable;

/**
 * Problem builder creating basic, non-reportable problems without enforcing any particular invocation order.
 *
 * @since 8.6
 */
@Incubating
public interface UnboundProblemBuilder extends
    ProblemBuilderDefiningDocumentation,
    ProblemBuilderDefiningLocation,
    ProblemBuilderDefiningLabel,
    ProblemBuilderDefiningCategory,
    ProblemBuilder {

    UnboundProblemBuilder documentedAt(DocLink doc);
    UnboundProblemBuilder undocumented();
    UnboundProblemBuilder fileLocation(String path, @Nullable Integer line, @Nullable Integer column, @Nullable Integer length);
    UnboundProblemBuilder pluginLocation(String pluginId);
    UnboundProblemBuilder stackLocation();
    UnboundProblemBuilder noLocation();
    UnboundProblemBuilder taskPathLocation(Path taskPath);
    UnboundProblemBuilder label(String label, Object... args);
    UnboundProblemBuilder category(String category, String... details);
    UnboundProblemBuilder details(String details);
    UnboundProblemBuilder solution(String solution);
    UnboundProblemBuilder additionalData(String key, String value);
    UnboundProblemBuilder withException(RuntimeException e);
    UnboundProblemBuilder severity(Severity severity);

}
