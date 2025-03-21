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

package org.gradle.internal.enterprise.impl;

import org.gradle.internal.buildtree.BuildModelParameters;
import org.gradle.internal.enterprise.GradleEnterprisePluginCheckInResult;
import org.gradle.internal.enterprise.GradleEnterprisePluginCheckInService;
import org.gradle.internal.enterprise.GradleEnterprisePluginMetadata;
import org.gradle.internal.enterprise.GradleEnterprisePluginServiceFactory;
import org.gradle.internal.enterprise.GradleEnterprisePluginServiceRef;
import org.gradle.internal.enterprise.core.GradleEnterprisePluginManager;
import org.gradle.internal.enterprise.impl.legacy.UnsupportedBuildScanPluginVersionException;
import org.gradle.util.internal.VersionNumber;

import java.util.function.Supplier;

public class DefaultGradleEnterprisePluginCheckInService implements GradleEnterprisePluginCheckInService {

    private final GradleEnterprisePluginManager manager;
    private final DefaultGradleEnterprisePluginAdapterFactory pluginAdapterFactory;
    private final boolean isIsolatedProjectsEnabled;

    public DefaultGradleEnterprisePluginCheckInService(
        BuildModelParameters buildModelParameters,
        GradleEnterprisePluginManager manager,
        DefaultGradleEnterprisePluginAdapterFactory pluginAdapterFactory
    ) {
        this.manager = manager;
        this.pluginAdapterFactory = pluginAdapterFactory;
        this.isIsolatedProjectsEnabled = buildModelParameters.isIsolatedProjects();
    }

    // Used just for testing
    public static final String UNSUPPORTED_TOGGLE = "org.gradle.internal.unsupported-enterprise-plugin";
    public static final String UNSUPPORTED_TOGGLE_MESSAGE = "Enterprise plugin unsupported due to secret toggle";

    public static final VersionNumber MINIMUM_SUPPORTED_PLUGIN_VERSION_FOR_ISOLATED_PROJECTS = VersionNumber.version(3, 15);
    public static final String UNSUPPORTED_PLUGIN_DUE_TO_ISOLATED_PROJECTS_MESSAGE = "Gradle Enterprise plugin has been disabled as it is incompatible with the isolated projects feature";

    private static final String DISABLE_TEST_ACCELERATION_PROPERTY = "gradle.internal.testacceleration.disableImplicitApplication";

    @Override
    public GradleEnterprisePluginCheckInResult checkIn(GradleEnterprisePluginMetadata pluginMetadata, GradleEnterprisePluginServiceFactory serviceFactory) {
        if (Boolean.getBoolean(UNSUPPORTED_TOGGLE)) {
            return checkInUnsupportedResult(UNSUPPORTED_TOGGLE_MESSAGE);
        }

        String pluginVersion = pluginMetadata.getVersion();
        VersionNumber pluginBaseVersion = VersionNumber.parse(pluginVersion).getBaseVersion();

        if (isUnsupportedWithIsolatedProjects(pluginBaseVersion)) {
            // Until GE plugin 3.14, Test Acceleration is applied even if the check-in returns an "unsupported" result.
            // We have to disable it explicitly, because it is not compatible with isolated projects.
            System.setProperty(DISABLE_TEST_ACCELERATION_PROPERTY, "true");

            return checkInUnsupportedResult(UNSUPPORTED_PLUGIN_DUE_TO_ISOLATED_PROJECTS_MESSAGE);
        }

        if (pluginBaseVersion.compareTo(GradleEnterprisePluginManager.FIRST_GRADLE_ENTERPRISE_PLUGIN_VERSION) < 0) {
            throw new UnsupportedBuildScanPluginVersionException(GradleEnterprisePluginManager.OLD_SCAN_PLUGIN_VERSION_MESSAGE);
        }

        DefaultGradleEnterprisePluginAdapter adapter = pluginAdapterFactory.create(serviceFactory);
        GradleEnterprisePluginServiceRef ref = adapter.getPluginServiceRef();
        manager.registerAdapter(adapter);
        return checkInResult(null, () -> ref);
    }

    private GradleEnterprisePluginCheckInResult checkInUnsupportedResult(String unsupportedMessage) {
        manager.unsupported();
        return checkInResult(unsupportedMessage, () -> {
            throw new IllegalStateException();
        });
    }

    private static GradleEnterprisePluginCheckInResult checkInResult(String unsupportedMessage, Supplier<GradleEnterprisePluginServiceRef> pluginServiceRefSupplier) {
        return new GradleEnterprisePluginCheckInResult() {
            @Override
            public String getUnsupportedMessage() {
                return unsupportedMessage;
            }

            @Override
            public GradleEnterprisePluginServiceRef getPluginServiceRef() {
                return pluginServiceRefSupplier.get();
            }
        };
    }

    private boolean isUnsupportedWithIsolatedProjects(VersionNumber pluginBaseVersion) {
        return isIsolatedProjectsEnabled && MINIMUM_SUPPORTED_PLUGIN_VERSION_FOR_ISOLATED_PROJECTS.compareTo(pluginBaseVersion) > 0;
    }

}
