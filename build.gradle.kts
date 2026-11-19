plugins {
    alias(libs.plugins.android.library) apply false
    kotlin("android") version libs.versions.kotlin apply false
    kotlin("multiplatform") version libs.versions.kotlin apply false
    alias(libs.plugins.maven.publish) apply false
    alias(libs.plugins.binary.compatibility.validator)
}
apiValidation {
    @OptIn(kotlinx.validation.ExperimentalBCVApi::class)
    klib {
        enabled = true
    }
}
