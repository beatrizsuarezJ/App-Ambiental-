plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-parcelize")
    // Add the Google services Gradle plugin
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.childcare"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.childcare"
        minSdk = 24
        targetSdk = 34
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

    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    //escencial para que funcipne el carrusel y mas cosas
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("com.google.firebase:firebase-auth-ktx:22.3.1")
    implementation("androidx.activity:activity-ktx:1.8.2")
    implementation("androidx.activity:activity:1.8.2")
    implementation("androidx.compose.ui:ui-text-desktop:1.6.5")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    // ***** Iniciooooooo Para conectar la BD de firebase
    implementation(platform("com.google.firebase:firebase-bom:32.8.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-auth-ktx")
    // ***** Finnnnnn para conectar la BD de firebase

    // *** Dependencias para loguearnos en firebase con correo y contraseña
    implementation("com.google.firebase:firebase-auth:22.3.1")
    // *** Dependencias para loguearnos en firebase con google
    implementation("com.google.android.gms:play-services-auth:21.0.0")
    // *** Dependencias para usar Google maps en nuestra app
    implementation("com.google.android.gms:play-services-maps:18.2.0")
    implementation("com.google.android.gms:play-services-location:21.2.0")
    // para las card
    implementation("androidx.cardview:cardview:1.0.0")
    //para mostrar datos
    implementation("com.google.firebase:firebase-database:20.3.1")
    //para mostrar dartos de la Bd
    implementation("androidx.recyclerview:recyclerview:1.3.2")

    // Indicador circular (para corregir el error "Clases faltantes" en la vista previa xml)
    implementation("me.relex:circleindicator:2.1.6")

    //libereria para que pueda funcionar el carrusel, el cual lo almacenara
    implementation("org.imaginativeworld.whynotimagecarousel:whynotimagecarousel:2.1.0")


    //pdf
    implementation ("com.itextpdf:itext7-core:7.2.1")

    //SubirImg a Firebase (Storague)
    implementation ("com.google.firebase:firebase-storage-ktx:20.3.0")

    //mostrar img desde firebae
    implementation ("com.github.bumptech.glide:glide:4.15.0")
    annotationProcessor ("com.github.bumptech.glide:compiler:4.12.0")



    //para el splash
}
