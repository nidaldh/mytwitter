package demo.twitter

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.app.ActivityCompat
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_login.*
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

@Suppress("DEPRECATION")
class login : AppCompatActivity() {

    private var mAuth:FirebaseAuth?=null
    private var database=FirebaseDatabase.getInstance()
    private var myRef=database.reference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        mAuth = FirebaseAuth.getInstance();
        imagePerson.setOnClickListener(View.OnClickListener {
        checkPermission()
        })
    }

    fun LoginToFireBase(email:String,password:String){
        mAuth!!.createUserWithEmailAndPassword(email,password)
            .addOnCompleteListener(this){ task ->
                if (task.isSuccessful){
                    Toast.makeText(applicationContext,"Successful login",Toast.LENGTH_LONG).show()
                    saveImage()
                }else
                {
                    Toast.makeText(applicationContext,"fail login",Toast.LENGTH_LONG).show()
                }
            }

    }

    fun saveImage(){
        var currentUser = mAuth!!.currentUser
        val email:String =currentUser!!.email.toString()
        val storage =FirebaseStorage.getInstance()
        val storageRef=storage.getReferenceFromUrl("gs://twitter-nidal.appspot.com/")
        val df=SimpleDateFormat("ddMMyyHHmmss")
        val dataobj=Date()
        val imagePath= email.split("@")[0] + df.format(dataobj)+".jpg"
        val ImageRef=storageRef.child("images/"+imagePath)
        imagePerson.isDrawingCacheEnabled=true
        imagePerson.buildDrawingCache()

        val drawable=imagePerson.drawable as BitmapDrawable
        val bitmap=drawable.bitmap
        val baos=ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG,100,baos)
        val data = baos.toByteArray()
        val uploadTask=ImageRef.putBytes(data)
        uploadTask.addOnFailureListener{
            Toast.makeText(applicationContext,"fail to upload",Toast.LENGTH_LONG).show()

        }.addOnSuccessListener {TaskSnapshot ->
            var DownloadURL= TaskSnapshot.getMetadata()!!.getReference()!!.getDownloadUrl().toString()
            myRef.child("Users").child(currentUser.uid).child("email").setValue(currentUser.email)
            myRef.child("Users").child(currentUser.uid).child("ProfileImage").setValue(DownloadURL)
//            Toast.makeText(applicationContext,"Successful to upload",Toast.LENGTH_LONG).show()
             LoadTweets()
        }

    }


    override fun onStart() {
        super.onStart()
        LoadTweets()
    }
    fun LoadTweets(){
        var crrentuser=mAuth!!.currentUser
        if (crrentuser!=null){
            var intent = Intent(this,MainActivity::class.java)
            intent.putExtra("email", crrentuser?.email)
            intent.putExtra("uid",crrentuser?.uid)
            startActivity(intent)
        }

    }


    val READIMAGE:Int= 253
    fun checkPermission(){
        if (Build.VERSION.SDK_INT>=23){
            if (ActivityCompat.checkSelfPermission(this,android.Manifest.permission.READ_EXTERNAL_STORAGE)!=PackageManager.PERMISSION_GRANTED){

                requestPermissions(arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),READIMAGE)
                return
            }
        }

        loadImage()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when(requestCode){
            READIMAGE->{
                if (grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    loadImage()
                }else{
                    Toast.makeText(this,"cant access to your image",Toast.LENGTH_LONG).show()
                }
            }else-> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }

    }
val PICKIMAGE=123
    fun loadImage(){
        var intent:Intent=Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI)//i want to pick image and from where
        startActivityForResult(intent,PICKIMAGE)//because the activity start from a result
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode==PICKIMAGE && data!=null && resultCode== RESULT_OK){
            val SelectedData=data.data
            val filePath= arrayOf(MediaStore.Images.Media.DATA)
            val cursor= contentResolver.query(SelectedData,filePath,null,null,null)
            cursor.moveToFirst()
            val culomIndex=cursor.getColumnIndex(filePath[0])
            val picpath=cursor.getString(culomIndex)
            cursor.close()
            imagePerson.setImageBitmap(BitmapFactory.decodeFile(picpath))
        }
    }





    fun buLogin(view:View){
        LoginToFireBase(email.text.toString(),password.text.toString())
    }
}
