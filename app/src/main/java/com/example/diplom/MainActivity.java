package com.example.diplom;

import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private TextView btnStart;
    private TextView tvRecord;
    private TextView tvRunner;
    private View llLeaderboard;
    private View llSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        setupClickListeners();
        startIdleAnimation();
    }

    private void initViews() {
        btnStart = findViewById(R.id.btn_start);
        tvRecord = findViewById(R.id.tv_record);
        tvRunner = findViewById(R.id.tv_runner);
        llLeaderboard = findViewById(R.id.ll_leaderboard);
        llSettings = findViewById(R.id.ll_settings);
    }

    private void setupClickListeners() {
        // Кнопка НАЧАТЬ
        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Анимация нажатия
                Animation scaleDown = AnimationUtils.loadAnimation(
                        MainActivity.this, R.anim.button_press);
                v.startAnimation(scaleDown);

                // TODO: Запуск игры
                Toast.makeText(MainActivity.this,
                        "Игра запущена! 🏃", Toast.LENGTH_SHORT).show();
            }
        });

        // Топ игроков
        llLeaderboard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: Открыть экран лидеров
                Toast.makeText(MainActivity.this,
                        "Топ игроков", Toast.LENGTH_SHORT).show();
            }
        });

        // Настройки
        llSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: Открыть настройки
                Toast.makeText(MainActivity.this,
                        "Настройки", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void startIdleAnimation() {
        // Легкая анимация персонажа (покачивание)
        Animation idleAnim = AnimationUtils.loadAnimation(
                this, R.anim.runner_idle);
        tvRunner.startAnimation(idleAnim);
    }

    // Метод для обновления рекорда (можно вызывать из игры)
    public void updateRecord(int score) {
        tvRecord.setText(String.valueOf(score));

        // Добавляем анимацию при обновлении рекорда
        Animation recordAnim = AnimationUtils.loadAnimation(
                this, R.anim.record_update);
        tvRecord.startAnimation(recordAnim);
    }
}