package com.rjhwork.mycompany.sampletextsplit

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.viewpager2.widget.ViewPager2

class MainActivity : AppCompatActivity() {

    val dataList = mutableListOf<String>()
    private lateinit var viewPager:ViewPager2
    private lateinit var adapter: ViewPagerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        dataList.add("오리너구리(학명: Ornithorhynchus anatinus)는 오스트레일리아와 태즈메이니아섬 토종의 반수서성 단공류(單孔類) 포유류의 일종이다. 가시두더지 4종과 함께 현존하는 다섯뿐인 단공류이며, 가장 원시적인 포유류인 동시에 난생(卵生)의 번식 방법을 택하고 있는 극소수의 포유류 중 하나이다.[2][3]\n")
        dataList.add("또한 오리너구리속, 오리너구리과에서도 유일하게 현재까지 명맥을 잇고 있는 종이자 모식종으로, 같은 오리너구리과에 속하는 것으로 여겨지는 다른 여러 종은 모두 화석으로만 발견된다. 다른 단공류 포유류처럼 오리너구리 역시 전기수용을 통하여 먹이의 동작을 포착한다. 포유류 가운데서는 매우 드물게도 독성 물질을 지니고 있는데, 그 중에서도 신경독을 보유하고 있고, 수컷 오리너구리의 뒷발 며느리발톱과 연결된 독샘을 통해서 분출되며, 인간이 여기에 베일 경우 찌르는 듯한 극심한 고통을 수반한다. 알에서 태어나지만 어미의 젖도 먹는다.\n")
        dataList.add("조류와 혼동되거나 조류와 포유류의 중간종이라는 오해를 받으나, 실제 유전적으로는 조류보다 파충류에 더 근접한 포유류이다. 오리를 닮은 부리, 비버를 닮은 꼬리, 수달을 닮은 발을 가진 다소 우스꽝스러운 외모에 알을 낳는 생태까지 겹쳐, 서구 박물학자들은 살아 있는 오리너구리를 확인하기 전까지, 1799년 학계에 기증된 오리너구리의 표본을 가리켜 다른 여러 동물들의 부위를 뒤섞어 조작해 놓은 가짜 표본이라고 의혹을 제기한 바도 있었다.[4][5]\n")
        dataList.add("태즈메이니아섬을 포함한 오스트레일리아의 동부에 서식한다. 뉴사우스웨일즈주의 상징동물이기도 한 오리너구리는[6][7][8] 20세기 초까지 모피를 얻고자 남획당했으나, 현재는 모든 서식지에서 법적으로 보호받고 있다. 인공번식으로 개체 수를 불리는 것이 어렵고 환경 파괴와 수질 오염에 취약하지만, 아직까지 개체 수의 폭락이나 눈에 띌 만한 위협은 보이지 않는다.")

        viewPager = findViewById(R.id.viewpager)
        adapter = ViewPagerAdapter(dataList, this)
        viewPager.adapter = adapter
    }
}