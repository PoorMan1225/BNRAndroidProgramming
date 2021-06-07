package com.bignerdranch.criminalintent2

import android.content.Context
import android.os.Bundle
import android.view.*
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bignerdranch.criminalintent2.viewmodel.CrimeListViewModel
import java.util.*

class CrimeListFragment() : Fragment() {

    private val crimeListViewModel by viewModels<CrimeListViewModel>()
    private lateinit var crimeRecyclerView: RecyclerView
    private lateinit var emptyLayout:ConstraintLayout
    private lateinit var emptyAddButton: Button
    private var adapter: CrimeAdapter? = CrimeAdapter()

    interface Callbacks {
        fun onCrimeSelected(crimeId: UUID)
    }

    private var callbacks:Callbacks? = null

//    var callbacks:((UUID) -> Unit)? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callbacks = context as Callbacks?
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_crime_list, container, false)
        crimeRecyclerView = view.findViewById(R.id.crime_recycler_view)
        emptyLayout = view.findViewById(R.id.emptyLayout)
        emptyAddButton = view.findViewById(R.id.empty_add_button)
        crimeRecyclerView.layoutManager = LinearLayoutManager(context)
        crimeRecyclerView.adapter = adapter  // 빈리스트 초기화.
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        crimeListViewModel.crimeListLiveData.observe(viewLifecycleOwner, { crimes ->
//            updateUI(crimes)
            if(crimes.isNullOrEmpty().not()) {
                emptyLayout.isVisible = false
                adapter?.submitList(crimes)
            }else {
                emptyLayout.isVisible = true
            }
        })
    }

    private fun addCrime() {
        val crime = Crime()
        // 데이터베이스에 새로운 crime 객체 등록.
        crimeListViewModel.addCrime(crime)
        // MainActivity 에서 CrimeFragment 로 crime.id 를 전달해준다.
        callbacks?.onCrimeSelected(crime.id)
    }

    override fun onStart() {
        super.onStart()
        emptyAddButton.setOnClickListener {
            addCrime()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.fragment_crime_list, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId) {
            R.id.new_crime -> {
                addCrime()
                true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

//    private fun updateUI(crimes: List<Crime>) {
//        adapter = CrimeAdapter(crimes)
//        crimeRecyclerView.adapter = adapter
//    }

    private inner class CrimeHolder(view: View) : RecyclerView.ViewHolder(view),
        View.OnClickListener {

        private lateinit var crime: Crime
        private val titleTextView: TextView = itemView.findViewById(R.id.crime_title)  // itemView 는 뷰홀더 내부의 itemView
        private val dateTextView: TextView = itemView.findViewById(R.id.crime_date)
        private val solvedImageView: ImageView = itemView.findViewById(R.id.crime_solved)

        init {
            itemView.setOnClickListener(this)
        }

        fun bind(crime: Crime) {
            this.crime = crime
            titleTextView.text = this.crime.title
            dateTextView.text = this.crime.date.toString()
            solvedImageView.visibility = if (crime.isSolved) {
                View.VISIBLE
            } else {
                View.GONE
            }
        }

        override fun onClick(v: View?) {
//            callbacks?.let { it(crime.id) }
            callbacks?.onCrimeSelected(crime.id)
        }
    }

    override fun onDetach() {
        super.onDetach()
        callbacks = null
    }

    private inner class CrimeAdapter() :
        ListAdapter<Crime, CrimeHolder>(diffUtil) {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CrimeHolder {
            val view = layoutInflater.inflate(R.layout.list_item_crime, parent, false)
            return CrimeHolder(view)
        }

        override fun onBindViewHolder(holder: CrimeHolder, position: Int) {
//            val crime = crimes[position]
            holder.bind(currentList[position])
        }
    }

    companion object {
        fun newInstance() = CrimeListFragment()

        val diffUtil = object : DiffUtil.ItemCallback<Crime>() {
            override fun areItemsTheSame(oldItem: Crime, newItem: Crime): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: Crime, newItem: Crime): Boolean {
                return oldItem.id == newItem.id
            }
        }
    }
}