@Suppress("DSL_SCOPE_VIOLATION") // TODO: Remove once KTIJ-19369 is fixed
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "me.rhunk.snapchatdark"
    compileSdk = 33

    signingConfigs {
        create("release") {
            storeFile = property("RELEASE_STORE_FILE")?.let { file(it) }
            storePassword = property("RELEASE_STORE_PASSWORD") as String?
            keyAlias = property("RELEASE_KEY_ALIAS") as String?
            keyPassword = property("RELEASE_KEY_PASSWORD") as String?
        }
    }

    defaultConfig {
        applicationId = android.namespace
        minSdk = 30
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            signingConfig = signingConfigs.getByName("release")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

tasks.register("assembleMagiskModule") {
    doLast{
        //create directory for magisk module in build dir
        val magiskModuleDir = File("${rootDir}/module")
        val outputMagiskDir = File("${buildDir}/magisk-module")
        magiskModuleDir.copyRecursively(outputMagiskDir, true)
        //copy the apk to the /system/app/SnapchatDark folder
        val apkFile = File("${buildDir}/outputs/apk/debug/app-debug.apk")
        val outputApkDir = File("${outputMagiskDir}/system/app/SnapchatDark")
        outputApkDir.mkdirs()
        apkFile.copyTo(File("${outputApkDir}/base.apk"), true)
    }
}

tasks.register<Zip>("zipMagiskModule", ) {
    dependsOn("assembleMagiskModule")
    from("${buildDir}/magisk-module")
    archiveFileName.set("SnapchatDark.zip")
    destinationDirectory.set(File("${buildDir}/outputs/apk/debug/"))
}

afterEvaluate {
    tasks.getByName("assembleDebug").finalizedBy("zipMagiskModule").doLast {
        exec {
            commandLine("adb", "install", "${buildDir}/outputs/apk/debug/app-debug.apk")
        }
        Thread.sleep(100L)
        exec {
            commandLine("adb", "shell", "cmd", "overlay", "enable", android.namespace)
        }
    }
}

dependencies {
}