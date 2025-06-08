import java.util.Properties // Adicione esta linha
import java.io.FileInputStream // Adicione esta linha

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)

}

android {
    namespace = "com.example.avalia"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.avalia"
        minSdk = 28
        targetSdk = 35 // Considere usar 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        val properties = Properties()
        val localPropertiesFile = project.rootProject.file("local.properties")
        if (localPropertiesFile.exists()) {
            try {
                properties.load(FileInputStream(localPropertiesFile))
            } catch (e: Exception) {
                println("Warning: Could not load local.properties: ${e.message}")
            }
        }
        buildConfigField(
            "String",
            "GEMINI_API_KEY",
            "\"${properties.getProperty("GEMINI_API_KEY", "")}\""
        )

    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
// In your app/build.gradle.kts file
    dependencies {
        implementation("io.noties.markwon:core:4.6.2")
        implementation("org.reactivestreams:reactive-streams:1.0.4")
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
        implementation(libs.generativeai) // CORRETO
        implementation(libs.guava) // Versão estável e suficiente
        implementation(libs.cardview)
        implementation(libs.recyclerview)
        implementation(libs.appcompat)
        implementation(libs.material)
        implementation(libs.activity)
        implementation(libs.constraintlayout)
        implementation(libs.generativeai)
        implementation(libs.core.ktx)
        testImplementation(libs.junit)
        androidTestImplementation(libs.ext.junit)
        androidTestImplementation(libs.espresso.core)
    }
}
dependencies {
    implementation(libs.lifecycle.process)
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
}
