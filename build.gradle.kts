import javax.inject.Inject

val generateFile by tasks.registering() {
    doLast {
       File("build").mkdirs()
       File("build/hello.txt").writeText("world")
    }
}

open class CatTask @Inject constructor(objectFactory: ObjectFactory) : DefaultTask() {
    @Internal
    val inputFile = objectFactory.property<File>()

    // See https://github.com/gradle/gradle/issues/12627
    @get:InputFile
    val actualInputFile: File? get() = try {
        inputFile.get()
    } catch (e: IllegalStateException) {
        // This means Gradle queries property too early
        null
    }

    @TaskAction
    fun run() {
        inputFile.get().let { println("fileContents: $it") }
    }
}

val printFile by tasks.registering(CatTask::class) {
    dependsOn(generateFile)
    inputFile.set(generateFile.map { fileTree("build").matching { include("hello.txt") }.singleFile })
}
