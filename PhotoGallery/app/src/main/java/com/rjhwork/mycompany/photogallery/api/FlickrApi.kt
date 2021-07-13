package com.rjhwork.mycompany.photogallery.api

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Url

interface FlickrApi {

//    @GET(
//        "https://api.flickr.com/services/rest/?method=flickr.interestingness.getList" +
//                "&api_key=c24e76e070ee117a4977053a56f2e720" +
//                "&format=json&nojsoncallback=1" +
//                "&extras=url_s"
//    )

    @GET("services/rest?method=flickr.interestingness.getList")
    fun fetchPhotos(): Call<FlickrResponse>

    // fetchUrlBytes(...) 의 매개변수에 지정된 @Url과 매개변수가 없는 @GET을
    // 같이 사용하면 Retrofit이 기본 URL 대신 fetchrUrlBytes(...)에 전달된 URL 을 사용한다.
    @GET
    fun fetchUrlBytes(@Url url:String):Call<ResponseBody>

    @GET("services/rest?method=flickr.photos.search")
    fun searchPhotos(@Query("text") query:String): Call<FlickrResponse>
}