plugins {
    id("java")
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("xyz.jpenilla.run-paper") version "2.2.0"
}

group = "dev.tn3w"
version = "1.0"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.3-R0.1-SNAPSHOT")
    implementation("redis.clients:jedis:5.2.0")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

tasks {
    shadowJar {
        archiveBaseName.set("PaperLogin")
        archiveClassifier.set("")
        relocate("redis.clients", "dev.tn3w.paperlogin.lib.redis")
    }
    
    runServer {
        minecraftVersion("1.21.3")
    }
    
    build {
        dependsOn(shadowJar)
    }
    
    withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
        options.release.set(21)
    }
} 