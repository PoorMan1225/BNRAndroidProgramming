package com.rjhwork.mycompany.beatbox

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.GridLayout
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.rjhwork.mycompany.beatbox.databinding.ActivityMainBinding
import com.rjhwork.mycompany.beatbox.databinding.ListItemSoundBinding

class MainActivity : AppCompatActivity() {

        private lateinit var beatBox: BeatBox

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)

            beatBox = BeatBox(assets) // assetManager

            val binding: ActivityMainBinding =
                DataBindingUtil.setContentView(this, R.layout.activity_main)

            binding.recyclerView.apply {
                layoutManager = GridLayoutManager(context, 3)
                adapter = SoundAdapter(beatBox.sounds)
            }
        }

    // binding.root -> ListItemSoundBinding 의 root xml
    private inner class SoundHolder(private val binding: ListItemSoundBinding) : RecyclerView.ViewHolder(binding.root) {

        init {
            // binding 객체 생성.
            binding.viewModel = SoundViewModel()
        }

        fun bind(sound:Sound) {
            binding.apply {
                viewModel?.sound = sound
                // 보통 excecutePendingBinding()를 호출할 필요 없다.
                // 그러나 이앱에서는 RecyclerView에 포함된 바인딩 데이터를
                // 변경해야 하며, RecyclerView는빠른 속도로 뷰를 변경해야 한다.
                // 따라서 RecyclerView에 포함된 레이아웃을 즉각 변경하도록
                // executePendingBindings()을 호출한 것이다.
                executePendingBindings()
            }
        }
    }

    private inner class SoundAdapter(private val sounds:List<Sound>) : RecyclerView.Adapter<SoundHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SoundHolder {
            // viewHolder item binding
            val binding = DataBindingUtil.inflate<ListItemSoundBinding>(
                layoutInflater,
                R.layout.list_item_sound,
                parent,
                false
            )
            // LiveData 를 위한 binding
            binding.lifecycleOwner = this@MainActivity
            return SoundHolder(binding)
        }

        override fun onBindViewHolder(holder: SoundHolder, position: Int) {
            val sound = sounds[position]
            holder.bind(sound)
        }

        override fun getItemCount() = sounds.size
    }
}