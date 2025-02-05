plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "org.bytedata.manager"
    compileSdk = 35

    defaultConfig {
        applicationId = "org.bytedata.manager"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.viewbinding)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    // Other
    val preference_version = "1.2.1"
    implementation("androidx.preference:preference:$preference_version")

    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.github.bumptech.glide:glide:4.16.0")
    implementation("com.google.code.gson:gson:2.11.0")
    implementation("org.jsoup:jsoup:1.18.3")
    implementation("com.github.getActivity:MultiLanguages:9.3")
    implementation("de.hdodenhof:circleimageview:3.1.0")

    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.1.4")
    implementation(platform("io.github.Rosemoe.sora-editor:bom:0.23.5"))
    implementation("io.github.Rosemoe.sora-editor:editor")
    implementation("io.github.Rosemoe.sora-editor:language-textmate")
}