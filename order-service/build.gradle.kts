plugins {
    idea
    java
    id("maven-publish")
    id("org.springframework.boot") version "3.5.6"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "ru.romanov"
version = "1.0.29"
description = "order-service"

repositories {
    mavenLocal()
    mavenCentral()
    maven {
        name = "nexus"
        url = uri(System.getenv("NEXUS_URL") ?: System.getProperty("NEXUS_URL") ?: "http://localhost:8081/repository/maven-releases")
        isAllowInsecureProtocol = true
        credentials {
            username = System.getenv("NEXUS_USERNAME") ?: System.getProperty("NEXUS_USERNAME") ?: "admin"
            password = System.getenv("NEXUS_PASSWORD") ?: System.getProperty("NEXUS_PASSWORD") ?: "admin"
        }
    }
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(24)
    }
}

val versions = mapOf(
    "logbackVersion" to "1.5.18",
    "outboxLibVersion" to "1.0.30",
    "opentelemetryBomVersion" to "2.15.0"
)

dependencyManagement {
    imports {
        mavenBom("io.opentelemetry.instrumentation:opentelemetry-instrumentation-bom:${versions["opentelemetryBomVersion"]}")
    }
}

dependencies {
    // OUTBOX-LIB
    implementation("ru.romanov:outbox-processor:${versions["outboxLibVersion"]}")

    // SPRING
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-web")

    // OBSERVABILITY
    implementation("io.micrometer:micrometer-registry-prometheus")
    implementation("io.opentelemetry:opentelemetry-exporter-otlp")
    implementation("io.micrometer:micrometer-observation")
    implementation("io.micrometer:micrometer-tracing")
    implementation("io.micrometer:micrometer-tracing-bridge-otel")
    runtimeOnly("io.micrometer:micrometer-registry-prometheus")
    implementation("io.opentelemetry.instrumentation:opentelemetry-spring-boot-starter")
    implementation("ch.qos.logback:logback-classic:${versions["logbackVersion"]}")

    // DB
    implementation("org.liquibase:liquibase-core")
    compileOnly("org.projectlombok:lombok")
    runtimeOnly("org.postgresql:postgresql")

    annotationProcessor("org.projectlombok:lombok")

    // TEST
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.withType<AbstractPublishToMaven> {
    dependsOn(tasks.named("jar"))
}

/*
──────────────────────────────────────────────────────
============== Resolve NEXUS credentials ==============
──────────────────────────────────────────────────────
*/

file(".env").takeIf { it.exists() }?.readLines()?.forEach {
    val (k, v) = it.split("=", limit = 2)
    System.setProperty(k.trim(), v.trim())
    logger.lifecycle("${k.trim()}=${v.trim()}")
}

val nexusUrl = System.getenv("NEXUS_URL") ?: System.getProperty("NEXUS_URL")
val nexusUser = System.getenv("NEXUS_USERNAME") ?: System.getProperty("NEXUS_USERNAME")
val nexusPassword = System.getenv("NEXUS_PASSWORD") ?: System.getProperty("NEXUS_PASSWORD")

if (nexusUrl.isNullOrBlank() || nexusUser.isNullOrBlank() || nexusPassword.isNullOrBlank()) {
    throw GradleException(
        "NEXUS details are not set. Create a .env file with correct properties: " + "NEXUS_URL, NEXUS_USERNAME, NEXUS_PASSWORD"
    )
}

/*
──────────────────────────────────────────────────────
============== Nexus Publishing ==============
──────────────────────────────────────────────────────
*/

publishing {
    publications {
        val jarBaseName = name
        val jarFile = file("build/libs").listFiles()
            ?.firstOrNull { it.name.contains(name) && (it.extension == "jar" || it.extension == "zip") }

        if (jarFile != null) {
            logger.lifecycle("publishing: ${jarFile.name}")

            create<MavenPublication>("publish${name.replaceFirstChar(Char::uppercase)}Jar") {
                artifact(jarFile)
                groupId = "ru.romanov"
                artifactId = jarBaseName
                version = "1.0.29"
            }
        }
    }

    repositories {
        maven {
            name = "nexus"
            url = uri(nexusUrl)
            isAllowInsecureProtocol = true
            credentials {
                username = nexusUser
                password = nexusPassword
            }
        }
    }
}
