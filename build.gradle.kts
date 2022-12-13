import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.7.22"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.squareup:kotlinpoet:1.12.0")
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.0")
    testImplementation("junit:junit:4.13.2")
    testImplementation(kotlin("test"))
    implementation(kotlin("reflect"))
}

tasks {
    sourceSets {
        main {
            kotlin.srcDirs("src/main/kotlin")
        }
        test {
            kotlin.srcDirs("src/test/kotlin")
        }
    }

    wrapper {
        gradleVersion = "7.6"
    }

}

tasks.test {
    useJUnitPlatform()
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

val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
    jvmTarget = "1.8"
}
val compileTestKotlin: KotlinCompile by tasks
compileTestKotlin.kotlinOptions {
    jvmTarget = "1.8"
}