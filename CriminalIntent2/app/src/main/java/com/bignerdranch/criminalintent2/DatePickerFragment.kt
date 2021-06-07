package com.bignerdranch.criminalintent2

import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import java.util.*
import java.util.Calendar.HOUR_OF_DAY
import java.util.Calendar.MINUTE

private const val ARG_DATE = "date"
private const val ARG_REQUEST_CODE = "requestCode"
private const val RESULT_DATE_KEY = "resultDate"

class DatePickerFragment : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val date = requireArguments().getSerializable(ARG_DATE) as Date
        val calendar = Calendar.getInstance()
        calendar.time = date

        // 이 날짜를 DatePickerFragment가 전달해야 한다. DatePickerDialog 의 리스너를
        // DatePickerFragment 에 추가하면, 이 리스너에서는 사용자가 선택한 날짜를
        // CrimeFragment 에 전달한다.
        val dateListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            val resultDate:Date = GregorianCalendar(year, month, dayOfMonth, calendar.get(
                HOUR_OF_DAY), calendar.get(
                MINUTE)).time

            // 결과 데이터를 번들 객체에 넣는다.
            val result = Bundle().apply {
                putSerializable(RESULT_DATE_KEY, resultDate)
            }

            // 반환 코드에 결과 번들 객체를 넣는다.
            val resultRequestCode = requireArguments().getString(ARG_REQUEST_CODE, "")
            setFragmentResult(resultRequestCode, result)
        }

        val initialYear = calendar.get(Calendar.YEAR)
        val initialMonth = calendar.get(Calendar.MONTH)
        val initialDay = calendar.get(Calendar.DAY_OF_MONTH)

        return DatePickerDialog(requireContext(), dateListener, initialYear, initialMonth, initialDay)
    }


    companion object {
        // CrimeFragment 에서 newInstance() 함수를 호출해 데이트 객체와
        // CrimeFragment 에서 String requestCode 를 보내서 날짜 설정과, 데이터를 반환할 때 사용한다.
        fun newInstance(date:Date?, requestCode:String):DatePickerFragment {
            val args = Bundle().apply {
                putSerializable(ARG_DATE, date)
                putString(ARG_REQUEST_CODE, requestCode)
            }

            return DatePickerFragment().apply {
                arguments = args
            }
        }

        // 결과를 반환할 함수를 지정.
        fun getSelectedDate(result: Bundle) = result.getSerializable(RESULT_DATE_KEY) as Date
    }
}