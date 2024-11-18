import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.bundling.Jar
import org.gradle.kotlin.dsl.`maven-publish`

plugins {
    `maven-publish`
    signing
}

publishing {
    // Configure all publications
    publications.withType<MavenPublication> {
        // Stub javadoc.jar artifact
        artifact(tasks.register("${name}JavadocJar", Jar::class) {
            archiveClassifier.set("javadoc")
            archiveAppendix.set(this@withType.name)
        })

        // Provide artifacts information required by Maven Central
        pom {
            name.set("Model-View-Intent (MVI) components for Kotlin Multiplatform")
            description.set("Minimalistic MVI implementation for Kotlin Multiplatform")
            url.set("https://github.com/horatiu-udrea/mvi-kotlin")

            licenses {
                license {
                    name.set("MIT")
                    url.set("https://opensource.org/licenses/MIT")
                }
            }
            developers {
                developer {
                    id.set("horatiu-udrea")
                    name.set("Horațiu Udrea")
                    email.set("dev@horatiu-udrea.ro")
                    // organization.set("")
                    // organizationUrl.set("")
                }
            }
            scm {
                url.set("https://github.com/horatiu-udrea/mvi-kotlin")
            }
        }
    }
}

signing {
    if (project.hasProperty("signing.gnupg.keyName")) {
        useGpgCmd()
        sign(publishing.publications)
    }
}
