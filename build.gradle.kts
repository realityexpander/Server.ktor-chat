import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

// Defined in the root `gradle.properties` file
val ktor_version: String by project
val kotlin_version: String by project
val logback_version: String by project
val kmongo_version: String by project
val koin_version: String by project

plugins {
    application
    kotlin("jvm") version "1.7.0"
    id("org.jetbrains.kotlin.plugin.serialization") version "1.7.0"
    id("com.github.johnrengelman.shadow") version "5.2.0"  // generates a fat jar
}

group = "com.realityexpander"
version = "0.0.1"
application {
    mainClass.set("io.ktor.server.netty.EngineMain")
    project.setProperty("mainClassName", mainClass.get())  // used by the shadow plugin
}

repositories {
    mavenCentral()
    gradlePluginPortal()  // for the shadow plugin
}

val sshAntTask = configurations.create("sshAntTask")

dependencies {

    println("Kotlin version: $kotlin_version")
    println("Ktor version: $ktor_version")
    println("Logback version: $logback_version")
    println("KMongo version: $kmongo_version")
    println("Koin version: $koin_version")

    implementation("io.ktor:ktor-server-core:$ktor_version")
    implementation("io.ktor:ktor-websockets:$ktor_version")
    implementation("io.ktor:ktor-serialization:$ktor_version")
    implementation("io.ktor:ktor-server-sessions:$ktor_version")
    implementation("io.ktor:ktor-server-netty:$ktor_version")
    implementation("ch.qos.logback:logback-classic:$logback_version")
    testImplementation("io.ktor:ktor-server-tests:$ktor_version")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlin_version")

    // KMongo
    implementation("org.litote.kmongo:kmongo:$kmongo_version")
    implementation("org.litote.kmongo:kmongo-coroutine:$kmongo_version")

    // Koin core features
    implementation("io.insert-koin:koin-core:$koin_version")
    implementation("io.insert-koin:koin-ktor:$koin_version")
    implementation("io.insert-koin:koin-logger-slf4j:$koin_version")

    sshAntTask("org.apache.ant:ant-jsch:1.9.2")
}

tasks.withType<ShadowJar> {
    manifest {
        attributes(
            "Main-Class" to application.mainClass.get()
        )
    }
}

ant.withGroovyBuilder {
    "taskdef"(
        "name" to "scp",
        "classname" to "org.apache.tools.ant.taskdefs.optional.ssh.Scp",
        "classpath" to configurations.get("sshAntTask").asPath
    )
    "taskdef"(
        "name" to "ssh",
        "classname" to "org.apache.tools.ant.taskdefs.optional.ssh.SSHExec",
        "classpath" to configurations.get("sshAntTask").asPath
    )
}


task("deploy") {
    dependsOn("clean", "shadowJar")

    ant.withGroovyBuilder {
        doLast {
            val knownHosts = File.createTempFile("knownhosts", "txt")
            val user = "root"
            val host = "82.180.173.232"
            val pk = file("keys/hostinger_rsa")
            val jarFileName = "com.realityexpander.ktor-chat-$version-all.jar"

            try {

                // Copy the jar file to the server
                "scp"(
                    "file" to file("build/libs/$jarFileName"),
                    "todir" to "$user@$host:/root/chat",
                    "keyfile" to pk,
                    "trust" to true,
                    "knownhosts" to knownHosts
                )

                // Rename the jar file
                "ssh"(
                    "host" to host,
                    "username" to user,
                    "keyfile" to pk,
                    "trust" to true,
                    "knownhosts" to knownHosts,
                    "command" to "mv /root/chat/$jarFileName /root/chat/chat-server.jar"
                )

                // Stop the current chat server
                "ssh"(
                    "host" to host,
                    "username" to user,
                    "keyfile" to pk,
                    "trust" to true,
                    "knownhosts" to knownHosts,
                    "command" to "systemctl stop chat"
                )

                // Start the new chat server
                "ssh"(
                    "host" to host,
                    "username" to user,
                    "keyfile" to pk,
                    "trust" to true,
                    "knownhosts" to knownHosts,
                    "command" to "systemctl start chat"
                )
            } finally {
                knownHosts.delete()
            }
        }
    }
}