/*
 * Copyright 2020 the original author or authors.
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

package org.gradle.internal.enterprise.legacy

import org.gradle.integtests.fixtures.AbstractIntegrationSpec

class LegacyGradleEnterpriseCheckInConfigCachingIntegTest extends AbstractIntegrationSpec {

    def scanPlugin = new GradleEnterprisePluginLegacyContactPointFixture(testDirectory, mavenRepo, createExecuter())

    def "plugin #pluginVersion is unsupported"() {
        given:
        settingsFile << scanPlugin.pluginManagement()

        scanPlugin.with {
            logConfig = true
            logApplied = true
            runtimeVersion = pluginVersion
            publishDummyPlugin(executer)
        }

        buildFile << """
            task t
        """

        when:
        succeeds "t"

        then:
        scanPlugin.assertUnsupportedMessage(output, "Gradle Enterprise plugin $pluginVersion has been disabled as it is incompatible with this version of Gradle. Upgrade to Gradle Enterprise plugin 3.13.1 or newer to restore functionality.")

        where:
        // Legacy check-in with graceful unsupported message works with this plugin versions range:
        pluginVersion << ["3.0", "3.3.4"]
    }

}
