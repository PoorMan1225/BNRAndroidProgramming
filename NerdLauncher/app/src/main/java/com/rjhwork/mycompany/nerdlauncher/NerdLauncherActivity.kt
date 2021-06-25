package com.rjhwork.mycompany.nerdlauncher

import android.content.Intent
import android.content.pm.ResolveInfo
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

private const val TAG = "NerdLauncherActivity"

class NerdLauncherActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerView = findViewById(R.id.app_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)

        setupAdapter()
    }

    private fun setupAdapter() {
        // launching 가능한 앱들의 리스트를 사용자에게 보여줘야 하기 때문에
        // 로친 가능한 메인 액티비티들을 조회해야 한다.
        val startupIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        // 액티비티의 라벨은 사용자가 알 수 있게 보여주는 이름이다. 앱이 시작될 때 제일먼저 실행되는
        // 론처 액티비티의 라벨은 앱 이름과 같다.
        val activities = packageManager.queryIntentActivities(startupIntent, 0)
        // activity 의 라벨을 알파벳 순서로 정렬.
        activities.sortWith(Comparator { a, b ->
            String.CASE_INSENSITIVE_ORDER.compare(
                a.loadLabel(packageManager).toString(),
                b.loadLabel(packageManager).toString()
            )
        })
        recyclerView.adapter = ActivityAdapter(activities)
    }

    private class ActivityAdapter(val activities:List<ResolveInfo>) : RecyclerView.Adapter<ActivityHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActivityHolder {
            return ActivityHolder(LayoutInflater.from(parent.context).inflate(R.layout.launcher_list_item, parent, false))
        }

        override fun onBindViewHolder(holder: ActivityHolder, position: Int) {
            holder.bindActivity(activities[position])
        }

        override fun getItemCount() = activities.size
    }

    private class ActivityHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        private val launcherText = itemView.findViewById<TextView>(R.id.name_text_view)
        private val launcherIcon = itemView.findViewById<ImageView>(R.id.icon_image)
        private lateinit var resolveInfo: ResolveInfo

        init {
            itemView.setOnClickListener(this)
        }

        fun bindActivity(resolveInfo: ResolveInfo) {
            this.resolveInfo = resolveInfo
            val packageManager = itemView.context.packageManager
            val appName = resolveInfo.loadLabel(packageManager).toString()
            val icon = resolveInfo.loadIcon(packageManager)
            launcherText.text = appName
            launcherIcon.setImageDrawable(icon)
        }

        override fun onClick(v: View) {
            // 브로드 캐스트 리시버로 정보를 가져오기 때문에
            // Task 에 없어도 정보를 가져올 수 있다.
            val activityInfo = resolveInfo.activityInfo

            // 이 인텐트에서는 명시적 인텐트의 일부로 액션을 전달한다. 이때 대부분의 앱이
            // 해당 액션의 포함 여부와 관계없이 동일하게 작동한다. 그러나 어떤 앱은 다르게 작동할 수 있는데.
            // 같은 액티비티 일지라도 어떻게 시작되었는가에 따라 화면 UI 화면을 다르게 보여줄 수 있기
            // 때문이다. 따라서 프로그래머 입장에서는 액션을 전달해서 자신의 의도를 명확하게 알려주는 것이
            // 가장 좋다.
            val intent = Intent(Intent.ACTION_MAIN).apply {
                // 해당 액티비티의 패키지 이름, 해당 액티비티 이름.
                setClassName(activityInfo.applicationInfo.packageName, activityInfo.name)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            // 내부클래스가 아니라 다른 클래스 이기 때문에
            // 해당 View 에서 context 를 가져와야 한다.
            val context = v.context
            context.startActivity(intent)
        }
    }
}