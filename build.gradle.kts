subprojects {
    group = "com.github.vlsi.reproducers"
    version = "1.0"

    repositories {
        mavenCentral()
    }

    apply(plugin = "java-library")
    apply(plugin = "maven-publish")

    tasks.register("generatePom") {
        dependsOn(tasks.withType<GenerateMavenPom>())
    }

    configure<PublishingExtension> {
        publications {
            create<MavenPublication>(project.name) {
                artifactId = "augmented-${project.name}"
                from(components["java"])

                if (!project.name.endsWith("-without-versionmapping")) {
                    versionMapping {
                        usage(Usage.JAVA_RUNTIME) {
                            fromResolutionResult()
                        }
                        usage(Usage.JAVA_API) {
                            fromResolutionOf("runtimeClasspath")
                        }
                    }
                }

                pom {
                    withXml {
                        val sb = asString()
                        // Normalize the XML
                        var s = sb.toString()
                        // <scope>compile</scope> is Maven default, so delete it
                        s = s.replace("<scope>compile</scope>", "")
                        // Cut <dependencyManagement> because all dependencies have the resolved versions
                        s = s.replace(
                            Regex(
                                "<dependencyManagement>.*?</dependencyManagement>",
                                RegexOption.DOT_MATCHES_ALL
                            ),
                            ""
                        )
                        sb.setLength(0)
                        sb.append(s)
                        println("POM for ${project.name}: $s")

                        // Checks
                        if (project.name.startsWith("core")) {
                            if (project.name.endsWith("-without-versionmapping")) {
                                if ("<version>2.12.0<" in s) {
                                    println("ERROR: jackson version 2.12.0 is present, however, it must not (platform dependency + NO versionMapping)")
                                } else {
                                    println("OK: jackson version 2.12.0 is not present in the generated pom.xml (platform dependency + NO versionMapping)")
                                }
                            } else {
                                if ("<version>2.12.0<" in s) {
                                    println("OK: jackson version 2.12.0 is present in the generated pom.xml (platform dependency + versionMapping)")
                                } else {
                                    println("ERROR: jackson version 2.12.0 is present, however, it should be in pom (platform dependency + versionMapping)")
                                }
                            }
                        } else if (project.name.startsWith("app")) {
                            if ("<artifactId>augmented-core" in s) {
                                println("OK: dependency on <artifactId>augmented-core... is present in the pom")
                            } else {
                                println("ERROR: dependency on <artifactId>augmented-core... is MISSING in the pom")
                            }
                        }

                        if ("<artifactId>augmented-${project.name}" in s) {
                            println("OK: pom file uses <artifactId>augmented-${project.name}")
                        } else {
                            println("ERROR: pom file misses <artifactId>augmented-${project.name}")
                        }

                        // Re-format the XML
                        asNode()
                    }
                }
            }
        }
    }
}
