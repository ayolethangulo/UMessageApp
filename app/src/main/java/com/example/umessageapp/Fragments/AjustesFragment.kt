package com.example.umessageapp.Fragments

import android.app.Activity
import android.app.ProgressDialog
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.umessageapp.Model.User
import com.example.umessageapp.R
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_ajustes.view.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [AjustesFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class AjustesFragment : Fragment() {

    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    var userReference: DatabaseReference? = null
    var firebaseUser: FirebaseUser? = null
    private val RequestCode = 438
    private  var imageUri: Uri? = null
    private var storageRef: StorageReference? = null
    private var coverChecker: String? = ""
    private var socialChecker: String? = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_ajustes, container, false)

        firebaseUser = FirebaseAuth.getInstance().currentUser
        userReference = FirebaseDatabase.getInstance().reference.child("Users").child(firebaseUser!!.uid)
        storageRef = FirebaseStorage.getInstance().reference.child("User images")

        userReference!!.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(p0: DataSnapshot) {
                if(p0.exists())
                {
                    val user: User? = p0.getValue(User::class.java)
                    if (context!=null)
                    {
                        view.username_settings.text = user!!.getUserName()
                        Picasso.get().load(user.getProfile()).into(view.profile_image_settings)
                        Picasso.get().load(user.getCover()).into(view.cover_image_settings)
                    }
                }
            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })

        view.profile_image_settings.setOnClickListener {
            coverChecker = "profile"
            elegirImagen()
        }
        view.cover_image_settings.setOnClickListener {
            coverChecker = "cover"
            elegirImagen()
        }
        view.set_facebook.setOnClickListener {
            socialChecker = "facebook"
            setSocialLinks()
        }
        view.set_instagram.setOnClickListener {
            socialChecker = "instagram"
            setSocialLinks()
        }
        view.set_website.setOnClickListener {
            socialChecker = "website"
            setSocialLinks()
        }

        return view
    }

    ///////
    private fun setSocialLinks() {
        val builder: AlertDialog.Builder =
            AlertDialog.Builder(this.requireContext(), R.style.Theme_AppCompat_DayNight_Dialog_Alert)

        if (socialChecker == "website"){
            builder.setTitle("Escriba URL:")
        }else{
            builder.setTitle("Escriba usuario:")
        }

        val editText = EditText(context)

        if (socialChecker == "website"){
            editText.hint = "www.google.com"
        }else{
            editText.hint = "user123"
        }
        builder.setView(editText)

        builder.setPositiveButton("Crear", DialogInterface.OnClickListener{
            dialog, which ->
            val str = editText.text.toString()
            if (str == ""){
                Toast.makeText(context, "Por favor escriba algo...", Toast.LENGTH_LONG).show()
            }else{
                guardarSocialLink(str)
            }
        })
        builder.setNegativeButton("Cancelar", DialogInterface.OnClickListener{
                dialog, which ->
            dialog.cancel()
        })
        builder.show()
    }

    private fun guardarSocialLink(str: String) {
        val mapSocial = HashMap<String, Any>()
        when(socialChecker){
            "facebook" ->
            {
                mapSocial["facebook"] = "https://m.facebook.com/$str"
            }
            "instagram" ->
            {
                mapSocial["instagram"] = "https://m.instagram.com/$str"
            }
            "website" ->
            {
                mapSocial["website"] = "https://$str"
            }
        }
        userReference!!.updateChildren(mapSocial).addOnCompleteListener {
            task ->
            if (task.isSuccessful){
                Toast.makeText(context, "Guardado con exito.", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun elegirImagen() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(intent, RequestCode)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RequestCode && resultCode == Activity.RESULT_OK && data!!.data != null){
            imageUri = data.data
            Toast.makeText(context, "Cargando...", Toast.LENGTH_LONG).show()
            cargarImagenBD()
        }
    }

    private fun cargarImagenBD() {
        val progressBar = ProgressDialog(context)
        progressBar.setMessage("La imagen está cargando, por favor espere.")
        progressBar.show()

        if (imageUri !=null){
            val fileRef = storageRef!!.child(System.currentTimeMillis().toString() + "jpg")
            var uploadTask: StorageTask<*>
            uploadTask = fileRef.putFile(imageUri!!)

            uploadTask.continueWithTask(Continuation <UploadTask.TaskSnapshot, Task<Uri>>{ task ->
              if (!task.isSuccessful)
              {
                  task.exception?.let {
                      throw it
                  }
              }
                return@Continuation fileRef.downloadUrl
            }).addOnCompleteListener { task ->
                if(task.isSuccessful)
                {
                    val downloadUrl = task.result
                    val url = downloadUrl.toString()

                    if(coverChecker == "cover")
                    {
                        val mapCoverImage = HashMap<String, Any>()
                            mapCoverImage["cover"] = url
                        userReference!!.updateChildren(mapCoverImage)
                        coverChecker = ""
                    }
                    else
                    {
                        val mapProfileImage = HashMap<String, Any>()
                        mapProfileImage["profile"] = url
                        userReference!!.updateChildren(mapProfileImage)
                        coverChecker = ""
                    }
                    progressBar.dismiss()
                }
            }
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment AjustesFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            AjustesFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}