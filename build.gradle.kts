import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.3.72"
    application
}

application {
    mainClassName = "ru.avem.rele.app.Rele"
}

tasks.compileKotlin {
    kotlinOptions.freeCompilerArgs = listOf("-Xopt-in=kotlin.RequiresOptIn")
}

group = "ru.avem"

repositories {
    mavenCentral()
    jcenter()
    maven {
        url = uri("https://oss.sonatype.org/content/repositories/snapshots/")
    }

    flatDir {
        dir("libs")
    }
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

tasks.jar {
    manifest {
        attributes["Class-Path"] = configurations.compile.map {
            it.name
        }

        attributes["Main-Class"] = application.mainClassName
    }

    from({
        configurations.runtimeClasspath.get().filter { it.name.endsWith("jar") }.map { zipTree(it) }
    })
}

dependencies {
    implementation(kotlin("stdlib"))

    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.3.71")
    implementation("no.tornado:tornadofx:1.7.19")
    implementation("no.tornado:tornadofx-controlsfx:0.1.1")
    implementation("org.jetbrains.exposed:exposed:0.13.6")
    implementation("org.xerial:sqlite-jdbc:3.27.2.1")
    implementation("de.jensd:fontawesomefx:8.9")
    implementation("de.jensd:fontawesomefx-icons525:2.0-2")
    implementation("de.jensd:fontawesomefx-materialdesignfont:1.4.57-2")
    implementation("de.jensd:fontawesomefx-materialicons:2.1-2")
    implementation("de.jensd:fontawesomefx-octicons:3.3.0-2")
    implementation("de.jensd:fontawesomefx-weathericons:2.0-2")
    implementation("de.jensd:fontawesomefx-icons525:2.0-2")
    implementation("org.apache.poi:poi:4.1.0")
    implementation("org.apache.poi:poi-ooxml:4.1.0")
    implementation("com.fazecast:jSerialComm:[2.0.0,3.0.0)")
    implementation("io.github.microutils:kotlin-logging:1.8.3")
    implementation("org.slf4j:slf4j-api:1.7.25")
    implementation("ch.qos.logback:logback-classic:1.2.3")
    implementation("ch.qos.logback:logback-core:1.2.3")

    implementation(":kserialpooler-1.0")
}
