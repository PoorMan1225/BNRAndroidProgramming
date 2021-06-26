package com.rjhwork.mycompany.photogallery.api

import retrofit2.Call
import retrofit2.http.GET

interface FlickrApi {

    @GET(
        "https://api.flickr.com/services/rest/?method=flickr.interestingness.getList" +
                "&api_key=c24e76e070ee117a4977053a56f2e720" +
                "&format=json&nojsoncallback=1" +
                "&extras=url_s"
    )
    fun fetchPhotos(): Call<FlickrResponse>
}