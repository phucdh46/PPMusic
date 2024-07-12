//package com.dhp.musicplayer.core.ui
//
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.MutableState
//import androidx.compose.ui.platform.LocalContext
//
//@Composable
//fun <T> rememberPreference(
//    key: Preferences.Key<T>,
//    defaultValue: T,
//): MutableState<T> {
//    val context = LocalContext.current
//    val coroutineScope = rememberCoroutineScope()
//
//    val state = remember {
//        context.dataStore.data
//            .map { it[key] ?: defaultValue }
//            .distinctUntilChanged()
//    }.collectAsState(context.dataStore[key] ?: defaultValue)
//
//    return remember {
//        object : MutableState<T> {
//            override var value: T
//                get() = state.value
//                set(value) {
//                    coroutineScope.launch {
//                        context.dataStore.edit {
//                            it[key] = value
//                        }
//                    }
//                }
//
//            override fun component1() = value
//            override fun component2(): (T) -> Unit = { value = it }
//        }
//    }
//}
//
//@Composable
//inline fun <reified T : Enum<T>> rememberEnumPreference(
//    key: Preferences.Key<String>,
//    defaultValue: T,
//): MutableState<T> {
//    val context = LocalContext.current
//    val coroutineScope = rememberCoroutineScope()
//
//    val initialValue = context.dataStore[key].toEnum(defaultValue = defaultValue)
//    val state = remember {
//        context.dataStore.data
//            .map { it[key].toEnum(defaultValue = defaultValue) }
//            .distinctUntilChanged()
//    }.collectAsState(initialValue)
//
//    return remember {
//        object : MutableState<T> {
//            override var value: T
//                get() = state.value
//                set(value) {
//                    coroutineScope.launch {
//                        context.dataStore.edit {
//                            it[key] = value.name
//                        }
//                    }
//                }
//
//            override fun component1() = value
//            override fun component2(): (T) -> Unit = { value = it }
//        }
//    }
//}