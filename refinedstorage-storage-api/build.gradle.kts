plugins {
    id("refinedarchitect.base")
}

refinedarchitect {
    testing()
    mutationTesting()
    javadoc()
    publishing {
        maven = true
    }
}

base {
    archivesName.set("refinedstorage-storage-api")
}

dependencies {
    api(libs.apiguardian)
    api(project(":refinedstorage-resource-api"))
    api(project(":refinedstorage-core-api"))
    implementation(libs.slf4j.api)
    testImplementation(libs.junit.api)
    testImplementation(libs.junit.params)
    testImplementation(libs.assertj)
    testImplementation(libs.mockito)
    testRuntimeOnly(libs.junit.engine)
    testRuntimeOnly(libs.slf4j.impl)
}