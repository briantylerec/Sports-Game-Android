package com.monksoft.sportsgame

import android.content.Context
import android.content.Intent
import android.opengl.Visibility
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import com.google.api.Distribution.BucketOptions.Linear
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firestore.v1.FirestoreGrpc.FirestoreBlockingStub
import com.monksoft.sportsgame.databinding.ActivityLoginBinding
import java.text.SimpleDateFormat
import java.util.Date
import kotlin.properties.Delegates

class LoginActivity : AppCompatActivity() {

    companion object {
        lateinit var userEmail: String
        lateinit var providerSession: String
    }

    lateinit var binding: ActivityLoginBinding

    private var email by Delegates.notNull<String>()
    private var password by Delegates.notNull<String>()

    private lateinit var mAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.lyTerms.visibility = View.INVISIBLE
        binding.etRepeatPassword.visibility = View.INVISIBLE
        mAuth = FirebaseAuth.getInstance()

        binding.btnLogin.isEnabled = false
        binding.btnLogin.setOnClickListener { loginUser() }
        binding.etEmail.doOnTextChanged { _, _, _, _ -> validateData()}
        binding.etPassword.doOnTextChanged { _, _, _, _ -> validateData() }
    }

    private fun validateData() {
        if(Utils.isEmail(binding.etEmail.text.toString()) && !TextUtils.isEmpty(binding.etPassword.text)){
            binding.btnLogin.isEnabled = true
            binding.btnLogin.setBackgroundResource(R.drawable.rounded_button_login)
        } else {
            binding.btnLogin.isEnabled = false
            binding.btnLogin.setBackgroundColor(ContextCompat.getColor(this, R.color.gray_light))
        }
    }

    override fun onStart() {
        super.onStart()
        val currentUser = FirebaseAuth.getInstance().currentUser

        if (currentUser != null) goHome(
            currentUser.email.toString(),
            currentUser.providerId.toString()
        )
    }

    override fun onBackPressed() {
        val startMain = Intent(Intent.ACTION_MAIN)
        startMain.addCategory(Intent.CATEGORY_HOME)
        startMain.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(startMain)
    }

    fun loginUser() {
        email = binding.etEmail.text.toString().trim()
        password = binding.etPassword.text.toString().trim()

        when {
            TextUtils.isEmpty(email) -> {
                Toast.makeText(this, "Enter Email ID! ", Toast.LENGTH_SHORT).show()
            }

            TextUtils.isEmpty(password) -> {
                Toast.makeText(this, "Enter Password! ", Toast.LENGTH_SHORT).show()
            }

            else -> {
                mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) {
                        if (it.isSuccessful) goHome(email, "email")
                        else {
                            binding.lyTerms.visibility = View.VISIBLE
                            binding.etRepeatPassword.visibility = View.VISIBLE
                            binding.btnLogin.text = getString(R.string.register)

                            Log.e("Firebase Auth", "Sign-in failed", it.exception);
                            if (binding.cbAccept.isChecked ) {
                                if (binding.etPassword.text.toString() == binding.etRepeatPassword.text.toString()) registrar()
                                else Toast.makeText(this, "Password doen't match", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(this, "Please accept terms and conditions", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
            }
        }
    }

    private fun goHome(email: String, provider: String) {

        userEmail = email
        providerSession = provider

        var intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

    private fun registrar() {
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(
            binding.etEmail.text.toString(),
            binding.etPassword.text.toString()
        )
            .addOnCompleteListener {
                if (it.isSuccessful) {

                    var dateRegister = SimpleDateFormat("dd/MM/yyyy").format(Date())
                    var dbRegister = FirebaseFirestore.getInstance()
                    dbRegister.collection("users").document(email).set(
                        hashMapOf(
                            "user" to binding.etEmail.text.toString(),
                            "dateRegister" to dateRegister
                        )
                    )

                    goHome(binding.etEmail.text.toString(), "email")

                } else Toast.makeText(this, "Error, something goes wrong!", Toast.LENGTH_LONG)
                    .show()
            }
    }

    fun goTerms(v: View) {
        val intent = Intent(this, TermsActivity::class.java)
        startActivity(intent)
    }

    fun forgotPassword(v: View) {
//        val intent = Intent(this, ForgotPasswordActivity::class.java)
//        startActivity(intent)
        resetPassword()
    }

    private fun resetPassword() {
        when {
            TextUtils.isEmpty(binding.etEmail.text.toString()) -> {
                Toast.makeText(this, "Enter a valid Email ID! ", Toast.LENGTH_SHORT).show()
            }

            else -> {
                if (!TextUtils.isEmpty(binding.etEmail.text.toString())) {
                    mAuth.sendPasswordResetEmail(binding.etEmail.text.toString())
                        .addOnCompleteListener {
                            if (it.isSuccessful) {
                                Toast.makeText(
                                    this,
                                    "Email sent to ${binding.etEmail.text.toString()} ",
                                    Toast.LENGTH_LONG
                                ).show()
                                val intent = Intent(this, LoginActivity::class.java)
                                startActivity(intent)
                            } else Toast.makeText(
                                this,
                                "${binding.etEmail.text.toString()} doesn't exist!",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                }
            }
        }
    }
}