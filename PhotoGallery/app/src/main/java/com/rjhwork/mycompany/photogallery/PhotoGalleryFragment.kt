package com.rjhwork.mycompany.photogallery

import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.rjhwork.mycompany.photogallery.api.FlickrApi
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory

private const val TAG = "PhotoGalleryFragment"

class PhotoGalleryFragment : Fragment() {

    private lateinit var photoRecyclerView: RecyclerView
    private lateinit var photoGalleryViewModel: PhotoGalleryViewModel
    private lateinit var thumbnailDownloader: ThumbnailDownloader<PhotoHolder>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        retainInstance = true

        // 화면 회전시에도 데이터 메모리에 계속 보존.
        photoGalleryViewModel = ViewModelProvider(this).get(PhotoGalleryViewModel::class.java)

        val responseHandler = Handler(Looper.getMainLooper())
        thumbnailDownloader = ThumbnailDownloader(responseHandler) { photoHolder, bitmap ->
            val drawable = BitmapDrawable(resources, bitmap)
            photoHolder.bindDrawable(drawable)
        }
        lifecycle.addObserver(thumbnailDownloader)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_photo_gallery, container, false)

        photoRecyclerView = view.findViewById(R.id.photo_recycler_view)
        photoRecyclerView.layoutManager = GridLayoutManager(context, 3)
        return view
    }

    // viewLifecyclerOwner 즉 뷰의 생명주기를 따르기 때문에
    // 뷰가 생성 다 되었을때 관찰자를 등록.
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        photoGalleryViewModel.galleryItemLiveData.observe(
            viewLifecycleOwner,
            Observer { galleryItems ->
                photoRecyclerView.adapter = PhotoAdapter(galleryItems)
            }
        )
    }

    private class PhotoHolder(itemImageView: ImageView):RecyclerView.ViewHolder(itemImageView) {

        // TextView 의 함수 참조. TextView 클래스의 setText() type (CharSequence) -> Unit 을 참조한다.
        // onBindViewHolder 에서 CharSequence 값이 들어왔을 때 bindTitle 이 된다.
        val bindDrawable : (Drawable) -> Unit = itemImageView::setImageDrawable
    }

    private inner class PhotoAdapter(private val galleryItems: List<GalleryItem>) : RecyclerView.Adapter<PhotoHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.list_item_gallery, parent, false) as ImageView
            return PhotoHolder(view)
        }

        override fun onBindViewHolder(holder: PhotoHolder, position: Int) {
            val galleryItem = galleryItems[position]

            // 아이탬을 보여주기전 placeHolder
            thumbnailDownloader.queueThumbnail(holder, galleryItem.url)
            val placeHolder = ContextCompat.getDrawable(requireContext(), R.drawable.bill_up_close) ?: ColorDrawable()
            holder.bindDrawable(placeHolder)
        }

        override fun getItemCount() = galleryItems.size
    }

    override fun onDestroy() {
        super.onDestroy()
        lifecycle.removeObserver(
            thumbnailDownloader
        )
    }

    companion object {
        fun newInstance() = PhotoGalleryFragment()
    }
}