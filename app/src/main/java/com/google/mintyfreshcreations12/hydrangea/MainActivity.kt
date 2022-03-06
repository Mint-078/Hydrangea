package com.google.mintyfreshcreations12.hydrangea

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.os.Bundle
import android.util.Log
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import java.io.ByteArrayOutputStream


class MainActivity : AppCompatActivity() {
    private var baseUrl = ""
    private val imageList : MutableList<Pair<String, Bitmap>> =
        emptyArray<Pair<String, Bitmap>>().toMutableList()
    private val idList : MutableList<String> = emptyArray<String>().toMutableList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val prefs = getSharedPreferences(Constants.PREFS, MODE_PRIVATE)
        baseUrl = prefs.getString(Constants.PREF_HYDRUS_BASE_URL, "").toString()
        val hydrus = HydrusApi(
            this, prefs.getString(Constants.PREF_HYDRUS_BASE_URL, "").toString(),
            prefs.getString(Constants.PREF_API_KEY, "").toString()
        )
        hydrus.setVersionCallback { apiVersion, hydrusVersion ->
            findViewById<TextView>(R.id.textApiVersion).text =
                getString(R.string.labelApiVersion, apiVersion)
            findViewById<TextView>(R.id.textHydrusVersion).text =
                getString(R.string.labelHydrusVersion, hydrusVersion)
        }
        findViewById<TextView>(R.id.textApiVersion).text =
            getString(R.string.labelApiVersion, "")
        findViewById<TextView>(R.id.textHydrusVersion).text =
            getString(R.string.labelHydrusVersion, "")
        findViewById<TextView>(R.id.textHydrangeaVersion).text =
            getString(R.string.labelHydrangeaVersion, BuildConfig.VERSION_NAME)



        // Initialize Gallery
        val gallery = findViewById<RecyclerView>(R.id.imageGallery)
        gallery.adapter = GalleryAdapter(imageList) {
            startActivity(
                Intent(this, ViewImage::class.java).putExtra(Constants.BNDL_IMAGE_ID, it)
                    .putExtra(Constants.BNDL_IMAGE_LIST, ArrayList(idList))
            )
        }
        val manager =
            StaggeredGridLayoutManager(Constants.PREF_COLUMNS, StaggeredGridLayoutManager.VERTICAL)
        manager.gapStrategy = StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS
        gallery.layoutManager = manager
        hydrus.setThumbnailCallback{
            id, thumbnail ->
            idList.add(id)
            if (imageList.add(id to thumbnail))
                (gallery.adapter as GalleryAdapter).notifyItemInserted(imageList.size - 1)
        }

        // Prevent draw access through means other than button
        findViewById<DrawerLayout>(R.id.drawer).setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)

        // Draw access button
        val openDrawerButton: ImageButton = findViewById(R.id.buttonOpenDrawer)
        openDrawerButton.setOnClickListener {
            findViewById<DrawerLayout>(R.id.drawer).openDrawer(findViewById(R.id.drawerMenu))
        }

        //TODO: Move networking validation code to Hydrus class
        // Check for connectivity to client API
        if (baseUrl.isNotEmpty()) {
            findViewById<TextInputLayout>(R.id.editURL).editText?.setText(baseUrl)
            hydrus.repeatVersionRequest()
        }

        findViewById<TextView>(R.id.labelURL).text = getString(R.string.labelUrl, "")

        val forResult =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                if (it.resultCode == Activity.RESULT_OK) {
                    val result = it.data
                    if (result != null && result.data != null) {
                        val source = ImageDecoder.createSource(this.contentResolver, result.data!!)
                        val bitmap = ImageDecoder.decodeBitmap(source)
                        val stream = ByteArrayOutputStream()
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
                        hydrus.addImage(stream)
                    }

                }
            }

        findViewById<Button>(R.id.buttonAdd).setOnClickListener {
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            forResult.launch(intent)
        }

        findViewById<TextInputLayout>(R.id.search).editText?.setOnEditorActionListener{
            textView, actionId, _ ->
            if(actionId == EditorInfo.IME_ACTION_SEARCH)
            {
                (getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager)
                    .hideSoftInputFromWindow(findViewById<TextInputLayout>(R.id.search).windowToken, 0)

                val tags : CharSequence = textView.text.trim()
                val trimmedTags : MutableList<String> = emptyList<String>().toMutableList()
                for(tag in tags.split(','))
                {
                    trimmedTags.add(tag.trim())
                }
                val size = imageList.size
                idList.clear()
                imageList.clear()
                (gallery.adapter as GalleryAdapter).notifyItemRangeRemoved(0, size)
                hydrus.search(trimmedTags.toTypedArray())
            }
            false
        }

        findViewById<TextInputLayout>(R.id.editURL).editText?.setOnEditorActionListener {
            textView, actionId, keyEvent ->
            if(actionId == EditorInfo.IME_ACTION_DONE)
            {
                (getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager)
                    .hideSoftInputFromWindow(textView.windowToken, 0)
                hydrus.setBaseUrl(findViewById<TextInputLayout>(R.id.editURL).editText?.text.toString())
                prefs.edit().putString(Constants.PREF_HYDRUS_BASE_URL, findViewById<TextInputLayout>(R.id.editURL).editText?.text.toString()).apply()
                hydrus.register {
                    prefs.edit().putString(Constants.PREF_API_KEY, it).apply()
                    hydrus.setApiKey(it)
                    Toast.makeText(this, getString(R.string.msgRegisterSuccess), Toast.LENGTH_SHORT).show()
                }
                hydrus.repeatVersionRequest()
            }
            false
        }

        class AddUrl : DialogFragment() {
            override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
                return activity?.let {
                    val builder = AlertDialog.Builder(it)
                    val view =
                        requireActivity().layoutInflater.inflate(R.layout.add_url_dialog, null)
                    builder.setView(view)
                    builder.setMessage("Add URL")
                        .setPositiveButton("Confirm"
                        ) { _, _ ->
                            hydrus.addImageUrl(view.findViewById<EditText>(R.id.editTextUrl).text.toString())
                        }
                        .setNegativeButton("Cancel"
                        ) { _, _ ->
                        }
                    // Create the AlertDialog object and return it
                    builder.create()
                } ?: throw IllegalStateException("Activity cannot be null")
            }
        }
        findViewById<Button>(R.id.buttonAddUrl).setOnClickListener {
            val alert = AddUrl()
            alert.show(supportFragmentManager, "addUrlFragment")
        }
    }
}
