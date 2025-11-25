plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-parcelize")

    // Plugin necesario para usar Firebase
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.childcare"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.childcare"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {

    // ---------------------------------------------
    //   DEPENDENCIAS BÁSICAS DE ANDROID
    // ---------------------------------------------
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    // Activity con funciones extra para Kotlin
    implementation("androidx.activity:activity-ktx:1.8.2")

    // RecyclerView (lista de elementos)
    implementation("androidx.recyclerview:recyclerview:1.4.0")


    // ---------------------------------------------
    //   CAMERAX (COMPLETO PARA EVITAR CRASHES)
    // ---------------------------------------------
    // Núcleo de CameraX (indispensable)
    implementation("androidx.camera:camera-core:1.3.3")

    // Motor de cámara basado en Camera2 (SIEMPRE necesario)
    implementation("androidx.camera:camera-camera2:1.3.3")

    // Permite que la cámara respete el ciclo de vida de la Activity
    implementation("androidx.camera:camera-lifecycle:1.3.3")

    // Vista del preview para la cámara (PreviewView)
    implementation("androidx.camera:camera-view:1.3.3")

    // Filtros como HDR, NightMode (opcional pero útil)
    implementation("androidx.camera:camera-extensions:1.3.3")


    // ---------------------------------------------
    //   TESTING
    // ---------------------------------------------
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")


    // ---------------------------------------------
    //   FIREBASE
    // ---------------------------------------------

    // BOMS para sincronizar versiones de Firebase
    implementation(platform("com.google.firebase:firebase-bom:32.8.0"))

    // Firebase Analytics
    implementation("com.google.firebase:firebase-analytics")

    // Firebase Auth (versión KTX)
    implementation("com.google.firebase:firebase-auth-ktx")

    // Autenticación normal con Firebase
    implementation("com.google.firebase:firebase-auth:22.3.1")

    // Firebase Realtime Database (mostrar datos)
    implementation("com.google.firebase:firebase-database:20.3.1")

    // Firebase Storage (subir imágenes y archivos)
    implementation("com.google.firebase:firebase-storage-ktx:20.3.0")


    // ---------------------------------------------
    //   LOGIN CON GOOGLE
    // ---------------------------------------------
    implementation("com.google.android.gms:play-services-auth:21.0.0")


    // ---------------------------------------------
    //   GOOGLE MAPS
    // ---------------------------------------------
    implementation("com.google.android.gms:play-services-maps:18.2.0")
    implementation("com.google.android.gms:play-services-location:21.2.0")


    // ---------------------------------------------
    //   INTERFAZ Y UI ADICIONAL
    // ---------------------------------------------

    // CardView para tarjetas bonitas
    implementation("androidx.cardview:cardview:1.0.0")

    // Indicador circular para carrusel (slide dots)
    implementation("me.relex:circleindicator:2.1.6")

    // Carrusel moderno para imágenes
    implementation("org.imaginativeworld.whynotimagecarousel:whynotimagecarousel:2.1.0")


    // ---------------------------------------------
    //   PDF
    // ---------------------------------------------
    implementation("com.itextpdf:itext7-core:7.2.1")


    // ---------------------------------------------
    //   GLIDE (mostrar imágenes desde Firebase)
    // ---------------------------------------------
    implementation("com.github.bumptech.glide:glide:4.15.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.12.0")
}
