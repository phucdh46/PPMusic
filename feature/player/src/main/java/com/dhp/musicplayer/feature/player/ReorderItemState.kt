package com.dhp.musicplayer.feature.player

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.dhp.musicplayer.core.common.utils.Logg

enum class ReorderItemStateType {
    START,
    MOVE,
    END,
}

open class ReorderItemState(private val onMove: (from: Int, to: Int) -> Unit) {
    private var from = 0
    private var to = 0
    private var state = ReorderItemStateType.START
    open fun onDragMoved(fromIndex: Int, toIndex: Int) {
        if (state == ReorderItemStateType.START) {
            from = fromIndex
        }
        to = toIndex
        state = ReorderItemStateType.MOVE

    }

    open fun onDragStarted() {
        state = ReorderItemStateType.START
    }

    open fun onDragStopped() {
        state = ReorderItemStateType.END
        if (from != to) {
            Logg.d("onDragStep: $from -> $to")
            onMove(from, to)
        }
        from = 0
        to = 0
    }
}

@Composable
fun rememberReorderItemState(onMove: (from: Int, to: Int) -> Unit)
        : ReorderItemState {
    return remember {
        ReorderItemState(onMove)
    }
}