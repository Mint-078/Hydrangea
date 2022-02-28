package com.google.mintyfreshcreations12.hydrangea

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.ImageRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONArray
import org.json.JSONException
import java.io.ByteArrayOutputStream


class MainActivity : AppCompatActivity() {
    var baseUrl = ""
    private val imageList : MutableList<Pair<String, Bitmap>> = emptyArray<Pair<String, Bitmap>>().toMutableList()
    private val idList : MutableList<String> = emptyArray<String>().toMutableList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val prefs = getSharedPreferences(Constants.PREFS, MODE_PRIVATE)
        baseUrl = prefs.getString(Constants.PREF_HYDRUS_BASE_URL, "").toString()
        val hydrus = HydrusApi(this, prefs.getString(Constants.PREF_HYDRUS_BASE_URL, "").toString(),
            prefs.getString(Constants.PREF_API_KEY, "").toString())
        hydrus.setVersionCallback{ apiVersion, hydrusVersion ->
            findViewById<TextView>(R.id.textApiVersion).text = getString(R.string.labelApiVersion, apiVersion)
            findViewById<TextView>(R.id.textHydrusVersion).text = getString(R.string.labelHydrusVersion, hydrusVersion)
        }

        // Initialize Gallery
        val gallery = findViewById<RecyclerView>(R.id.imageGallery)
        gallery.adapter = GalleryAdapter(imageList) {
            startActivity(Intent(this, ViewImage::class.java).putExtra(Constants.BNDL_IMAGE_ID, it)
                .putExtra(Constants.BNDL_IMAGE_LIST, ArrayList(idList)))
        }
        val manager = StaggeredGridLayoutManager(Constants.PREF_COLUMNS, StaggeredGridLayoutManager.VERTICAL)
        manager.gapStrategy = StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS
        gallery.layoutManager = manager
        hydrus.setThumbnailCallback {
            id, thumbnail ->
            idList.add(id)
            if (imageList.add(id to thumbnail)) (gallery.adapter as GalleryAdapter).notifyItemInserted(imageList.size - 1)
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
        if(baseUrl.isNotEmpty())
        {
            findViewById<EditText>(R.id.editURL).setText(baseUrl)
            hydrus.repeatVersionRequest()
        }

        // Update URL and attempt connection
        findViewById<EditText>(R.id.editURL).setOnFocusChangeListener{ view, hasFocus ->
            if(!hasFocus)
            {
                baseUrl = (view as EditText).text.toString()
                hydrus.repeatVersionRequest()
                prefs.edit().putString(Constants.PREF_HYDRUS_BASE_URL, baseUrl).apply()
            }
        }

        // Register with client API for access key
        findViewById<Button>(R.id.buttonRegister).setOnClickListener{
            hydrus.register {
                prefs.edit().putString(Constants.PREF_API_KEY, it).apply()
                Toast.makeText(this, getString(R.string.msgRegisterSuccess), Toast.LENGTH_SHORT).show()
            }
        }

        // Query the API for file_ids from default collection
        findViewById<Button>(R.id.buttonSearch).setOnClickListener{
            hydrus.search(emptyArray())
        }

        val forResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
            if(it.resultCode == Activity.RESULT_OK){
                val result = it.data
                if(result != null && result.data != null)
                {
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
    }
}
