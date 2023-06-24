package com.dhp.musicplayer.ui.sign_in

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import com.bumptech.glide.Glide
import com.dhp.musicplayer.MainActivity
import com.dhp.musicplayer.base.BaseActivity
import com.dhp.musicplayer.databinding.ActivityLoginBinding
import com.facebook.*
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task


class LoginActivity : BaseActivity<ActivityLoginBinding>() {

    private val loginViewModel: LoginViewModel by viewModels()
    private val callbackManager = CallbackManager.Factory.create()

    private val startGoogleSignInLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        handleSignInResult(task)
    }

    override fun getViewBinding() = ActivityLoginBinding.inflate(layoutInflater)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val account = GoogleSignIn.getLastSignedInAccount(this)
        account?.let {
            //Signed in google successfully
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        } ?: LoginManager.getInstance().retrieveLoginStatus(this, object : LoginStatusCallback {
            override fun onCompleted(accessToken: AccessToken) {
                //Signed in fb successfully
                handleSignedInSuccessfully()
            }
            override fun onError(exception: Exception) {}
            override fun onFailure() {}
        })

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()
        val mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        binding.btnSignInGoogle?.apply {
            setSize(SignInButton.SIZE_ICON_ONLY)
            setOnClickListener {
                val signInIntent: Intent = mGoogleSignInClient.signInIntent
                startGoogleSignInLauncher.launch(signInIntent)
            }
        }
        binding.btnSignInFb?.registerCallback(callbackManager, object :FacebookCallback<LoginResult> {
            override fun onSuccess(result: LoginResult) {
                handleSignedInSuccessfully()
            }
            override fun onCancel() {}
            override fun onError(error: FacebookException) {}
        })

        LoginManager.getInstance().registerCallback(callbackManager, object :FacebookCallback<LoginResult> {
            override fun onSuccess(result: LoginResult) {
                handleSignedInSuccessfully()
            }
            override fun onCancel() {}
            override fun onError(error: FacebookException) {}
        })
    }

    override fun initUI() {
        super.initUI()
        binding.imgLogo?.let { Glide.with(this).load(com.dhp.musicplayer.R.drawable.bg_gif).into(it) }

    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)
            // Signed in successfully, show authenticated UI.
            account?.let {
                handleSignedInSuccessfully()
            }
        } catch (e: ApiException) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w(TAG, "signInResult:failed code=" + e.statusCode)
        }
    }

    private fun handleSignedInSuccessfully() {
        startActivity(Intent(this@LoginActivity, MainActivity::class.java))
        finish()
    }
}
