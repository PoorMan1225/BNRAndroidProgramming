package com.bignerdranch.criminalintent2

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.Room
import com.bignerdranch.criminalintent2.database.CrimeDatabase
import com.bignerdranch.criminalintent2.database.migration_1_2
import com.bignerdranch.criminalintent2.database.migration_2_3
import java.io.File
import java.util.*
import java.util.concurrent.Executors

// CrimeRepository 는 싱글톤이다.
// 즉 앱이 실행되는 동안 하나의 인스턴스만 생성된다.

private const val DATABASE_NAME = "crime-database"

class CrimeRepository private constructor(context: Context){

    private val database:CrimeDatabase = Room.databaseBuilder(
        context.applicationContext,
        CrimeDatabase::class.java,
        DATABASE_NAME
    ).addMigrations(migration_1_2, migration_2_3).build()

    private val crimeDao = database.crimeDao()

    private val executor = Executors.newSingleThreadExecutor()

    fun getCrimes(): LiveData<List<Crime>> = crimeDao.getCrimes()

    fun getCrime(id: UUID) : LiveData<Crime?> = crimeDao.getCrime(id)

    fun updateCrime(crime:Crime) {
        executor.execute {
            crimeDao.updateCrime(crime)
        }
    }

    fun addCrime(crime:Crime) {
        executor.execute {
            crimeDao.addCrime(crime)
        }
    }

    // 안드로이드의 파일 디렉터리 핸들을 반환.
    private val fileDir = context.applicationContext.filesDir

    // crime.photoFileName 이 제공하는 파일의 경로를 제공하는 함수.
    fun getPhotoFile(crime:Crime): File = File(fileDir, crime.photoFileName)

    companion object {
        private var INSTANCE: CrimeRepository? = null

        // 앱실행시 최초 초기화.
        fun initialize(context: Context) {
            if(INSTANCE == null) {
                INSTANCE = CrimeRepository(context)
            }
        }

        // 저장소 객체 가져오는 역할.
        fun get():CrimeRepository {
            return INSTANCE ?: throw IllegalStateException("CrimeRepository must be initialized")
        }
    }
}