package com.rjhwork.mycompany.photogallery

import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.*
import android.widget.Gallery
import androidx.fragment.app.Fragment
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.work.*
import com.rjhwork.mycompany.photogallery.api.FlickrApi
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.util.concurrent.TimeUnit

private const val TAG = "PhotoGalleryFragment"
private const val POLL_WORK = "POLL_WORK"

class PhotoGalleryFragment : VisibleFragment() {

    private lateinit var photoRecyclerView: RecyclerView
    private lateinit var photoGalleryViewModel: PhotoGalleryViewModel
    private lateinit var thumbnailDownloader: ThumbnailDownloader<PhotoHolder>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        retainInstance = true
        setHasOptionsMenu(true) // 메뉴 항목들 툴바에 추가.

        // 화면 회전시에도 데이터 메모리에 계속 보존.
        photoGalleryViewModel = ViewModelProvider(this).get(PhotoGalleryViewModel::class.java)

        val responseHandler = Handler(Looper.getMainLooper())
        thumbnailDownloader = ThumbnailDownloader(responseHandler) { photoHolder, bitmap ->
            val drawable = BitmapDrawable(resources, bitmap)
            photoHolder.bindDrawable(drawable)
        }
        lifecycle.addObserver(thumbnailDownloader.fragmentLifecycleObserver)
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_photo_gallery, container, false)

        // 뷰의 LifecycleObserver는 Fragment.onCreateView(..) 에서 안전하게 등록할 수 있다.
        viewLifecycleOwner.lifecycle.addObserver(
            thumbnailDownloader.viewLifecycleObserver
        )

        photoRecyclerView = view.findViewById(R.id.photo_recycler_view)
        photoRecyclerView.layoutManager = GridLayoutManager(context, 3)
        return view
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.fragment_photo_galley, menu)

        val searchItem: MenuItem = menu.findItem(R.id.menu_item_search)
        val searchView = searchItem.actionView as SearchView

        searchView.apply {
            setOnQueryTextListener(object : SearchView.OnQueryTextListener {

                // 쿼리 문자열을 제출 할때, 검색버튼 누를 때.
                override fun onQueryTextSubmit(query: String?): Boolean {
                    query ?: return false

                    photoGalleryViewModel.fetchPhotos(query)
                    return true
                }

                // 텍스트가 변경 될때마다 호출.
                override fun onQueryTextChange(newText: String?): Boolean {
                    return false
                }
            })

            setOnClickListener {
                searchView.setQuery(photoGalleryViewModel.searchTerm, false)
            }
        }

        val toggleItem = menu.findItem(R.id.menu_item_toggle_polling)
        val isPolling = QueryPreferences.isPolling(requireContext())
        val toggleItemTitle = if (isPolling) {
            R.string.stop_polling
        } else {
            R.string.start_polling
        }
        toggleItem.setTitle(toggleItemTitle)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_item_clear -> {
                photoGalleryViewModel.fetchPhotos("")
                true
            }
            R.id.menu_item_toggle_polling -> {
                val isPolling = QueryPreferences.isPolling(requireContext())
                if (isPolling) {
                    WorkManager.getInstance(requireContext()).cancelUniqueWork(POLL_WORK)
                    QueryPreferences.setPolling(requireContext(), false)
                } else {
                    // 네트워크 제약.
                    val constraints = Constraints.Builder()
                        // 와이파에 에서 즉 비용이 들지 않는 네트워크에서 동작.
                        .setRequiredNetworkType(NetworkType.UNMETERED)
                        .build()

                    // 정기적 요청.
                    val periodicRequest = PeriodicWorkRequest
                        // 요청할 작업, 시간, 분
                        .Builder(PollWorker::class.java, 15, TimeUnit.MINUTES)
                        .setConstraints(constraints)
                        .build()

                    WorkManager.getInstance(requireContext()).enqueueUniquePeriodicWork(
                        POLL_WORK, // 작업 이름(고유 값. 작업 취소할때도 쓰임.)
                        ExistingPeriodicWorkPolicy.KEEP,// 기존 작업을 그대로 두고 새로운 작업 요청을 무시. 교체는 REPLACE
                        periodicRequest)

                    QueryPreferences.setPolling(requireContext(), true)
                }
                activity?.invalidateOptionsMenu()
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewLifecycleOwner.lifecycle.removeObserver(
            thumbnailDownloader.viewLifecycleObserver
        )
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

    private inner class PhotoHolder(itemImageView: ImageView) : RecyclerView.ViewHolder(itemImageView), View.OnClickListener {

        // TextView 의 함수 참조. TextView 클래스의 setText() type (CharSequence) -> Unit 을 참조한다.
        // onBindViewHolder 에서 CharSequence 값이 들어왔을 때 bindTitle 이 된다.
        val bindDrawable: (Drawable) -> Unit = itemImageView::setImageDrawable
        private lateinit var galleryItem: GalleryItem

        init {
            itemView.setOnClickListener(this)
        }

        fun bindGalleryItem(item:GalleryItem) {
            galleryItem = item
        }

        override fun onClick(v: View?) {
            val intent = PhotoPageActivity.newInstance(requireContext(), galleryItem.photoPageUri)
            startActivity(intent)
        }
    }

    private inner class PhotoAdapter(private val galleryItems: List<GalleryItem>) :
        RecyclerView.Adapter<PhotoHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.list_item_gallery, parent, false) as ImageView
            return PhotoHolder(view)
        }

        override fun onBindViewHolder(holder: PhotoHolder, position: Int) {
            val galleryItem = galleryItems[position]
            holder.bindGalleryItem(galleryItem)
            // 아이탬을 보여주기전 placeHolder
            thumbnailDownloader.queueThumbnail(holder, galleryItem.url)
            val placeHolder = ContextCompat.getDrawable(requireContext(), R.drawable.bill_up_close)
                ?: ColorDrawable()
            holder.bindDrawable(placeHolder)
        }

        override fun getItemCount() = galleryItems.size
    }

    override fun onDestroy() {
        super.onDestroy()
        lifecycle.removeObserver(
            thumbnailDownloader.fragmentLifecycleObserver
        )
    }

    companion object {
        fun newInstance() = PhotoGalleryFragment()
    }
}