plugins {
    `java-library`
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
