package com.example.childcare

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import java.io.File

class CameraActivity : AppCompatActivity() {

    private lateinit var previewView: PreviewView
    private var imageCapture: ImageCapture? = null
    private var flashState = ImageCapture.FLASH_MODE_OFF
    private var cameraProvider: ProcessCameraProvider? = null

    // ====================================
    //  RECIBE FOTO DESDE PREVIEW
    // ====================================
    private val previewResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val uriString = result.data?.getStringExtra("photoUri")
                if (uriString != null) {
                    val returnIntent = Intent()
                    returnIntent.putExtra("image_uri", Uri.parse(uriString))
                    setResult(RESULT_OK, returnIntent)
                    finish()
                }
            }
        }

    // ====================================
    //  SELECCIÓN DE GALERÍA
    // ====================================
    private val galleryResult =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            if (uri != null) {
                val intent = Intent(this, PreviewActivity::class.java)
                intent.putExtra("photoUri", uri.toString())
                previewResult.launch(intent)
            }
        }

    // ====================================
    //  PERMISOS DE CÁMARA
    // ====================================
    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                startCamera()
            } else {
                Toast.makeText(this, "Permiso de cámara requerido", Toast.LENGTH_LONG).show()
                finish()
            }
        }

    private fun checkPermissions() {
        val granted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        if (granted) {
            startCamera()
        } else {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    // ====================================
    //  ON CREATE
    // ====================================
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)

        previewView = findViewById(R.id.previewView)

        checkPermissions()

        // Botones
        findViewById<ImageButton>(R.id.btnCapture).setOnClickListener { takePhoto() }
        findViewById<ImageButton>(R.id.btnFlash).setOnClickListener { toggleFlash() }
        findViewById<ImageButton>(R.id.btnClose).setOnClickListener { finish() }
        findViewById<ImageButton>(R.id.btnGallery).setOnClickListener { openGallery() }
    }

    // ====================================
    //  INICIAR CÁMARA X
    // ====================================
    private fun startCamera() {
        val providerFuture = ProcessCameraProvider.getInstance(this)

        providerFuture.addListener({
            cameraProvider = providerFuture.get()

            val preview = Preview.Builder()
                .setTargetRotation(previewView.display.rotation)
                .build()
                .apply {
                    setSurfaceProvider(previewView.surfaceProvider)
                }

            imageCapture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .setFlashMode(flashState)
                .setTargetRotation(previewView.display.rotation)
                .build()

            val selector = CameraSelector.DEFAULT_BACK_CAMERA

            cameraProvider?.unbindAll()
            cameraProvider?.bindToLifecycle(
                this,
                selector,
                preview,
                imageCapture
            )

        }, ContextCompat.getMainExecutor(this))
    }

    // ====================================
    //  TOMAR FOTO
    // ====================================
    private fun takePhoto() {

        if (imageCapture == null) {
            Toast.makeText(this, "Cámara no lista aún", Toast.LENGTH_SHORT).show()
            return
        }

        val file = File(cacheDir, "IMG_${System.currentTimeMillis()}.jpg")
        val output = ImageCapture.OutputFileOptions.Builder(file).build()

        imageCapture!!.takePicture(
            output,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {

                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    val uri = Uri.fromFile(file)
                    val intent = Intent(this@CameraActivity, PreviewActivity::class.java)
                    intent.putExtra("photoUri", uri.toString())
                    previewResult.launch(intent)
                }

                override fun onError(e: ImageCaptureException) {
                    Log.e("CameraX", "Error al tomar foto: ${e.message}")
                }
            }
        )
    }

    // ====================================
    //  ABRIR GALERÍA
    // ====================================
    private fun openGallery() {
        galleryResult.launch("image/*")
    }

    // ====================================
    //  FLASH
    // ====================================
    private fun toggleFlash() {
        flashState = when (flashState) {
            ImageCapture.FLASH_MODE_OFF -> ImageCapture.FLASH_MODE_ON
            ImageCapture.FLASH_MODE_ON -> ImageCapture.FLASH_MODE_AUTO
            else -> ImageCapture.FLASH_MODE_OFF
        }

        imageCapture?.flashMode = flashState
    }

    // ====================================
    //  LIBERAR CÁMARA
    // ====================================
    override fun onDestroy() {
        super.onDestroy()
        cameraProvider?.unbindAll()
    }
}
