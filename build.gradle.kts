val ktor_version: String by project
val koin_version: String by project
val kotlin_version: String by project
val logback_version: String by project

plugins {
    application
    kotlin("jvm") version "1.7.0"
}

group = "com.couchbase.kotlin"
version = "0.0.1"

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
    implementation("com.couchbase.client:kotlin-client:1.0.1")
    implementation("io.insert-koin:koin-ktor:$koin_version")
    implementation("io.insert-koin:koin-logger-slf4j:$koin_version")
    implementation("io.github.config4k:config4k:0.4.2")
    implementation("org.junit.jupiter:junit-jupiter:5.7.0")
    implementation("dev.forst", "ktor-openapi-generator", "0.4.3")
    testImplementation("io.ktor:ktor-server-tests-jvm:$ktor_version")
    testImplementation("io.mockk:mockk:1.12.4")
}
