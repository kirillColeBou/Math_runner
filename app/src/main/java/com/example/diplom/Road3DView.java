package com.example.diplom;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import androidx.annotation.Nullable;

public class Road3DView extends View {
    private Paint roadPaint;
    private Paint stripePaint;
    private Paint edgePaint;
    private Paint centerLinePaint;
    private Paint flagPaint;
    private Paint flagBorderPaint;
    private Paint flagTextPaint;
    private float stripeOffset = 0;

    private String leftAnswer = "15";
    private String rightAnswer = "16";

    // Флаги для проверки ответа
    private boolean leftIsCorrect = false;
    private boolean rightIsCorrect = true;

    public Road3DView(Context context) {
        super(context);
        init();
    }

    public Road3DView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        roadPaint = new Paint();
        roadPaint.setColor(Color.parseColor("#FF3A3A3A"));
        roadPaint.setStyle(Paint.Style.FILL);
        roadPaint.setAntiAlias(true);

        centerLinePaint = new Paint();
        centerLinePaint.setColor(Color.parseColor("#FFE0E0E0"));
        centerLinePaint.setStyle(Paint.Style.STROKE);
        centerLinePaint.setStrokeWidth(3);
        centerLinePaint.setAntiAlias(true);

        stripePaint = new Paint();
        stripePaint.setColor(Color.argb(80, 255, 255, 255));
        stripePaint.setStyle(Paint.Style.FILL);
        stripePaint.setAntiAlias(true);

        edgePaint = new Paint();
        edgePaint.setColor(Color.parseColor("#FFFF5252"));
        edgePaint.setStyle(Paint.Style.STROKE);
        edgePaint.setStrokeWidth(5);
        edgePaint.setAntiAlias(true);

        flagPaint = new Paint();
        flagPaint.setColor(Color.parseColor("#FF1A2530"));
        flagPaint.setStyle(Paint.Style.FILL);
        flagPaint.setAntiAlias(true);

        flagBorderPaint = new Paint();
        flagBorderPaint.setColor(Color.parseColor("#FF00E676"));
        flagBorderPaint.setStyle(Paint.Style.STROKE);
        flagBorderPaint.setStrokeWidth(3);
        flagBorderPaint.setAntiAlias(true);

        flagTextPaint = new Paint();
        flagTextPaint.setColor(Color.parseColor("#FFFFFFFF"));
        flagTextPaint.setTextSize(40);
        flagTextPaint.setTextAlign(Paint.Align.CENTER);
        flagTextPaint.setAntiAlias(true);
        flagTextPaint.setFakeBoldText(true);
        flagTextPaint.setShadowLayer(4, 0, 2, Color.parseColor("#99000000"));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int w = getWidth();
        int h = getHeight();

        float topY = h * 0.45f;
        float bottomY = h * 0.85f;
        float vanishX = w / 2f;

        float topWidth = w * 0.06f;
        float bottomWidth = w * 0.95f;

        float leftTop = vanishX - topWidth / 2;
        float rightTop = vanishX + topWidth / 2;
        float leftBottom = vanishX - bottomWidth / 2;
        float rightBottom = vanishX + bottomWidth / 2;

        // Рисуем дорогу
        Path roadPath = new Path();
        roadPath.moveTo(leftTop, topY);
        roadPath.lineTo(rightTop, topY);
        roadPath.lineTo(rightBottom, bottomY);
        roadPath.lineTo(leftBottom, bottomY);
        roadPath.close();
        canvas.drawPath(roadPath, roadPaint);

        // Боковые линии
        canvas.drawLine(leftTop, topY, leftBottom, bottomY, edgePaint);
        canvas.drawLine(rightTop, topY, rightBottom, bottomY, edgePaint);

        // Центральная линия
        canvas.drawLine(vanishX, topY, vanishX, bottomY, centerLinePaint);

        // Линия проигрыша (красная, внизу)
        Paint loseLine = new Paint();
        loseLine.setColor(Color.parseColor("#FFFF5252"));
        loseLine.setStrokeWidth(4);
        loseLine.setAntiAlias(true);
        canvas.drawLine(leftBottom, bottomY, rightBottom, bottomY, loseLine);

        // Плашки с ответами — одна позиция от 0 (верх) до 1 (низ)
        float flagT = stripeOffset; // 0 = верх, 1 = низ
        flagT = Math.max(0, Math.min(1, flagT)); // Ограничиваем 0..1

        float flagY = topY + flagT * (bottomY - topY);
        float flagRoadWidth = topWidth + (bottomWidth - topWidth) * flagT;

        // Левый флажок
        float leftFlagX = vanishX - flagRoadWidth * 0.28f;
        drawFlagOnRoad(canvas, leftFlagX, flagY, leftAnswer, flagRoadWidth, flagT);

        // Правый флажок
        float rightFlagX = vanishX + flagRoadWidth * 0.28f;
        drawFlagOnRoad(canvas, rightFlagX, flagY, rightAnswer, flagRoadWidth, flagT);
    }

    private void drawFlagOnRoad(Canvas canvas, float x, float y, String text, float roadWidth, float t) {
        float scale = 0.5f + 0.5f * t;
        float flagWidth = roadWidth * 0.20f * scale;
        float flagHeight = 85 * scale;
        RectF flagRect = new RectF(
                x - flagWidth / 2, y - flagHeight / 2,
                x + flagWidth / 2, y + flagHeight / 2
        );
        canvas.drawRoundRect(flagRect, 10, 10, flagPaint);
        canvas.drawRoundRect(flagRect, 10, 10, flagBorderPaint);
        float textSize = 42 * scale;
        flagTextPaint.setTextSize(textSize);
        float textY = y + textSize / 3;
        canvas.drawText(text, x, textY, flagTextPaint);
    }

    public void setStripeOffset(float offset) {
        this.stripeOffset = offset;
        invalidate();
    }

    public void setAnswers(String left, String right) {
        this.leftAnswer = left;
        this.rightAnswer = right;
        invalidate();
    }
}