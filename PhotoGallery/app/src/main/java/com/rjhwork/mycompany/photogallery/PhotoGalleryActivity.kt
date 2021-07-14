package com.rjhwork.mycompany.photogallery

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class PhotoGalleryActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photo_gallery)

        // savedInstanceState 가 null 이면 앱이 시작될 때 최초 실행된 액티비티
        // 인스턴스임을 의미하므로 아직 프래그먼트를 호스팅하지 않았음을 나타낸다.

        // 반면에 savedInstanceState 가 null 이 아니면 장치 회전 등의 구성 변경이나
        // 프로세스 종료로 액티비티 인스턴스가 다시 생성된 것이므로 이전에 호스팅 된
        // 프래그먼트가 있음을 의미한다.
        val isFragmentContainerEmpty = savedInstanceState == null
        if(isFragmentContainerEmpty) {
            supportFragmentManager
                .beginTransaction()
                .add(R.id.fragmentContainer, PhotoGalleryFragment.newInstance())
                .commit()
        }
    }

    companion object {
        fun newIntent(context: Context): Intent {
            return Intent(context, PhotoGalleryActivity::class.java)
        }
    }
}