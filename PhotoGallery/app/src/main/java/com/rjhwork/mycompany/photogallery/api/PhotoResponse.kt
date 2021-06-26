package com.rjhwork.mycompany.photogallery.api

import com.google.gson.annotations.SerializedName
import com.rjhwork.mycompany.photogallery.GalleryItem

// 이렇게 하면 Gson 이 자동으로 List 를 생성하고 데이터
// ("photo" 라는 이름의 JSON 배열에 저장된 GalleryItem 객체)를 추가한다.
class PhotoResponse {
    @SerializedName("photo")
    lateinit var galleryItems: List<GalleryItem>
}