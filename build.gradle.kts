import io.gitlab.arturbosch.detekt.Detekt
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "3.1.7"
    id("io.spring.dependency-management") version "1.1.4"
    id("org.graalvm.buildtools.native") version "0.10.1"
    kotlin("jvm") version "1.9.22"
    kotlin("plugin.spring") version "1.9.22"
    id("io.gitlab.arturbosch.detekt") version "1.23.5"
}

group = "com.saltynote.service"
version = "0.0.3"

java {
    sourceCompatibility = JavaVersion.VERSION_17
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("org.springframework.boot:spring-boot-starter-mail")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-mongodb")
    implementation("org.springframework:spring-aop")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.freemarker:freemarker")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("io.github.oshai:kotlin-logging-jvm:6.0.3")
    implementation("org.apache.commons:commons-lang3:3.14.0")
    implementation("com.google.guava:guava:33.0.0-jre")
    implementation("org.mapstruct:mapstruct:1.5.5.Final")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.3.0")
    developmentOnly("org.springframework.boot:spring-boot-devtools")
    implementation("com.auth0:java-jwt:4.4.0")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.15.3")
    developmentOnly("org.springframework.boot:spring-boot-devtools")
    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(module = "mockito-core")
    }
    testImplementation("com.ninja-squad:springmockk:4.0.2")
    testImplementation("net.datafaker:datafaker:2.1.0")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs += "-Xjsr305=strict"
        jvmTarget = "17"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

detekt {
    toolVersion = "1.23.4"
    basePath = "$rootDir"
    config = files("$rootDir/config/detekt/detekt.yml")
    buildUponDefaultConfig = true
}

tasks.withType<Detekt>().configureEach {
    jvmTarget = "17"
    reports {
        html.required.set(true)
        xml.required.set(true)
        md.required.set(true)
        sarif.required.set(true)
    }
}