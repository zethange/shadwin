plugins {
    id("java")
    id("org.teavm") version "0.11.0"
}

group = "win.shad"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.teavm:teavm-classlib:0.11.0")
    implementation("org.teavm:teavm-jso:0.11.0")

    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}

teavm {
    all {
        mainClass = "win.shad.AleksandrShakhovWin"
    }
}