package com.dhp.musicplayer.ui.user_profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.dhp.musicplayer.databinding.FragmentUserProfileBinding
import com.dhp.musicplayer.ui.sign_in.LoginActivity
import com.facebook.AccessToken
import com.facebook.GraphRequest
import com.facebook.GraphResponse
import com.facebook.login.LoginManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import org.json.JSONObject

class UserProfileFragment : Fragment() {
    private val userProfileViewModel: UserProfileViewModel by viewModels()
    private var _binding: FragmentUserProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUserProfileBinding.inflate(inflater, container, false)

        val account = GoogleSignIn.getLastSignedInAccount(requireContext())
        if (account != null) {
            binding.name.text = account.displayName
            Glide.with(requireContext()).load(account.photoUrl).into(binding.imgAvatar)
        } else {
            val accessToken = AccessToken.getCurrentAccessToken()
            val request = GraphRequest.newMeRequest(accessToken
            ) { obj, _ ->
                val name = obj?.getString("name")
                binding.name.text = name
                val url = obj?.getJSONObject("picture")?.getJSONObject("data")?.getString("url")
                Glide.with(requireContext()).load(url).into(binding.imgAvatar)
            }
            val bundle = Bundle()
            bundle.putString("fields","id,name,link,picture")
            request.parameters = bundle
            request.executeAsync()
        }

        binding.btnLogout.setOnClickListener {
            if (account != null) {
                val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestEmail()
                    .build()
                val mGoogleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)
                mGoogleSignInClient.signOut().addOnCompleteListener {
                    if (it.isComplete) {
                        startActivity(Intent(requireContext(), LoginActivity::class.java))
                        backToLoginScreen()
                    }
                }
            } else {
                LoginManager.getInstance().logOut()
                backToLoginScreen()
            }
        }
        return binding.root
    }

    private fun backToLoginScreen() {
        startActivity(Intent(requireContext(), LoginActivity::class.java))
        requireActivity().finish()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}