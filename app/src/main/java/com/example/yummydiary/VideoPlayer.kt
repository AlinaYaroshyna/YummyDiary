package com.example.yummydiary

import android.net.Uri
import android.widget.MediaController
import android.widget.VideoView
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView

@Composable
fun VideoPlayer(videoResId: Int, modifier: Modifier = Modifier) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            VideoView(context).apply {
                val videoPath = "android.resource://${context.packageName}/$videoResId"
                setVideoURI(Uri.parse(videoPath))
                
                val mediaController = MediaController(context)
                setMediaController(mediaController)
                mediaController.setAnchorView(this)
                
                start()
            }
        }
    )
}
