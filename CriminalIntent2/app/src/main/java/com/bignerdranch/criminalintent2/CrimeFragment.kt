package com.bignerdranch.criminalintent2

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.ContactsContract
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.text.format.DateFormat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentResultListener
import androidx.fragment.app.viewModels
import com.bignerdranch.criminalintent2.viewmodel.CrimeDetailViewModel
import java.io.File
import java.util.*

private const val ARG_CRIME_ID = "crime_id"

// choose date
private const val DIALOG_DATE = "DialogDate"

// choose time
private const val DIALOG_TIME = "DialogTime"

private const val TAG = "CrimeFragment"
private const val DATE_FORMAT = "yyyy년 M월 d일 H시 m분, E요일"

class CrimeFragment : Fragment(), FragmentResultListener {

    private lateinit var crime: Crime
    private lateinit var photoFile: File
    private lateinit var titleField: EditText
    private lateinit var dateButton: Button
    private lateinit var timeButton: Button
    private lateinit var reportButton: Button
    private lateinit var suspectButton: Button
    private lateinit var solvedCheckBox: CheckBox
    private lateinit var callButton: Button
    private lateinit var photoButton:ImageButton
    private lateinit var photoView: ImageView
    private lateinit var photoUri: Uri

    private val crimeDetailViewModel by viewModels<CrimeDetailViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        crime = Crime()
        val crimeId: UUID = arguments?.getSerializable(ARG_CRIME_ID) as UUID
        crimeDetailViewModel.loadCrime(crimeId)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_crime, container, false)
        titleField = view.findViewById(R.id.crime_title) as EditText
        dateButton = view.findViewById(R.id.crime_date) as Button
        solvedCheckBox = view.findViewById(R.id.crime_solved) as CheckBox
        timeButton = view.findViewById(R.id.choose_time) as Button
        reportButton = view.findViewById(R.id.crime_report) as Button
        suspectButton = view.findViewById(R.id.crime_suspect) as Button
        callButton = view.findViewById(R.id.crime_call) as Button
        photoButton = view.findViewById(R.id.crime_camera) as ImageButton
        photoView = view.findViewById(R.id.crime_photo) as ImageView
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // 옵저버에서 데이터 변경을 하게 되므로 써 파일 경로가 자꾸 변하게 된다.
        // 왜냐하면 crime id 가 파일 경로에 포함되기 때문에 고유한 id 의 파일 경로를 갖게 된다.
        crimeDetailViewModel.crimeLiveData.observe(viewLifecycleOwner, { crime ->
            crime?.let {
                this.crime = crime
                // 해당 파일의 경로를
                photoFile = crimeDetailViewModel.getPhotoFile(crime)
                // FileProvider 가 읽을 수 있게 Uri 로 노출.
                photoUri = FileProvider.getUriForFile(requireActivity(), "com.bignerdranch.criminalintent2.fileprovider", photoFile)
                updateUI()
            }
        })
        childFragmentManager.apply {
            setFragmentResultListener(DIALOG_DATE, viewLifecycleOwner, this@CrimeFragment)
            setFragmentResultListener(DIALOG_TIME, viewLifecycleOwner, this@CrimeFragment)
        }
    }

    private fun updateUI() {
        titleField.setText(crime.title)
        dateButton.text = crime.date.toString()
        solvedCheckBox.apply {
            isChecked = crime.isSolved
            jumpDrawablesToCurrentState()
        }

        if (crime.suspect.isNotEmpty()) {
            suspectButton.text = crime.suspect
        }

        if (crime.number.isNotEmpty()) {
            callButton.text = crime.number
        }
        updatePhotoView()
    }

    private fun getCrimeReport(): String {
        val solvedString = if (crime.isSolved) {
            getString(R.string.crime_report_solved)
        } else {
            getString(R.string.crime_report_unsolved)
        }

        val dateString = DateFormat.format(DATE_FORMAT, crime.date).toString()
        val suspect = if (crime.suspect.isBlank()) {
            getString(R.string.crime_report_no_suspect)
        } else {
            getString(R.string.crime_report_suspect, crime.suspect)
        }
        return getString(R.string.crime_report, crime.title, dateString, solvedString, suspect)
    }

    override fun onStart() {
        super.onStart()

        val titleWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                crime.title = s.toString()
                Log.d(TAG, "${s.toString()} : onTextChanged")
            }

            override fun afterTextChanged(s: Editable?) {}
        }
        titleField.addTextChangedListener(titleWatcher)

        solvedCheckBox.apply {
            setOnCheckedChangeListener { _, isChecked ->
                crime.isSolved = isChecked
            }
        }
        dateButton.setOnClickListener {
            DatePickerFragment.newInstance(crime.date, DIALOG_DATE).apply {
                show(this@CrimeFragment.childFragmentManager, DIALOG_DATE)
            }
        }
        timeButton.setOnClickListener {
            TimePickerFragment.newInstance(crime.date, DIALOG_TIME).apply {
                show(this@CrimeFragment.childFragmentManager, DIALOG_TIME)
            }
        }
        reportButton.setOnClickListener {
            Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, getCrimeReport())
                putExtra(Intent.EXTRA_SUBJECT, getString(R.string.crime_report_subject))
            }.also { intent ->
                val chooserIntent = Intent.createChooser(intent, getString(R.string.send_report))
                startActivity(chooserIntent)
            }
        }
        suspectButton.apply {
            val pickContactIntent =
                Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI)

            val packageManager = requireActivity().packageManager

            // 데이터 하나 가지고 와서 연락처 앱이 있는지 확인.
            val resolvedActivity: ResolveInfo? =
                packageManager.resolveActivity(pickContactIntent, PackageManager.MATCH_DEFAULT_ONLY)

            // 없다면 버튼 비활성화.
            if (resolvedActivity == null) {
                isEnabled = false
            }

            // 있다면 인텐트 실행. 그전에 퍼미션 체크.
            setOnClickListener {

                when {
                    ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.READ_CONTACTS)
                            == PackageManager.PERMISSION_GRANTED -> {
                        requestPhoneData.launch(pickContactIntent)
                    }
                    ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.READ_CONTACTS)
                            == PackageManager.PERMISSION_DENIED -> {
                        showPermissionContextPopup()
                    }

                    else -> requestPermission.launch(android.Manifest.permission.READ_CONTACTS)
                }
            }
        }

        callButton.setOnClickListener {
            val number = Uri.parse("tel:${crime.number}")
            startActivity(Intent(Intent.ACTION_DIAL, number))
        }

        photoButton.apply {
            // 카메라 인텐트를 실행하기 위한 ACTION
            val captureImage = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            val packageManager = requireActivity().packageManager

            // 한개 데이터만 가지고 와서 있는지 체크.
            val resolvedActivity: ResolveInfo? =
                packageManager.resolveActivity(captureImage, PackageManager.MATCH_DEFAULT_ONLY)
            if(resolvedActivity == null) {
                isEnabled = false
            }

            setOnClickListener {
                captureImage.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)

                // 가져온 암시적 인텐트 액티비들
                val cameraActivities:List<ResolveInfo> = packageManager.queryIntentActivities(captureImage, PackageManager.MATCH_DEFAULT_ONLY)

                // 해당 액티비티 들에게 photoUri 에 사진 파일을 쓸 수 있도록
                // permission 을 추가한다. (매니 패스트에서 grantUriPermissions속성을 추가 했으므로
                // 퍼미션을 부여할 수 있다.)
                for(cameraActivity in cameraActivities) {
                    requireActivity().grantUriPermission(
                        cameraActivity.activityInfo.packageName,
                        photoUri,
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                }
                // Intent 실행.
                requestCamera.launch(captureImage)
            }
        }
    }

    // ImageView 에 Bitmap 을 로드하기 위해 CrimeFragment 에 새로운 함수를 추가하고
    // photoView 를 변경한다.
    private fun updatePhotoView() {
        if(photoFile.exists()) {
            val bitmap = getScaledBitmap(photoFile.path, requireActivity())
            photoView.setImageBitmap(bitmap)
        }else {
            photoView.setImageBitmap(null)
        }
    }

    private fun showPermissionContextPopup() {
        AlertDialog.Builder(requireContext())
            .setTitle("권한이 필요합니다.")
            .setMessage("연락처에 접근하기 위한 권한이 필요합니다.")
            .setPositiveButton("동의하기") { _, _ ->
                requestPermission.launch(android.Manifest.permission.READ_CONTACTS)
            }
            .setNegativeButton("취소하기") { _, _ -> }
            .show()
    }

    private val requestPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { result ->
        if (result) {
            val pickContactIntent =
                Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI)
            requestPhoneData.launch(pickContactIntent)
        } else {
            Toast.makeText(requireContext(), "권한을 거부 하셨습니다.", Toast.LENGTH_SHORT).show()
        }
    }

    private val requestCamera = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if(result.resultCode == Activity.RESULT_OK) {
            // 이제 사진을 업데이트 할 수 있으니 퍼미션 취소.
            requireActivity().revokeUriPermission(photoUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            updatePhotoView() // 사진을 찍었을 경우 photoUpdate
        }
    }

    private val requestPhoneData =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {

                val contactUri: Uri = result.data?.data ?: return@registerForActivityResult
                //쿼리에서 값으로 반환할 필드를 지정한다.
                val queryFields = arrayOf(
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                    ContactsContract.CommonDataKinds.Phone.NUMBER
                )

                // 쿼리를 수행한다. contactUri는 콘텐츠 제공자의 테이블을 나타낸다.
                val cursor = requireActivity().contentResolver
                    .query(contactUri, queryFields, null, null, null)
                cursor?.use {
                    // 쿼리 결과 데이터가 있는지 확인한다.
                    if (it.count == 0) {
                        return@registerForActivityResult
                    }
                    // 첫 번째 데이터 행의 첫 번째 열의 값을 가져온다.
                    it.moveToFirst()
                    val suspect = it.getString(0)
                    val number = it.getString(1)
                    crime.apply {
                        this.suspect = suspect
                        this.number = number
                    }
                    crimeDetailViewModel.saveCrime(crime)
                    suspectButton.text = suspect
                    callButton.text = number
                }
            }
        }

    override fun onStop() {
        super.onStop()
        crimeDetailViewModel.saveCrime(crime)
    }

    // 부적합한 응답에 대비한 퍼미션 취소.
    override fun onDetach() {
        super.onDetach()
        requireActivity().revokeUriPermission(photoUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
    }

    companion object {
        fun newInstance(crimeId: UUID): CrimeFragment {
            val args = Bundle().apply {
                putSerializable(ARG_CRIME_ID, crimeId)
            }
            return CrimeFragment().apply {
                arguments = args
            }
        }
    }

    override fun onFragmentResult(requestKey: String, result: Bundle) {
        when (requestKey) {
            DIALOG_DATE -> {
                crime.date = DatePickerFragment.getSelectedDate(result)
                updateUI()
            }
            DIALOG_TIME -> {
                crime.date = TimePickerFragment.getTimeDate(result)
                updateUI()
            }
        }
    }
}