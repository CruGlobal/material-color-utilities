plugins {
    `java-library`
    `maven-publish`
    id("org.ajoberstar.grgit") version "4.1.0"
}

repositories {
    mavenCentral()
}

sourceSets {
    val main by getting {
        java.setSrcDirs(listOf("java"))
    }
}

dependencies {
    compileOnly("com.google.errorprone:error_prone_annotations:2.16")
}

group = "org.cru.mobile.fork.material-color-utilities"
version = "${version}_${grgit.log { includes = listOf("HEAD") }.size}"
publishing {
    repositories {
        maven {
            name = "cruGlobalMavenRepository"
            setUrl("https://cruglobal.jfrog.io/artifactory/maven-cru-mobile-forks-local/")
            credentials(PasswordCredentials::class)
        }
    }

    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            artifactId = "material-color-utilities"
        }
    }
}
