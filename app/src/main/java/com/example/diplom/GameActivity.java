package com.example.diplom;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class GameActivity extends AppCompatActivity {

    private ImageView ivRunner;
    private Road3DView road3DView;
    private TextView tvQuestion;
    private TextView btnPause;
    private View roadLeft, roadRight;

    private String currentPosition = "center";
    private boolean isMoving = false;
    private boolean isPaused = false;
    private ValueAnimator stripeAnimator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        initViews();
        setupSwipeListener();
        startRoadAnimation();
        updateQuestion();

        roadLeft.setOnClickListener(v -> moveRunnerToLeft());
        roadRight.setOnClickListener(v -> moveRunnerToRight());

        btnPause.setOnClickListener(v -> showPauseDialog());
    }

    private void initViews() {
        ivRunner = findViewById(R.id.iv_runner);
        road3DView = findViewById(R.id.road_3d_view);
        tvQuestion = findViewById(R.id.tv_question);
        btnPause = findViewById(R.id.btn_pause);
        roadLeft = findViewById(R.id.road_left_half);
        roadRight = findViewById(R.id.road_right_half);

        Animation bounceAnim = AnimationUtils.loadAnimation(this, R.anim.runner_bounce);
        ivRunner.startAnimation(bounceAnim);
    }

    private void updateQuestion() {
        tvQuestion.setText("12 + 3 = ?");
        road3DView.setAnswers("15", "16");
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
        isPaused = true;
    }

    private void resumeGame() {
        if (stripeAnimator != null && stripeAnimator.isPaused()) {
            stripeAnimator.resume();
        }
        isPaused = false;
    }

    private void exitGame() {
        // Останавливаем анимацию
        if (stripeAnimator != null) {
            stripeAnimator.cancel();
        }
        // Возвращаемся на главный экран
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
        dialog.setCancelable(false); // Нельзя закрыть кнопкой назад

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setLayout(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            dialog.getWindow().setGravity(android.view.Gravity.CENTER);
        }

        View btnResume = dialog.findViewById(R.id.btn_resume);
        View btnExit = dialog.findViewById(R.id.btn_exit);

        btnResume.setOnClickListener(v -> {
            dialog.dismiss();
            resumeGame();
        });

        btnExit.setOnClickListener(v -> {
            dialog.dismiss();
            exitGame();
        });

        // Если диалог каким-то образом закрыт — возобновляем игру
        dialog.setOnDismissListener(dialogInterface -> {
            if (isPaused) {
                resumeGame();
            }
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
                        float endX = event.getX();
                        float diff = endX - startX;

                        if (Math.abs(diff) > 50) {
                            if (diff > 0) {
                                moveRunnerToRight();
                            } else {
                                moveRunnerToLeft();
                            }
                        }
                        return true;
                }
                return false;
            }
        });
    }

    private void moveRunnerToLeft() {
        if (isMoving || currentPosition.equals("left")) return;
        isMoving = true;
        currentPosition = "left";

        float screenWidth = getResources().getDisplayMetrics().widthPixels;
        float targetX = -(screenWidth / 5);
        animateRunner(targetX);
    }

    private void moveRunnerToRight() {
        if (isMoving || currentPosition.equals("right")) return;
        isMoving = true;
        currentPosition = "right";

        float screenWidth = getResources().getDisplayMetrics().widthPixels;
        float targetX = screenWidth / 5;
        animateRunner(targetX);
    }

    private void animateRunner(float targetX) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(ivRunner, "translationX", targetX);
        animator.setDuration(200);
        animator.addListener(new android.animation.AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                isMoving = false;
            }
        });
        animator.start();
    }

    @Override
    public void onBackPressed() {
        // При нажатии "назад" тоже показываем паузу
        showPauseDialog();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (stripeAnimator != null) {
            stripeAnimator.cancel();
        }
    }
}