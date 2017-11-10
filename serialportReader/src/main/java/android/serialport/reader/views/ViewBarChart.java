package android.serialport.reader.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.serialport.reader.R;
import android.serialport.reader.model.DataPackage;
import android.serialport.reader.MainActivity;
import android.serialport.reader.utils.Utils;
import android.view.View;

import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by ning on 17/8/31.
 */

public class ViewBarChart extends View {

    public static int drawTime = 0;

    Context context;

    protected Paint paint = new Paint();

    private RectF mainRect;
    private RectF bar1Rect;
    private RectF bar2Rect;
    private RectF bar3Rect;
    private RectF[] barRects = new RectF[3];


    float data1 = 0f;
    float data2 = 0f;
    float data3 = 0f;

    DataPackage cacheDatapackage;

    public ViewBarChart(Context context) {
        super(context);
        this.context = context;
    }


    @Override
    protected void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {
        super.onSizeChanged(width, height, oldWidth, oldHeight);
        if (width > 0 && height > 0) {
            mainRect = new RectF(0, 0, width, height);
            computeRect();
        }
    }

    private void computeRect() {
        float everyWidth = mainRect.width() / 7;

        float topMargin = mainRect.height() / 8;
        float bottomMargin = mainRect.height() / 6;

        bar1Rect = new RectF(everyWidth, mainRect.top + topMargin, everyWidth * 2, mainRect.bottom - bottomMargin);
        bar2Rect = new RectF(everyWidth * 3, mainRect.top + topMargin, everyWidth * 4, mainRect.bottom - bottomMargin);
        bar3Rect = new RectF(everyWidth * 5, mainRect.top + topMargin, everyWidth * 6, mainRect.bottom - bottomMargin);
        barRects[0] = bar1Rect;
        barRects[1] = bar2Rect;
        barRects[2] = bar3Rect;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (System.currentTimeMillis() - drawTime > 40) {
            drawFrame(canvas);
            drawBars(canvas);
        }
        invalidate();
    }

    private void drawFrame(Canvas canvas) {
        paint.setAntiAlias(false);
        paint.setStyle(Paint.Style.FILL);

        paint.setColor(Color.GRAY);
        for (RectF rect : barRects) {
            canvas.drawRect(rect, paint);
        }

        paint.setColor(Color.BLACK);
        float margin = Utils.dp2px(3);
        for (RectF rect : barRects) {
            canvas.drawRect(new RectF(rect.left + margin, rect.top + margin, rect.right - margin, rect.bottom - margin), paint);
        }

        paint.setColor(Color.GRAY);
        margin = Utils.dp2px(13);
        for (RectF rect : barRects) {
            canvas.drawRoundRect(new RectF(rect.left + margin, rect.top + margin, rect.right - margin, rect.bottom - margin), Utils.dp2px(10), Utils.dp2px(10), paint);
        }

        //draw legends
        float top = bar1Rect.top + margin;
        float bottom = bar1Rect.bottom - margin;
        float left = bar1Rect.right - Utils.dp2px(5);
        float everyHeight = (bottom - top) / 20;
        paint.setColor(Color.GRAY);
        paint.setTextSize(Utils.dp2px(12));
        for (int i = 0; i < 21; i++) {
            float lineWidth = i % 2 == 0 ? Utils.dp2px(15) : Utils.dp2px(8);
            float y = top + everyHeight * i;
            canvas.drawLine(left, y, left + lineWidth, y, paint);

            if (i % 2 == 0)
                canvas.drawText((100 - 5 * i) + "", left + lineWidth + Utils.dp2px(3), y + Utils.dp2px(5), paint);
        }

        float left2 = bar2Rect.right - Utils.dp2px(5);
        float right2 = bar3Rect.left + Utils.dp2px(5);
        float textX = (bar2Rect.right + bar3Rect.left) / 2;
        paint.setTextAlign(Paint.Align.CENTER);
        for (int i = 10; i < 21; i++) {
            float lineWidth = i % 2 == 0 ? Utils.dp2px(15) : Utils.dp2px(8);
            float y = top + everyHeight * i;
            canvas.drawLine(left2, y, left2 + lineWidth, y, paint);
            canvas.drawLine(right2, y, right2 - lineWidth, y, paint);

            if (i % 2 == 0)
                canvas.drawText((100 - 5 * i) + "", textX, y + Utils.dp2px(5), paint);
        }

        //draw bar legend
        float legendTop = bar1Rect.bottom + mainRect.height() / 40;
        float legendBottom = mainRect.bottom - mainRect.height() / 40;

        RectF legend1Rect = new RectF(bar1Rect.left - 10, legendTop, bar1Rect.right + 10, legendBottom);
        RectF legend2Rect = new RectF(bar2Rect.left - 10, legendTop, bar2Rect.right + 10, legendBottom);
        RectF legend3Rect = new RectF(bar3Rect.left - 10, legendTop, bar3Rect.right + 10, legendBottom);
        paint.setColor(Color.GREEN);
        canvas.drawRoundRect(legend1Rect, Utils.dp2px(5), Utils.dp2px(5), paint);
        paint.setColor(Color.RED);
        canvas.drawRoundRect(legend2Rect, Utils.dp2px(5), Utils.dp2px(5), paint);
        paint.setColor(Color.YELLOW);
        canvas.drawRoundRect(legend3Rect, Utils.dp2px(5), Utils.dp2px(5), paint);

        paint.setColor(Color.BLACK);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(Utils.dp2px(13));
        canvas.drawText("发射基波", (legend1Rect.left + legend1Rect.right) / 2, (legend1Rect.top + legend1Rect.bottom) / 2 + Utils.dp2px(5), paint);
        canvas.drawText("2次谐波", (legend2Rect.left + legend2Rect.right) / 2, (legend2Rect.top + legend2Rect.bottom) / 2 + Utils.dp2px(5), paint);
        canvas.drawText("3次谐波", (legend3Rect.left + legend3Rect.right) / 2, (legend3Rect.top + legend3Rect.bottom) / 2 + Utils.dp2px(5), paint);

    }

