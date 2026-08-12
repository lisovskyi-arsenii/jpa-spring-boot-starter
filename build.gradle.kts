plugins {
    id("java-library")
    id("io.spring.dependency-management") version "1.1.7"
    id("maven-publish")
}

repositories {
    mavenCentral()
    mavenLocal()
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.boot:spring-boot-dependencies:4.1.0")
    }
}

val lombokVersion = "1.18.46"

dependencies {
    api("org.springframework.boot:spring-boot-starter-data-jpa")

    compileOnly("org.springframework.security:spring-security-core")
    compileOnly("com.lisovskyi:security-starter-core:0.1.1")
    compileOnly("org.springframework.boot:spring-boot-autoconfigure")
    compileOnly("org.projectlombok:lombok:$lombokVersion")

    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    annotationProcessor("org.projectlombok:lombok:$lombokVersion")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
            groupId = "com.lisovskyi"
            artifactId = "lisovskyi-jpa-starter"
            version = "0.2.0"
        }
    }

    repositories {
        mavenLocal()
    }
}
