plugins {
    id("java")
    id("org.springframework.boot") version "3.5.4"
    id("io.spring.dependency-management") version "1.1.7"
    id("jacoco")
}

group = "io.identityaccess"
version = "0.1.0-SNAPSHOT"

description = "Identity and access microservice for self-hosted authentication, sessions, and authorization"

val testcontainersVersion = "2.0.5"
extra["testcontainers.version"] = testcontainersVersion

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-data-r2dbc")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-data-redis-reactive")
    implementation("org.springframework.kafka:spring-kafka")
    implementation("org.springframework.security:spring-security-oauth2-jose")
    implementation("org.postgresql:r2dbc-postgresql")
    
    // OpenAPI/Swagger documentation
    implementation("org.springdoc:springdoc-openapi-starter-webflux-ui:2.8.9")

    runtimeOnly("org.postgresql:postgresql")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.projectreactor:reactor-test")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("org.springframework.kafka:spring-kafka-test")
    testImplementation(enforcedPlatform("org.testcontainers:testcontainers-bom:$testcontainersVersion"))
    testImplementation("org.testcontainers:testcontainers-junit-jupiter")
    testImplementation("org.testcontainers:testcontainers-postgresql")
    testImplementation("org.testcontainers:testcontainers-kafka")
}

val integrationTest by sourceSets.creating {
    java.srcDir("src/integrationTest/java")
    resources.srcDir("src/integrationTest/resources")
    compileClasspath += sourceSets.main.get().output + sourceSets.test.get().output
    runtimeClasspath += output + compileClasspath
}

configurations[integrationTest.implementationConfigurationName].extendsFrom(configurations.testImplementation.get())
configurations[integrationTest.runtimeOnlyConfigurationName].extendsFrom(configurations.testRuntimeOnly.get())

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.test {
    finalizedBy(tasks.jacocoTestReport)
}

tasks.register<Test>("integrationTest") {
    description = "Runs integration tests backed by Testcontainers."
    group = "verification"
    testClassesDirs = integrationTest.output.classesDirs
    classpath = integrationTest.runtimeClasspath
    shouldRunAfter(tasks.test)
    environment("DOCKER_API_VERSION", "1.54")
    environment("DOCKER_HOST", "unix://${System.getProperty("user.home")}/.docker/run/docker.sock")
    environment("TESTCONTAINERS_DOCKER_SOCKET_OVERRIDE", "/var/run/docker.sock")
    systemProperty(
        "docker.client.strategy",
        "org.testcontainers.dockerclient.EnvironmentAndSystemPropertyClientProviderStrategy"
    )
    useJUnitPlatform()
}

jacoco {
    toolVersion = "0.8.12"
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
}
