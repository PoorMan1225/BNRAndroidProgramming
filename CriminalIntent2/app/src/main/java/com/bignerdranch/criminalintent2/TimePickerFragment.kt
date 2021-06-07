package com.bignerdranch.criminalintent2

import android.app.Dialog
import android.app.TimePickerDialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import java.util.*

private const val ARG_TIME = "time"
private const val ARG_REQUEST_CODE = "requestCode"
private const val RESULT_DATE_KEY = "resultData"

class TimePickerFragment: DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val calendar = Calendar.getInstance()
        val date = requireArguments().getSerializable(ARG_TIME) as Date

        calendar.time = date
        val hourOfDay = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        val timePickerListener = TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
            val resultDate = GregorianCalendar(
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH),
                hourOfDay,
                minute
            ).time

            val result = Bundle().apply {
                putSerializable(RESULT_DATE_KEY, resultDate)
            }

            val resultRequestCode = requireArguments().getString(ARG_REQUEST_CODE, "")
            setFragmentResult(resultRequestCode, result)
        }

        return TimePickerDialog(
            requireContext(),
            timePickerListener,
            hourOfDay,
            minute,
            false)
    }

    companion object {
        fun getTimeDate(result: Bundle) = result.getSerializable(RESULT_DATE_KEY) as Date

        fun newInstance(date:Date?, requestCode:String): TimePickerFragment {
            val args = Bundle().apply {
                putSerializable(ARG_TIME, date)
                putString(ARG_REQUEST_CODE, requestCode)
            }

            return TimePickerFragment().apply {
                arguments = args
            }
        }
    }
}