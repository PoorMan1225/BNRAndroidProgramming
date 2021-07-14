package com.rjhwork.mycompany.photogallery

import android.app.PendingIntent
import android.content.Context
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.Worker
import androidx.work.WorkerParameters

private const val TAG = "PollWorker"

// WorkManager 가 요청할 작업.
class PollWorker(val context: Context, workerParams:WorkerParameters): Worker(context, workerParams) {

    override fun doWork(): Result {
        val query = QueryPreferences.getStoredQuery(context)
        val lastResultId = QueryPreferences.getLastResultId(context)

        // Call.execute() 를 호출하면 요청이 실행되어 곧바로 Response 객체가 반환된다.
        // 그런데 Call.enqueue(...) 는 요청이 비동기로 실행되지만, Call.execute() 는 요청이
        // 동기적으로 실행된다.
        val items: List<GalleryItem> = if(query.isEmpty()) {
            FlickrFetchr().fetchPhotosRequest()
                .execute()
                .body()
                ?.photos
                ?.galleryItems
        }else {
            FlickrFetchr().searchPhotosRequest(query)
                .execute()
                .body()
                ?.photos
                ?.galleryItems
        } ?: emptyList()

        if(items.isEmpty()) {
            return Result.success()
        }

        // 새로운 사진이 있는지 확인.
        val resultId = items.first().id

        if(resultId == lastResultId) {
            Log.i(TAG, "Got an old result : $resultId")
        }else {
            Log.i(TAG, "Got a new result : $resultId")
            QueryPreferences.setLastResultId(context, resultId)

            val intent = PhotoGalleryActivity.newIntent(context)
            val pendingIntent = PendingIntent.getActivity(context, 0, intent, 0)

            val resources = context.resources

            // Notification 객체 생성.
            val notification = NotificationCompat
                .Builder(context, NOTIFICATION_CHANNEL_ID)
                .setTicker(resources.getString(R.string.pictures_title)) // 시각장애인 을 위한. 읽어주기.
                .setSmallIcon(android.R.drawable.ic_menu_report_image)
                .setContentTitle(resources.getString(R.string.pictures_title))
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build()

            val notificationManager = NotificationManagerCompat.from(context)
            notificationManager.notify(0, notification)
        }

        return Result.success()
    }
}