package com.rjhwork.mycompany.beatbox

import android.content.res.AssetManager
import android.util.Log
import java.lang.Exception


private const val TAG = "BeatBox"
private const val SOUNDS_FOLDER = "sample_sounds"

// 애셋은 AssetManager 클래스로 사용하며, AssetManager 인스턴스는 어떤 Context 에서도
// 생성할 수 있다. BeatBox 생성자는 AssetManager 인스턴스 참조를 받는다.
// 애셋을 사용할 때 어떤 Context 를 사용할 것인지 고민할 필요는 없다. 어떤 상황이든 모든 Context
// 의 AssetManager 가 애셋과 연결될 수 있기 때문이다.
class BeatBox(private val assets: AssetManager) {

    val sounds:List<Sound>

    init {
        sounds = loadSounds()
    }

    // 애셋에 있는 파일 내역을 얻을 때는 list(String) 함수를 사용한다.
    private fun loadSounds() : List<Sound> {
        val soundNames:Array<String>

        try{
            soundNames = assets.list(SOUNDS_FOLDER)!! // String 배열 타입.
        } catch (e: Exception) { // soundNames 가 null 이면 Exception
            return emptyList()
        }

        val sounds = mutableListOf<Sound>()
        soundNames.forEach { filename ->
            val assetPath = "$SOUNDS_FOLDER/$filename"
            sounds.add(Sound(assetPath))
        }
        return sounds
    }
}