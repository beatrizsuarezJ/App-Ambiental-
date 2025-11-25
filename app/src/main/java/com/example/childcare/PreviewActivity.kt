package com.example.childcare

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class PreviewActivity : AppCompatActivity() {

    private lateinit var uri: Uri

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_preview)

        uri = Uri.parse(intent.getStringExtra("photoUri")!!)

        findViewById<ImageView>(R.id.imgPreview).setImageURI(uri)

        findViewById<Button>(R.id.btnSend).setOnClickListener {
            val result = Intent()
            result.putExtra("photoUri", uri.toString())
            setResult(Activity.RESULT_OK, result)
            finish()
        }
    }
}
