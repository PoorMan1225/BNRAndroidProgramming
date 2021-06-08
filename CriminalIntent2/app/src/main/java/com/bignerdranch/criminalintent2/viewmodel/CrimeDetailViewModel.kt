package com.bignerdranch.criminalintent2.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.bignerdranch.criminalintent2.Crime
import com.bignerdranch.criminalintent2.CrimeRepository
import java.io.File
import java.util.*

class CrimeDetailViewModel : ViewModel() {
    private val crimeRepository = CrimeRepository.get()
    private val crimeIdLiveData = MutableLiveData<UUID>()

    var destWidth:Int = 0
    var destHeight:Int = 0

    var crimeLiveData: LiveData<Crime?> =
        Transformations.switchMap(crimeIdLiveData) { crimeId ->
            crimeRepository.getCrime(crimeId)
        }

    fun loadCrime(crimeId:UUID) {
        crimeIdLiveData.value = crimeId
    }

    fun saveCrime(crime:Crime) {
        crimeRepository.updateCrime(crime)
    }

    fun getPhotoFile(crime:Crime): File {
        return crimeRepository.getPhotoFile(crime)
    }
}