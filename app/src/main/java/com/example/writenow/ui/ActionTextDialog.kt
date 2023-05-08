package com.example.writenow.ui

import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.provider.ContactsContract
import android.provider.SimPhonebookContract
import android.util.Log
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import com.example.writenow.R
import com.example.writenow.base.BaseDialogFragment
import com.example.writenow.databinding.DialogActionTextBinding
import com.example.writenow.databinding.FragmentShowResultBinding
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.normal.TedPermission

class ActionTextDialog(txt:String):BaseDialogFragment<DialogActionTextBinding>(R.layout.dialog_action_text) {
    private val text:String = txt
    private lateinit var name:String
    private lateinit var phone:String

    private var requestLauncher:ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ it ->
            if (it.resultCode == RESULT_OK) {
                // 누른 주소록 정보 받아오기
                val oneCursor = requireContext().contentResolver.query(
                    it.data!!.data!!,
                    arrayOf(
                        ContactsContract.PhoneLookup.DISPLAY_NAME
                    )
                    ,null,null,null
                )
                // 주소록 상세 정보 받아오기
                val cursor = requireContext().contentResolver.query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    arrayOf(
                        ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                        ContactsContract.CommonDataKinds.Phone.NUMBER
                    )
                    ,null, null,null
                )

                oneCursor?.use{
                    if(it.moveToFirst())
                        name = it.getString(0)
                }

                cursor?.use {
                    while (cursor!=null){
                        if (it.moveToNext()) {
                            if (name==cursor.getString(0)){
                                phone = cursor.getString(1)
                                break
                            }
                        }
                    }
                }

                Log.d("addresss", "$name, $phone")

                val p = Uri.parse("smsto: $phone")
                val intent = Intent(Intent.ACTION_SENDTO, p)
                intent.putExtra("sms_body", text)

                startActivity(intent)
            }
        }

    override fun initAfterBinding() {
        super.initAfterBinding()

        binding.tvResultText.text = text

        binding.btnEsc.setOnClickListener{
            dismiss()
        }
        binding.btnCopyText.setOnClickListener{
            val clipboardManager: ClipboardManager =
                requireContext().getSystemService(
                    Context.CLIPBOARD_SERVICE
                ) as ClipboardManager
            val clipData = ClipData.newPlainText("text", text)
            clipboardManager.setPrimaryClip(clipData)
            Toast.makeText(
                context,
                getString(R.string.copied),
                Toast.LENGTH_SHORT
            ).show()
        }
        binding.btnSendText.setOnClickListener {
            var myIntent:Intent = Intent()

            val permissionListener = object : PermissionListener {

                override fun onPermissionGranted() {
                    myIntent = Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI)
                    requestLauncher.launch(myIntent)
                }

                override fun onPermissionDenied(deniedPermissions: MutableList<String>?) {
                    Toast.makeText(requireContext(),"[주소록 읽기 권한]이 거부 상태입니다.", Toast.LENGTH_SHORT).show()
                }
            }

            TedPermission.create()
                .setPermissionListener(permissionListener)
                .setDeniedMessage("[주소록 읽기 권한]을 허용해야 메세지 보내기가 가능합니다.")
                .setPermissions(android.Manifest.permission.READ_CONTACTS)
                .check()
        }
    }

    override fun onResume() {
        super.onResume()

        // dialog full Screen code
        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog?.window?.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
    }
}