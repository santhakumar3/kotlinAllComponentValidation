package com.example.allcomponentvalidation

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.util.Patterns
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.CheckBox
import android.widget.RadioButton
import android.widget.Switch
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import coil.load
import com.example.allcomponentvalidation.databinding.ActivityMainBinding
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import java.util.regex.Pattern

class MainActivity : AppCompatActivity() {

    // kotlin ( kotlin by nature use binding by default ) validation process
    private lateinit var binding: ActivityMainBinding

    private var acceptance = ""
    private var ownhouse = false
    private var gender = ""
    private var occupation = ""

    private var CAMERA_REQUEST_CODE = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

//      Call Email,Password,Phone Listener Method
        emailFocusListener()
        passwordFocusListener()
        phoneFocusListener()

        binding.submitButton.setOnClickListener {
            submitForm()
        }

        //Camera and Gallery Implementation

        binding.cameraBtn.setOnClickListener {
        cameraCheckPermission()
    }

        //Radio button
        val  male = findViewById<RadioButton>(R.id.male)
        val female = findViewById<RadioButton>(R.id.female)

        male.setOnClickListener(View.OnClickListener {

                male.setChecked(true)
                female.setChecked(false)
            gender = "Male"
        })

        female.setOnClickListener(View.OnClickListener {

            male.setChecked(false)
            female.setChecked(true)
            gender = "Female"
        })

        //Dropdown list
        val items = listOf("App Developer","Web Developer","iOS Developer","Tester","UI/UX Designer")
        val autoComplete : AutoCompleteTextView = findViewById(R.id.auto_complete)

        val adapter = ArrayAdapter(this,R.layout.list_item,items)

        autoComplete.setAdapter(adapter)

