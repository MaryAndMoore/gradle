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

package org.gradle.api.problems.deprecation;

import org.gradle.api.Incubating;
import org.gradle.api.problems.DocLink;

/**
 * Common, shared specification for other deprecation specs to build upon.
 *
 * @param <T> the subtype of the deprecation spec
 * @since 8.14
 */
@Incubating
public interface BaseDeprecationSpec<T extends BaseDeprecationSpec<?>> {

    /**
     * Declares a longer, potentially multi-line detailing of the deprecation.
     * Possibly can be used to detail the reasons of the deprecation.
     *
     * @return the fluent builder used to call this
     * @since 8.14
     */
    T details(String reason);

    /**
     * Declares the replacement for the deprecated behavior.
     *
     * @param replacement the replacement for the deprecated behavior
     * @return the fluent builder used to call this
     * @since 8.14
     */
    T replacedBy(String replacement);

    /**
     * Declares in which version the deprecated item will be removed.
     *
     * @param version the version from which the deprecated behavior will be removed. E.g. "version-1.2.3"
     * @return the fluent builder used to call this
     * @since 8.14
     */
    T removedInVersion(String version);

    /**
     * Declares where extra documentation can be found.
     *
     * @param link a {@link DocLink} object describing where the documentation can be found
     * @return the fluent builder used to call this
     * @since 8.14
     */
    T documentedAt(DocLink link);

    /**
     * Declares where extra documentation can be found.
     *
     * @param url the URL to the documentation
     * @return the fluent builder used to call this
     */
    T documentedAt(String url);
}
