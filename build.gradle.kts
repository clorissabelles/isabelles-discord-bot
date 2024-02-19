import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    application

    kotlin("jvm") version "1.9.22"
    kotlin("plugin.serialization") version "1.9.22"

    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "io.github.clorissabelles.isabelles-discord-bot"
version = "0.1.0"

repositories {
    google()
    mavenCentral()

    maven {
        name = "Sonatype Snapshots (Legacy)"
        url = uri("https://oss.sonatype.org/content/repositories/snapshots")
    }

    maven {
        name = "Sonatype Snapshots"
        url = uri("https://s01.oss.sonatype.org/content/repositories/snapshots")
    }
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(kotlin("reflect"))
    testImplementation(kotlin("test"))

    implementation("com.kotlindiscord.kord.extensions:kord-extensions:1.7.1-SNAPSHOT")

    val ktorVersion = "2.3.8"

    implementation("io.ktor:ktor-client-core:$ktorVersion")
    implementation("io.ktor:ktor-client-cio:$ktorVersion")

    implementation("org.slf4j:slf4j-simple:2.0.11")
}

val mainClassLocation = "io.github.clorissabelles.isabelles_discord_bot.ApplicationKt"

application {
    mainClass = mainClassLocation
}

tasks {
    test {
        useJUnitPlatform()
    }

    jar {
        manifest.attributes("Main-Class" to mainClassLocation)
    }

    withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "17"
    }

    named<JavaExec>("run") {
        file("run").also {
            if (!it.exists()) {
                it.mkdirs()
            }

            workingDir = it
        }

        args("-dev")
    }

    val moveReleaseTask = create("move_release") {
        doLast {
            if (!file("release").exists()) {
                file("release").mkdirs()
            }

            shadowJar.get().outputs.files.forEach {
                if (it.name.endsWith("-all.jar")) {
                    it.copyTo(file("release/${it.name.replace("-all.jar", ".jar")}"), true)
                }

            }
        }
    }

    build.get().finalizedBy(moveReleaseTask)
}