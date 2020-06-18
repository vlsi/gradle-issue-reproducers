plugins {
    java
}

val srcLicense by configurations.creating {
    isCanBeResolved = false
    isCanBeConsumed = true
}

val srcLicenseDir by tasks.registering(Sync::class) {
    into("$buildDir/$name")
    from("$rootDir/gradle")
}

(artifacts) {
    srcLicense(srcLicenseDir)
    // The below fails as well
    // add("srcLicense", srcLicenseDir)
}
