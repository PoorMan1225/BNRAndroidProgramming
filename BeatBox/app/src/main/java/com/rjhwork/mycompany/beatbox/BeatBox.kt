package com.rjhwork.mycompany.beatbox

import android.content.res.AssetFileDescriptor
import android.content.res.AssetManager
import android.media.SoundPool
import android.util.Log
import java.io.IOException
import java.lang.Exception


private const val TAG = "BeatBox"
private const val SOUNDS_FOLDER = "sample_sounds"
private const val MAX_SOUNDS = 5

// 애셋은 AssetManager 클래스로 사용하며, AssetManager 인스턴스는 어떤 Context 에서도
// 생성할 수 있다. BeatBox 생성자는 AssetManager 인스턴스 참조를 받는다.
// 애셋을 사용할 때 어떤 Context 를 사용할 것인지 고민할 필요는 없다. 어떤 상황이든 모든 Context
// 의 AssetManager 가 애셋과 연결될 수 있기 때문이다.
class BeatBox(private val assets: AssetManager) {

    val sounds:List<Sound>
    private val soundPool = SoundPool.Builder()
        .setMaxStreams(MAX_SOUNDS) // 현재 시점에 재생할 음원의 최대 개수 지정.
        .build()

    init {
        sounds = loadSounds()
    }

    var soundPlay:Float = 1.0f
        set(value) {
            field = value
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
            val sound = Sound(assetPath)
            try{
                load(sound)
                sounds.add(sound)
            }catch (ioe: IOException) {
                Log.e(TAG, "Could not load sound $filename", ioe)
            }
        }
        return sounds
    }

    private fun load(sound:Sound) {
        val afd:AssetFileDescriptor = assets.openFd(sound.assetPath)
        val soundId = soundPool.load(afd, 1)
        sound.soundId = soundId
    }

    fun play(sound:Sound) {
        sound.soundId?.let {  // null 체크.
            soundPool.play(it, 1.0f, 1.0f, 1, 0, soundPlay) // 재생률은 1이면 녹음된 속도, 2 두배, 3 세배
        }
    }

    fun release() {
        soundPool.release()
    }
}