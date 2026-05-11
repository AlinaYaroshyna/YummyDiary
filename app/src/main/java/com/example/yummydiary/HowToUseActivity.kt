package com.example.yummydiary

import android.net.Uri
import android.os.Bundle
import android.widget.MediaController
import android.widget.VideoView

class HowToUseActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_how_to_use)
        setToolbarTitle("Jak używać aplikacji")

        val videoView = findViewById<VideoView>(R.id.videoView)
        val videoPath = "android.resource://" + packageName + "/" + R.raw.how_to_use
        val uri = Uri.parse(videoPath)
        videoView.setVideoURI(uri)

        val mediaController = MediaController(this)
        videoView.setMediaController(mediaController)
        mediaController.setAnchorView(videoView)
        
        videoView.start()
    }
}