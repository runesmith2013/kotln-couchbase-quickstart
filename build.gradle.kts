import org.gradle.jvm.tasks.Jar

val ktor_version: String by project
val koin_version: String by project
val kotlin_version: String by project
val logback_version: String by project

plugins {
    application
    kotlin("jvm") version "2.0.0"
}

group = "com.couchbase.kotlin"
version = "1.2.0"

application {
    mainClass.set("com.couchbase.kotlin.quickstart.ApplicationKt")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

repositories {
    mavenCentral()
    maven { url = uri("https://maven.pkg.jetbrains.space/public/p/ktor/eap") }
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    implementation("io.ktor:ktor-server-core-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-netty-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-content-negotiation:$ktor_version")
    implementation("io.ktor:ktor-serialization-jackson:$ktor_version")
    implementation("ch.qos.logback:logback-classic:$logback_version")
    implementation("com.couchbase.client:kotlin-client:1.4.0")
    implementation("io.insert-koin:koin-ktor:$koin_version")
    implementation("io.insert-koin:koin-logger-slf4j:$koin_version")
    implementation("io.github.config4k:config4k:0.7.0")
    implementation("org.junit.jupiter:junit-jupiter:5.10.3")
    implementation("org.reflections:reflections:0.10.2")
    implementation("dev.forst", "ktor-openapi-generator", "0.6.1")
    implementation("io.ktor:ktor-client-serialization:$ktor_version")
    implementation ("io.ktor:ktor-server-core:$ktor_version")
    implementation("io.ktor:ktor-server-cors-jvm:$ktor_version")

    testImplementation("io.ktor:ktor-server-tests-jvm:$ktor_version")
    testImplementation("io.mockk:mockk:1.13.11")
}

tasks.withType<Jar> {
  manifest {
      attributes["Main-Class"] = "com.couchbase.kotlin.quickstart.ApplicationKt"
  }
  configurations["compileClasspath"].forEach { file: File ->
      from(zipTree(file.absoluteFile))
  }
  duplicatesStrategy = DuplicatesStrategy.INCLUDE
}

tasks.test {
    useJUnitPlatform()
}
