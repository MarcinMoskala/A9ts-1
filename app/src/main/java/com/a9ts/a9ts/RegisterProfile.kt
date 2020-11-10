package com.a9ts.a9ts

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.a9ts.a9ts.databinding.ActivityRegisterProfileBinding

class RegisterProfile : AppCompatActivity() {
    lateinit var binding : ActivityRegisterProfileBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)


    }
}