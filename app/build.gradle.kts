import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

// Réglages de publication : atoll.properties (non versionné) ou variables d'environnement (CI).
val cfg = Properties().apply {
    val f = rootProject.file("atoll.properties")
    if (f.exists()) f.inputStream().use { load(it) }
}
fun setting(key: String, env: String, default: String = ""): String =
    System.getenv(env)?.takeIf { it.isNotBlank() } ?: cfg.getProperty(key) ?: default

// Identifiants de TEST Google par défaut : aucune vraie pub tant qu'ils ne sont pas remplacés.
val admobAppId = setting("admob.appId", "ATOLL_ADMOB_APP_ID", "ca-app-pub-3940256099942544~3347511713")
val admobRewarded = setting("admob.rewarded", "ATOLL_ADMOB_REWARDED", "ca-app-pub-3940256099942544/5224354917")
val admobInterstitial = setting("admob.interstitial", "ATOLL_ADMOB_INTERSTITIAL", "ca-app-pub-3940256099942544/1033173712")
val linkHost = setting("links.host", "ATOLL_LINK_HOST", "atoll.example.com")

android {
    namespace = "com.atoll.app"
    compileSdk = 36

    defaultConfig {
        applicationId = setting("app.id", "ATOLL_APP_ID", "com.chicocoo.atoll")
        minSdk = 26
        targetSdk = 36
        versionCode = setting("app.versionCode", "ATOLL_VERSION_CODE", "1").toInt()
        versionName = setting("app.versionName", "ATOLL_VERSION_NAME", "1.0.0")
        manifestPlaceholders["admobAppId"] = admobAppId
        manifestPlaceholders["linkHost"] = linkHost
        buildConfigField("String", "ADMOB_REWARDED", "\"$admobRewarded\"")
        buildConfigField("String", "ADMOB_INTERSTITIAL", "\"$admobInterstitial\"")
        buildConfigField("String", "LINK_HOST", "\"$linkHost\"")
    }

    val keystore = setting("signing.storeFile", "ATOLL_KEYSTORE")
    signingConfigs {
        if (keystore.isNotBlank() && file(keystore).exists()) {
            create("upload") {
                storeFile = file(keystore)
                storePassword = setting("signing.storePassword", "ATOLL_KEYSTORE_PASSWORD")
                keyAlias = setting("signing.keyAlias", "ATOLL_KEY_ALIAS")
                keyPassword = setting("signing.keyPassword", "ATOLL_KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfigs.findByName("upload")?.let { signingConfig = it }
        }
        debug {
            applicationIdSuffix = ".debug"
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
    packaging { resources.excludes += "/META-INF/{AL2.0,LGPL2.1}" }
}

kotlin { jvmToolchain(17) }

dependencies {
    implementation(project(":core"))

    implementation(platform("androidx.compose:compose-bom:2025.06.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.activity:activity-compose:1.10.1")
    implementation("androidx.core:core-ktx:1.16.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.9.0")
    implementation("androidx.lifecycle:lifecycle-process:2.9.0")
    implementation("androidx.work:work-runtime-ktx:2.10.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.10.2")

    implementation("com.google.android.gms:play-services-games-v2:20.1.2")
    implementation("com.google.android.gms:play-services-ads:23.6.0")
    implementation("com.google.android.ump:user-messaging-platform:3.1.0")
    implementation("com.android.billingclient:billing-ktx:8.0.0")
    implementation("com.android.installreferrer:installreferrer:2.2")
    implementation("com.google.android.play:review-ktx:2.0.2")

    debugImplementation("androidx.compose.ui:ui-tooling")
}
