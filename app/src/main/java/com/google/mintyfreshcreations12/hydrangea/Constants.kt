package com.google.mintyfreshcreations12.hydrangea

object Constants {
    const val PREFS = "HYDRANGEA_PREFS"
    const val PREF_HYDRUS_BASE_URL = "PREF_HYDRUS_BASE_URL"
    const val PREF_API_KEY = "PREF_API_KEY"

    const val BNDL_IMAGE_ID = "IMAGE_ID"
    const val BNDL_IMAGE_POS = "IMAGE_POS"
    const val BNDL_IMAGE_LIST = "IMAGE_LIST"

    const val API_RESP_API_VER = "version"
    const val API_RESP_HYDRUS_VER = "hydrus_version"
    const val API_RESP_ACCESS_KEY = "access_key"
    const val API_RESP_FILE_IDS = "file_ids"
    const val API_RESP_ADD_STATUS = "status"
    enum class ADD_STATUS(val code: Int){
        SUCCESS(1),
        DUPLICATE(2),
        RESTORATION(3),
        FAILURE(4),
        VETO(7)
    }

    const val API_REQU_VERSION = "/api_version"
    const val API_REQU_REGISTER = "/request_new_permissions?name=Hydrangea&amp;basic_permissions=[0,1,2,3]"
    const val API_REQU_SEARCH = "/get_files/search_files?tags=%1\$s&Hydrus-Client-API-Access-Key=%2\$s"
    const val API_REQU_THUMBNAIL = "/get_files/thumbnail?file_id=%1\$s&Hydrus-Client-API-Access-Key=%2\$s"
    const val API_REQU_IMAGE = "/get_files/file?file_id=%1\$s&Hydrus-Client-API-Access-Key=%2\$s"
    const val API_REQU_ADD_IMAGE = "/add_files/add_file"
    const val API_REQU_ADD_URL = "/add_urls/add_url"

    const val API_HEAD_ADD_IMAGE = "application/octet-stream"
    const val API_HEAD_ADD_URL = "application/json"
    const val API_HEAD_API_KEY = "Hydrus-Client-API-Access-Key"

    const val PREF_COLUMNS = 3

    const val PICK_IMAGE = 40
}