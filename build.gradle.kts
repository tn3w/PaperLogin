plugins {
    java
    id("xyz.jpenilla.run-paper") version "2.3.1"
}

group = "dev.tn3w"
version = "1.1"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.3-R0.1-SNAPSHOT")
    compileOnly("redis.clients:jedis:5.2.0")
}

java.toolchain.languageVersion = JavaLanguageVersion.of(21)

tasks {
    processResources {
        filesMatching("plugin.yml") { expand("version" to project.version) }
    }
    runServer {
        minecraftVersion("1.21.3")
    }
}
