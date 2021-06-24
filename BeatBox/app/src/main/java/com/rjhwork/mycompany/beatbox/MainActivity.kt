package com.rjhwork.mycompany.beatbox

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.SeekBar
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.rjhwork.mycompany.beatbox.databinding.ActivityMainBinding
import com.rjhwork.mycompany.beatbox.databinding.ListItemSoundBinding

class MainActivity : AppCompatActivity() {
        private val mainViewModel:MainViewModel by lazy {
            ViewModelProvider(this).get(MainViewModel::class.java)
        }

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)

            mainViewModel.beatBox = BeatBox(assets) // assetManager

            val binding: ActivityMainBinding =
                DataBindingUtil.setContentView(this, R.layout.activity_main)

            binding.recyclerView.apply {
                layoutManager = GridLayoutManager(context, 3)
                adapter = SoundAdapter(mainViewModel.beatBox.sounds)
            }

            binding.seekBar.setOnSeekBarChangeListener(object:SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    if(fromUser) {
                        mainViewModel.beatBox.soundPlay = (progress / 100.0).toFloat()
                        binding.percentTextView.text = getString(R.string.seek_bar_percent_text, progress, "%")
                    }
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {}

                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            })
        }

    // binding.root -> ListItemSoundBinding 의 root xml
    private inner class SoundHolder(private val binding: ListItemSoundBinding) : RecyclerView.ViewHolder(binding.root) {

        init {
            // binding 객체 생성.
            binding.viewModel = SoundViewModel(mainViewModel.beatBox)
        }

        fun bind(sound:Sound) {
            binding.apply {
                viewModel?.sound = sound
                // 보통 excecutePendingBinding()를 호출할 필요 없다.
                // 그러나 이앱에서는 RecyclerView에 포함된 바인딩 데이터를
                // 변경해야 하며, RecyclerView는 빠른 속도로 뷰를 변경해야 한다.
                // 따라서 RecyclerView 에 포함된 레이아웃을 즉각 변경하도록
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
//            binding.lifecycleOwner = this@MainActivity
            return SoundHolder(binding)
        }

        override fun onBindViewHolder(holder: SoundHolder, position: Int) {
            val sound = sounds[position]
            holder.bind(sound)
        }

        override fun getItemCount() = sounds.size
    }

    override fun onDestroy() {
        super.onDestroy()
        mainViewModel.beatBox.release()
    }
}