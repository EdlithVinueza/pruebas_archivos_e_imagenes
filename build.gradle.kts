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

}

application {
    mainClass.set("org.example.Main")
}

tasks.test {
    useJUnitPlatform()
}