        autoComplete.onItemClickListener = AdapterView.OnItemClickListener { adapterView, view, i, l ->
            val itemSelected = adapterView.getItemAtPosition(i)

            occupation = itemSelected.toString()

        }


    }

    private fun cameraCheckPermission() {
        Dexter.withContext(this)
            .withPermissions(android.Manifest.permission.READ_EXTERNAL_STORAGE,
                        android.Manifest.permission.CAMERA).withListener(
                            object : MultiplePermissionsListener{
                                override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                                    report?.let {
                                        if(report.areAllPermissionsGranted()){
                                            camera()
                                        }
                                    }
                                }

                                override fun onPermissionRationaleShouldBeShown(
                                    p0: MutableList<PermissionRequest>?,
                                    p1: PermissionToken?
                                ) {
                                    showRorationalDialogPermission()
                                }

                            }
                        ).onSameThread().check()

    }

    private fun showRorationalDialogPermission() {
        AlertDialog.Builder(this)
            .setMessage("It looks like you have turned off permissions"
            +"required for this feature. It can be enable under App settings!!!")
            .setPositiveButton("Go TO SETTINGS"){
                _,_,->
                try{

                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri = Uri.fromParts("package",packageName,null)
                    intent.data = uri
                    startActivity(intent)

                }catch (e: ActivityNotFoundException){
                    e.printStackTrace()
                }
            }
            .setNegativeButton("CANCEL"){
                dialog, _->
                dialog.dismiss()
            }.show()
    }

    private fun camera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, CAMERA_REQUEST_CODE)
    }

    private fun submitForm() {

        binding.emailcontainer.helperText = validEmail()
        binding.passwordcontainer.helperText = validPassword()
        binding.phonecontainer.helperText = validPhone()


        val validEmail = binding.emailcontainer.helperText == null
        val validPassword = binding.passwordcontainer.helperText == null
        val validPhone = binding.phonecontainer.helperText == null

        if (validEmail && validPassword && validPhone) {
            //check your checkbox vaidation
            val checkbox1 = findViewById<CheckBox>(R.id.checkbox1)
            val switchbutton = findViewById<Switch>(R.id.own_house_switch)

            //checkbox check condition
            if (checkbox1.isChecked) {
                acceptance = "Accepted"
                if (switchbutton.isChecked) {
                    ownhouse = true;

                    //Radiobutton checking for Gender
                    if(!gender.isEmpty()) {
                        if(!occupation.isEmpty()){
                            resetForm()
                        }else{
                            Toast.makeText(this,"Select your occupation..",Toast.LENGTH_LONG).show()
                        }
                    }else{
                        Toast.makeText(this,"Select the gender..",Toast.LENGTH_LONG).show()
                    }
                } else {
                    if(!gender.isEmpty()) {
                        if(!occupation.isEmpty()){
                            resetForm()
                        }else{
                            Toast.makeText(this,"Select your occupation..",Toast.LENGTH_LONG).show()
                        }
                    }else{
                        Toast.makeText(this,"Select the gender..",Toast.LENGTH_LONG).show()
                    }
                }
            } else {

                Toast.makeText(this, "Please select the acceptance..", Toast.LENGTH_LONG).show()

            }

        } else
            invalidForm()
    }

    private fun invalidForm() {
        var message = ""
        if (binding.emailcontainer.helperText != null)
            message += "\n\nEmail: " + binding.emailcontainer.helperText
        if (binding.passwordcontainer.helperText != null)
            message += "\n\nPassword: " + binding.passwordcontainer.helperText
        if (binding.phonecontainer.helperText != null)
            message += "\n\nPhone: " + binding.phonecontainer.helperText
        if (acceptance != null)
            message += "\n\nAcceptance: " + "select the terms & conditions"
        if (gender.isEmpty())
            message += "\n\nGender: " + "select the gender.."
        if (occupation.isEmpty())
            message += "\n\nOccupation: " + "select the occupation.."

        AlertDialog.Builder(this)
            .setTitle("Invalid Form")
            .setMessage(message)
            .setPositiveButton("Okay") { _, _ ->
                // do nothing
            }.show()
    }

    private fun resetForm() {

        var message = "Email: " + binding.emaileditext.text
        message += "\nPassword: " + binding.passwordeditext.text
        message += "\nPhone: " + binding.phoneeditext.text
        message += "\nAcceptance: " + acceptance
        message += "\nOwn House: " + if (ownhouse) {
            "Yes, I have Ownhouse"
        } else {
            "No, I don't have Ownhouse"
        }
        message += "\nGender: " + gender
        message += "\nOccupation: " + occupation


        AlertDialog.Builder(this)
            .setTitle("Form submitted")
            .setMessage(message)
            .setPositiveButton("Okay") { _, _ ->
                binding.emaileditext.text = null
                binding.passwordeditext.text = null
                binding.phoneeditext.text = null

                acceptance = ""
                ownhouse = false
                gender  = ""
                occupation = ""


                binding.emailcontainer.helperText = "Required"
                binding.passwordcontainer.helperText = "Required"
                binding.phonecontainer.helperText = "Required"
            }.show()
    }

    //    Email Validation process with smart kotlin binding validation
    private fun emailFocusListener() {
        binding.emaileditext.setOnFocusChangeListener { _, focused ->
            if (!focused) {
                binding.emailcontainer.helperText = validEmail()
            }
        }
    }

    private fun validEmail(): String? {
        val emailText = binding.emaileditext.text.toString()
        if (!Patterns.EMAIL_ADDRESS.matcher(emailText).matches()) {
            return "Invalid Email Address"
        }
        return null
    }

    //    Password Validation process with smart kotlin binding validation
    private fun passwordFocusListener() {
        binding.passwordeditext.setOnFocusChangeListener { _, focused ->
            if (!focused) {
                binding.passwordcontainer.helperText = validPassword()
            }
        }
    }

    private fun validPassword(): String? {
        val passwordText = binding.passwordeditext.text.toString()
        if (passwordText.length < 8) {
            return "Minimum 8 Character Password"
        }
        if (!passwordText.matches(".*[A-Z].*".toRegex())) {
            return "Must Contain 1 Upper-case Character"
        }
        if (!passwordText.matches(".*[a-z].*".toRegex())) {
            return "Must Contain 1 Lower-case Character"
        }
        if (!passwordText.matches(".*[@#\$%^&+=].*".toRegex())) {
            return "Must Contain 1 Special Character (@#\$%^&+=)"
        }
        return null
    }


    //    Phone Validation process with smart kotlin binding validation
    private fun phoneFocusListener() {
        binding.phoneeditext.setOnFocusChangeListener { _, focused ->
            if (!focused) {
                binding.phonecontainer.helperText = validPhone()
            }
        }
    }

    private fun validPhone(): String? {
        val phoneText = binding.phoneeditext.text.toString()
        if (!phoneText.matches(".*[0-9].*".toRegex())) {
            return "Must be all Digits"
        }
        if (phoneText.length != 10) {
            return "Must be 10 Digits"
        }
        return null
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK){
            when(requestCode){
                CAMERA_REQUEST_CODE->{
                    val bitmap = data?.extras?.get("data") as Bitmap

                    //we are using coroutine image loader (coil)
                    binding.profileImage.load(bitmap)
                }
            }
        }
    }

}