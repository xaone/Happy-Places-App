package mihi.adone.happyplace.Activities
import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.android.material.textfield.TextInputEditText
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import mihi.adone.happyplace.Database.DatabaseHandler
import mihi.adone.happyplace.Models.HappyPlaceModel
import mihi.adone.happyplace.R
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.UUID


@Suppress("DEPRECATION")
class AddHappyPlaceActivity : AppCompatActivity()     , View.OnClickListener
{

    private var cal = Calendar.getInstance()
    private lateinit var dateSetListener: DatePickerDialog.OnDateSetListener
    private lateinit var galleryActivityResultLauncher: ActivityResultLauncher<Intent>
    private var saveImageToInternalStorage:Uri?=null
    private var mLatitude : Double = 0.0
    private var mLongitude : Double = 0.0





    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_happy_place)


        val toolbaraddplace: Toolbar = findViewById(R.id.toolbar_add_place)
        setSupportActionBar(toolbaraddplace)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbaraddplace.setNavigationOnClickListener{
            @Suppress("DEPRECATION")
            onBackPressed()
        }
        val et_date: TextInputEditText = findViewById(R.id.et_date)
        val tv_add_image: TextView = findViewById(R.id.tv_add_image)

        fun updateDateInView(){
            val myFormate = "dd.MM.yyyy"
            val sdf = SimpleDateFormat(myFormate, Locale.getDefault())
            et_date.setText(sdf.format(cal.time).toString())
        }


        dateSetListener = DatePickerDialog.OnDateSetListener{
            view, year, month, dayOfMonth ->
            cal.set(Calendar.YEAR,year)
            cal.set(Calendar.MONTH,month)
            cal.set(Calendar.DAY_OF_MONTH,dayOfMonth)
            updateDateInView()
        }
        updateDateInView()

        et_date.setOnClickListener(this)
        tv_add_image.setOnClickListener(this)
        val btn_save: Button = findViewById(R.id.btn_save)
        btn_save.setOnClickListener(this)


    }
    private fun saveImageToInternalStorage(bitmap: Bitmap):Uri{
        val wrapper = ContextWrapper(applicationContext)
        var file = wrapper.getDir(IMAGE_DIRECTORY, Context.MODE_PRIVATE)
        file = File(file,"${UUID.randomUUID()}.jpg")

        try {
            val stream : OutputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG,100,stream)
            stream.flush()
            stream.close()
        }catch(e: IOException){
            e.printStackTrace()
        }
        return Uri.parse(file.absolutePath)
    }
    companion object {
        private const val GALLERY = 1
        private const val CAMERA = 2
        private const val IMAGE_DIRECTORY = "HappyPlacesImages"
    }

    @Deprecated("Deprecated in Java")
    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode== Activity.RESULT_OK){
            if(requestCode == GALLERY){
                if(data != null){
                    val contentURI = data.data
                    try{
                       val selectedImageBitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, contentURI)
                        saveImageToInternalStorage=saveImageToInternalStorage(selectedImageBitmap)
                        Log.e("Saved image", "Path :: $saveImageToInternalStorage")
                        val iv_place_image: ImageView =findViewById(R.id.iv_place_image)
                        iv_place_image.setImageBitmap(selectedImageBitmap)
                    }
                    catch(e: IOException){
                        e.printStackTrace()
                        Toast.makeText(
                    this@AddHappyPlaceActivity,
                    "Failed to load an image from gallary",
                    Toast.LENGTH_SHORT).show()

                    }
                }
            }
            else if(requestCode == CAMERA){
                val thumbnail : Bitmap = data!!.extras!!.get("data") as Bitmap
                saveImageToInternalStorage=saveImageToInternalStorage(thumbnail)
                Log.e("Saved image", "Path :: $saveImageToInternalStorage")
                val iv_place_image: ImageView =findViewById(R.id.iv_place_image)

                iv_place_image.setImageBitmap(thumbnail)

            }
        }
    }
    @SuppressLint("InlinedApi")
    private fun takePhotoFromCamera(){
        Dexter.withContext(this).withPermissions(
            Manifest.permission.READ_MEDIA_VIDEO,
            Manifest.permission.READ_MEDIA_IMAGES,
            Manifest.permission.CAMERA
//            Manifest.permission.WRITE_EXTERNAL_STORAGE,

        ).withListener(object: MultiplePermissionsListener {
            override fun onPermissionsChecked(report: MultiplePermissionsReport?)
            {
                if(report!!.areAllPermissionsGranted()){
                    val galleryIntent=Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    startActivityForResult(galleryIntent, CAMERA)
//                Toast.makeText(
//                    this@AddHappyPlaceActivity,
//                    "Storage Read/Write permissions are granted.Now you can select an image from gallary",
//                    Toast.LENGTH_SHORT).show()
                }
                else {
                    showRationalDialogForPermissions()
                }
            }
            override fun onPermissionRationaleShouldBeShown(
                permissions:MutableList<PermissionRequest>,
                token: PermissionToken
            )
            {
//                token.continuePermissionRequest();
                showRationalDialogForPermissions()
            }
        }).withErrorListener {
            // we are displaying a toast message for error message.
            Toast.makeText(applicationContext, "Error occurred! ", Toast.LENGTH_SHORT).show()
        }

            .onSameThread().check()
    }

    @SuppressLint("InlinedApi")
    private fun choosePhotoFromGallary() {
        Dexter.withContext(this).withPermissions(
            Manifest.permission.READ_MEDIA_VIDEO,
            Manifest.permission.READ_MEDIA_IMAGES,
//            Manifest.permission.READ_EXTERNAL_STORAGE,
//            Manifest.permission.WRITE_EXTERNAL_STORAGE,

        ).withListener(object: MultiplePermissionsListener {
            override fun onPermissionsChecked(report: MultiplePermissionsReport?)
            {
                if(report!!.areAllPermissionsGranted()){
                    val galleryIntent=Intent(Intent.ACTION_PICK,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    startActivityForResult(galleryIntent, GALLERY)
//                Toast.makeText(
//                    this@AddHappyPlaceActivity,
//                    "Storage Read/Write permissions are granted.Now you can select an image from gallary",
//                    Toast.LENGTH_SHORT).show()
                }
                else {
                    showRationalDialogForPermissions()
                }
            }
            override fun onPermissionRationaleShouldBeShown(
                permissions:MutableList<PermissionRequest>,
                token: PermissionToken
            )
            {
//                token.continuePermissionRequest();
                showRationalDialogForPermissions()
            }
        }).withErrorListener {
            // we are displaying a toast message for error message.
            Toast.makeText(applicationContext, "Error occurred! ", Toast.LENGTH_SHORT).show()
        }

            .onSameThread().check()
    }
    private fun showRationalDialogForPermissions(){
        AlertDialog.Builder(this)
            .setMessage("It looks like you have not granted the necessary permissions for accessing the gallery.")
            .setPositiveButton("GO TO SETTINGS")
            {_,_->
                try {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri = Uri.fromParts("package",packageName, null)
                    intent.data = uri
                    startActivity(intent)
                }catch(e: ActivityNotFoundException){
                    e.printStackTrace()
                }

            }
            .setNegativeButton("Cancel"){ dialog,_->
                dialog.dismiss()
            }
            .show()

    }

    override fun onClick(v: View?) {
        when(v!!.id) {
            R.id.et_date -> {
                val datePickerDialog = DatePickerDialog(
                    this@AddHappyPlaceActivity,
                    dateSetListener,
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH),
                    cal.get(Calendar.DAY_OF_MONTH)
                )
                datePickerDialog.show()
            }

            R.id.tv_add_image -> {
                val pictureDialog = AlertDialog.Builder(this)
                pictureDialog.setTitle("Select Action")
                val pictureDialogItems = arrayOf(
                    "Select photo from Gallary",
                    "Capture photo from Camera"
                )
                pictureDialog.setItems(pictureDialogItems) { _, which ->
                    when (which) {
                        0 -> choosePhotoFromGallary()
                        1 -> takePhotoFromCamera()
                    }
                }.show()


            }
            R.id.btn_save->{
                val et_title:TextInputEditText=findViewById(R.id.et_title)
                val et_date:TextInputEditText=findViewById(R.id.et_date)
                val et_description:TextInputEditText=findViewById(R.id.et_description)
                val et_location:TextInputEditText=findViewById(R.id.et_location)
                when{


                    et_title.text.isNullOrEmpty() ->{
                        Toast.makeText(this,"Please enter title", Toast.LENGTH_SHORT).show()
                    }

                    et_description.text.isNullOrEmpty() ->{
                        Toast.makeText(this,"Please enter description", Toast.LENGTH_SHORT).show()
                    }
                    et_location.text.isNullOrEmpty() ->{
                        Toast.makeText(this,"Please enter location", Toast.LENGTH_SHORT).show()
                    }
                    saveImageToInternalStorage==null->{
                        Toast.makeText(this,"Please select an image", Toast.LENGTH_SHORT).show()

                    }
                    else->{
                        val happyPlaceModel= HappyPlaceModel(
                            0,
                            et_title.text.toString(),
                            saveImageToInternalStorage.toString(),
                            et_description.text.toString(),
                            et_date.text.toString(),
                            et_location.text.toString(),
                            mLatitude,
                            mLongitude
                        )
                        val dbHandler = DatabaseHandler(this)
                        val addHappyPlace = dbHandler.addHappyPlace(happyPlaceModel)

                        if(addHappyPlace > 0){
                            setResult(Activity.RESULT_OK)
                            finish()
                        }
                    }

                }


            }

        }
    }

}