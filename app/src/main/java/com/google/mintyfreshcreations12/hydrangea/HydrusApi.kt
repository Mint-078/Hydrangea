package com.google.mintyfreshcreations12.hydrangea

import android.app.Activity
import android.graphics.Bitmap
import android.os.Build
import android.util.DisplayMetrics
import android.widget.ImageView
import android.widget.Toast
import com.android.volley.Request
import com.android.volley.toolbox.ImageRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.net.URLEncoder

//TODO: Move Hydrangea specific logic and callbacks to Hydrus class so that HydrusApi can be a simple wrapper of the client API
typealias VersionCallback = (apiVersion: String, hydrusVersion: String) -> Unit
typealias RegistrationCallback = (apiKey: String) -> Unit
typealias SearchCallback = () -> Unit
typealias ThumbnailCallback = (id: String, thumbnail: Bitmap) -> Unit
typealias ImageCallback = (thumbnail: Bitmap) -> Unit
typealias AddImageCallback = () -> Unit
class HydrusApi(private val context: Activity, private var baseUrl: String, private var apiKey: String) {
    private val queue = Volley.newRequestQueue(context)
    private var versionCallback: VersionCallback = { _,_-> }
    private var registrationCallback: RegistrationCallback = {}
    private var searchCallback: SearchCallback = {}
    private var thumbnailCallback: ThumbnailCallback = {_,_->}

    fun setBaseUrl(baseUrl: String){this.baseUrl = baseUrl}
    fun setApiKey(apiKey: String){this.apiKey = apiKey}
    fun setVersionCallback(versionCallback: VersionCallback) {this.versionCallback = versionCallback}
    fun setRegistrationCallback(registrationCallback: RegistrationCallback) {this.registrationCallback = registrationCallback}
    fun setSearchCallback(searchCallback: SearchCallback) {this.searchCallback = searchCallback}
    fun setThumbnailCallback(thumbnailCallback: ThumbnailCallback) {this.thumbnailCallback = thumbnailCallback}

    fun repeatVersionRequest(){uniqueVersionRequest(versionCallback)}
    private fun uniqueVersionRequest(responseCallback: VersionCallback){
        queue.add( JsonObjectRequest(Request.Method.GET, baseUrl + Constants.API_REQU_VERSION, null,
            {
                responseCallback.invoke(it.getString(Constants.API_RESP_API_VER), it.getString(Constants.API_RESP_HYDRUS_VER))
            },
            {
                responseCallback.invoke("", "")
                Toast.makeText(context, context.getString(R.string.msgContactError), Toast.LENGTH_SHORT).show()
            }))
    }

    fun register(registrationCallback: RegistrationCallback){
        queue.add(JsonObjectRequest(Request.Method.GET, baseUrl + Constants.API_REQU_REGISTER, null,
            {
                registrationCallback(it.getString(Constants.API_RESP_ACCESS_KEY))
            },
            {
                Toast.makeText(context, context.getString(R.string.msgRegisterError), Toast.LENGTH_LONG).show()
            }))
    }

    fun search(tags: Array<String>)
    {
        val jsonTags = JSONArray(tags)
        queue.add(JsonObjectRequest(Request.Method.GET, baseUrl + Constants.API_REQU_SEARCH.format(URLEncoder.encode(jsonTags.toString(), "UTF-8"), apiKey), null,
            {
                batchThumbnailRequest(it.getJSONArray(Constants.API_RESP_FILE_IDS))
            },
            {
                Toast.makeText(context, context.getString(R.string.msgContactError), Toast.LENGTH_SHORT).show()
            }))
    }

