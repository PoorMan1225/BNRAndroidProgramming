package com.rjhwork.mycompany.beatbox

import org.hamcrest.MatcherAssert
import org.hamcrest.core.Is.`is`
import org.junit.Before

import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

class SoundViewModelTest {

    private lateinit var beatBox:BeatBox
    private lateinit var sound:Sound
    private lateinit var subject:SoundViewModel

    @Before
    fun setUp() {
        beatBox = mock(BeatBox::class.java)
        sound = Sound("assetPath")
        subject = SoundViewModel(beatBox)
        subject.sound = sound
    }

    @Test
    fun exposeSoundNameAsTitle() {
        MatcherAssert.assertThat(subject.title, `is`(sound.name))
    }

    @Test
    fun callsBeatBoxPlayOnButtonClicked() {
        subject.onButtonClicked()
        verify(beatBox).play(sound) // 여기서 sound 는 문제될 함수가 없는 데이터 객체 이므로 모의 객체를 생성할 필요가 없다.
    }
}