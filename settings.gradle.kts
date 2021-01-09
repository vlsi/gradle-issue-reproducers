plugins {
    `gradle-enterprise`
}

include(
    "core",
    "core-without-versionmapping",
    "app",
    "app-without-versionmapping"
)

val isCiServer = System.getenv().containsKey("CI")

if (isCiServer) {
    gradleEnterprise {
        buildScan {
            termsOfServiceUrl = "https://gradle.com/terms-of-service"
            termsOfServiceAgree = "yes"
            tag("CI")
        }
    }
}