    fun batchThumbnailRequest(fileIds: JSONArray, responseCallback: ThumbnailCallback) {for(index in 0 until fileIds.length()) uniqueThumbnailRequest(fileIds.getString(index), responseCallback)}
    fun batchThumbnailRequest(fileIds: JSONArray) {for(index in 0 until fileIds.length()) uniqueThumbnailRequest(fileIds.getString(index), thumbnailCallback)}
    private fun uniqueThumbnailRequest(id: String, responseCallback: ThumbnailCallback){
        queue.add(ImageRequest(baseUrl + Constants.API_REQU_THUMBNAIL.format(id, apiKey),
            {
                responseCallback(id, it)
            }, 0, 0, ImageView.ScaleType.FIT_CENTER, Bitmap.Config.ARGB_8888, null))
    }

    fun loadImage(id: String, imageCallback: ImageCallback){
        var maxWidth = 0
        var maxHeight = 0
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            maxWidth = context.windowManager.currentWindowMetrics.bounds.width()
            maxHeight = context.windowManager.currentWindowMetrics.bounds.width()
        } else {
            val displayMetrics = DisplayMetrics()
            context.windowManager.defaultDisplay.getMetrics(displayMetrics)
            maxWidth = displayMetrics.widthPixels
            maxHeight = displayMetrics.heightPixels
        }
        queue.add(ImageRequest(baseUrl + Constants.API_REQU_IMAGE.format(id, apiKey),
            {
                imageCallback(it)
            }, maxWidth, maxHeight, ImageView.ScaleType.FIT_CENTER, Bitmap.Config.ARGB_8888, null))
    }

    fun addImage(stream: ByteArrayOutputStream){
        val request = object: JsonObjectRequest(Request.Method.POST, baseUrl + Constants.API_REQU_ADD_IMAGE, null,
            {
                when(it.getInt(Constants.API_RESP_ADD_STATUS)){
                    Constants.ADD_STATUS.SUCCESS.code -> Toast.makeText(context, context.getString(R.string.msgAddFileSuccess), Toast.LENGTH_LONG).show()
                    Constants.ADD_STATUS.DUPLICATE.code -> Toast.makeText(context, context.getString(R.string.msgAddFileDuplicate), Toast.LENGTH_LONG).show()
                    Constants.ADD_STATUS.RESTORATION.code -> Toast.makeText(context, context.getString(R.string.msgAddFileRestore), Toast.LENGTH_LONG).show()
                    Constants.ADD_STATUS.FAILURE.code -> Toast.makeText(context, context.getString(R.string.msgAddFileFailure), Toast.LENGTH_LONG).show()
                    Constants.ADD_STATUS.VETO.code -> Toast.makeText(context, context.getString(R.string.msgAddFileVeto), Toast.LENGTH_LONG).show()
                }
            },
            {
                Toast.makeText(context, context.getString(R.string.msgContactError), Toast.LENGTH_SHORT).show()
            }){
            override fun getBody(): ByteArray {
                return stream.toByteArray()
            }

            override fun getBodyContentType(): String {
                return Constants.API_HEAD_ADD_IMAGE
            }

            override fun getHeaders(): MutableMap<String, String> {
                val map = emptyMap<String, String>().toMutableMap()
                map[Constants.API_HEAD_API_KEY] = apiKey
                return map
            }
        }
        queue.add(request)
    }

    fun addImageUrl(url: String){

        val request = object: JsonObjectRequest(Request.Method.POST, baseUrl + Constants.API_REQU_ADD_URL, null,
            {

            },
            {
                Toast.makeText(context, context.getString(R.string.msgContactError), Toast.LENGTH_SHORT).show()
            }){
            override fun getBody(): ByteArray {
                val obj = JSONObject()
                obj.put("url", url)
                return obj.toString().toByteArray(Charsets.UTF_8)
            }

            override fun getBodyContentType(): String {
                return Constants.API_HEAD_ADD_URL
            }

            override fun getHeaders(): MutableMap<String, String> {
                val map = emptyMap<String, String>().toMutableMap()
                map[Constants.API_HEAD_API_KEY] = apiKey
                return map
            }
        }
        queue.add(request)
    }
}