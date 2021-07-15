package com.rjhwork.mycompany.photogallery

import android.net.Uri
import com.google.gson.annotations.SerializedName

// 각 Json 객체와 연관된다. 기본적으로 Gson 은
// Json 객체의 필드 이름을 보델 객체의 속성 이름과 연관 시키므로
// 모델 객체의 속성 이름이 Json 객체의 필드 이름과 같으면 그냥
// 사용하면 된다.

// https://www.flickr.com/photos/owner/id -> 해당 사진의 url
data class GalleryItem(
    var title:String = "",
    var id: String = "",
    @SerializedName("url_s") var url:String = "",
    // 사용자 id
    @SerializedName("owner") var owner: String = ""
) {
    val photoPageUri: Uri
    get() {
        return Uri.parse("https://www.flickr.com/photos/")
            .buildUpon()
            .appendPath(owner)
            .appendPath(id)
            .build()
    }
}