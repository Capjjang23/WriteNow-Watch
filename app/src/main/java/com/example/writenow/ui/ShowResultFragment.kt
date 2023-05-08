package com.example.writenow.ui

import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.fragment.app.setFragmentResultListener
import com.example.writenow.R
import com.example.writenow.base.BaseFragment
import com.example.writenow.databinding.FragmentShowResultBinding
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.normal.TedPermission

class ShowResultFragment:BaseFragment<FragmentShowResultBinding>(R.layout.fragment_show_result) {
    private lateinit var recordResult:String
    private lateinit var cmdResult:String
    private var text:String = ""

    override fun initDataBinding() {
        super.initDataBinding()

        // RecordFragment로 부터 데이터 받음
        setFragmentResultListener("recordResult") { _, bundle ->
            recordResult = bundle.getString("result").toString()
            binding.tvResult.text = recordResult
        }

        // CmdFragment로 부터 데이터 받음
        // Cmd 취소 눌렀을 경우 ActionTextDialog 띄우기
        setFragmentResultListener("cancelCmd") { _, bundle ->
            cmdResult = bundle.getString("result").toString()

            if (cmdResult == "do") {
                Log.d("cmdDo","실행")

                // 어디에 전화를 걸건지 text 정보 받기
                val input = getString(R.string.sos)
                val permissionListener = object : PermissionListener {
                    override fun onPermissionGranted() {
                        val myUri = Uri.parse("tel:${input}")
                        val myIntent = Intent(Intent.ACTION_CALL, myUri)
                        startActivity(myIntent)
                    }

                    override fun onPermissionDenied(deniedPermissions: MutableList<String>?) {
                        Toast.makeText(requireContext(),"[전화 연결 권한]이 거부 상태입니다.", Toast.LENGTH_SHORT).show()
                    }
                }

                TedPermission.create()
                    .setPermissionListener(permissionListener)
                    .setDeniedMessage("[전화 연결 권한]을 허용해야 전화 연결이 가능합니다.")
                    .setPermissions(android.Manifest.permission.CALL_PHONE)
                    .check()
            }
            else if (cmdResult == "cancel") {
                ActionTextDialog(recordResult).show(parentFragmentManager, "actionText")
            }
            cmdResult=""
        }
    }

    override fun initAfterBinding() {
        super.initAfterBinding()

        binding.btnAgainRecord.setOnClickListener{
            navController.navigate(R.id.action_showResultFragment_to_recordFragment)
        }

        binding.btnDoAction.setOnClickListener{
            //ActionTextDialog().show(parentFragmentManager, "actionText")
            ActionCmdDialog(recordResult).show(parentFragmentManager, "actionCmd")
        }
    }
}