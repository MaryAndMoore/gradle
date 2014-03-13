/*
 * Copyright 2013 the original author or authors.
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

package org.gradle.api.internal.artifacts.ivyservice.ivyresolve.parser

import org.gradle.api.internal.artifacts.ivyservice.ivyresolve.parser.data.MavenDependencyKey
import org.gradle.api.internal.artifacts.ivyservice.ivyresolve.parser.data.PomProfile

class PomReaderProfileTest extends AbstractPomReaderTest {
    def "parse POM without active profile"() {
        when:
        pomFile << """
<project>
    <modelVersion>4.0.0</modelVersion>
    <groupId>group-one</groupId>
    <artifactId>artifact-one</artifactId>
    <version>version-one</version>
    <name>Test Artifact One</name>
    <profiles>
        <profile>
            <id>profile-1</id>
            <activation>
                <activeByDefault>false</activeByDefault>
            </activation>
        </profile>
        <profile>
            <id>profile-2</id>
            <activation/>
        </profile>
        <profile>
            <id>profile-3</id>
        </profile>
    </profiles>
</project>
"""
        pomReader = new PomReader(locallyAvailableExternalResource)

        then:
        pomReader.parseActivePomProfiles().size() == 0
    }

    def "parse POM with active profile providing properties"() {
        when:
        pomFile << """
<project>
    <modelVersion>4.0.0</modelVersion>
    <groupId>group-one</groupId>
    <artifactId>artifact-one</artifactId>
    <version>version-one</version>
    <name>Test Artifact One</name>
    <profiles>
        <profile>
            <id>profile-1</id>
            <activation>
                <activeByDefault>true</activeByDefault>
            </activation>
            <properties>
                <groupId.prop>group-two</groupId.prop>
                <artifactId.prop>artifact-two</artifactId.prop>
                <version.prop>version-two</version.prop>
            </properties>
        </profile>
    </profiles>
</project>
"""
        pomReader = new PomReader(locallyAvailableExternalResource)

        then:
        List<PomProfile> activePomProfiles = pomReader.parseActivePomProfiles()
        activePomProfiles.size() == 1
        PomProfile pomProfile = activePomProfiles[0]
        pomProfile
        pomProfile.id == 'profile-1'
        pomProfile.properties.size() == 3
        pomProfile.properties['groupId.prop'] == 'group-two'
        pomProfile.properties['artifactId.prop'] == 'artifact-two'
        pomProfile.properties['version.prop'] == 'version-two'
        pomReader.properties.size() == 7
        pomReader.properties['groupId.prop'] == 'group-two'
        pomReader.properties['artifactId.prop'] == 'artifact-two'
        pomReader.properties['version.prop'] == 'version-two'
    }

    def "parse POM with multiple active profile providing same properties"() {
        when:
        pomFile << """
<project>
    <modelVersion>4.0.0</modelVersion>
    <groupId>group-one</groupId>
    <artifactId>artifact-one</artifactId>
    <version>version-one</version>
    <name>Test Artifact One</name>
    <profiles>
        <profile>
            <id>profile-1</id>
            <activation>
                <activeByDefault>true</activeByDefault>
            </activation>
            <properties>
                <groupId.prop>group-two</groupId.prop>
                <artifactId.prop>artifact-two</artifactId.prop>
                <version.prop>version-two</version.prop>
            </properties>
        </profile>
        <profile>
            <id>profile-1</id>
            <activation>
                <activeByDefault>true</activeByDefault>
            </activation>
            <properties>
                <groupId.prop>group-two</groupId.prop>
                <artifactId.prop>artifact-two</artifactId.prop>
                <version.prop>version-two</version.prop>
            </properties>
        </profile>
    </profiles>
</project>
"""
        pomReader = new PomReader(locallyAvailableExternalResource)

        then:
        pomReader.parseActivePomProfiles().size() == 2
        pomReader.properties.size() == 7
        pomReader.properties['groupId.prop'] == 'group-two'
        pomReader.properties['artifactId.prop'] == 'artifact-two'
        pomReader.properties['version.prop'] == 'version-two'
    }

    def "get dependencies with custom properties of active profile"() {
        when:
        pomFile << """
<project>
    <modelVersion>4.0.0</modelVersion>
    <groupId>group-one</groupId>
    <artifactId>artifact-one</artifactId>
    <version>version-one</version>
    <name>Test Artifact One</name>
    <dependencies>
        <dependency>
            <groupId>\${groupId.prop}</groupId>
            <artifactId>\${artifactId.prop}</artifactId>
            <version>\${version.prop}</version>
        </dependency>
    </dependencies>
    <profiles>
        <profile>
            <id>profile-1</id>
            <activation>
                <activeByDefault>true</activeByDefault>
            </activation>
            <properties>
                <groupId.prop>group-two</groupId.prop>
                <artifactId.prop>artifact-two</artifactId.prop>
                <version.prop>version-two</version.prop>
            </properties>
        </profile>
    </profiles>
</project>
"""

        pomReader = new PomReader(locallyAvailableExternalResource)
        MavenDependencyKey key = new MavenDependencyKey('group-two', 'artifact-two', 'jar', null)

        then:
        pomReader.getDependencies().size() == 1
        assertResolvedPomDependency(key, 'version-two')
    }

    def "custom properties from last active profile determines dependency coordinates"() {
        when:
        pomFile << """
<project>
    <modelVersion>4.0.0</modelVersion>
    <groupId>group-one</groupId>
    <artifactId>artifact-one</artifactId>
    <version>version-one</version>
    <name>Test Artifact One</name>
    <dependencies>
        <dependency>
            <groupId>\${groupId.prop}</groupId>
            <artifactId>\${artifactId.prop}</artifactId>
            <version>\${version.prop}</version>
        </dependency>
    </dependencies>
    <profiles>
        <profile>
            <id>profile-1</id>
            <activation>
                <activeByDefault>true</activeByDefault>
            </activation>
            <properties>
                <groupId.prop>group-two</groupId.prop>
                <artifactId.prop>artifact-two</artifactId.prop>
                <version.prop>version-two</version.prop>
            </properties>
        </profile>
        <profile>
            <id>profile-2</id>
            <activation>
                <activeByDefault>true</activeByDefault>
            </activation>
            <properties>
                <groupId.prop>group-two</groupId.prop>
                <artifactId.prop>artifact-two</artifactId.prop>
                <version.prop>version-three</version.prop>
            </properties>
        </profile>
    </profiles>
</project>
"""

        pomReader = new PomReader(locallyAvailableExternalResource)
        MavenDependencyKey key = new MavenDependencyKey('group-two', 'artifact-two', 'jar', null)

        then:
        pomReader.getDependencies().size() == 1
        assertResolvedPomDependency(key, 'version-three')
    }

    def "properties from an active profile override existing POM properties"() {
        when:
        pomFile << """
<project>
    <modelVersion>4.0.0</modelVersion>
    <groupId>group-one</groupId>
    <artifactId>artifact-one</artifactId>
    <version>version-one</version>
    <name>Test Artifact One</name>
    <properties>
        <groupId.prop>group-two</groupId.prop>
        <artifactId.prop>artifact-two</artifactId.prop>
        <version.prop>version-two</version.prop>
    </properties>
    <dependencies>
        <dependency>
            <groupId>\${groupId.prop}</groupId>
            <artifactId>\${artifactId.prop}</artifactId>
            <version>\${version.prop}</version>
        </dependency>
    </dependencies>
    <profiles>
        <profile>
            <id>profile-1</id>
            <activation>
                <activeByDefault>true</activeByDefault>
            </activation>
            <properties>
                <groupId.prop>group-two</groupId.prop>
                <artifactId.prop>artifact-two</artifactId.prop>
                <version.prop>version-three</version.prop>
            </properties>
        </profile>
    </profiles>
</project>
"""

        pomReader = new PomReader(locallyAvailableExternalResource)
        MavenDependencyKey key = new MavenDependencyKey('group-two', 'artifact-two', 'jar', null)

        then:
        pomReader.getDependencies().size() == 1
        assertResolvedPomDependency(key, 'version-three')
    }

    def "multiple active profiles can determine different dependency coordinates"() {
        when:
        pomFile << """
<project>
    <modelVersion>4.0.0</modelVersion>
    <groupId>group-one</groupId>
    <artifactId>artifact-one</artifactId>
    <version>version-one</version>
    <name>Test Artifact One</name>
    <dependencies>
        <dependency>
            <groupId>\${groupId.prop}</groupId>
            <artifactId>\${artifactId.prop}</artifactId>
            <version>\${version.prop}</version>
        </dependency>
        <dependency>
            <groupId>\${otherGroupId.prop}</groupId>
            <artifactId>\${otherArtifactId.prop}</artifactId>
            <version>\${otherVersion.prop}</version>
        </dependency>
    </dependencies>
    <profiles>
        <profile>
            <id>profile-1</id>
            <activation>
                <activeByDefault>true</activeByDefault>
            </activation>
            <properties>
                <groupId.prop>group-two</groupId.prop>
                <artifactId.prop>artifact-two</artifactId.prop>
                <version.prop>version-two</version.prop>
            </properties>
        </profile>
        <profile>
            <id>profile-2</id>
            <activation>
                <activeByDefault>true</activeByDefault>
            </activation>
            <properties>
                <otherGroupId.prop>group-three</otherGroupId.prop>
                <otherArtifactId.prop>artifact-three</otherArtifactId.prop>
                <otherVersion.prop>version-three</otherVersion.prop>
            </properties>
        </profile>
    </profiles>
</project>
"""

        pomReader = new PomReader(locallyAvailableExternalResource)
        MavenDependencyKey keyDep1 = new MavenDependencyKey('group-two', 'artifact-two', 'jar', null)
        MavenDependencyKey keyDep2 = new MavenDependencyKey('group-three', 'artifact-three', 'jar', null)

        then:
        pomReader.getDependencies().size() == 2
        assertResolvedPomDependency(keyDep1, 'version-two')
        assertResolvedPomDependency(keyDep2, 'version-three')
    }

    def "get dependencies management without properties from active profile"() {
        when:
        pomFile << """
<project>
    <modelVersion>4.0.0</modelVersion>
    <groupId>group-one</groupId>
    <artifactId>artifact-one</artifactId>
    <version>version-one</version>

    <profiles>
        <profile>
            <id>profile-1</id>
            <activation>
                <activeByDefault>true</activeByDefault>
            </activation>
            <dependencyManagement>
                <dependencies>
                    <dependency>
                        <groupId>group-two</groupId>
                        <artifactId>artifact-two</artifactId>
                        <version>version-two</version>
                    </dependency>
                </dependencies>
            </dependencyManagement>
        </profile>
    </profiles>
</project>
"""
        pomReader = new PomReader(locallyAvailableExternalResource)
        MavenDependencyKey key = new MavenDependencyKey('group-two', 'artifact-two', 'jar', null)

        then:
        List<PomProfile> activePomProfiles = pomReader.parseActivePomProfiles()
        activePomProfiles.size() == 1
        activePomProfiles[0].getDependencyMgts().size() == 1
        assertResolvedPomDependencyManagement(key, 'version-two')
    }

    def "get dependencies management with properties from active profile"() {
        when:
        pomFile << """
<project>
    <modelVersion>4.0.0</modelVersion>
    <groupId>group-one</groupId>
    <artifactId>artifact-one</artifactId>
    <version>version-one</version>

    <profiles>
        <profile>
            <id>profile-1</id>
            <activation>
                <activeByDefault>true</activeByDefault>
            </activation>
            <properties>
                <groupId.prop>group-two</groupId.prop>
                <artifactId.prop>artifact-two</artifactId.prop>
                <version.prop>version-two</version.prop>
            </properties>
            <dependencyManagement>
                <dependencies>
                    <dependency>
                        <groupId>\${groupId.prop}</groupId>
                        <artifactId>\${artifactId.prop}</artifactId>
                        <version>\${version.prop}</version>
                    </dependency>
                </dependencies>
            </dependencyManagement>
        </profile>
    </profiles>
</project>
"""
        pomReader = new PomReader(locallyAvailableExternalResource)
        MavenDependencyKey key = new MavenDependencyKey('group-two', 'artifact-two', 'jar', null)

        then:
        List<PomProfile> activePomProfiles = pomReader.parseActivePomProfiles()
        activePomProfiles.size() == 1
        activePomProfiles[0].getDependencyMgts().size() == 1
        assertResolvedPomDependencyManagement(key, 'version-two')
    }

    def "finds dependency default if declared in active profile"() {
        when:
        pomFile << """
<project>
    <modelVersion>4.0.0</modelVersion>
    <groupId>group-one</groupId>
    <artifactId>artifact-one</artifactId>
    <version>version-one</version>

    <dependencies>
        <dependency>
            <groupId>group-two</groupId>
            <artifactId>artifact-two</artifactId>
        </dependency>
        <dependency>
            <groupId>group-two</groupId>
            <artifactId>artifact-three</artifactId>
        </dependency>
    </dependencies>

    <profiles>
        <profile>
            <id>profile-1</id>
            <activation>
                <activeByDefault>true</activeByDefault>
            </activation>
            <dependencyManagement>
                <dependencies>
                    <dependency>
                        <groupId>group-two</groupId>
                        <artifactId>artifact-two</artifactId>
                        <version>version-two</version>
                    </dependency>
                </dependencies>
            </dependencyManagement>
        </profile>
    </profiles>
</project>
"""
        pomReader = new PomReader(locallyAvailableExternalResource)
        MavenDependencyKey keyGroupTwo = new MavenDependencyKey('group-two', 'artifact-two', 'jar', null)
        MavenDependencyKey keyGroupThree = new MavenDependencyKey('group-two', 'artifact-three', 'jar', null)

        then:
        pomReader.getDependencyMgt().size() == 1
        assertResolvedPomDependencyManagement(keyGroupTwo, 'version-two')
        pomReader.findDependencyDefaults(keyGroupTwo) == pomReader.dependencyMgt[keyGroupTwo]
        !pomReader.findDependencyDefaults(keyGroupThree)
    }

    def "uses dependency default from active profile over POM dependency default"() {
        when:
        pomFile << """
<project>
    <modelVersion>4.0.0</modelVersion>
    <groupId>group-one</groupId>
    <artifactId>artifact-one</artifactId>
    <version>version-one</version>

    <dependencies>
        <dependency>
            <groupId>group-two</groupId>
            <artifactId>artifact-two</artifactId>
        </dependency>
    </dependencies>

    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>group-two</groupId>
                <artifactId>artifact-two</artifactId>
                <version>version-two</version>
            </dependency>
        </dependencies>
    </dependencyManagement>

    <profiles>
        <profile>
            <id>profile-1</id>
            <activation>
                <activeByDefault>true</activeByDefault>
            </activation>
            <dependencyManagement>
                <dependencies>
                    <dependency>
                        <groupId>group-two</groupId>
                        <artifactId>artifact-two</artifactId>
                        <version>version-three</version>
                    </dependency>
                </dependencies>
            </dependencyManagement>
        </profile>
    </profiles>
</project>
"""
        pomReader = new PomReader(locallyAvailableExternalResource)
        MavenDependencyKey keyGroupTwo = new MavenDependencyKey('group-two', 'artifact-two', 'jar', null)

        then:
        pomReader.getDependencyMgt().size() == 1
        assertResolvedPomDependencyManagement(keyGroupTwo, 'version-three')
        pomReader.findDependencyDefaults(keyGroupTwo) == pomReader.dependencyMgt[keyGroupTwo]
    }

    def "finds dependency default if declared in multiple active profiles"() {
        when:
        pomFile << """
<project>
    <modelVersion>4.0.0</modelVersion>
    <groupId>group-one</groupId>
    <artifactId>artifact-one</artifactId>
    <version>version-one</version>

    <dependencies>
        <dependency>
            <groupId>group-two</groupId>
            <artifactId>artifact-two</artifactId>
        </dependency>
        <dependency>
            <groupId>group-two</groupId>
            <artifactId>artifact-three</artifactId>
        </dependency>
    </dependencies>

    <profiles>
        <profile>
            <id>profile-1</id>
            <activation>
                <activeByDefault>true</activeByDefault>
            </activation>
            <dependencyManagement>
                <dependencies>
                    <dependency>
                        <groupId>group-two</groupId>
                        <artifactId>artifact-two</artifactId>
                        <version>version-two</version>
                    </dependency>
                </dependencies>
            </dependencyManagement>
        </profile>
        <profile>
            <id>profile-2</id>
            <activation>
                <activeByDefault>true</activeByDefault>
            </activation>
            <dependencyManagement>
                <dependencies>
                    <dependency>
                        <groupId>group-two</groupId>
                        <artifactId>artifact-three</artifactId>
                        <version>version-three</version>
                    </dependency>
                </dependencies>
            </dependencyManagement>
        </profile>
    </profiles>
</project>
"""
        pomReader = new PomReader(locallyAvailableExternalResource)
        MavenDependencyKey keyGroupTwo = new MavenDependencyKey('group-two', 'artifact-two', 'jar', null)
        MavenDependencyKey keyGroupThree = new MavenDependencyKey('group-two', 'artifact-three', 'jar', null)

        then:
        pomReader.getDependencyMgt().size() == 2
        assertResolvedPomDependencyManagement(keyGroupTwo, 'version-two')
        pomReader.findDependencyDefaults(keyGroupTwo) == pomReader.dependencyMgt[keyGroupTwo]
        assertResolvedPomDependencyManagement(keyGroupThree, 'version-three')
        pomReader.findDependencyDefaults(keyGroupThree) == pomReader.dependencyMgt[keyGroupThree]
    }

    def "finds dependency default if declared in parent POM active profile"() {
        when:
        String parentPom = """
<project>
    <modelVersion>4.0.0</modelVersion>
    <groupId>group-two</groupId>
    <artifactId>artifact-two</artifactId>
    <version>version-two</version>

    <profiles>
        <profile>
            <id>profile-1</id>
            <activation>
                <activeByDefault>true</activeByDefault>
            </activation>
            <dependencyManagement>
                <dependencies>
                    <dependency>
                        <groupId>group-two</groupId>
                        <artifactId>artifact-two</artifactId>
                        <version>version-two</version>
                    </dependency>
                </dependencies>
            </dependencyManagement>
        </profile>
    </profiles>
</project>
"""
        PomReader parentPomReader = createPomReader('parent-pom.xml', parentPom)
        pomFile << """
<project>
    <modelVersion>4.0.0</modelVersion>
    <groupId>group-one</groupId>
    <artifactId>artifact-one</artifactId>
    <version>version-one</version>

    <parent>
        <groupId>group-two</groupId>
        <artifactId>artifact-two</artifactId>
        <version>version-two</version>
    </parent>

    <dependencies>
        <dependency>
            <groupId>group-four</groupId>
            <artifactId>artifact-four</artifactId>
        </dependency>
        <dependency>
            <groupId>group-five</groupId>
            <artifactId>artifact-five</artifactId>
        </dependency>
    </dependencies>
</project>
"""
        pomReader = new PomReader(locallyAvailableExternalResource)
        pomReader.setPomParent(parentPomReader)
        MavenDependencyKey keyGroupTwo = new MavenDependencyKey('group-two', 'artifact-two', 'jar', null)
        MavenDependencyKey keyGroupThree = new MavenDependencyKey('group-two', 'artifact-three', 'jar', null)

        then:
        pomReader.getDependencyMgt().size() == 1
        assertResolvedPomDependencyManagement(keyGroupTwo, 'version-two')
        pomReader.findDependencyDefaults(keyGroupTwo) == pomReader.dependencyMgt[keyGroupTwo]
        !pomReader.findDependencyDefaults(keyGroupThree)
    }
}
