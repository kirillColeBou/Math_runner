package com.example.diplom;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.SoundPool;

public class SoundManager {
    private SoundPool soundPool;
    private int correctSoundId;
    private int wrongSoundId;
    private boolean soundEnabled = true;

    public SoundManager(Context context) {
        AudioAttributes attributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();

        soundPool = new SoundPool.Builder()
                .setMaxStreams(3)
                .setAudioAttributes(attributes)
                .build();

        correctSoundId = soundPool.load(context, R.raw.correct, 1);
        wrongSoundId = soundPool.load(context, R.raw.wrong, 1);
    }

    public void setSoundEnabled(boolean enabled) {
        this.soundEnabled = enabled;
    }

    public boolean isSoundEnabled() {
        return soundEnabled;
    }

    public void playCorrect() {
        if (soundEnabled && soundPool != null) {
            soundPool.play(correctSoundId, 1.0f, 1.0f, 1, 0, 1.0f);
        }
    }

    public void playWrong() {
        if (soundEnabled && soundPool != null) {
            soundPool.play(wrongSoundId, 1.0f, 1.0f, 1, 0, 1.0f);
        }
    }

    public void release() {
        if (soundPool != null) {
            soundPool.release();
            soundPool = null;
        }
    }
}