package com.dhp.musicplayer.feature.settings.section

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.dhp.musicplayer.core.common.extensions.findActivity
import com.dhp.musicplayer.core.common.extensions.toast
import com.dhp.musicplayer.core.designsystem.R
import com.dhp.musicplayer.core.ui.LocalMenuState
import com.dhp.musicplayer.feature.settings.SettingsViewModel
import com.dhp.musicplayer.feature.settings.dialog.BillingPremiumDialog
import com.dhp.musicplayer.feature.settings.items.SettingTextItem
import com.dhp.musicplayer.feature.settings.items.SettingTopTitleItem
import com.google.android.play.core.review.ReviewManagerFactory

@Composable
internal fun SettingOthersSection(
    modifier: Modifier = Modifier,
    versionName: String,
    onClickBuyInAppProducts: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
    onNavigationToFeedback: () -> Unit
) {
    val productDetails by viewModel.productDetails.collectAsState()
    val menuState = LocalMenuState.current
    val context = LocalContext.current
    val manager = ReviewManagerFactory.create(context)
    val request = manager.requestReviewFlow()

    Column(modifier) {
        SettingTopTitleItem(
            modifier = Modifier.fillMaxWidth(),
            text = R.string.setting_top_others,
        )

        SettingTextItem(
            modifier = Modifier.fillMaxWidth(),
            title = stringResource(R.string.setting_top_others_premium_mode_title),
            description = stringResource(R.string.setting_top_others_premium_mode_description),
            onClick = {
                menuState.show {
                    BillingPremiumDialog(
                        priceText = productDetails?.oneTimePurchaseOfferDetails?.formattedPrice,
                        onClickPurchase = onClickBuyInAppProducts,
                        onClickDismiss = menuState::dismiss
                    )
                }
            },
        )

        SettingTextItem(
            modifier = Modifier.fillMaxWidth(),
            title = stringResource(R.string.setting_top_others_review_title),
            description = stringResource(R.string.setting_top_others_review_description),
            onClick = {
                request.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // We got the ReviewInfo object
                        val reviewInfo = task.result
                        val activity = context.findActivity() ?: return@addOnCompleteListener
                        val flow = manager.launchReviewFlow(activity, reviewInfo)
                        flow.addOnCompleteListener { _ ->
                            // The flow has finished. The API does not indicate whether the user
                            // reviewed or not, or even whether the review dialog was shown. Thus, no
                            // matter the result, we continue our app flow.
                            context.toast("Thank you for review")
                        }
                    } else {
                        // There was some problem, log or handle the error code.
//                        @ReviewErrorCode val reviewErrorCode = (task.getException() as ReviewException).errorCode
                    }
                }
            },
        )

        SettingTextItem(
            modifier = Modifier.fillMaxWidth(),
            title = stringResource(R.string.setting_top_others_feedback_title),
            description = stringResource(R.string.setting_top_others_feedback_description),
            onClick = {
                onNavigationToFeedback()
            },
        )

        SettingTextItem(
            modifier = Modifier.fillMaxWidth(),
            title = stringResource(R.string.setting_top_others_version),
            description = versionName,
            onClick = { /* do nothing */ },
        )
    }
}