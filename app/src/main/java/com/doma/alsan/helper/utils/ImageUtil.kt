package com.doma.alsan.helper.utils

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import androidx.core.view.setPadding
import coil.Coil
import coil.ImageLoader
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.decode.SvgDecoder
import coil.load
import coil.request.ImageRequest
import coil.transform.CircleCropTransformation
import com.stfalcon.imageviewer.StfalconImageViewer
import com.doma.alsan.R
import com.doma.alsan.helper.extensions.getAttrValue
import com.zen.overlapimagelistview.OverlapImageListView

object ImageUtil {

    fun init(context: Context) {
        // Coil 2.x uses components {} builder instead of componentRegistry {}
        val imageLoader = ImageLoader.Builder(context)
            .components {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    add(ImageDecoderDecoder.Factory())
                } else {
                    add(GifDecoder.Factory())
                }
                add(SvgDecoder.Factory())
            }
            .allowHardware(false)
            .build()

        Coil.setImageLoader(imageLoader)
    }

    fun loadImage(context: Context, url: String, imageView: AppCompatImageView) {
        imageView.load(url)
    }

    fun loadImage(context: Context, resourceId: Int, imageView: AppCompatImageView) {
        imageView.load(resourceId)
    }

    fun loadImage(context: Context, uri: Uri, imageView: AppCompatImageView) {
        imageView.load(uri)
    }

    fun loadCircleImage(context: Context, url: String, imageView: AppCompatImageView)  {
        imageView.background = ContextCompat.getDrawable(context, R.drawable.shape_oval_with_border)
        imageView.backgroundTintList = ColorStateList.valueOf(context.getAttrValue(R.attr.themeContentColor))
        imageView.setPadding(context.resources.getDimensionPixelSize(R.dimen.lineWidth))
        imageView.load(url) {
            transformations(CircleCropTransformation())
        }
    }

    fun loadRectangleImage(context: Context, url: String, imageView: AppCompatImageView) {
        imageView.background = ContextCompat.getDrawable(context, R.drawable.shape_rectangle)
        imageView.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context, android.R.color.transparent))
        imageView.load(url)
    }

    fun showFullScreenImage(context: Context, url: String, imageView: AppCompatImageView) {
        StfalconImageViewer.Builder<String>(context, arrayOf(url)) { view, image ->
            view.load(image)
        }.withTransitionFrom(imageView).withHiddenStatusBar(false).show(true)
    }

    fun loadImagesIntoOverlapImageListView(context: Context, urls: List<String>, overlapImageListView: OverlapImageListView) {
        val bitmaps = ArrayList<Bitmap>()
        val loader = Coil.imageLoader(context)

        urls.forEach {
            val request = ImageRequest.Builder(context)
                .data(it)
                .allowHardware(false)
                .transformations(CircleCropTransformation())
                .target { drawable ->
                    if (drawable is BitmapDrawable) {
                        bitmaps.add(drawable.bitmap)

                        if (bitmaps.size == overlapImageListView.circleCount) {
                            overlapImageListView.imageList = bitmaps
                        }
                    }
                }
                .build()

            loader.enqueue(request)
        }
    }

    fun downloadImage(context: Context, url: String, fileName: String?) {
        try {
            val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as android.app.DownloadManager
            val uri = Uri.parse(url)
            
            // Generate valid filename
            val extension = url.substringAfterLast('.', "jpg").substringBefore('?')
            val validFileName = (fileName ?: "image_${System.currentTimeMillis()}").replace(Regex("[^a-zA-Z0-9.-]"), "_") + ".$extension"

            val request = android.app.DownloadManager.Request(uri)
            request.setAllowedNetworkTypes(android.app.DownloadManager.Request.NETWORK_WIFI or android.app.DownloadManager.Request.NETWORK_MOBILE)
            request.setAllowedOverRoaming(false)
            request.setTitle(validFileName)
            request.setMimeType("image/*")
            request.setNotificationVisibility(android.app.DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            request.setDestinationInExternalPublicDir(android.os.Environment.DIRECTORY_PICTURES, validFileName)
            
            downloadManager.enqueue(request)
            android.widget.Toast.makeText(context, R.string.downloading, android.widget.Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            android.widget.Toast.makeText(context, R.string.failed_to_download, android.widget.Toast.LENGTH_SHORT).show()
        }
    }
}