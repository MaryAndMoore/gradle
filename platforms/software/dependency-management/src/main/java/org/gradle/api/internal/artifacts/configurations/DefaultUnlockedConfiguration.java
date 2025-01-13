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

package org.gradle.api.internal.artifacts.configurations;

import org.gradle.api.artifacts.ConfigurablePublishArtifact;
import org.gradle.api.artifacts.DependencyResolutionListener;
import org.gradle.api.capabilities.Capability;
import org.gradle.api.internal.DomainObjectContext;
import org.gradle.api.internal.artifacts.ResolveExceptionMapper;
import org.gradle.api.internal.artifacts.ivyservice.moduleconverter.RootComponentMetadataBuilder;
import org.gradle.api.internal.attributes.AttributeDesugaring;
import org.gradle.api.internal.attributes.AttributesFactory;
import org.gradle.api.internal.collections.DomainObjectCollectionFactory;
import org.gradle.api.internal.file.FileCollectionFactory;
import org.gradle.api.internal.project.ProjectStateRegistry;
import org.gradle.api.internal.tasks.TaskDependencyFactory;
import org.gradle.api.problems.internal.InternalProblems;
import org.gradle.internal.Factory;
import org.gradle.internal.code.UserCodeApplicationContext;
import org.gradle.internal.event.ListenerBroadcast;
import org.gradle.internal.model.CalculatedValueContainerFactory;
import org.gradle.internal.reflect.Instantiator;
import org.gradle.internal.typeconversion.NotationParser;

/**
 * A concrete {@link DefaultConfiguration} implementation which can change roles.
 */
public class DefaultUnlockedConfiguration extends DefaultConfiguration {

    public DefaultUnlockedConfiguration(
        DomainObjectContext domainObjectContext,
        String name,
        ConfigurationsProvider configurationsProvider,
        ConfigurationResolverFactory resolver,
        ListenerBroadcast<DependencyResolutionListener> dependencyResolutionListeners,
        Factory<ResolutionStrategyInternal> resolutionStrategyFactory,
        FileCollectionFactory fileCollectionFactory,
        Instantiator instantiator,
        NotationParser<Object, ConfigurablePublishArtifact> artifactNotationParser,
        NotationParser<Object, Capability> capabilityNotationParser,
        AttributesFactory attributesFactory,
        RootComponentMetadataBuilder rootComponentMetadataBuilder,
        ResolveExceptionMapper exceptionMapper,
        AttributeDesugaring attributeDesugaring,
        UserCodeApplicationContext userCodeApplicationContext,
        ProjectStateRegistry projectStateRegistry,
        DomainObjectCollectionFactory domainObjectCollectionFactory,
        CalculatedValueContainerFactory calculatedValueContainerFactory,
        DefaultConfigurationFactory defaultConfigurationFactory,
        TaskDependencyFactory taskDependencyFactory,
        ConfigurationRole roleAtCreation,
        InternalProblems problemsService
    ) {
        super(
            domainObjectContext,
            name,
            configurationsProvider,
            resolver,
            dependencyResolutionListeners,
            resolutionStrategyFactory,
            fileCollectionFactory,
            instantiator,
            artifactNotationParser,
            capabilityNotationParser,
            attributesFactory,
            rootComponentMetadataBuilder,
            exceptionMapper,
            attributeDesugaring,
            userCodeApplicationContext,
            projectStateRegistry,
            domainObjectCollectionFactory,
            calculatedValueContainerFactory,
            defaultConfigurationFactory,
            taskDependencyFactory,
            roleAtCreation,
            problemsService,
            false
        );
    }

}
