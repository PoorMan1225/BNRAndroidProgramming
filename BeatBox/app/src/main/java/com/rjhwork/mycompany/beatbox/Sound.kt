package com.rjhwork.mycompany.beatbox

private const val WAV = ".wav"

// 파일 이름과 사용자가 볼 수 있는 이름 및 해당 음원 관련 정보를
// 유지하고 관리하는 객체
class Sound(val assetPath:String, var soundId: Int? = null) { // sound 값이 없음을 지정.
    val name = assetPath.split("/").last().removeSuffix(WAV)
}