package com.dhp.musicplayer.feature.settings.dialog

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.dhp.musicplayer.core.datastore.IsEnablePremiumModeKey
import com.dhp.musicplayer.core.designsystem.R
import com.dhp.musicplayer.core.designsystem.icon.IconApp
import com.dhp.musicplayer.core.designsystem.theme.bold
import com.dhp.musicplayer.core.ui.common.rememberPreference

@Composable
fun BillingPremiumDialog(
    priceText: String?,
    onClickPurchase: () -> Unit,
    onClickDismiss: () -> Unit
) {

    val (isEnablePremiumMode, _) = rememberPreference(
        IsEnablePremiumModeKey,
        defaultValue = false
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {

        HeaderSection(
            modifier = Modifier
                .fillMaxWidth(),
            onClickDismiss = onClickDismiss,
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {

            TitleItem(
                modifier = Modifier
                    .padding(top = 16.dp)
                    .fillMaxWidth(),
                isEnablePremiumMode = isEnablePremiumMode,
            )

            Text(
                modifier = Modifier
                    .padding(top = 8.dp)
                    .fillMaxWidth(),
                text = stringResource(R.string.billing_premium_description),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Button(
                modifier = Modifier
                    .padding(top = 24.dp)
                    .fillMaxWidth(),
                enabled = !isEnablePremiumMode,
                onClick = { onClickPurchase.invoke() },
            ) {
//            Text(stringResource(R.string.billing_plus_purchase_button, productDetails?.rawProductDetails?.oneTimePurchaseOfferDetails?.formattedPrice ?: "￥300"))
                Text(
                    stringResource(
                        if (isEnablePremiumMode) R.string.billing_premium_purchase_button
                        else R.string.billing_premium_purchase_button,
                        priceText.orEmpty()
                    )
                )
            }
            Box(modifier = Modifier.weight(1f)) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(top = 24.dp)
                        .navigationBarsPadding(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    PlusItem(
                        modifier = Modifier.fillMaxWidth(),
                        title = R.string.billing_premium_item_hide_ads,
                        description = R.string.billing_premium_item_hide_ads_description,
                        icon = IconApp.HideSource,
                    )

                    PlusItem(
                        modifier = Modifier.fillMaxWidth(),
                        title = R.string.billing_premium_item_download,
                        description = R.string.billing_premium_item_download_description,
                        icon = IconApp.DownloadForOffline,
                    )

                    PlusItem(
                        modifier = Modifier.fillMaxWidth(),
                        title = R.string.billing_premium_item_feature,
                        description = R.string.billing_premium_item_feature_description,
                        icon = IconApp.FiberNew,
                    )
                }
            }
        }
    }
}

@Composable
internal fun HeaderSection(
    onClickDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {

    Row(
        modifier = modifier.padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(16.dp))
                .clickable { onClickDismiss.invoke() }
                .padding(4.dp),
            imageVector = Icons.Default.ExpandMore,
            contentDescription = null,
        )

        Spacer(
            modifier = Modifier
                .weight(1f)

        )
    }
}

@Composable
private fun TitleItem(
    modifier: Modifier = Modifier,
    isEnablePremiumMode: Boolean = false,
) {
    val titleStyle = MaterialTheme.typography.titleMedium.bold()
    val annotatedString = buildAnnotatedString {
        if (!isEnablePremiumMode) append("Buy ")

        withStyle(titleStyle.copy(color = MaterialTheme.colorScheme.primary).toSpanStyle()) {
            append("PPMusic Premium")
        }
    }

    Text(
        modifier = modifier,
        text = annotatedString,
        style = titleStyle,
    )
}

@Composable
private fun PlusItem(
    @StringRes title: Int,
    @StringRes description: Int,
    icon: ImageVector,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Icon(
            modifier = Modifier.size(24.dp),
            imageVector = icon,
            contentDescription = null,
        )

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(title),
                style = MaterialTheme.typography.titleMedium,
            )

            Text(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(description),
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}
