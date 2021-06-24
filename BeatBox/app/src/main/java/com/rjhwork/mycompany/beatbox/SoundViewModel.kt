package com.rjhwork.mycompany.beatbox

import androidx.databinding.BaseObservable
import androidx.databinding.Bindable

class SoundViewModel(val beatBox: BeatBox) : BaseObservable() {

    // 해당 함수를 버튼과 연결해야 한다.
    fun onButtonClicked() {
        sound?.let { beatBox.play(it) }
    }

    var sound: Sound? = null
        set(sound) {
            field = sound
            // notifyChange()를 호출하면
            // 데이터 객체(Sound)의 모든 바인딩 속성 값이
            // 변경 되었음을 바인딩 클래스 (ListItemSoundBinding)
            // 에 알린다.
            notifyChange()
        }


    @get:Bindable
    val title: String?
        get() = sound?.name
}
