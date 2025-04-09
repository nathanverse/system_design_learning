plugins {
	java
	id("org.springframework.boot") version "3.4.4"
	id("io.spring.dependency-management") version "1.1.7"
	id("com.diffplug.spotless") version "7.0.2"
	id("com.google.cloud.tools.jib") version "3.4.5"

}

group = "com.enjoy.ds"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(17)
	}
}

jib{
	to{
		image = "narutosimaha/rate-limiter"
	}

	from{
		platforms {
			platform {
				architecture = "amd64"
				os = "linux"
			}
			platform {
				architecture = "arm64"
				os = "linux"
			}
		}
	}
}

repositories {
	mavenCentral()
}

spotless {
	// Configure Java formatting
	java {
		target("**/*.java") // Applies to all Java files
		removeUnusedImports() // Removes unused imports
		googleJavaFormat() // Formats with Google Java Style
		// Alternatively: eclipse().configFile("formatter.xml")
	}

	// Configure Kotlin formatting (if applicable)
	kotlin {
		target("**/*.kt") // Applies to all Kotlin files
		ktlint() // Uses Ktlint (standard Kotlin formatter)
		// Optional: Add license header
		licenseHeader("/* License Header */")
	}
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-webflux")
	implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("org.springframework.boot:spring-boot-starter-aop")
	implementation("io.lettuce:lettuce-core:6.5.5.RELEASE")

	implementation("io.jsonwebtoken:jjwt-api:0.12.6")
	runtimeOnly("io.jsonwebtoken:jjwt-impl:0.12.6")
	runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.12.6")
	compileOnly("org.projectlombok:lombok:1.18.30") // Or the latest version
	annotationProcessor("org.projectlombok:lombok:1.18.30")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("io.projectreactor:reactor-test:3.7.4")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
	useJUnitPlatform()
}
