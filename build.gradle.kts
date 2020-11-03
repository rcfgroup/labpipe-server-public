import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    java
    application
    kotlin("jvm") version "1.4.10"
    id("com.github.johnrengelman.shadow") version "5.2.0"
    kotlin("kapt") version "1.4.10"
}

group = "uk.ac.le.ember"
version = "0.0.0"

repositories {
    mavenCentral()
    jcenter()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.3")
    testImplementation("io.kotest", "kotest-runner-junit5", "4.3.1")
    testImplementation("io.kotest", "kotest-assertions-core", "4.3.1")
    testImplementation("io.kotest", "kotest-property", "4.3.1")
    testImplementation("io.mockk:mockk:1.10.2")


    kapt("org.litote.kmongo:kmongo-annotation-processor:3.12.0")


    // https://mvnrepository.com/artifact/org.slf4j/slf4j-simple
    implementation("org.slf4j", "slf4j-simple", "1.7.30")

    // TODO add support for more native detailed logs with debug option can be toggled from commandline
    // https://mvnrepository.com/artifact/io.github.microutils/kotlin-logging
    implementation("io.github.microutils", "kotlin-logging", "2.0.3")


    // https://mvnrepository.com/artifact/com.github.ajalt/clikt
    implementation("com.github.ajalt", "clikt", "2.8.0")


    // https://mvnrepository.com/artifact/commons-beanutils/commons-beanutils
    implementation("commons-beanutils", "commons-beanutils", "1.9.4")


    // https://mvnrepository.com/artifact/org.apache.commons/commons-configuration2
    implementation("org.apache.commons", "commons-configuration2", "2.7")

    // https://mvnrepository.com/artifact/org.litote.kmongo/kmongo
    implementation("org.litote.kmongo", "kmongo", "4.1.3")

    // https://mvnrepository.com/artifact/org.mindrot/jbcrypt
    implementation("org.mindrot", "jbcrypt", "0.4")

    // https://mvnrepository.com/artifact/com.j2html/j2html
    implementation("com.j2html", "j2html", "1.4.0")

    // https://mvnrepository.com/artifact/org.apache.commons/commons-lang3
    implementation("org.apache.commons", "commons-lang3", "3.11")

    // https://mvnrepository.com/artifact/commons-codec/commons-codec
    implementation("commons-codec", "commons-codec", "1.15")

    // https://mvnrepository.com/artifact/io.javalin/javalin
    implementation("io.javalin", "javalin", "3.11.2")

    // https://mvnrepository.com/artifact/org.simplejavamail/simple-java-mail
    implementation("org.simplejavamail", "simple-java-mail", "6.4.4")

    // https://mvnrepository.com/artifact/org.simplejavamail/batch-module
    implementation("org.simplejavamail", "batch-module", "6.4.4")

    // https://mvnrepository.com/artifact/com.google.code.gson/gson
    implementation("com.google.code.gson", "gson", "2.8.6")

    // https://mvnrepository.com/artifact/com.google.zxing/core
    implementation("com.google.zxing", "core", "3.4.1")

    // https://mvnrepository.com/artifact/com.google.zxing/javase
    implementation("com.google.zxing", "javase", "3.4.1")

    // https://mvnrepository.com/artifact/commons-io/commons-io
    implementation("commons-io", "commons-io", "2.8.0")

    // https://mvnrepository.com/artifact/com.fasterxml.jackson.module/jackson-module-kotlin
    implementation("com.fasterxml.jackson.module", "jackson-module-kotlin", "2.11.3")

    implementation("com.uchuhimo", "konf", "0.23.0")
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_1_8
}

tasks.withType<Test> {
    useJUnitPlatform()
}

application {
    mainClassName = "AppKt"
}

tasks {
    named<ShadowJar>("shadowJar") {
        classifier = null
    }
    withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "1.8"
    }
}