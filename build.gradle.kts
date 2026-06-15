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

    // BouncyCastle para Criptografía P12 y ECDSA
    implementation("org.bouncycastle:bcprov-jdk18on:1.78.1")
    implementation("org.bouncycastle:bcpkix-jdk18on:1.78.1")

    // Jackson para JSON-LD / Serialización
    implementation("com.fasterxml.jackson.core:jackson-databind:2.17.0")
    
    // Apache Commons Imaging para manipulación de metadatos (EXIF/XMP)
    implementation("org.apache.commons:commons-imaging:1.0-alpha3")

    // iText para Generación de PDF/A-3 Forense y HTML2PDF
    implementation("com.itextpdf:itext7-core:7.2.5")
    implementation("com.itextpdf:html2pdf:4.0.5")

    // ZXing para generar los Códigos QR de las Firmas
    implementation("com.google.zxing:core:3.5.3")
    implementation("com.google.zxing:javase:3.5.3")

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
}