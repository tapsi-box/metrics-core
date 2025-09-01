plugins {
  kotlin("jvm") version "1.9.25"
  kotlin("plugin.spring") version "1.9.25"
  id("org.springframework.boot") version "3.5.5"
  id("io.spring.dependency-management") version "1.1.7"

  id("com.vanniktech.maven.publish") version "0.34.0"
}

group = "box.tapsi.libs"
version = "1.0.0"
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
  implementation("io.micrometer:micrometer-core:1.15.3")
  implementation("io.micrometer:micrometer-observation:1.15.3")
  implementation("org.springframework.boot:spring-boot-starter-aop:3.5.5")
  implementation("io.projectreactor:reactor-core:3.7.9")
  implementation("io.projectreactor:reactor-core-micrometer:1.2.9")
  implementation("org.springframework.boot:spring-boot-starter-actuator:3.5.5")
  implementation("org.springframework:spring-context:6.2.10")
  implementation("org.springframework.boot:spring-boot-autoconfigure:3.5.5")

  testImplementation("org.springframework.boot:spring-boot-starter-test")
  testImplementation("io.projectreactor:reactor-test")

  annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
}

kotlin {
  compilerOptions {
    freeCompilerArgs.addAll("-Xjsr305=strict")
  }
}

tasks.withType<Test> {
  useJUnitPlatform()
}

mavenPublishing {
  publishToMavenCentral()
  signAllPublications()

  pom {
    name.set("metrics-core")
    description.set("Tapsi Metrics Core - Reactive Observability Library for Spring Boot Applications.")
    url.set("https://github.com/tapsi-box/metrics-core")
    licenses {
      license {
        name.set("MIT License")
        url.set("https://opensource.org/licenses/MIT")
        distribution.set("repo")
      }
    }
    developers {
      developer {
        id.set("mahdibohloul")
        name.set("Mahdi Bohloul")
        email.set("mahdiibohloul@gmail.com")
        url.set("https://github.com/mahdibohloul/")
      }
    }
    scm {
      url.set("https://github.com/tapsi-box/metrics-core")
    }
  }
}
