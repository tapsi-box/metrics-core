plugins {
  kotlin("jvm") version "1.9.25"
  kotlin("plugin.spring") version "1.9.25"
  id("org.springframework.boot") version "3.5.5"
  id("io.spring.dependency-management") version "1.1.7"
}

group = "box.tapsi"
version = "0.0.1-SNAPSHOT"
description = "metrics-core"

java {
  toolchain {
    languageVersion = JavaLanguageVersion.of(21)
  }
}

repositories {
  mavenCentral()
}

dependencies {
  implementation("io.micrometer:micrometer-core")
  implementation("io.micrometer:micrometer-observation")
  implementation("org.springframework.boot:spring-boot-starter-aop")
  implementation("io.projectreactor:reactor-core")
  implementation("io.projectreactor:reactor-core-micrometer")
  implementation("org.springframework.boot:spring-boot-starter-actuator")

  compileOnly("org.springframework:spring-context")
  compileOnly("org.springframework.boot:spring-boot-autoconfigure")

  testImplementation("org.springframework.boot:spring-boot-starter-test")
  testImplementation("io.projectreactor:reactor-test")
}

kotlin {
  compilerOptions {
    freeCompilerArgs.addAll("-Xjsr305=strict")
  }
}

tasks.withType<Test> {
  useJUnitPlatform()
}
