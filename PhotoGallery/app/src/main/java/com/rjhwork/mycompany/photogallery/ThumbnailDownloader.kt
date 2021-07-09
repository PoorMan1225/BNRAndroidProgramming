package com.rjhwork.mycompany.photogallery

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.os.Message
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import java.util.concurrent.ConcurrentHashMap

private const val TAG = "ThumbnailDownloader"
private const val MESSAGE_DOWNLOAD = 0

// 들어오는 ThumbnailDownloader 의 제네릭 타입이 in 이라면 해당 타입을 소비하는 경우
// 들오는 타입이 out 이라면 해당 타입을 생성하는 경우로 볼 수 있다.
class ThumbnailDownloader<in T>(
    private val responseHandler: Handler,
    private val onThumbnailDownloaded: (T, Bitmap) -> Unit
): HandlerThread(TAG), LifecycleObserver {

    private var hasQuit = false
    // requestHandler 는 Handler 의 참조를 보존한다. 이 Handler 는 내려받기 요청을
    // 큐로 관리하는 책임이 있으며, 큐에서 내려받기 요청 메시지를 꺼낼 때 처리하는 일을 맡는다.
    private lateinit var requestHandler: Handler

    // requestMap 속성은 ConcurrentHashMap의 참조를 갖는다. ConcurrentHashMap은 스레드 안전한
    // HashMap 이다. 여기서는 내려받기 요청의 식별 객체(제네릭 T 타입)를 키로 사용해서 특정 다운로드
    // 요청과 연관된 URL 을 저장하고 꺼낸다. (여기서는 식별 객체가 PhotoHolder다. 따라서 다운로드
    // 된 이미지가 위치하는 UI 요소인 PhotoHolder 에 요청 응답이 쉽게 전달된다.)
    private val requestMap = ConcurrentHashMap<T, String>()

    // flickrFetchr 속성은 FlickrFetchr 인스턴스의 참조를 보존한다. 이와 같은 모든 Retrofit 설정 코드는
    // 스레드의 생애동안 한번만 실행된다.(웹 요청을 할 때마다 매번 Retrofit 인스턴스를 생성해서 구성하면 앱의 성능이
    // 저하 된다. 특히 연거푸 많은 요청을 수행할 때가 그렇다.)
    private val flickrFetchr = FlickrFetchr()

    override fun quit(): Boolean {
        hasQuit = true
        return super.quit()
    }

    fun queueThumbnail(target: T, url:String) {
        Log.i(TAG, "Got a URL: $url")
        requestMap[target] = url
        requestHandler.obtainMessage(MESSAGE_DOWNLOAD, target)
            .sendToTarget()
    }

    // 이제는 ThumbnailDownloader 가 PhotoGalleryFragment 의 생명주기를 관찰하게 되었으니
    // PhotoGalleryFragment.onCreate(...)가 호출되면 ThumbnailDownloader 가 시작되고,
    // PhotoGalleryFragment.onDestroy()가 호출되면 중단되도록 변경한다.
    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun setup() {
        Log.i(TAG, "Starting background thread")
        start() // PhotoGalleryFragment 의 onCreate 가 호출될때 스레드 시작.
        // Looper 준비. HandlerThread 를 상속 받았기 때문에 Looper 를 가질 수 있다.
        // 그리고 looper 를 준비시켜서 스레드가 준비되도록 하는 이유는 처음 Looper 를 사용하면
        // onLooperPrepared() 가 호출되어야 하는데 이렇게 된다는 보장이 없기 때문에
        // looper 를 준비시켜 놓는 것이다.
        looper
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun tearDown() {
        Log.i(TAG, "Destroying background thread")
        quit() // ON_DESTROY 가 호출될때 스레드 중단.
    }

    // Looper 가 최초로 큐를 확인하기 전에 호출
    override fun onLooperPrepared() {
        requestHandler = @SuppressLint("HandlerLeak")
        object :Handler(Looper.myLooper()!!) {
            override fun handleMessage(msg: Message) {
                if(msg.what == MESSAGE_DOWNLOAD) {
                    val target = msg.obj as T
                    Log.i(TAG, "Got a request for URL : ${requestMap[target]}")
                    handleRequest(target)
                }
            }
        }
    }

    private fun handleRequest(target:T) {
        val url = requestMap[target] ?: return
        val bitmap = flickrFetchr.fetchPhoto(url) ?: return

        responseHandler.post(Runnable {
            if(requestMap[target] != url || hasQuit) {
                return@Runnable
            }

            requestMap.remove(target)
            onThumbnailDownloaded(target, bitmap)
        })
    }
}