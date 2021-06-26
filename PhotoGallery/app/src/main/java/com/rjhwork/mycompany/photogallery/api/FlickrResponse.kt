package com.rjhwork.mycompany.photogallery.api

// 이 클래스는 Json 데이터의 가장 바깥쪽
// 객체({})와 연관된다. 그런다음 Json 응답 데이터의
// "photos"필드와 연관되는 속성을 추가한다.
class FlickrResponse {
    lateinit var photos: PhotoResponse
}