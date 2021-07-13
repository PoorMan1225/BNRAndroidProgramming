package com.rjhwork.mycompany.photogallery

import android.app.Application
import androidx.lifecycle.*

// PhotoGalleryViewModel 에서 QueryPreference 의 함수들을 사용하려면 애플리케이션 컨텍스트가 필요하다.
// 따라서 PhotoGalleyViewModel의 생성자에서 애플리케이션 컨텍스트 의 참조를 받아 속성으로 보존하며,
// PhotoGlleryViewModel 을 AndroidViewModel의 서브 클래스로 변경한다.
class PhotoGalleryViewModel(private val app: Application): AndroidViewModel(app) {
    val galleryItemLiveData: LiveData<List<GalleryItem>>

    private val flickrFetchr = FlickrFetchr()
    private val mutableSearchTerm = MutableLiveData<String>()

    // 저장된 검색값 텍스트에 채워 넣기.
    val searchTerm: String
        get() = mutableSearchTerm.value ?: ""

    init {
        mutableSearchTerm.value = QueryPreferences.getStoredQuery(app)

        // 저장된 값이 없을 경우 새로운 사진을 검색.
        galleryItemLiveData = Transformations.switchMap(mutableSearchTerm) { searchTerm ->
            if(searchTerm.isBlank()) {
                flickrFetchr.fetchPhotos()
            }else {
                flickrFetchr.searchPhotos(searchTerm)
            }
        }
    }

    fun fetchPhotos(query:String = "") {
        QueryPreferences.setStoredQuery(app, query)
        mutableSearchTerm.value = query
    }
}