    private void drawBars(Canvas canvas) {
        CopyOnWriteArrayList<DataPackage> dataPackageArrayList = ((MainActivity)context).dataPackages4display;
        DataPackage dataPackage;
        if (dataPackageArrayList.size() < 1 && cacheDatapackage == null)
            return;
        else if (dataPackageArrayList.size() < 1)
            dataPackage = cacheDatapackage;
        else {
            dataPackage = dataPackageArrayList.get(dataPackageArrayList.size() - 1);
            cacheDatapackage = dataPackage;
        }
        data1 = dataPackage.getSettingPower() / 10f;
        if (dataPackage.getSettingWorkMode() == 3) {//2次3次谐波轮流测
            if (dataPackage.getWaveType() == 0) {
                data3 = dataPackage.getWavePower() / 100f;
            } else if (dataPackage.getWaveType() == 1)
                data2 = dataPackage.getWavePower() / 100f;
        } else if (dataPackage.getSettingWorkMode() == 2) {//仅测3次谐波
            if (dataPackage.getWaveType() == 0) {
                data3 = dataPackage.getWavePower() / 100f;
                data2 = 0f;
            }
        } else if (dataPackage.getSettingWorkMode() == 1) {//仅测2次谐波
            if (dataPackage.getWaveType() == 1) {
                data2 = dataPackage.getWavePower() / 100f;
                data3 = 0f;
            }
        } else {//0：射频待机
            data2 = 0f;
            data3 = 0f;
        }

        float margin = Utils.dp2px(13);
        paint.setColor(getResources().getColor(R.color.colorRX1));
        canvas.drawRoundRect(new RectF(barRects[0].left + margin, barRects[0].top + (barRects[0].height() - margin * 2) * (1 - data1) + margin, barRects[0].right - margin, barRects[0].bottom - margin), Utils.dp2px(10), Utils.dp2px(10), paint);
        paint.setColor(getResources().getColor(R.color.colorRX2));
        canvas.drawRoundRect(new RectF(barRects[1].left + margin, barRects[1].top + (barRects[1].height() - margin * 2) * (1 - data2) + margin, barRects[1].right - margin, barRects[1].bottom - margin), Utils.dp2px(10), Utils.dp2px(10), paint);
        paint.setColor(getResources().getColor(R.color.colorRX3));
        canvas.drawRoundRect(new RectF(barRects[2].left + margin, barRects[2].top + (barRects[2].height() - margin * 2) * (1 - data3)+ margin, barRects[2].right - margin, barRects[2].bottom - margin), Utils.dp2px(10), Utils.dp2px(10), paint);

    }

}