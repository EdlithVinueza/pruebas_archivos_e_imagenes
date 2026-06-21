plugins {
    id("java")
    application
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.drewnoakes:metadata-extractor:2.20.0")

    // TwelveMonkeys: permite leer PSD con ImageIO.read() compositeando todas las capas visibles
    implementation("com.twelvemonkeys.imageio:imageio-psd:3.11.0")
    // Core necesario para que TwelveMonkeys funcione
    implementation("com.twelvemonkeys.imageio:imageio-core:3.11.0")
    implementation("com.twelvemonkeys.common:common-lang:3.11.0")
    implementation("com.twelvemonkeys.common:common-io:3.11.0")
    implementation("com.twelvemonkeys.common:common-image:3.11.0")

    // Lombok: elimina boilerplate de getters, setters y builders
    compileOnly("org.projectlombok:lombok:1.18.46")
    annotationProcessor("org.projectlombok:lombok:1.18.46")

    // JUnit 5 para pruebas unitarias
    testImplementation(platform("org.junit:junit-bom:5.10.2"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

application {
    mainClass.set("org.example.Main")
}

tasks.test {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
        showStandardStreams = true   // muestra System.out.println de los tests
        showExceptions = true
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
    }
}