package com.lifeline.safety.activities;

import android.os.Bundle;
import android.content.Intent;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.os.VibratorManager;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.widget.*;
import android.os.VibrationEffect;
import android.os.Vibrator;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.cardview.widget.CardView;
import androidx.core.view.WindowCompat;

import com.lifeline.safety.R;
import com.lifeline.safety.utils.CooldownManager;
import com.lifeline.safety.utils.PermissionManager;
import com.lifeline.safety.utils.ShakeDetector;
import com.lifeline.safety.utils.SosEngine;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.Objects;

public class HomeActivity extends AppCompatActivity {

    // UI Components
    private RelativeLayout sosButton;
    private CardView contactsCard, historyCard, safetyGuideCard, shakeToggleCard;
    private ImageView settingsIcon;
    private SwitchCompat shakeToggle;

    // Shake Detection
    private ShakeDetector shakeDetector;
    private boolean shakeEnabled = false;

    // SOS State
    private boolean sosArmed = false;
    private CountDownTimer sosCountdown;
    private AlertDialog countdownDialog;

    private static final int COUNTDOWN_SECONDS = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), true);
        setContentView(R.layout.activity_home);

        initializeViews();
        setupShakeDetection();
        setupClickListeners();
    }

    private void initializeViews() {
        // Main SOS Button
        sosButton = findViewById(R.id.sosButton);

        // Navigation Cards
        contactsCard = findViewById(R.id.contactsCard);
        historyCard = findViewById(R.id.historyCard);
        safetyGuideCard = findViewById(R.id.safetyGuideCard);

        // Shake Toggle Card
        shakeToggleCard = findViewById(R.id.shakeToggleCard);
        shakeToggle = findViewById(R.id.shakeToggle);

        // Settings Icon
        settingsIcon = findViewById(R.id.settingsIcon);
    }

    private void setupShakeDetection() {
        // Load saved shake preference
        shakeEnabled = isShakeEnabled();
        shakeToggle.setChecked(shakeEnabled);

        // Initialize shake detector
        shakeDetector = new ShakeDetector(this, () -> runOnUiThread(() -> {
            if (!sosArmed
                    && shakeEnabled
                    && !CooldownManager.isCoolingDown()
                    && PermissionManager.hasSOSPermissions(this)) {

                vibrateWarning();
                sosArmed = true;

                new Handler(Looper.getMainLooper()).postDelayed(this::vibrateWarning, 100);

                triggerSOS();
            }
        }));

        // Setup shake toggle listener
        shakeToggle.setOnCheckedChangeListener((buttonView, isChecked) -> {
            shakeEnabled = isChecked;

            // Save preference
            getSharedPreferences("lifeline_prefs", MODE_PRIVATE)
                    .edit()
                    .putBoolean("shake_sos", isChecked)
                    .apply();

            // Start/Stop shake detector
            if (isChecked) {
                shakeDetector.start();
                Toast.makeText(this, "Shake-to-SOS enabled", Toast.LENGTH_SHORT).show();
            } else {
                shakeDetector.stop();
                Toast.makeText(this, "Shake-to-SOS disabled", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupClickListeners() {
        // SOS Button Click (Tap)
        sosButton.setOnClickListener(v -> handleSOSClick());

        // SOS Button Long Press (Alternative trigger)
        sosButton.setOnLongClickListener(v -> {
            if (!sosArmed
                    && !CooldownManager.isCoolingDown()
                    && PermissionManager.hasSOSPermissions(this)) {

                sosButton.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
                vibrateWarning();
                sosArmed = true;
                triggerSOS();
            }
            return true;
        });

        // Navigation Cards
        contactsCard.setOnClickListener(v ->
                startActivity(new Intent(this, ViewContactsActivity.class))
        );

        historyCard.setOnClickListener(v ->
                startActivity(new Intent(this, AlertHistoryActivity.class))
        );

        safetyGuideCard.setOnClickListener(v ->
                startActivity(new Intent(this, SafetyGuideActivity.class))
        );

        // Settings Icon
        settingsIcon.setOnClickListener(v -> {
            // Open settings activity or show settings dialog
            Toast.makeText(this, "Settings coming soon", Toast.LENGTH_SHORT).show();
            // startActivity(new Intent(this, SettingsActivity.class));
        });

        // Bottom navigation
        LinearLayout navHome = findViewById(R.id.navHome);
        LinearLayout navHistory = findViewById(R.id.navHistory);
        RelativeLayout navSOS = findViewById(R.id.navSOS);
        LinearLayout navContacts = findViewById(R.id.navContacts);
        LinearLayout navSettings = findViewById(R.id.navSettings);

        navHome.setOnClickListener(v -> {
            // Already on Home
        });

        navHistory.setOnClickListener(v -> {
            startActivity(new Intent(this, AlertHistoryActivity.class));
            finish();
        });

        navSOS.setOnClickListener(v -> {
            handleSOSClick();
        });

        navContacts.setOnClickListener(v -> {
            startActivity(new Intent(this, ViewContactsActivity.class));
            finish();
        });

        navSettings.setOnClickListener(v -> {
            startActivity(new Intent(this, SafetyGuideActivity.class));
            finish();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        shakeEnabled = isShakeEnabled();
        if (shakeEnabled && shakeDetector != null && PermissionManager.hasSOSPermissions(this)) {
            shakeDetector.start();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (shakeDetector != null) {
            shakeDetector.stop();
        }
        cleanupCountdown();
    }

    /**
     * Handle SOS button tap
     */
    private void handleSOSClick() {
        if (sosArmed) return;

        sosArmed = true;
        sosButton.setAlpha(0.85f);
        sosButton.setEnabled(false);
        vibrateWarning();

        if (PermissionManager.hasSOSPermissions(this)) {
            startSOSCountdown();
        } else if (PermissionManager.shouldShowRationale(this)) {
            resetSOSUI();
            PermissionManager.showRationaleDialog(this);
        } else {
            resetSOSUI();
            PermissionManager.requestSOSPermissions(this);
        }
    }

    /**
     * Handle permission results
     */
    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String[] permissions,
            @NonNull int[] grantResults
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PermissionManager.SOS_PERMISSION_CODE) {

            if (PermissionManager.hasSOSPermissions(this)) {
                if (shakeEnabled && shakeDetector != null) {
                    shakeDetector.start();
                }
                triggerSOS();
                return;
            }

            resetSOSUI();

            if (!PermissionManager.shouldShowRationale(this)) {
                PermissionManager.showSettingsDialog(this);
            } else {
                Toast.makeText(
                        this,
                        "Location and SMS permissions are required for SOS",
                        Toast.LENGTH_LONG
                ).show();
            }
        }
    }

    private void startSOSCountdown() {
        if (sosCountdown != null) return;

        View dialogView = getLayoutInflater()
                .inflate(R.layout.dialog_sos_countdown, null);

        TextView tvCountdown = dialogView.findViewById(R.id.tvCountdown);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);
        ImageView btnClose = dialogView.findViewById(R.id.btnClose);
        com.lifeline.safety.views.CircularProgressView circularProgress =
                dialogView.findViewById(R.id.circularProgress);

        // Create fullscreen dialog
        countdownDialog = new AlertDialog.Builder(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen)
                .setView(dialogView)
                .setCancelable(false)
                .create();

        // Make dialog fullscreen and transparent background
        if (countdownDialog.getWindow() != null) {
            countdownDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            countdownDialog.getWindow().setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
            );
        }

        countdownDialog.show();

        // Start countdown
        sosCountdown = new CountDownTimer(COUNTDOWN_SECONDS * 1000L, 1000) {
            int secondsLeft = COUNTDOWN_SECONDS;

            @Override
            public void onTick(long millisUntilFinished) {
                tvCountdown.setText(String.valueOf(secondsLeft));

                // Animate circular progress
                float progressPercentage = ((COUNTDOWN_SECONDS - secondsLeft + 1) * 100f) / COUNTDOWN_SECONDS;
                circularProgress.animateProgress(progressPercentage, 1000);

                secondsLeft--;
            }

            @Override
            public void onFinish() {
                cleanupCountdown();
                triggerSOS();
            }
        }.start();

        // Set initial progress to 0
        circularProgress.setProgress(0);

        // Cancel button click
        btnCancel.setOnClickListener(v -> {
            cleanupCountdown();
            resetSOSUI();
            Toast.makeText(this, "SOS cancelled", Toast.LENGTH_SHORT).show();
        });

        // Close button click (same as cancel)
        btnClose.setOnClickListener(v -> {
            cleanupCountdown();
            resetSOSUI();
            Toast.makeText(this, "SOS cancelled", Toast.LENGTH_SHORT).show();
        });
    }

    private void triggerSOS() {
        CooldownManager.markTriggered();

        SosEngine sosEngine = new SosEngine(this);

        sosEngine.triggerSOS(new SosEngine.SosCallback() {

            @Override
            public void onStarted() {
                Toast.makeText(
                        HomeActivity.this,
                        "🚨 SOS activated...",
                        Toast.LENGTH_SHORT
                ).show();
            }

            @Override
            public void onSmsIntentReady(Intent intent) {
                startActivity(intent);
            }

            @Override
            public void onCompleted() {
                resetSOSUI();
                Toast.makeText(
                        HomeActivity.this,
                        "SOS prepared. Opening messaging app...",
                        Toast.LENGTH_LONG
                ).show();
            }

            @Override
            public void onFailed(String reason) {
                resetSOSUI();
                Toast.makeText(
                        HomeActivity.this,
                        reason,
                        Toast.LENGTH_LONG
                ).show();
            }
        });
    }

    private void cleanupCountdown() {
        if (sosCountdown != null) {
            sosCountdown.cancel();
            sosCountdown = null;
        }
        if (countdownDialog != null && countdownDialog.isShowing()) {
            countdownDialog.dismiss();
            countdownDialog = null;
        }
        resetSOSUI();
    }

    private void vibrateWarning() {
        try {
            long[] pattern = new long[]{
                    0,    // start immediately
                    800,  // vibrate 800ms
                    200,  // pause 200ms
                    800,  // vibrate again
                    200,
                    1200  // final long vibration
            };
            int[] amplitudes = new int[]{
                    0,
                    255,  // MAX strength
                    0,
                    255,
                    0,
                    255
            };

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                VibratorManager vm =
                        (VibratorManager) getSystemService(VIBRATOR_MANAGER_SERVICE);

                if (vm != null) {
                    vm.getDefaultVibrator().vibrate(
                            VibrationEffect.createWaveform(pattern, amplitudes, -1)
                    );
                }
            } else {
                Vibrator v = (Vibrator) getSystemService(VIBRATOR_SERVICE);
                if (v != null && v.hasVibrator()) {
                    v.vibrate(
                            VibrationEffect.createWaveform(pattern, amplitudes, -1)
                    );
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void resetSOSUI() {
        sosArmed = false;
        sosButton.setAlpha(1f);
        sosButton.setEnabled(true);
    }

    private boolean isShakeEnabled() {
        return getSharedPreferences("lifeline_prefs", MODE_PRIVATE)
                .getBoolean("shake_sos", false);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (shakeDetector != null) {
            shakeDetector.stop();
        }
        cleanupCountdown();
    }
}