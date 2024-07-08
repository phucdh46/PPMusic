package com.dhp.musicplayer.feature.settings.section

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.dhp.musicplayer.core.designsystem.R
import com.dhp.musicplayer.core.ui.LocalMenuState
import com.dhp.musicplayer.feature.settings.dialog.BillingPremiumDialog
import com.dhp.musicplayer.feature.settings.SettingsViewModel
import com.dhp.musicplayer.feature.settings.items.SettingTextItem
import com.dhp.musicplayer.feature.settings.items.SettingTopTitleItem

@Composable
internal fun SettingOthersSection(
    modifier: Modifier = Modifier,
    versionName: String,
    onClickBuyInAppProducts: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val productDetails by viewModel.productDetails.collectAsState()
    val menuState = LocalMenuState.current
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
            title = stringResource(R.string.setting_top_others_version),
            description = versionName,
            onClick = { /* do nothing */ },
        )
    }
}