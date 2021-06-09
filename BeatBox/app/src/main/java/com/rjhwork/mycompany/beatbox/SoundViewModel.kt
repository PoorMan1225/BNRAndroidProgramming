package com.rjhwork.mycompany.beatbox

import androidx.lifecycle.MutableLiveData

class SoundViewModel {

    // 관찰을 뷰모델에서 한다.
    val title:MutableLiveData<String?> = MutableLiveData()
    // Sound 객체참조 변수.
    var sound: Sound? = null
        set(sound) {
            field = sound
            title.postValue(sound?.name)
        }
}