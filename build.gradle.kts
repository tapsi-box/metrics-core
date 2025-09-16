plugins {
  kotlin("jvm") version "1.9.23"
  kotlin("plugin.spring") version "1.9.23"
  id("io.spring.dependency-management") version "1.1.7"

  id("com.vanniktech.maven.publish") version "0.34.0"
  id("com.diffplug.spotless") version "7.2.1"
  id("io.gitlab.arturbosch.detekt") version "1.23.6"
  `java-library`
}

group = "box.tapsi.libs"
version = "1.0.3"
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
  api("io.micrometer:micrometer-core:1.15.3")
  api("io.micrometer:micrometer-observation:1.15.3")
  api("io.micrometer:context-propagation:1.1.3")
  api("io.projectreactor:reactor-core-micrometer:1.2.9")

  implementation("org.springframework.boot:spring-boot-starter-aop:3.5.5")
  implementation("io.projectreactor:reactor-core:3.7.9")
  implementation("org.springframework.boot:spring-boot-starter-actuator:3.5.5")
  implementation("org.springframework:spring-context:6.2.10")
  implementation("org.springframework.boot:spring-boot-autoconfigure:3.5.5")

  testImplementation("org.springframework.boot:spring-boot-starter-test:3.5.5")
  testImplementation("org.jetbrains.kotlin:kotlin-test-junit5:1.9.23")
  testImplementation("io.projectreactor:reactor-test:3.7.9")

  testRuntimeOnly("org.junit.platform:junit-platform-launcher")
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

spotless {
  kotlin {
    target("src/**/*.kt")
    ktlint()
      .editorConfigOverride(
        mapOf(
          "indent_size" to 2,
          "ktlint_standard_filename" to "disabled",
          "ktlint_standard_max-line-length" to "120"
        )
      )
    trimTrailingWhitespace()
    leadingTabsToSpaces()
    endWithNewline()
  }
}

detekt {
  buildUponDefaultConfig = true
  allRules = true
  config.setFrom("$projectDir/detekt.yml")
  baseline = file("$projectDir/detekt-baseline.xml")
}

tasks.register("verifyReadmeContent") {
  doLast {
    val readmeFile = file("README.md")
    val content = readmeFile.readText()

    // List of checks
    val checks = listOf(
      Check("group ID", """<groupId>${project.group}</groupId>"""),
      Check("version", """<version>${project.version}</version>"""),
      Check("Spring Boot version", "spring-boot-starter-actuator"),
    )

    val errors = checks.mapNotNull { check ->
      if (!content.contains(check.expectedValue)) {
        "Missing or incorrect ${check.name}: ${check.expectedValue}"
      } else null
    }

    if (errors.isNotEmpty()) {
      throw GradleException(
        """
                README content verification failed!
                ${errors.joinToString("\n")}
                Please update the README.md with correct values
            """.trimIndent()
      )
    }
  }
}

tasks.check {
  dependsOn("verifyReadmeContent")
}

data class Check(val name: String, val expectedValue: String)
