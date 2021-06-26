package com.rjhwork.mycompany.photogallery

import com.google.gson.annotations.SerializedName

// 각 Json 객체와 연관된다. 기본적으로 Gson 은
// Json 객체의 필드 이름을 보델 객체의 속성 이름과 연관 시키므로
// 모델 객체의 속성 이름이 Json 객체의 필드 이름과 같으면 그냥
// 사용하면 된다.
data class GalleryItem(
    var title:String = "",
    var id: String = "",
    @SerializedName("url_s") var url:String = ""
)