plugins {
    `java-library`
}

val zips by configurations.creating {
    isCanBeConsumed = true
    isCanBeResolved = false
}

dependencies {
    // This dependency breaks :dist:test task
    implementation(project(":"))
}

// Here we generate a zip file along with its checksum and share it via zips configuration with the root project
val buildZip by tasks.registering(Zip::class) {
    archiveBaseName.set("buildZip")
    archiveVersion.set("")
    from("build.gradle.kts")
}

artifacts {
    add(zips.name, buildZip)
}

val archiveFile = buildZip.flatMap { it.archiveFile }
val sha512File = archiveFile.map { File(it.asFile.absolutePath + ".sha512") }
val shaTask = project.tasks.register(buildZip.name + "Sha512") {
    onlyIf { archiveFile.get().asFile.exists() }
    inputs.file(archiveFile)
    outputs.file(sha512File)
    doLast {
        ant.withGroovyBuilder {
            "checksum"(
                "file" to archiveFile.get(),
                "algorithm" to "SHA-512",
                "fileext" to ".sha512",
                // Make the files verifiable with shasum -c *.sha512
                "format" to "MD5SUM"
            )
        }
    }
}
artifacts {
    // https://github.com/gradle/gradle/issues/10960
    add(zips.name, sha512File) {
        type = "sha512"
        builtBy(shaTask)
    }
}

project.tasks.named(BasePlugin.ASSEMBLE_TASK_NAME) {
    dependsOn(shaTask)
}
