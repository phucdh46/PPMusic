package com.dhp.musicplayer.utils

import android.app.Activity
import android.content.IntentSender
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.dhp.musicplayer.constant.LastTimeUserCancelFlexibleUpdateKey
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date

class InAppUpdateManager(private var activity: ComponentActivity) : DefaultLifecycleObserver {
    private var appUpdateManager: AppUpdateManager = AppUpdateManagerFactory.create(activity)

    private var onShowSnackBar: ((onActionClick: () -> Unit) -> Unit)? = null
    private val scope = CoroutineScope(
        SupervisorJob() + Dispatchers.IO
    )

    companion object {
        var isAppInFlexibleUpdating = false
    }

    private val installStateUpdatedListener = InstallStateUpdatedListener { state ->
        when (state.installStatus()) {
            InstallStatus.DOWNLOADED -> {
                popupSnackBarForCompleteUpdate()
                unregisterListener()
            }
        }
    }

    init {
        activity.lifecycle.addObserver(this)
        checkAndRegisterInstallStateUpdateListener()
    }

    fun checkForUpdate(onShowSnackBar: (onActionClick: () -> Unit) -> Unit) {
        this.onShowSnackBar = onShowSnackBar
        appUpdateManager.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
            val inAppUpdatePriority = appUpdateInfo.updatePriority()
            when (appUpdateInfo.updateAvailability()) {
                UpdateAvailability.UPDATE_AVAILABLE -> {
                    when {
                        inAppUpdatePriority >= 4 -> {
                            if (appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {
                                startAppUpdateImmediate(appUpdateInfo)
                            }
                        }

                        inAppUpdatePriority == 3 -> {
                            if (appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)) {
                                startAppUpdateFlexible(appUpdateInfo)
                            }
                        }

                        else -> {
//                        inAppUpdatePriority > 0 -> {
                            if (appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE) && isNeedShowFlexibleUpdateInLowPriority()) {
                                startAppUpdateFlexible(appUpdateInfo)
                            }
                        }
                    }
                }

                UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS -> {
                    startAppUpdateImmediate(appUpdateInfo)
                }
            }
        }
    }

    private fun isNeedShowFlexibleUpdateInLowPriority(): Boolean {
        val lastTimeUserCancelFlexibleUpdate =
            activity.dataStore.get(LastTimeUserCancelFlexibleUpdateKey, 0L)
        if (lastTimeUserCancelFlexibleUpdate != 0L) {
            val lastDateUserCancelFlexibleUpdate = Calendar.getInstance()
            lastDateUserCancelFlexibleUpdate.time = Date(lastTimeUserCancelFlexibleUpdate)

            val currentDate = Calendar.getInstance()
            currentDate.time = Calendar.getInstance().time

            return (
                    currentDate.get(Calendar.DATE) != lastDateUserCancelFlexibleUpdate.get(Calendar.DATE)
                            || currentDate.get(Calendar.MONTH) != lastDateUserCancelFlexibleUpdate.get(
                        Calendar.MONTH
                    )
                            || currentDate.get(Calendar.YEAR) != lastDateUserCancelFlexibleUpdate.get(
                        Calendar.YEAR
                    )
                    )
        }
        return true
    }

    private fun onUserChooseUpdateInFlexibleUpdatePopup() {
        isAppInFlexibleUpdating = true
        appUpdateManager.registerListener(installStateUpdatedListener)
    }

    private fun onUserChooseDoNotUpdate() {
        isAppInFlexibleUpdating = false
        scope.launch {
            activity.dataStore.edit { dataStore ->
                dataStore[LastTimeUserCancelFlexibleUpdateKey] = Calendar.getInstance().time.time
            }
        }
    }

    private val startImmediateUpdateAppLauncher = activity.registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_CANCELED) {
            activity.moveTaskToBack(true)
            activity.finish()
        }
    }

    private val startFlexibleUpdateAppLauncher = activity.registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        when (result.resultCode) {
            Activity.RESULT_OK -> {
                onUserChooseUpdateInFlexibleUpdatePopup()
            }

            Activity.RESULT_CANCELED -> {
                onUserChooseDoNotUpdate()
            }
        }
    }

    private fun startAppUpdateImmediate(appUpdateInfo: AppUpdateInfo) {
        try {
            isAppInFlexibleUpdating = false
            appUpdateManager.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
                if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                    // This example applies an immediate update. To apply a flexible update
                    // instead, pass in AppUpdateType.FLEXIBLE
                    && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)
                ) {
                    // Request the update.
                    appUpdateManager.startUpdateFlowForResult(
                        // Pass the intent that is returned by 'getAppUpdateInfo()'.
                        appUpdateInfo,
                        // an activity result launcher registered via registerForActivityResult
                        startImmediateUpdateAppLauncher,
                        // Or pass 'AppUpdateType.FLEXIBLE' to newBuilder() for
                        // flexible updates.
                        AppUpdateOptions.newBuilder(AppUpdateType.IMMEDIATE).build()
                    )
                }

            }
        } catch (e: IntentSender.SendIntentException) {
        }
    }

    private fun startAppUpdateFlexible(appUpdateInfo: AppUpdateInfo) {
        try {
            appUpdateManager.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
                if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                    // This example applies an immediate update. To apply a flexible update
                    // instead, pass in AppUpdateType.FLEXIBLE
                    && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)
                ) {
                    // Request the update.
                    appUpdateManager.startUpdateFlowForResult(
                        // Pass the intent that is returned by 'getAppUpdateInfo()'.
                        appUpdateInfo,
                        // an activity result launcher registered via registerForActivityResult
                        startFlexibleUpdateAppLauncher,
                        // Or pass 'AppUpdateType.FLEXIBLE' to newBuilder() for
                        // flexible updates.
                        AppUpdateOptions.newBuilder(AppUpdateType.FLEXIBLE).build()
                    )
                }
            }
            checkAndRegisterInstallStateUpdateListener()
        } catch (e: IntentSender.SendIntentException) {
        }
    }

    private fun checkNewAppVersionState() {
        appUpdateManager.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED) {
                popupSnackBarForCompleteUpdate()
            } else {
                if ((appUpdateInfo.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS)
                    && (!isAppInFlexibleUpdating)
                ) {
                    startAppUpdateImmediate(appUpdateInfo)
                }
            }
        }
    }

    private fun popupSnackBarForCompleteUpdate() {
        onShowSnackBar?.invoke {
            appUpdateManager.completeUpdate()
        }
    }

    private fun checkAndRegisterInstallStateUpdateListener() {
        appUpdateManager.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
            if ((isAppInFlexibleUpdating)
                && (appUpdateInfo.installStatus() != InstallStatus.DOWNLOADED)
                && (appUpdateInfo.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS)
            ) {
                appUpdateManager.registerListener(installStateUpdatedListener)
            }
        }
    }

    private fun unregisterListener() {
        appUpdateManager.unregisterListener(installStateUpdatedListener)
    }

    override fun onResume(owner: LifecycleOwner) {
        super.onResume(owner)
        checkNewAppVersionState()
    }

    override fun onDestroy(owner: LifecycleOwner) {
        scope.cancel()
        super.onDestroy(owner)
        unregisterListener()
    }
}