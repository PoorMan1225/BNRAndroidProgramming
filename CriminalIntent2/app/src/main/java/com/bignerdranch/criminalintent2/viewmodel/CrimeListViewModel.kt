package com.bignerdranch.criminalintent2.viewmodel

import androidx.lifecycle.ViewModel
import com.bignerdranch.criminalintent2.Crime
import com.bignerdranch.criminalintent2.CrimeRepository

class CrimeListViewModel : ViewModel() {
    private val crimeRepository: CrimeRepository = CrimeRepository.get()
    val crimeListLiveData = crimeRepository.getCrimes()

    fun addCrime(crime:Crime) {
        crimeRepository.addCrime(crime)
    }
}