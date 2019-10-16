import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    java
    application
    kotlin("jvm") version "1.3.40"
    id("com.github.johnrengelman.shadow") version "5.1.0"
}

group = "uk.ac.le.ember"
version = "0.0.0"

repositories {
    mavenCentral()
    jcenter()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.1")
    testImplementation("io.kotlintest", "kotlintest-runner-junit5", "3.4.2")


    // https://mvnrepository.com/artifact/org.slf4j/slf4j-simple
    compile("org.slf4j", "slf4j-simple", "1.7.28")

    // TODO add support for more native detailed logs with debug option can be toggled from commandline
    // https://mvnrepository.com/artifact/io.github.microutils/kotlin-logging
    compile("io.github.microutils", "kotlin-logging", "1.7.6")


    // https://mvnrepository.com/artifact/com.github.ajalt/clikt
    compile("com.github.ajalt", "clikt", "2.1.0")


    // https://mvnrepository.com/artifact/commons-beanutils/commons-beanutils
    compile("commons-beanutils", "commons-beanutils", "1.9.4")


    // https://mvnrepository.com/artifact/org.apache.commons/commons-configuration2
    compile("org.apache.commons", "commons-configuration2", "2.6")

    // https://mvnrepository.com/artifact/org.litote.kmongo/kmongo
    compile("org.litote.kmongo", "kmongo", "3.11.0")

    // https://mvnrepository.com/artifact/org.mindrot/jbcrypt
    compile("org.mindrot", "jbcrypt", "0.4")

    // https://mvnrepository.com/artifact/com.j2html/j2html
    compile("com.j2html", "j2html", "1.4.0")

    // https://mvnrepository.com/artifact/org.apache.commons/commons-lang3
    compile("org.apache.commons", "commons-lang3", "3.9")

    // https://mvnrepository.com/artifact/io.javalin/javalin
    compile("io.javalin", "javalin", "3.5.0")

    // https://mvnrepository.com/artifact/org.simplejavamail/simple-java-mail
    compile("org.simplejavamail", "simple-java-mail", "5.3.0")

    // https://mvnrepository.com/artifact/com.google.code.gson/gson
    compile("com.google.code.gson", "gson", "2.8.5")

    // https://mvnrepository.com/artifact/com.google.zxing/core
    compile("com.google.zxing", "core", "3.4.0")

    // https://mvnrepository.com/artifact/com.google.zxing/javase
    compile("com.google.zxing", "javase", "3.4.0")

    // https://mvnrepository.com/artifact/commons-io/commons-io
    compile("commons-io", "commons-io", "2.6")

    // https://mvnrepository.com/artifact/io.swagger.core.v3/swagger-core
    compile("io.swagger.core.v3", "swagger-core", "2.0.10")

    // https://mvnrepository.com/artifact/com.fasterxml.jackson.module/jackson-module-kotlin
    compile("com.fasterxml.jackson.module", "jackson-module-kotlin", "2.10.0")

    // https://mvnrepository.com/artifact/cc.vileda/kotlin-openapi3-dsl
    compile("cc.vileda", "kotlin-openapi3-dsl", "0.20.2")



}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_11
}
tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

application {
    mainClassName = "AppKt"
}

tasks {
    named<ShadowJar>("shadowJar") {
        classifier = null
    }
}