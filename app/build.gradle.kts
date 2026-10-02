import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.compose.compiler)
    id("com.google.devtools.ksp")
    id("com.google.dagger.hilt.android")
    // Kotlin serialization plugin for type safe routes and navigation arguments
    kotlin("plugin.serialization") version "2.2.20"
}

android {
    namespace = "com.morshues.morshuesandroid"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.morshues.morshuesandroid"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        buildConfig = true
        compose = true
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlin {
        compilerOptions {
            jvmTarget = JvmTarget.JVM_11
        }
    }
}

androidComponents {
    onVariants { variant ->
        val mergeNetworkSecurityConfig = tasks.register<MergeNetworkSecurityConfigTask>(
            "merge${variant.name.replaceFirstChar { it.uppercase() }}NetworkSecurityConfig"
        ) {
            baseConfig.set(layout.projectDirectory.file("src/main/res/xml/network_security_config.xml"))
            localDomains.from(layout.projectDirectory.file("network_security_config.local.xml"))
        }
        variant.sources.res?.addGeneratedSourceDirectory(
            mergeNetworkSecurityConfig,
            MergeNetworkSecurityConfigTask::outputDir
        )
    }
}

dependencies {
    // AndroidX & Material
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.material)

    // Compose
    implementation(libs.androidx.runtime)
    implementation(libs.androidx.foundation.layout)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.coil.compose)
    implementation(libs.coil.video)
    debugImplementation(libs.androidx.ui.tooling)

    // Network
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.logging.interceptor)
    implementation(libs.jwtdecode)

    // Room
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.room.runtime)
    ksp(libs.androidx.room.compiler)

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.androidx.hilt.work)
    ksp(libs.androidx.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    debugImplementation(libs.androidx.ui.tooling)
}

// Inserts the git-ignored network_security_config.local.xml into the <domain-config> of
// res/xml/network_security_config.xml, so machine-specific test hosts never get committed.
abstract class MergeNetworkSecurityConfigTask : DefaultTask() {
    @get:InputFile
    abstract val baseConfig: RegularFileProperty

    // A file collection (not a RegularFileProperty) so the local file is allowed to be absent.
    @get:InputFiles
    abstract val localDomains: ConfigurableFileCollection

    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    @TaskAction
    fun merge() {
        val baseFile = baseConfig.get().asFile
        val base = baseFile.readText()
        val local = localDomains.files.filter { it.isFile }.joinToString("\n") { it.readText().trimEnd() }
        val merged = if (local.isBlank()) {
            base
        } else {
            val anchor = Regex("""(?m)^[ \t]*</domain-config>""").find(base)
                ?: error("No </domain-config> found in $baseFile")
            base.substring(0, anchor.range.first) + local + "\n" + base.substring(anchor.range.first)
        }
        outputDir.file("xml/network_security_config.xml").get().asFile.apply {
            parentFile.mkdirs()
            writeText(merged)
        }
    }
}