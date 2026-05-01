package com.example.diplom;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.chip.Chip;
import com.google.android.material.switchmaterial.SwitchMaterial;
import java.util.HashSet;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private TextView btnStart;
    private TextView tvRecord;
    private TextView tvRunner;
    private View llLeaderboard;
    private View llSettings;

    // Переменные настроек
    private boolean soundEnabled = true;
    private boolean vibrationEnabled = true;
    private Set<String> selectedOperations = new HashSet<>();
    private String difficulty = "medium";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnStart = findViewById(R.id.btn_start);
        tvRecord = findViewById(R.id.tv_record);
        tvRunner = findViewById(R.id.tv_runner);
        llLeaderboard = findViewById(R.id.ll_leaderboard);
        llSettings = findViewById(R.id.ll_settings);

        loadSettings();

        try {
            Animation bounceAnim = AnimationUtils.loadAnimation(this, R.anim.runner_bounce);
            if (bounceAnim != null) {
                tvRunner.startAnimation(bounceAnim);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        btnStart.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, GameActivity.class);
            startActivity(intent);
        });

        llLeaderboard.setOnClickListener(v -> {
            Toast.makeText(MainActivity.this,
                    "Топ игроков", Toast.LENGTH_SHORT).show();
        });

        llSettings.setOnClickListener(v -> {
            showSettingsDialog();
        });
        updateRecordDisplay();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Обновляем рекорд при возврате из игры
        updateRecordDisplay();
    }

    private void updateRecordDisplay() {
        SharedPreferences prefs = getSharedPreferences("MathRunner", MODE_PRIVATE);
        int bestScore = prefs.getInt("best_score", 0);
        tvRecord.setText(String.valueOf(bestScore));
    }

    private void loadSettings() {
        SharedPreferences prefs = getSharedPreferences("MathRunner", MODE_PRIVATE);
        soundEnabled = prefs.getBoolean("sound", true);
        vibrationEnabled = prefs.getBoolean("vibration", true);

        String operations = prefs.getString("operations", "addition,subtraction");
        selectedOperations.clear();
        if (operations != null && !operations.isEmpty()) {
            String[] ops = operations.split(",");
            for (String op : ops) {
                selectedOperations.add(op.trim());
            }
        }

        difficulty = prefs.getString("difficulty", "medium");
    }

    private void saveSettings() {
        SharedPreferences prefs = getSharedPreferences("MathRunner", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("sound", soundEnabled);
        editor.putBoolean("vibration", vibrationEnabled);

        StringBuilder ops = new StringBuilder();
        for (String op : selectedOperations) {
            if (ops.length() > 0) ops.append(",");
            ops.append(op);
        }
        editor.putString("operations", ops.toString());
        editor.putString("difficulty", difficulty);

        editor.apply();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void showSettingsDialog() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_settings);
        dialog.setCancelable(true);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            dialog.getWindow().setGravity(android.view.Gravity.CENTER);
        }

        Switch switchSound = dialog.findViewById(R.id.switch_sound);
        Switch switchVibration = dialog.findViewById(R.id.switch_vibration);

        View btnAddition = dialog.findViewById(R.id.btn_addition);
        View btnSubtraction = dialog.findViewById(R.id.btn_subtraction);
        View btnMultiplication = dialog.findViewById(R.id.btn_multiplication);
        View btnDivision = dialog.findViewById(R.id.btn_division);

        TextView tvDifficulty = dialog.findViewById(R.id.tv_difficulty);
        TextView arrowLeft = dialog.findViewById(R.id.arrow_left);
        TextView arrowRight = dialog.findViewById(R.id.arrow_right);

        View btnCancel = dialog.findViewById(R.id.btn_cancel);
        View btnSave = dialog.findViewById(R.id.btn_save);

        switchSound.setChecked(soundEnabled);
        switchVibration.setChecked(vibrationEnabled);

        updateOperationButton(btnAddition, selectedOperations.contains("addition"));
        updateOperationButton(btnSubtraction, selectedOperations.contains("subtraction"));
        updateOperationButton(btnMultiplication, selectedOperations.contains("multiplication"));
        updateOperationButton(btnDivision, selectedOperations.contains("division"));

        updateDifficultyText(tvDifficulty);
        updateDifficultyArrows(tvDifficulty);

        switchSound.setOnCheckedChangeListener((buttonView, isChecked) -> {
            soundEnabled = isChecked;
        });

        switchVibration.setOnCheckedChangeListener((buttonView, isChecked) -> {
            vibrationEnabled = isChecked;
        });

        setupOperationButton(btnAddition, "addition");
        setupOperationButton(btnSubtraction, "subtraction");
        setupOperationButton(btnMultiplication, "multiplication");
        setupOperationButton(btnDivision, "division");

        arrowLeft.setOnClickListener(v -> {
            switchDifficultyLeft(tvDifficulty);
            updateDifficultyArrows(tvDifficulty);
        });

        arrowRight.setOnClickListener(v -> {
            switchDifficultyRight(tvDifficulty);
            updateDifficultyArrows(tvDifficulty);
        });

        tvDifficulty.setOnTouchListener(new View.OnTouchListener() {
            private float startX;

            @Override
            public boolean onTouch(View v, android.view.MotionEvent event) {
                switch (event.getAction()) {
                    case android.view.MotionEvent.ACTION_DOWN:
                        startX = event.getX();
                        return true;

                    case android.view.MotionEvent.ACTION_UP:
                        float endX = event.getX();
                        float diff = endX - startX;

                        if (Math.abs(diff) > 50) {
                            if (diff < 0) {
                                switchDifficultyRight(tvDifficulty);
                            } else {
                                switchDifficultyLeft(tvDifficulty);
                            }
                            updateDifficultyArrows(tvDifficulty);
                        }
                        return true;
                }
                return false;
            }
        });

        btnCancel.setOnClickListener(v -> {
            loadSettings();
            dialog.dismiss();
        });

        btnSave.setOnClickListener(v -> {
            saveSettings();
            dialog.dismiss();
            Toast.makeText(MainActivity.this,
                    "Настройки сохранены", Toast.LENGTH_SHORT).show();
        });

        dialog.show();
    }

    private void switchDifficultyLeft(TextView tvDifficulty) {
        switch (difficulty) {
            case "easy":
                return;
            case "medium":
                difficulty = "easy";
                break;
            case "hard":
                difficulty = "medium";
                break;
        }
        updateDifficultyText(tvDifficulty);
        updateDifficultyArrows(tvDifficulty);
    }


    private void switchDifficultyRight(TextView tvDifficulty) {
        switch (difficulty) {
            case "easy":
                difficulty = "medium";
                break;
            case "medium":
                difficulty = "hard";
                break;
            case "hard":
                return;
        }
        updateDifficultyText(tvDifficulty);
        updateDifficultyArrows(tvDifficulty);
    }

    private void updateDifficultyText(TextView tvDifficulty) {
        if (tvDifficulty == null) return;

        switch (difficulty) {
            case "easy":
                tvDifficulty.setText("Легкая");
                tvDifficulty.setTextColor(Color.parseColor("#FF81C784"));
                break;
            case "medium":
                tvDifficulty.setText("Средняя");
                tvDifficulty.setTextColor(Color.parseColor("#FFFFD700"));
                break;
            case "hard":
                tvDifficulty.setText("Сложная");
                tvDifficulty.setTextColor(Color.parseColor("#FFFF5252"));
                break;
        }
    }

    private void updateDifficultyArrows(TextView tvDifficulty) {
        if (tvDifficulty == null) return;

        View parent = (View) tvDifficulty.getParent();
        if (parent instanceof LinearLayout) {
            LinearLayout container = (LinearLayout) parent;
            TextView arrowLeft = (TextView) container.getChildAt(0);
            TextView arrowRight = (TextView) container.getChildAt(2);

            if (difficulty.equals("easy")) {
                arrowLeft.setTextColor(Color.parseColor("#FF4A4A4A"));
                arrowLeft.setClickable(false);
            } else {
                arrowLeft.setTextColor(Color.parseColor("#FF81C784"));
                arrowLeft.setClickable(true);
            }

            if (difficulty.equals("hard")) {
                arrowRight.setTextColor(Color.parseColor("#FF4A4A4A"));
                arrowRight.setClickable(false);
            } else {
                arrowRight.setTextColor(Color.parseColor("#FF81C784"));
                arrowRight.setClickable(true);
            }
        }
    }

    private void setupOperationButton(View button, String operation) {
        button.setOnClickListener(v -> {
            if (selectedOperations.contains(operation)) {
                selectedOperations.remove(operation);

                if (selectedOperations.isEmpty()) {
                    selectedOperations.add(operation);
                    Toast.makeText(MainActivity.this,
                            "⚠️ Должно быть выбрано хотя бы одно действие",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
            } else {
                selectedOperations.add(operation);
            }

            updateOperationButton(button, selectedOperations.contains(operation));
        });
    }

    private void updateOperationButton(View button, boolean isSelected) {
        if (isSelected) {
            button.setBackgroundResource(R.drawable.operation_button_selected);

            if (button instanceof LinearLayout) {
                LinearLayout ll = (LinearLayout) button;
                for (int i = 0; i < ll.getChildCount(); i++) {
                    View child = ll.getChildAt(i);
                    if (child instanceof TextView) {
                        ((TextView) child).setTextColor(Color.parseColor("#FF0F1923"));
                    }
                }
            }
        } else {
            button.setBackgroundResource(R.drawable.operation_button_bg);

            if (button instanceof LinearLayout) {
                LinearLayout ll = (LinearLayout) button;
                for (int i = 0; i < ll.getChildCount(); i++) {
                    View child = ll.getChildAt(i);
                    if (child instanceof TextView) {
                        if (i == 0) {
                            ((TextView) child).setTextColor(Color.parseColor("#FFFFFFFF"));
                        } else {
                            ((TextView) child).setTextColor(Color.parseColor("#FFB0BEC5"));
                        }
                    }
                }
            }
        }
    }
}