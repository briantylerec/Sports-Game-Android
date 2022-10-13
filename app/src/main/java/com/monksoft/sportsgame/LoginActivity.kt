package com.monksoft.sportsgame

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.widget.doOnTextChanged
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.monksoft.sportsgame.databinding.ActivityLoginBinding
import java.text.SimpleDateFormat
import java.util.Date
import kotlin.properties.Delegates
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.firebase.auth.FacebookAuthProvider

class LoginActivity : AppCompatActivity() {

    companion object {
        lateinit var userEmail: String
        lateinit var providerSession: String
    }

    private val REQ_ONE_TAP = 100  // Can be any integer unique to the Activity
    private var showOneTapUI = true

    lateinit var binding: ActivityLoginBinding

    private var email by Delegates.notNull<String>()
    private var password by Delegates.notNull<String>()

    private lateinit var mAuth: FirebaseAuth
    private var callbackManager = CallbackManager.Factory.create()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.lyTerms.visibility = View.INVISIBLE
        binding.etRepeatPassword.visibility = View.GONE
        mAuth = FirebaseAuth.getInstance()

        binding.btnLogin.isEnabled = false

        onClickOptions()

        binding.etEmail.doOnTextChanged { _, _, _, _ -> validateData()}
        binding.etPassword.doOnTextChanged { _, _, _, _ -> validateData() }
    }

    private fun onClickOptions(){
        binding.btnLogin.setOnClickListener { loginUser() }
        binding.btnSignGoogle.setOnClickListener {
            Log.i("TEST", "Login con google")
            signGoogle() }
        binding.btnSignFacebook.setOnClickListener {
            Log.i("TEST", "Login con facebook")
            signFacebook() }
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

    private fun loginUser() {
        email = binding.etEmail.text.toString().trim()
        password = binding.etPassword.text.toString().trim()

        when {
            TextUtils.isEmpty(email) -> { Toast.makeText(this, "Enter Email ID! ", Toast.LENGTH_SHORT).show() }

            TextUtils.isEmpty(password) -> { Toast.makeText(this, "Enter Password! ", Toast.LENGTH_SHORT).show() }

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

    private fun signGoogle(){
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        var googleSignInClient = GoogleSignIn.getClient(this, gso)
        googleSignInClient.signOut()

        startActivityForResult(googleSignInClient.signInIntent, REQ_ONE_TAP)
    }

    private fun signFacebook() {
        LoginManager.getInstance().logInWithReadPermissions(this, listOf("email"))

        LoginManager.getInstance().registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
            override fun onSuccess(result: LoginResult) {
                //handleFacebookAccessToken(loginResult.accessToken)
                result.let {
                    val token = it.accessToken
                    val credential = FacebookAuthProvider.getCredential(token.token)
                    mAuth.signInWithCredential(credential).addOnCompleteListener {
                        email = it.result.user?.email.toString()
                        if(it.isSuccessful) goHome(email, "facebook")
                        else showError("facebook")
                    }
                }
            }

            override fun onCancel() {
            }

            override fun onError(error: FacebookException) {
                showError("facebook")
            }
        })
    }

    private fun showError(provider : String){
        Toast.makeText(this, "Error conecting with $provider! ", Toast.LENGTH_SHORT).show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        callbackManager.onActivityResult(resultCode, resultCode, data)

        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == REQ_ONE_TAP) {
            try {
                val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)!!

                if(account!=null){
                    email = account.email.toString()
                    val credential = GoogleAuthProvider.getCredential(account.idToken, null)
                    mAuth.signInWithCredential(credential).addOnCompleteListener{
                        if(it.isSuccessful) goHome(email, "google")
                        else showError("google")
                    }
                }
            } catch (e: ApiException) {
                showError("google")
            }
        }
    }
}