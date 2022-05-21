plugins {
    id("java")
    id("java-library")
}

group = "com.victoriametrics.client"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.2")
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

tasks {
    test {
        useJUnitPlatform()
    }

    // To update, run: ./gradlew wrapper
    wrapper {
        distributionType = Wrapper.DistributionType.ALL
        gradleVersion = "7.4.2"
    }
}
