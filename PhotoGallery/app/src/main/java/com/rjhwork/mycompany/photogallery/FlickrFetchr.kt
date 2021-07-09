package com.rjhwork.mycompany.photogallery

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.rjhwork.mycompany.photogallery.api.FlickrApi
import com.rjhwork.mycompany.photogallery.api.FlickrResponse
import com.rjhwork.mycompany.photogallery.api.PhotoResponse
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

private const val TAG = "FlickrFetchr"

class FlickrFetchr {

    private val flickrApi: FlickrApi

    init {
        //https://api.flickr.com/services/rest/?method=flickr.interestingness.getList&api_key=c24e76e070ee117a4977053a56f2e720&format=json&nojsoncallback=1&extras=url_s
        val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl("https://api.flickr.com/") // baseuri 가 변경되었기 때문에 변경
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        flickrApi = retrofit.create(FlickrApi::class.java)
    }

    fun fetchPhotos(): LiveData<List<GalleryItem>> {
        val responseLiveData: MutableLiveData<List<GalleryItem>> = MutableLiveData()
        // FlickrResponse 로 요청.
        val flickrRequest: Call<FlickrResponse> = flickrApi.fetchPhotos()

        flickrRequest.enqueue(object : Callback<FlickrResponse> {
            override fun onResponse(call: Call<FlickrResponse>, response: Response<FlickrResponse>) {
                Log.d(TAG, "Response received")
                Log.d(TAG, "${response.body()}")
                // 데이터 필드 파싱.
                val flickrResponse: FlickrResponse? = response.body()
                val photoResponse: PhotoResponse? = flickrResponse?.photos
                var galleryItems: List<GalleryItem> = photoResponse?.galleryItems ?: mutableListOf()
                // 플리커의 이미지 중에는 url_s 필드 값이 없는 것도 있다. 따라서 여기서는 fliterNot{..}
                // 을 사용해서 그런 이미지 데이터를 걸러낸다.
                galleryItems = galleryItems.filterNot {
                    it.url.isBlank()
                }
                responseLiveData.value = galleryItems
            }

            override fun onFailure(call: Call<FlickrResponse>, t: Throwable) {
                Log.e(TAG, "Failed to fetch photos", t)
            }
        })
        return responseLiveData
    }

    @WorkerThread
    fun fetchPhoto(url:String): Bitmap? {
        val response: Response<ResponseBody> = flickrApi.fetchUrlBytes(url).execute()
        // 비트맵객체로 변환. use 를 사용해서 resource 해제  
        val bitmap = response.body()?.byteStream()?.use(BitmapFactory::decodeStream)
        Log.i(TAG, "Decoded bitmap=$bitmap from Response=$response")
        return bitmap
    }
}