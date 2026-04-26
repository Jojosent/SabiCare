package com.example.sabicare_j.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.sabicare_j.databinding.ActivityLoginBinding
import com.example.sabicare_j.ui.main.MainActivity
import com.example.sabicare_j.ui.onboarding.OnboardingActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Егер пайдаланушы кірген болса — тікелей MainActivity-ге
        if (auth.currentUser != null) {
            goToMain()
            return
        }

        setupTabSwitcher()
        setupLoginButton()
        setupRegisterButton()
    }

    private fun setupTabSwitcher() {
        binding.tabLogin.setOnClickListener {
            binding.tabLogin.isSelected = true
            binding.tabRegister.isSelected = false
            binding.layoutLogin.visibility = View.VISIBLE
            binding.layoutRegister.visibility = View.GONE
        }
        binding.tabRegister.setOnClickListener {
            binding.tabRegister.isSelected = true
            binding.tabLogin.isSelected = false
            binding.layoutLogin.visibility = View.GONE
            binding.layoutRegister.visibility = View.VISIBLE
        }
    }

    private fun setupLoginButton() {
        binding.btnLogin.setOnClickListener {
            val email = binding.etLoginEmail.text.toString().trim()
            val password = binding.etLoginPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Барлық өрістерді толтырыңыз", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            binding.progressBar.visibility = View.VISIBLE
            binding.btnLogin.isEnabled = false

            auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener {
                    binding.progressBar.visibility = View.GONE
                    goToMain()
                }
                .addOnFailureListener { e ->
                    binding.progressBar.visibility = View.GONE
                    binding.btnLogin.isEnabled = true
                    Toast.makeText(this, "Қате: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                }
        }
    }

    private fun setupRegisterButton() {
        binding.btnRegister.setOnClickListener {
            val name = binding.etRegisterName.text.toString().trim()
            val email = binding.etRegisterEmail.text.toString().trim()
            val password = binding.etRegisterPassword.text.toString().trim()
            val confirm = binding.etRegisterConfirm.text.toString().trim()

            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Барлық өрістерді толтырыңыз", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (password != confirm) {
                Toast.makeText(this, "Құпия сөздер сәйкес келмейді", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (password.length < 6) {
                Toast.makeText(this, "Құпия сөз кемінде 6 таңба болуы керек", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            binding.progressBar.visibility = View.VISIBLE
            binding.btnRegister.isEnabled = false

            auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener { result ->
                    val uid = result.user!!.uid
                    // Firestore-ға пайдаланушы деректерін сақтау
                    val userData = hashMapOf(
                        "name" to name,
                        "email" to email,
                        "createdAt" to System.currentTimeMillis()
                    )
                    db.collection("users").document(uid)
                        .set(userData)
                        .addOnSuccessListener {
                            binding.progressBar.visibility = View.GONE
                            // Алғаш рет кіргенде онбординг
                            startActivity(Intent(this, OnboardingActivity::class.java))
                            finish()
                        }
                }
                .addOnFailureListener { e ->
                    binding.progressBar.visibility = View.GONE
                    binding.btnRegister.isEnabled = true
                    Toast.makeText(this, "Қате: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                }
        }
    }

    private fun goToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}