plugins {
    `java-library`
}

val zips by configurations.creating {
    isCanBeResolved = true
    isCanBeConsumed = false
}

dependencies {
    zips(project(":dist", "zips"))
}

val printDistFiles by tasks.registering {
    inputs.files(zips)
    doLast {
        val names = zips.map { it.name }.sorted()
        val expected = listOf("buildZip.zip", "buildZip.zip.sha512")
        require(names == expected) {
            "expected: $expected, got: $names"
        }
    }
}
