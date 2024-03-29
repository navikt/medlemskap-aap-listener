val ktorVersion = "1.6.0"
val jacksonVersion = "2.13.3"
val konfigVersion = "1.6.10.0"
val kotlinLoggerVersion = "1.8.3"
val resilience4jVersion = "1.5.0"
val logstashVersion = "6.4"
val logbackVersion = "1.2.3"
val mainClass = "no.nav.medlemskap.aap.lytter.ApplicationKt"

plugins {
    kotlin("jvm") version "1.6.10"
    application
    id("com.github.davidmc24.gradle.plugin.avro") version "1.3.0"
    id("com.github.johnrengelman.shadow") version "7.0.0"
}

group = "no.nav.medlemskap"
version = "1.0-SNAPSHOT"
val githubUser: String by project
val githubPassword: String by project
repositories {
    mavenCentral()
    maven("https://packages.confluent.io/maven/")
    maven("https://jitpack.io")
    maven {
        url = uri("https://maven.pkg.github.com/navikt/aap-avroskjema")
        credentials {
            username = githubUser
            password = githubPassword
        }
    }
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("io.ktor:ktor-client-apache:$ktorVersion")
    implementation("io.github.resilience4j:resilience4j-retry:$resilience4jVersion")
    implementation("io.github.resilience4j:resilience4j-kotlin:$resilience4jVersion")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:$jacksonVersion")
    implementation("com.fasterxml.jackson.core:jackson-annotations:$jacksonVersion")
    implementation("com.fasterxml.jackson.core:jackson-databind:$jacksonVersion")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:$jacksonVersion")
    implementation("io.ktor:ktor-server-netty:$ktorVersion")
    implementation("io.ktor:ktor-client-core:$ktorVersion")
    implementation("io.ktor:ktor-client-cio:$ktorVersion")
    implementation("io.ktor:ktor-client-jackson:$ktorVersion")
    implementation("io.ktor:ktor-client-auth:$ktorVersion")
    implementation("io.ktor:ktor-metrics-micrometer:$ktorVersion")
    implementation("io.micrometer:micrometer-registry-prometheus:1.7.0")
    implementation("com.natpryce:konfig:$konfigVersion")
    implementation("io.github.microutils:kotlin-logging:$kotlinLoggerVersion")
    implementation("net.logstash.logback:logstash-logback-encoder:$logstashVersion")
    implementation("ch.qos.logback:logback-classic:$logbackVersion")
    // 2.8.0 er tilgjengelig, burde kanskje oppdatere
    implementation("org.apache.kafka:kafka-clients:2.5.0")
    testImplementation(platform("org.junit:junit-bom:5.7.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("io.mockk:mockk:1.11.0")
    testImplementation("io.ktor:ktor-client-mock:$ktorVersion")
    testImplementation("org.testcontainers:kafka:1.16.0")
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "17"
        kotlinOptions.freeCompilerArgs += "-Xopt-in=kotlin.RequiresOptIn"
    }
    shadowJar {
        archiveBaseName.set("app")
        archiveClassifier.set("")
        archiveVersion.set("")
        manifest {
            attributes(
                mapOf(
                    "Main-Class" to mainClass
                )
            )
        }
    }

    test {
        useJUnitPlatform()
        //Trengs inntil videre for bytebuddy med java 16, som brukes av mockk.
        //jvmArgs = listOf("-Dnet.bytebuddy.experimental=true")
    }
}

application {
    mainClass.set("no.nav.medlemskap.aap.listener.ApplicationKt")
}
