package com.example.diplom;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Vibrator;
import android.os.VibratorManager;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.HashSet;
import java.util.Set;

public class GameActivity extends AppCompatActivity {

    private ImageView ivRunner;
    private Road3DView road3DView;
    private TextView tvQuestion;
    private TextView tvScore;
    private TextView tvBest;
    private TextView btnPause;
    private View roadLeft, roadRight;

    private String currentPosition = "center";
    private boolean isMoving = false;
    private boolean isPaused = false;
    private boolean isWaitingForAnswer = false;
    private ValueAnimator stripeAnimator;

    private int score = 0;
    private int bestScore = 0;
    private int questionsAnswered = 0;
    private int timePerQuestion = 10;
    private MathGenerator mathGenerator;
    private MathGenerator.MathProblem currentProblem;
    private CountDownTimer questionTimer;
    private Vibrator vibrator;
    private SoundManager soundManager;
    private boolean soundEnabled;
    private boolean vibrationEnabled;
    private Set<String> selectedOperations;
    private String difficulty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        loadSettings();
        soundManager = new SoundManager(this);
        soundManager.setSoundEnabled(soundEnabled);
        initViews();
        setupSwipeListener();
        startRoadAnimation();

        mathGenerator = new MathGenerator(difficulty, selectedOperations);
        try {
            VibratorManager vm = (VibratorManager) getSystemService(VIBRATOR_MANAGER_SERVICE);
            if (vm != null) {
                vibrator = vm.getDefaultVibrator();
            }
        } catch (Exception e) {
            vibrator = null;
        }

