package com.example.diplom;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.Random;

public class PlayerManager {
    private static final String PREFS_NAME = "MathRunnerPlayer";
    private static final String KEY_PLAYER_NAME = "player_name";

    private static String[] adjectives = {
            "Быстрый", "Умный", "Хитрый", "Ловкий", "Мощный",
            "Точный", "Смелый", "Крутой", "Великий", "Грозный"
    };

    private static String[] nouns = {
            "Калькулятор", "Математик", "Решатель", "Гений", "Профессор",
            "Чемпион", "Волшебник", "Ниндзя", "Мастер", "Гигачад"
    };

    public static String getPlayerName(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String name = prefs.getString(KEY_PLAYER_NAME, "");

        if (name.isEmpty()) {
            name = generateRandomName();
            prefs.edit().putString(KEY_PLAYER_NAME, name).apply();
        }

        return name;
    }

    private static String generateRandomName() {
        Random random = new Random();
        String adj = adjectives[random.nextInt(adjectives.length)];
        String noun = nouns[random.nextInt(nouns.length)];
        int number = random.nextInt(1000);
        return adj + noun + number;
    }
}