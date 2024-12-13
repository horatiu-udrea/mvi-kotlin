import com.vanniktech.maven.publish.SonatypeHost

plugins {
    id("com.vanniktech.maven.publish")
}

mavenPublishing {

    coordinates(
        groupId = "ro.horatiu-udrea",
        artifactId = "mvi",
        version = "0.1.1"
    )
    // Provide artifacts information required by Maven Central
    pom {
        name.set("Model-View-Intent (MVI) components for Kotlin Multiplatform")
        description.set("Minimalistic MVI implementation for Kotlin Multiplatform")
        inceptionYear.set("2024")
        url.set("https://github.com/horatiu-udrea/mvi-kotlin")

        licenses {
            license {
                name.set("MIT")
                url.set("https://opensource.org/licenses/MIT")
                distribution.set("https://opensource.org/licenses/MIT")
            }
        }
        developers {
            developer {
                id.set("horatiu-udrea")
                name.set("Horațiu Udrea")
                email.set("dev@horatiu-udrea.ro")
                url.set("https://github.com/horatiu-udrea")
            }
        }
        scm {
            url.set("https://github.com/horatiu-udrea/mvi-kotlin")
        }
    }

    // Configure publishing to Sonatype
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)

    // Enable GPG signing for all publications
    signAllPublications()
}