plugins {
    `java-library`
    alias(libs.plugins.spring.dependency.management)
    alias(libs.plugins.maven.publish.plugin)
}

dependencyManagement {
    imports {
        mavenBom(libs.spring.boot.dependencies.get().toString())
    }
}

dependencies {
    api(libs.spring.boot.starter.data.jpa)

    compileOnly(libs.spring.security.core)
    compileOnly(libs.security.starter.core)
    compileOnly(libs.spring.boot.autoconfigure)
    compileOnly(libs.lombok)

    // Для генерації spring-configuration-metadata.json для @ConfigurationProperties
    annotationProcessor(libs.lombok)
    annotationProcessor(libs.spring.boot.configuration.processor)

    // Тестовий classpath
    testImplementation(libs.spring.boot.starter.test)
    testImplementation(libs.spring.security.core)
    testImplementation(libs.spring.boot.autoconfigure)
    testRuntimeOnly(libs.h2)
    testRuntimeOnly(libs.junit.platform.launcher)
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(libs.versions.java.get()))
    }
}

tasks.test {
    useJUnitPlatform()
}

mavenPublishing {
    publishToMavenCentral()
    signAllPublications()

    coordinates(
        groupId = project.group.toString(),
        artifactId = "lisovskyi-jpa-starter",
        version = project.version.toString()
    )

    pom {
        name.set("Lisovskyi JPA Spring Boot Starter")
        description.set("JPA auto-configuration starter providing UUID/sequence identity strategies, auditable base entities, and Spring Security-aware auditing for Spring Boot")
        inceptionYear.set("2026")
        url.set("https://github.com/lisovskyi-arsenii/jpa-spring-boot-starter")
        licenses {
            license {
                name.set("The Apache License, Version 2.0")
                url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
            }
        }
        developers {
            developer {
                id.set("lisovskyi-arsenii")
                name.set("Arsenii Lisovskyi")
            }
        }
        scm {
            url.set("https://github.com/lisovskyi-arsenii/jpa-spring-boot-starter")
            connection.set("scm:git:git://github.com/lisovskyi-arsenii/jpa-spring-boot-starter.git")
            developerConnection.set("scm:git:ssh://git@github.com/lisovskyi-arsenii/jpa-spring-boot-starter.git")
        }
    }
}

tasks.withType<GenerateModuleMetadata>().configureEach {
    suppressedValidationErrors.add("dependencies-without-versions")
}
