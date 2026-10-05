import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

val keystorePropertiesFile = rootProject.file("keystore.properties")
val keystoreProperties = Properties().apply {
    if (keystorePropertiesFile.exists()) {
        keystorePropertiesFile.inputStream().use(::load)
    }
}
val hasLocalReleaseSigning = keystorePropertiesFile.exists()

android {
    namespace = "com.example.workdayplanner"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.smithware.workdayplanner"
        minSdk = 26
        targetSdk = 36
        versionCode = 70
        versionName = "2.50-fullscreen-policy-fix"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    // "public" is what ships to Play Store / other Workday Planner users --
    // it never compiles in the LifeOS summary provider (see
    // app/src/personal/). "personal" is Kyle's own device-only build, signed
    // with the same keystore as LifeOS, with a distinct app ID suffix so it
    // can never collide with or be mistaken for the Play Store package.
    flavorDimensions += "distribution"
    productFlavors {
        create("public") {
            dimension = "distribution"
        }
        create("personal") {
            dimension = "distribution"
            applicationIdSuffix = ".personal"
        }
    }

    signingConfigs {
        create("localRelease") {
            if (hasLocalReleaseSigning) {
                storeFile = file(keystoreProperties.getProperty("storeFile") ?: throw GradleException("Missing storeFile in keystore.properties"))
                storePassword = keystoreProperties.getProperty("storePassword") ?: throw GradleException("Missing storePassword in keystore.properties")
                keyAlias = keystoreProperties.getProperty("keyAlias") ?: throw GradleException("Missing keyAlias in keystore.properties")
                keyPassword = keystoreProperties.getProperty("keyPassword") ?: throw GradleException("Missing keyPassword in keystore.properties")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("localRelease")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
}

gradle.taskGraph.whenReady {
    if (!hasLocalReleaseSigning && allTasks.any { it.name.contains("Release") }) {
        throw GradleException("Release signing requires local keystore.properties. Copy keystore.properties.example and fill it with local-only values.")
    }
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation("androidx.compose.material:material-icons-extended")
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.mlkit.text.recognition)
    implementation(libs.kotlinx.coroutines.play.services)

    debugImplementation(libs.androidx.compose.ui.tooling)

    testImplementation(libs.junit)
}
