package com.example.similaritycheck

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.ContentValues
import android.media.Image
import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity;
import android.view.Menu
import android.view.MenuItem

import android.content.Intent

import android.provider.MediaStore

import android.net.Uri
import android.widget.ImageView

import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.widget.ImageViewCompat

import butterknife.BindView
import butterknife.ButterKnife
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.drawee.view.SimpleDraweeView
import com.facebook.imagepipeline.common.ResizeOptions
import com.facebook.imagepipeline.request.ImageRequestBuilder

import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener


import org.opencv.android.OpenCVLoader


import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import java.io.File
import java.io.IOException
import java.util.*


class MainActivity : AppCompatActivity() {

    @JvmField @BindView(R.id.main_container)
    var mainContainer: ConstraintLayout? = null

    @JvmField @BindView(R.id.imgv_photo)
    var imgvPhoto: SimpleDraweeView? = null



    private var mCurrentPhotoPath: String = ""
    private val TAKE_PHOTO_REQUEST = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Fresco.initialize(this)

        println("loading")
        initLoadOpenCVLibs()

        setContentView(R.layout.activity_main)

        ButterKnife.bind(this)
        btn_take_photo.setOnClickListener { validatePermissions() }
    }

    private fun validatePermissions() {
        Dexter.withActivity(this)
            .withPermission(Manifest.permission.CAMERA)
            .withListener(object: PermissionListener {
                override fun onPermissionGranted(
                    response: PermissionGrantedResponse?) {
                    launchCamera()
                }

                override fun onPermissionRationaleShouldBeShown(
                    permission: PermissionRequest?,
                    token: PermissionToken?) {
                    AlertDialog.Builder(this@MainActivity)
                        .setTitle(
                            R.string.storage_permission_rationale_title)
                        .setMessage(
                            R.string.storage_permition_rationale_message)
                        .setNegativeButton(
                            android.R.string.cancel
                        ) { dialog, _ ->
                            dialog.dismiss()
                            token?.cancelPermissionRequest()
                        }
                        .setPositiveButton(android.R.string.ok
                        ) { dialog, _ ->
                            dialog.dismiss()
                            token?.continuePermissionRequest()
                        }
                        .setOnDismissListener({
                            token?.cancelPermissionRequest() })
                        .show()
                }

                override fun onPermissionDenied(
                    response: PermissionDeniedResponse?) {
                    Snackbar.make(mainContainer!!,
                        R.string.storage_permission_denied_message,
                        Snackbar.LENGTH_LONG)
                        .show()
                }
            })
            .check()
    }

    private fun launchCamera() {
        val values = ContentValues(1)
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpg")
        val fileUri = contentResolver
            .insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                values)
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if(intent.resolveActivity(packageManager) != null) {
            mCurrentPhotoPath = fileUri.toString()
            intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION
                    or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            startActivityForResult(intent, TAKE_PHOTO_REQUEST)
        }
    }



    override fun onActivityResult(requestCode: Int, resultCode: Int,
                                  data: Intent?) {
        if (resultCode == Activity.RESULT_OK
            && requestCode == TAKE_PHOTO_REQUEST) {
            processCapturedPhoto()
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun processCapturedPhoto() {
        val cursor = contentResolver.query(Uri.parse(mCurrentPhotoPath),
            Array(1) {MediaStore.Images.ImageColumns.DATA},
            null, null, null)
        cursor?.moveToFirst()
        val photoPath = cursor?.getString(0)
        cursor?.close()
        val file = File(photoPath)
        val uri = Uri.fromFile(file)




        val height = resources.getDimensionPixelSize(R.dimen.photo_height)
        val width = resources.getDimensionPixelSize(R.dimen.photo_width)

        val request = ImageRequestBuilder.newBuilderWithSource(uri)
            .setResizeOptions(ResizeOptions(width, height))
            .build()

        val controller = Fresco.newDraweeControllerBuilder()
            .setOldController(imgvPhoto?.controller)
            .setImageRequest(request)
            .build()

        imgvPhoto?.controller = controller

        //imgvPhoto?.setImageResource(R.drawable.ic_image_error)
    }


    private fun initLoadOpenCVLibs() {
        val success = OpenCVLoader.initDebug()
        if (success) {
            println("loading success")
            //Log.d("test", "initLoadOpenCVLibs:OpenCV Success!")
        } else {
            println("loading failed")
            //Log.d("test", "initLoadOpenCVLibs:OpenCV Failed!")
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }







    fun compareSimilarityOfTwoPicture(imageA: Image,imageB:Image): Boolean{
        //Call OpenCV compare two picture's similarity
        return false

    }
}
