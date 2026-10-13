plugins {
    application
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.xerial:sqlite-jdbc:3.51.1.0")
}

application {
    mainClass = "school.SchoolbookGUI"
}

sourceSets {
    main {
        java {
            setSrcDirs(listOf("src"))
        }
    }
}