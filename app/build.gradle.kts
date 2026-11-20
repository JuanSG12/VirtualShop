import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

// El plugin de Google Services exige el archivo app/google-services.json (se descarga de la consola de Firebase).
// Si no existe, el proyecto igual compila; en ejecución la app avisa que falta configurar Firebase.
if (file("google-services.json").exists()) {
    apply(plugin = "com.google.gms.google-services")
}

// Llaves privadas del entorno: se leen de local.properties (que NO se sube a Git).
val localProps = Properties().apply {
    val f = rootProject.file("local.properties")
    if (f.exists()) f.inputStream().use { load(it) }
}

android {
    namespace = "com.tramas.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.tramas.app"
        minSdk = 26
        targetSdk = 34
        versionCode = 2
        versionName = "2.0"

        // ePayco: llave pública de pruebas (Dashboard ePayco > Configuración > Llaves API).
        buildConfigField("String", "EPAYCO_PUBLIC_KEY", "\"${localProps.getProperty("EPAYCO_PUBLIC_KEY", "")}\"")
        buildConfigField("boolean", "EPAYCO_TEST", localProps.getProperty("EPAYCO_TEST", "true"))
        // Solo para el checkout v2 (crea la sesión de pago). Usar SIEMPRE la llave de PRUEBAS: una llave privada dentro de un APK es extraíble.
        buildConfigField("String", "EPAYCO_PRIVATE_KEY", "\"${localProps.getProperty("EPAYCO_PRIVATE_KEY", "")}\"")
    }

    // Llave de depuración FIJA del proyecto: así todos los APK (míos, tuyos, de CI) se firman igual y se pueden
    // instalar unos sobre otros sin el error "conflicto con el paquete". Es una llave de depuración pública, no un secreto.
    signingConfigs {
        getByName("debug") {
            storeFile = file("debug.keystore")
            storePassword = "android"
            keyAlias = "androiddebugkey"
            keyPassword = "android"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
    testOptions {
        unitTests.isIncludeAndroidResources = true
        unitTests.all { test ->
            // Opcional: capturas de pantalla y jar de Robolectric descargado a mano (entornos con red restringida)
            project.findProperty("shotsDir")?.let { test.systemProperty("shots.dir", it) }
            project.findProperty("roboDir")?.let {
                test.systemProperty("robolectric.offline", "true")
                test.systemProperty("robolectric.dependency.dir", it)
            }
        }
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("androidx.fragment:fragment-ktx:1.8.5")
    implementation("androidx.navigation:navigation-fragment-ktx:2.8.4")
    implementation("androidx.navigation:navigation-ui-ktx:2.8.4")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.8.7")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation("androidx.biometric:biometric:1.1.0")

    // Firebase: Authentication, Firestore (base de datos) y Storage (imágenes)
    implementation(platform("com.google.firebase:firebase-bom:33.7.0"))
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-storage")

    // Google: ubicación (Fused Location Provider) y corrutinas para esperar las tareas de Google (await)
    implementation("com.google.android.gms:play-services-location:21.3.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.9.0")

    // Carga de imágenes remotas en ImageView
    implementation("io.coil-kt:coil:2.7.0")

    testImplementation("junit:junit:4.13.2")
    testImplementation("org.robolectric:robolectric:4.14.1")
    testImplementation("androidx.test:core-ktx:1.6.1")
    testImplementation("androidx.test.ext:junit:1.2.1")
    testImplementation("androidx.fragment:fragment-testing:1.8.5")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.9.0")
}
