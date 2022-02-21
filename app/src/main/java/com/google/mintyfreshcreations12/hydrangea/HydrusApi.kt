package com.google.mintyfreshcreations12.hydrangea

import android.app.Activity
import android.graphics.Bitmap
import android.widget.ImageView
import android.widget.Toast
import com.android.volley.Request
import com.android.volley.toolbox.ImageRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONArray

//TODO: Move Hydrangea specific logic and callbacks to Hydrus class so that HydrusApi can be a simple wrapper of the client API
typealias VersionCallback = (apiVersion: String, hydrusVersion: String) -> Unit
typealias RegistrationCallback = (apiKey: String) -> Unit
typealias SearchCallback = () -> Unit
typealias ThumbnailCallback = (id: String, thumbnail: Bitmap) -> Unit
typealias ImageCallback = (thumbnail: Bitmap) -> Unit
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
        queue.add(JsonObjectRequest(Request.Method.GET, baseUrl + Constants.API_REQU_SEARCH.format(apiKey), null,
            {
                batchThumbnailRequest(it.getJSONArray(Constants.API_RESP_FILE_IDS))
            },
            {
                Toast.makeText(context, "Search Failure", Toast.LENGTH_SHORT).show()
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
        queue.add(ImageRequest(baseUrl + Constants.API_REQU_IMAGE.format(id, apiKey),
            {
                imageCallback(it)
            }, 0, 0, ImageView.ScaleType.FIT_CENTER, Bitmap.Config.ARGB_8888, null))
    }
}