package com.bignerdranch.criminalintent2

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.graphics.Bitmap
import android.graphics.Point
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.DialogFragment

private const val ARG_PATH = "picturePath"

class DetailPictureFragment : DialogFragment() {

    // DialogFragment 에서 DialogBuilder 를 통한 다이얼로그 생성.
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val path = requireArguments().getString(ARG_PATH, "")
        // 내가 원하는 layout 의 뷰를 인플레이트 해서 builder에 set해야한다.
        val view = requireActivity().layoutInflater.inflate(R.layout.fragment_detail_picture, null)
        val imageView = view.findViewById(R.id.detail_picture) as ImageView
        val bitmap = getScaleBitmap(path, imageView.width, imageView.height)
        imageView.setImageBitmap(bitmap)

        return AlertDialog.Builder(requireActivity())
            .setTitle("상세 보기")
            .setView(view)
            .create()
    }


    companion object {
        fun newInstance(path: String): DetailPictureFragment {
            val args = Bundle().apply {
                putString(ARG_PATH, path)
            }

            return DetailPictureFragment().apply {
                arguments = args
            }
        }
    }
}