package com.example.diplom;

import java.util.Random;
import java.util.Set;

public class MathGenerator {
    private Random random = new Random();
    private String difficulty;
    private Set<String> operations;

    public MathGenerator(String difficulty, Set<String> operations) {
        this.difficulty = difficulty;
        this.operations = operations;
    }

    public MathProblem generate() {
        String[] opsArray = operations.toArray(new String[0]);
        String operation = opsArray[random.nextInt(opsArray.length)];

        int a = 0, b = 0, correctAnswer = 0;

        switch (difficulty) {
            case "easy":
                switch (operation) {
                    case "addition":
                        a = random.nextInt(50) + 1;
                        b = random.nextInt(50) + 1;
                        correctAnswer = a + b;
                        break;
                    case "subtraction":
                        a = random.nextInt(100) + 1;
                        b = random.nextInt(a) + 1;
                        correctAnswer = a - b;
                        break;
                    case "multiplication":
                        a = random.nextInt(8) + 1;
                        b = random.nextInt(8) + 1;
                        correctAnswer = a * b;
                        break;
                    case "division":
                        b = random.nextInt(10) + 2;
                        correctAnswer = random.nextInt(10) + 1;
                        a = b * correctAnswer;
                        break;
                }
                break;

            case "medium":
                switch (operation) {
                    case "addition":
                        a = random.nextInt(200) + 50;
                        b = random.nextInt(200) + 50;
                        correctAnswer = a + b;
                        break;
                    case "subtraction":
                        a = random.nextInt(200) + 100;
                        b = random.nextInt(100) + 1;
                        correctAnswer = a - b;
                        break;
                    case "multiplication":
                        a = random.nextInt(20) + 5;
                        b = random.nextInt(15) + 2;
                        correctAnswer = a * b;
                        break;
                    case "division":
                        b = random.nextInt(12) + 2;
                        correctAnswer = random.nextInt(20) + 5;
                        a = b * correctAnswer;
                        break;
                }
                break;

            case "hard":
                switch (operation) {
                    case "addition":
                        a = random.nextInt(500) + 100;
                        b = random.nextInt(500) + 100;
                        correctAnswer = a + b;
                        break;
                    case "subtraction":
                        a = random.nextInt(500) + 200;
                        b = random.nextInt(200) + 50;
                        correctAnswer = a - b;
                        break;
                    case "multiplication":
                        a = random.nextInt(30) + 10;
                        b = random.nextInt(25) + 5;
                        correctAnswer = a * b;
                        break;
                    case "division":
                        b = random.nextInt(15) + 2;
                        correctAnswer = random.nextInt(30) + 10;
                        a = b * correctAnswer;
                        break;
                }
                break;
        }

        String symbol = getOperationSymbol(operation);
        String question = a + " " + symbol + " " + b + " = ?";

        // Генерируем гарантированно другой неправильный ответ
        int wrongAnswer = generateWrongAnswer(correctAnswer);

        // Случайно меняем левый и правый ответ
        boolean correctOnLeft = random.nextBoolean();
        String leftAnswer = correctOnLeft ? String.valueOf(correctAnswer) : String.valueOf(wrongAnswer);
        String rightAnswer = correctOnLeft ? String.valueOf(wrongAnswer) : String.valueOf(correctAnswer);
        boolean isLeftCorrect = correctOnLeft;

        return new MathProblem(question, leftAnswer, rightAnswer, isLeftCorrect);
    }

    private int generateWrongAnswer(int correct) {
        int wrongAnswer;
        int attempts = 0;

        do {
            int offset = random.nextInt(25) + 1;
            if (random.nextBoolean()) {
                wrongAnswer = correct + offset;
            } else {
                wrongAnswer = Math.max(1, correct - offset);
            }
            attempts++;
            if (attempts > 10) {
                wrongAnswer = correct + 10;
            }
        } while (wrongAnswer == correct);

        return wrongAnswer;
    }

    private String getOperationSymbol(String operation) {
        switch (operation) {
            case "addition": return "+";
            case "subtraction": return "−";
            case "multiplication": return "×";
            case "division": return "÷";
            default: return "+";
        }
    }

    public static class MathProblem {
        public String question;
        public String leftAnswer;
        public String rightAnswer;
        public boolean isLeftCorrect;

        MathProblem(String question, String leftAnswer, String rightAnswer, boolean isLeftCorrect) {
            this.question = question;
            this.leftAnswer = leftAnswer;
            this.rightAnswer = rightAnswer;
            this.isLeftCorrect = isLeftCorrect;
        }
    }
}