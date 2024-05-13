package com.dhp.musicplayer.ui.items

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dhp.musicplayer.ui.IconApp
import com.dhp.musicplayer.ui.component.VerticalGrid

@Composable
fun HomeGridSection(modifier: Modifier) {
    VerticalGrid {
        HomeGridItem(name = "Local Song", icon = IconApp.DownloadForOffline) {

        }
        HomeGridItem(name = "Your Playlist", icon = IconApp.PlaylistPlay) {

        }
        HomeGridItem(name = "Search Online", icon = IconApp.Search) {

        }
        HomeGridItem(name = "Settings", icon = IconApp.Settings) {

        }

    }
}
@Composable
fun HomeGridItem(
    name: String, icon: ImageVector,
    onItemClicked: () -> Unit
) {
    ElevatedCard(
    modifier = Modifier
        .clip(RoundedCornerShape(8.dp))
        .padding(4.dp)
        .clickable(onClick = onItemClicked)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(8.dp)) {
            IconButton(
                onClick = {},
                modifier = Modifier
                    .size(32.dp)

            ) {
                Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            }
            Text(
                text = name,
                style = typography.titleMedium,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }
    }
}

@Preview
@Composable
fun PreviewSpotifyHomeGridItem() {
    HomeGridItem("Search", IconApp.Search){

    }
}