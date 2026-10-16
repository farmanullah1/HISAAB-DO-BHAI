package com.example.ui.security

import android.app.Activity
import android.app.Application
import android.app.KeyguardManager
import android.content.Context
import android.content.SharedPreferences
import android.hardware.biometrics.BiometricManager
import android.hardware.biometrics.BiometricPrompt
import android.hardware.fingerprint.FingerprintManager
import android.os.Build
import android.os.Bundle
import android.os.CancellationSignal
import android.view.WindowManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.Executors

enum class BiometricStatus(val description: String) {
    AVAILABLE("Biometric hardware ready & enrolled"),
    NONE_ENROLLED("Hardware available but no fingerprint enrolled"),
    NO_HARDWARE("No biometric sensor detected"),
    HW_UNAVAILABLE("Sensor temporarily unavailable"),
    UNAVAILABLE("Biometrics unsupported on this device")
}

class SecurityManager private constructor(private val context: Context) : Application.ActivityLifecycleCallbacks {

    private val prefs: SharedPreferences = context.getSharedPreferences("hisaab_security_prefs", Context.MODE_PRIVATE)

    private val _isLocked = MutableStateFlow(false)
    val isLocked: StateFlow<Boolean> = _isLocked.asStateFlow()

    private val _isLockConfigured = MutableStateFlow(prefs.getBoolean("lock_enabled", false))
    val isLockConfigured: StateFlow<Boolean> = _isLockConfigured.asStateFlow()

    private val _isBiometricEnabled = MutableStateFlow(prefs.getBoolean("biometric_enabled", true))
    val isBiometricEnabled: StateFlow<Boolean> = _isBiometricEnabled.asStateFlow()

    private val _isFlagSecureEnabled = MutableStateFlow(prefs.getBoolean("flag_secure_enabled", false))
    val isFlagSecureEnabled: StateFlow<Boolean> = _isFlagSecureEnabled.asStateFlow()

    private var backgroundTimestamp: Long = 0L
    private val lockTimeoutMs: Long = 15_000L // 15 seconds grace period when switching apps
    private var startedActivityCount: Int = 0
    private var activeCancellationSignal: CancellationSignal? = null

    init {
        // Initial state: if lock is enabled, start locked
        if (isLockConfigured.value) {
            _isLocked.value = true
        }
        (context.applicationContext as? Application)?.registerActivityLifecycleCallbacks(this)
    }

    override fun onActivityStarted(activity: Activity) {
        if (startedActivityCount == 0 && isLockConfigured.value) {
            val elapsed = System.currentTimeMillis() - backgroundTimestamp
            if (backgroundTimestamp > 0 && elapsed > lockTimeoutMs) {
                _isLocked.value = true
            }
        }
        startedActivityCount++
    }

    override fun onActivityStopped(activity: Activity) {
        startedActivityCount = (startedActivityCount - 1).coerceAtLeast(0)
        if (startedActivityCount == 0) {
            backgroundTimestamp = System.currentTimeMillis()
        }
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        applyFlagSecure(activity)
    }

    override fun onActivityResumed(activity: Activity) {}
    override fun onActivityPaused(activity: Activity) {}
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
    override fun onActivityDestroyed(activity: Activity) {
        cancelBiometricPrompt()
    }

    fun getBiometricStatus(): BiometricStatus {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val bm = context.getSystemService(BiometricManager::class.java) ?: return BiometricStatus.UNAVAILABLE
            return when (bm.canAuthenticate()) {
                BiometricManager.BIOMETRIC_SUCCESS -> BiometricStatus.AVAILABLE
                BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> BiometricStatus.NONE_ENROLLED
                BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> BiometricStatus.NO_HARDWARE
                BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> BiometricStatus.HW_UNAVAILABLE
                else -> BiometricStatus.UNAVAILABLE
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val fp = context.getSystemService(FingerprintManager::class.java)
            return if (fp?.isHardwareDetected == true) {
                if (fp.hasEnrolledFingerprints()) BiometricStatus.AVAILABLE else BiometricStatus.NONE_ENROLLED
            } else {
                BiometricStatus.NO_HARDWARE
            }
        }
        return BiometricStatus.UNAVAILABLE
    }

    fun isDeviceSecure(): Boolean {
        val km = context.getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager
        return km?.isDeviceSecure == true
    }

    fun hasVaultPin(): Boolean {
        return prefs.getString("vault_pin", null)?.isNotEmpty() == true
    }

    fun verifyPin(pin: String): Boolean {
        val storedPin = prefs.getString("vault_pin", null)
        if (storedPin == null) {
            // Default pin if none configured yet
            return pin == "1234"
        }
        return storedPin == pin
    }

    fun setVaultPin(newPin: String) {
        prefs.edit().putString("vault_pin", newPin).apply()
    }

    fun setLockConfigured(enabled: Boolean) {
        prefs.edit().putBoolean("lock_enabled", enabled).apply()
        _isLockConfigured.value = enabled
        if (!enabled) {
            _isLocked.value = false
        }
    }

    fun setBiometricEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("biometric_enabled", enabled).apply()
        _isBiometricEnabled.value = enabled
    }

    fun setFlagSecureEnabled(activity: Activity?, enabled: Boolean) {
        prefs.edit().putBoolean("flag_secure_enabled", enabled).apply()
        _isFlagSecureEnabled.value = enabled
        activity?.let { applyFlagSecure(it, enabled) }
    }

    fun applyFlagSecure(activity: Activity, enabled: Boolean = _isFlagSecureEnabled.value) {
        if (enabled) {
            activity.window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        } else {
            activity.window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }
    }

    fun unlock() {
        cancelBiometricPrompt()
        _isLocked.value = false
    }

    fun lockNow() {
        if (_isLockConfigured.value) {
            _isLocked.value = true
        }
    }

    fun cancelBiometricPrompt() {
        activeCancellationSignal?.let {
            if (!it.isCanceled) {
                it.cancel()
            }
        }
        activeCancellationSignal = null
    }

    fun promptBiometric(
        activity: Activity,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            cancelBiometricPrompt()

            val executor = Executors.newSingleThreadExecutor()
            val signal = CancellationSignal()
            activeCancellationSignal = signal

            val builder = BiometricPrompt.Builder(activity)
                .setTitle("Hisaab Do Bhai Vault")
                .setSubtitle("حساب کی سیکیورٹی کے لیے فنگر پرنٹ استعمال کریں")
                .setDescription("Verify your identity with fingerprint to unlock your private ledger")
                .setNegativeButton("Use Vault PIN", executor) { _, _ ->
                    activity.runOnUiThread {
                        onError("Use Vault PIN below")
                    }
                }

            val prompt = builder.build()

            prompt.authenticate(signal, executor, object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult?) {
                    super.onAuthenticationSucceeded(result)
                    activeCancellationSignal = null
                    activity.runOnUiThread {
                        unlock()
                        onSuccess()
                    }
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence?) {
                    super.onAuthenticationError(errorCode, errString)
                    activeCancellationSignal = null
                    activity.runOnUiThread {
                        if (errorCode != BiometricPrompt.BIOMETRIC_ERROR_USER_CANCELED &&
                            errorCode != BiometricPrompt.BIOMETRIC_ERROR_CANCELED
                        ) {
                            onError(errString?.toString() ?: "Authentication failed")
                        }
                    }
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    activity.runOnUiThread {
                        onError("Fingerprint not recognized. Try again or enter PIN.")
                    }
                }
            })
        } else {
            onError("Biometrics not supported on this Android version")
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: SecurityManager? = null

        fun getInstance(context: Context): SecurityManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: SecurityManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}
