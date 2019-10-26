package info.nightscout.androidaps.utils.protection

import androidx.biometric.BiometricConstants
import androidx.biometric.BiometricPrompt
import androidx.fragment.app.FragmentActivity
import info.nightscout.androidaps.MainApp
import info.nightscout.androidaps.R
import info.nightscout.androidaps.utils.ToastUtils
import java.util.concurrent.Executors

object BiometricCheck {
    fun biometricPrompt(activity: FragmentActivity, title: Int, ok: Runnable?, cancel: Runnable? = null, fail: Runnable? = null) {
        val executor = Executors.newSingleThreadExecutor()

        val biometricPrompt = BiometricPrompt(activity, executor, object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                when (errorCode) {
                    BiometricConstants.ERROR_UNABLE_TO_PROCESS,
                    BiometricConstants.ERROR_TIMEOUT,
                    BiometricConstants.ERROR_CANCELED,
                    BiometricConstants.ERROR_LOCKOUT,
                    BiometricConstants.ERROR_VENDOR,
                    BiometricConstants.ERROR_LOCKOUT_PERMANENT,
                    BiometricConstants.ERROR_USER_CANCELED -> {
                        ToastUtils.showToastInUiThread(activity.baseContext, errString.toString())
                        fail?.run()
                    }
                    BiometricConstants.ERROR_NEGATIVE_BUTTON ->
                        cancel?.run()
                    BiometricConstants.ERROR_NO_SPACE,
                    BiometricConstants.ERROR_HW_UNAVAILABLE,
                    BiometricConstants.ERROR_HW_NOT_PRESENT,
                    BiometricConstants.ERROR_NO_DEVICE_CREDENTIAL,
                    BiometricConstants.ERROR_NO_BIOMETRICS ->
                        // call ok, because it's not possible to bypass it when biometrics fail
                        ok?.run()
                }
            }

            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                // Called when a biometric is recognized.
                ok?.run()
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                // Called when a biometric is valid but not recognized.
                fail?.run()
            }
        })

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
                .setTitle(MainApp.gs(title))
                .setDescription(MainApp.gs(R.string.biometric_title))
                .setNegativeButtonText(MainApp.gs(R.string.cancel))
                .build()

        biometricPrompt.authenticate(promptInfo)
    }

}