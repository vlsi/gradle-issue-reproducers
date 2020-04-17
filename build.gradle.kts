import javax.inject.Inject

val generateFile by tasks.registering() {
    doLast {
       File("build").mkdirs()
       File("build/hello.txt").writeText("world")
    }
}

open class CatTask @Inject constructor(objectFactory: ObjectFactory) : DefaultTask() {
    @InputFile
    val inputFile = objectFactory.property<File>()

    @TaskAction
    fun run() {
        inputFile.get().let { println("fileContents: $it") }
    }
}

val printFile by tasks.registering(CatTask::class) {
    dependsOn(generateFile)
    inputFile.set(generateFile.map { fileTree("build").matching { include("hello.txt") }.singleFile })
}