        roadLeft.setOnClickListener(v -> {
            if (isWaitingForAnswer) onAnswerChosen(true);
        });
        roadRight.setOnClickListener(v -> {
            if (isWaitingForAnswer) onAnswerChosen(false);
        });
        btnPause.setOnClickListener(v -> showPauseDialog());
        tvQuestion.setText("");
        road3DView.setAnswers("", "");
        showNextQuestion();
    }

    private void loadSettings() {
        SharedPreferences prefs = getSharedPreferences("MathRunner", MODE_PRIVATE);
        soundEnabled = prefs.getBoolean("sound", true);
        vibrationEnabled = prefs.getBoolean("vibration", true);
        difficulty = prefs.getString("difficulty", "medium");

        String operations = prefs.getString("operations", "addition,subtraction");
        selectedOperations = new HashSet<>();
        if (operations != null && !operations.isEmpty()) {
            for (String op : operations.split(",")) {
                selectedOperations.add(op.trim());
            }
        }

        bestScore = prefs.getInt("best_score", 0);
    }

    private void saveBestScore() {
        SharedPreferences prefs = getSharedPreferences("MathRunner", MODE_PRIVATE);
        if (score > bestScore) {
            bestScore = score;
            prefs.edit().putInt("best_score", bestScore).apply();
            String playerName = PlayerManager.getPlayerName(this);
            new ApiManager(this).saveScore(playerName, bestScore, new ApiManager.SaveCallback() {
                @Override
                public void onSuccess(int rank) { }

                @Override
                public void onError(String error) { }
            });
        }
    }

    private void initViews() {
        ivRunner = findViewById(R.id.iv_runner);
        road3DView = findViewById(R.id.road_3d_view);
        tvQuestion = findViewById(R.id.tv_question);
        tvScore = findViewById(R.id.tv_score);
        tvBest = findViewById(R.id.tv_best);
        btnPause = findViewById(R.id.btn_pause);
        roadLeft = findViewById(R.id.road_left_half);
        roadRight = findViewById(R.id.road_right_half);

        tvBest.setText(String.valueOf(bestScore));

        // Анимация бега
        try {
            Animation bounceAnim = AnimationUtils.loadAnimation(this, R.anim.runner_bounce);
            if (bounceAnim != null) {
                ivRunner.startAnimation(bounceAnim);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showNextQuestion() {
        if (isPaused) return;
        mathGenerator = new MathGenerator(difficulty, selectedOperations);
        currentProblem = mathGenerator.generate();

        tvQuestion.setText(currentProblem.question);
        road3DView.setAnswers(currentProblem.leftAnswer, currentProblem.rightAnswer);

        moveRunnerToCenter();
        isWaitingForAnswer = true;

        startQuestionTimer();
    }

    private void startQuestionTimer() {
        if (questionTimer != null) questionTimer.cancel();

        questionTimer = new CountDownTimer(timePerQuestion * 1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {}

            @Override
            public void onFinish() {
                isWaitingForAnswer = false;
                onWrongAnswer();
            }
        };
        questionTimer.start();
    }

    private void onAnswerChosen(boolean choseLeft) {
        if (!isWaitingForAnswer || isPaused) return;

        isWaitingForAnswer = false;
        if (questionTimer != null) questionTimer.cancel();

        // Двигаем персонажа
        if (choseLeft) {
            moveRunnerToLeft();
        } else {
            moveRunnerToRight();
        }

        // Проверяем ответ
        if (choseLeft == currentProblem.isLeftCorrect) {
            onCorrectAnswer();
        } else {
            onWrongAnswer();
        }
    }

    private void onCorrectAnswer() {
        score++;
        tvScore.setText(String.valueOf(score));

        safeVibrate(100);
        if (soundManager != null) {
            soundManager.playCorrect();
        }

        questionsAnswered++;
        updateDifficulty();

        new Handler().postDelayed(this::showNextQuestion, 1500);
    }

    private void onWrongAnswer() {
        saveBestScore();

        safeVibrate(new long[]{0, 100, 100, 200});
        if (soundManager != null) {
            soundManager.playWrong();
        }

        if (stripeAnimator != null) stripeAnimator.pause();
        if (questionTimer != null) questionTimer.cancel();
        isPaused = true;

        showGameOverDialog();
    }

    private void showGameOverDialog() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_game_over);
        dialog.setCancelable(false);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setLayout(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            dialog.getWindow().setGravity(android.view.Gravity.CENTER);
        }

        TextView tvFinalScore = dialog.findViewById(R.id.tv_final_score);
        TextView tvBestScore = dialog.findViewById(R.id.tv_best_score);
        View btnRestart = dialog.findViewById(R.id.btn_restart);
        View btnExit = dialog.findViewById(R.id.btn_exit_game);

        tvFinalScore.setText(String.valueOf(score));
        tvBestScore.setText(String.valueOf(bestScore));

        btnRestart.setOnClickListener(v -> {
            dialog.dismiss();
            restartGame();
        });

        btnExit.setOnClickListener(v -> {
            dialog.dismiss();
            exitGame();
        });

        dialog.show();
    }

    private void restartGame() {
        // Сбрасываем всё
        score = 0;
        questionsAnswered = 0;
        timePerQuestion = 10;
        isWaitingForAnswer = false;
        isPaused = false;

        tvScore.setText("0");
        tvBest.setText(String.valueOf(bestScore));

        // Возвращаем персонажа в центр
        moveRunnerToCenter();

        // Запускаем дорогу заново
        if (stripeAnimator != null) {
            stripeAnimator.cancel();
        }
        startRoadAnimation();

        // Новый пример
        showNextQuestion();
    }

    private void safeVibrate(long duration) {
        if (!vibrationEnabled || vibrator == null) return;
        try {
            vibrator.vibrate(duration);
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    private void safeVibrate(long[] pattern) {
        if (!vibrationEnabled || vibrator == null) return;
        try {
            vibrator.vibrate(pattern, -1);
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    private void updateDifficulty() {
        if (questionsAnswered > 0 && questionsAnswered % 5 == 0) {
            if (timePerQuestion > 5) {
                timePerQuestion--;
            }
        }
    }

    private void moveRunnerToCenter() {
        if (currentPosition.equals("center")) return;
        isMoving = true;
        currentPosition = "center";
        animateRunner(0);
    }

    private void moveRunnerToLeft() {
        if (currentPosition.equals("left")) return;
        isMoving = true;
        currentPosition = "left";
        float screenWidth = getResources().getDisplayMetrics().widthPixels;
        animateRunner(-(screenWidth / 5));
    }

    private void moveRunnerToRight() {
        if (currentPosition.equals("right")) return;
        isMoving = true;
        currentPosition = "right";
        float screenWidth = getResources().getDisplayMetrics().widthPixels;
        animateRunner(screenWidth / 5);
    }

    private void animateRunner(float targetX) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(ivRunner, "translationX", targetX);
        animator.setDuration(250);
        animator.addListener(new android.animation.AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                isMoving = false;
            }
        });
        animator.start();
    }

    private void startRoadAnimation() {
        stripeAnimator = ValueAnimator.ofFloat(0f, 500f);
        stripeAnimator.setDuration(3000);
        stripeAnimator.setRepeatCount(ValueAnimator.INFINITE);
        stripeAnimator.setInterpolator(null);
        stripeAnimator.addUpdateListener(animation -> {
            float value = (float) animation.getAnimatedValue();
            road3DView.setStripeOffset(value);
        });
        stripeAnimator.start();
    }

    private void pauseGame() {
        if (stripeAnimator != null && stripeAnimator.isRunning()) {
            stripeAnimator.pause();
        }
        if (questionTimer != null) {
            questionTimer.cancel();
        }
        isPaused = true;
    }

    private void resumeGame() {
        if (stripeAnimator != null && stripeAnimator.isPaused()) {
            stripeAnimator.resume();
        }
        isPaused = false;
        if (isWaitingForAnswer) {
            startQuestionTimer();
        }
    }

    private void exitGame() {
        saveBestScore();
        if (stripeAnimator != null) stripeAnimator.cancel();
        if (questionTimer != null) questionTimer.cancel();
        Intent intent = new Intent(GameActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    private void showPauseDialog() {
        pauseGame();

        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_pause);
        dialog.setCancelable(false);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setLayout(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            dialog.getWindow().setGravity(android.view.Gravity.CENTER);
        }

        dialog.findViewById(R.id.btn_resume).setOnClickListener(v -> {
            dialog.dismiss();
            resumeGame();
        });

        dialog.findViewById(R.id.btn_exit).setOnClickListener(v -> {
            dialog.dismiss();
            exitGame();
        });

        dialog.setOnDismissListener(d -> {
            if (isPaused) resumeGame();
        });

        dialog.show();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupSwipeListener() {
        View gameArea = findViewById(R.id.game_area);

        gameArea.setOnTouchListener(new View.OnTouchListener() {
            private float startX;

            @Override
            public boolean onTouch(View v, android.view.MotionEvent event) {
                if (isMoving || isPaused) return true;

                switch (event.getAction()) {
                    case android.view.MotionEvent.ACTION_DOWN:
                        startX = event.getX();
                        return true;

                    case android.view.MotionEvent.ACTION_UP:
                        if (!isWaitingForAnswer) return true;

                        float endX = event.getX();
                        float diff = endX - startX;

                        if (Math.abs(diff) > 50) {
                            onAnswerChosen(diff > 0);
                        }
                        return true;
                }
                return false;
            }
        });
    }

    @Override
    public void onBackPressed() {
        showPauseDialog();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        saveBestScore();
        if (soundManager != null) {
            soundManager.release();
        }
        if (stripeAnimator != null) stripeAnimator.cancel();
        if (questionTimer != null) questionTimer.cancel();
    }
}