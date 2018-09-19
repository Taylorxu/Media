package www.xuzhiguang.com.media

import android.app.Activity
import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast

import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import java.util.jar.Manifest
import android.os.Environment.DIRECTORY_PICTURES
import android.support.v4.content.FileProvider
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity() {
    val CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100
    val CAMERA_PERMISSION_REQUEST_CODE = 1001
    private val REQUEST_CODE_PERMISSION = 0x38
    private val REQUEST_CODE_SETTING = 0x39
    private var mCurrentPhotoPath: String? = null
    var permissionArray = arrayOf(android.Manifest.permission.CAMERA, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        fab.setOnClickListener { view ->
            Snackbar.make(view, "点击右侧打开相机", Snackbar.LENGTH_LONG)
                    .setAction("打开相机", { checkSelfPermission() }).show()
        }
    }

    fun decodeAndShowImg() {
        var targetW = iv_center.width
        var targetH = iv_center.height

        var bmOptions = BitmapFactory.Options()
        bmOptions.inJustDecodeBounds = true
        BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions)
        var photoW = bmOptions.outWidth
        var photoH = bmOptions.outHeight

        var scaleFactor = Math.min(photoW / targetW, photoH / targetH)

        bmOptions.inJustDecodeBounds = false
        bmOptions.inSampleSize = scaleFactor
        bmOptions.inPurgeable = true

        var bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions)
        iv_center.setImageBitmap(bitmap)
    }

    fun openCamera() {
        var intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

        if (intent.resolveActivity(packageManager) != null) {
            var photoFile: File? = null
            try {
                photoFile = createImageFile()
            } catch (ex: IOException) {

            }
            if (photoFile != null) {
                var photoURI: Uri = FileProvider.getUriForFile(this,
                        "com.example.android.fileprovider",
                        photoFile)
                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE)
            }
        }
    }


    @Throws(IOException::class)
    private fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imageFileName = "JPEG_" + timeStamp + "_"
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val image = File.createTempFile(
                imageFileName, /* prefix */
                ".jpg", /* suffix */
                storageDir      /* directory */
        )

        mCurrentPhotoPath = image.absolutePath
        return image
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE)
            if (resultCode == Activity.RESULT_OK) {
                decodeAndShowImg()
            }

    }


    //检查权限
    private fun checkSelfPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            for (permission in permissionArray) {
                if (this.checkSelfPermission(permission) == PackageManager.PERMISSION_DENIED) {//被拒绝
                    if (this.shouldShowRequestPermissionRationale(permission)) {//第一次请求后被拒绝,但是没有禁止显示提示框
                        AlertDialog.Builder(this)
                                .setMessage("需要此权限")
                                .setPositiveButton("好吧", { _, _ -> requestPermission() }).show()
                    } else {
                        requestPermission()
                    }
                    return
                }
            }
            openCamera()
        }

    }

    private fun requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            this.requestPermissions(permissionArray, CAMERA_PERMISSION_REQUEST_CODE)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {//请求后，并同意
                openCamera()
            } else {//请求的权限被禁止显示提示框
                showDeniedDialog()
            }
        }
    }

    /**
     * 拒绝权限提示框
     */
    @Synchronized
    private fun showDeniedDialog() {
        AlertDialog.Builder(this)
                .setMessage("去设置中打开此权限")
                .setCancelable(false)
                .setNegativeButton("就不！", { _, _ ->
                    finish()
                })
                .setPositiveButton("服了了", { _, _ -> startSetting() }).show()
    }

    /**
     * 跳转到设置界面
     */
    private fun startSetting() {

        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    .setData(Uri.parse("package:$packageName"))
            startActivityForResult(intent, REQUEST_CODE_SETTING)
        } catch (e: ActivityNotFoundException) {
            e.printStackTrace()
            try {
                val intent = Intent(Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS)
                startActivityForResult(intent, REQUEST_CODE_SETTING)
            } catch (e1: Exception) {
                e1.printStackTrace()
            }

        }

    }

}
