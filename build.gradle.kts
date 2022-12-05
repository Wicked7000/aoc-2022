plugins {
    kotlin("jvm") version "1.7.22"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.squareup:kotlinpoet:1.12.0")
}

tasks {
    sourceSets {
        main {
            java.srcDirs("src")
        }
    }

    wrapper {
        gradleVersion = "7.6"
    }

}

tasks.register("generateDay") {
    group = "generateDay"
    description = "creates boilerplate files"
    dependsOn(tasks.build)

    doLast {
        tasks.create<JavaExec>("boilerplateCreator") {
            mainClass.set("BoilerplateCreatorKt")
            classpath = sourceSets.main.get().runtimeClasspath
            if(project.hasProperty("day")){
                args(project.property("day"))
            } else {
                throw Error("Specify the day with -Pday=?")
            }
        }.exec()
    }
}

