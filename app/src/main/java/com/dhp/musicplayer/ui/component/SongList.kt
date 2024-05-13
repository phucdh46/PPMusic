package com.dhp.musicplayer.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.dhp.musicplayer.R
import com.dhp.musicplayer.model.Song

@Composable
fun SongList(
    exploreList: List<Song>,
    onItemClicked: (Song) -> Unit,
    modifier: Modifier = Modifier,
    listState: LazyListState = rememberLazyListState()
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = WindowInsets.navigationBars.asPaddingValues(),
        state = listState
    ) {
//        items(exploreList, key = {it.id}) { exploreItem ->
        items(exploreList) { exploreItem ->
            Column(Modifier.fillParentMaxWidth()) {
                SongListItem(
                    modifier = Modifier.fillParentMaxWidth(),
                    song = exploreItem,
                    onItemClicked = onItemClicked
                )
                HorizontalDivider()
            }
        }
    }
}

@Composable
private fun SongListItem(
    modifier: Modifier = Modifier,
    song: Song,
    onItemClicked: (Song) -> Unit,
) {
    Row(
        modifier = modifier
            .clickable { onItemClicked(song) }
            .padding(top = 8.dp, bottom = 8.dp)
    ) {
        SongImageContainer {
            Box {
                val painter = rememberAsyncImagePainter(
                    ImageRequest.Builder(LocalContext.current).data(data = song.thumbnailUrl)
                        .apply(block = fun ImageRequest.Builder.() {
                            crossfade(true)
                        }).build()
                )
                Image(
                    painter = painter,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )

                if (painter.state is AsyncImagePainter.State.Error) {
                    Image(
                        painter = painterResource(id = R.drawable.logo),
                        contentDescription = null,
                        modifier = Modifier
                            .size(36.dp)
                            .align(Alignment.Center),
                    )
                }
            }
        }
        Spacer(Modifier.width(16.dp))
        Column(Modifier.padding(4.dp)) {
            Text(
                text = song.title.orEmpty(),
                style = MaterialTheme.typography.titleMedium,
                overflow = TextOverflow.Visible
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = song.artistsText.orEmpty(),
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}
@Composable
private fun SongImageContainer(content: @Composable () -> Unit) {
    Surface(Modifier.size(width = 60.dp, height = 60.dp), RoundedCornerShape(4.dp)) {
        content()
    }
}