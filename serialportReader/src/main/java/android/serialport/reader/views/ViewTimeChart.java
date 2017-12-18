package android.serialport.reader.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.serialport.reader.MainActivity;
import android.serialport.reader.R;
import android.serialport.reader.model.DataPackage;
import android.serialport.reader.utils.Utils;
import android.view.View;

import java.util.LinkedList;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by ning on 17/8/31.
 */

public class ViewTimeChart extends View {

    public static final float MAX_POWER = 100f;//y轴最高点

    Context context;

    protected Paint paint = new Paint();

    private RectF mainRect;
    private RectF barRect;
    private RectF barAxisRect;
    private RectF chartRect;
    private RectF chartTopRect;
    private RectF chartBottomRect;

    float data1 = 0f;

    public ViewTimeChart(Context context) {
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
        barRect = new RectF(Utils.dp2px(10), mainRect.top + Utils.dp2px(20), Utils.dp2px(10) + mainRect.width() / 6, mainRect.bottom - Utils.dp2px(20));
        barAxisRect = new RectF(barRect.right, mainRect.top + Utils.dp2px(20), Utils.dp2px(10) + mainRect.width() / 6 * 2, mainRect.bottom - Utils.dp2px(20));
        float everyHeight = mainRect.height() / 7;
        chartRect = new RectF(barAxisRect.right, mainRect.top + everyHeight, mainRect.right - Utils.dp2px(20), mainRect.bottom - everyHeight);
        chartTopRect = new RectF(chartRect.left, mainRect.top, chartRect.right, chartRect.top);
        chartBottomRect = new RectF(chartRect.left, chartRect.bottom, chartRect.right, mainRect.bottom);

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawFrame(canvas);
        drawTimeChart(canvas);
        invalidate();
    }

    private void drawTimeChart(Canvas canvas) {
//        ArrayList<DataPackage> dataPackageArrayList = new ArrayList<>();
//        ((MainActivity)context).dataPackageLinkedBlockingQueue.drainTo(dataPackageArrayList);
        //CopyOnWriteArrayList<DataPackage> dataPackageArrayList = (CopyOnWriteArrayList<DataPackage>) ((MainActivity)context).dataPackages4display.clone();
        CopyOnWriteArrayList<Float> power2DbFiltList4Disp = (CopyOnWriteArrayList<Float>) ((MainActivity)context).power2DbFiltList.clone();
        CopyOnWriteArrayList<Float> power3DbFiltList4Disp = (CopyOnWriteArrayList<Float>) ((MainActivity)context).power3DbFiltList.clone();


        Path pathRX2 = new Path();
        Path pathRX3 = new Path();
        Path pathBase = new Path();

        int i = 0;
        float x = 0;
        float y = 0;
        float power_temp = 0;
        int dataCount = MainActivity.maxDisplayLength;
        int enb_rx2 = 1;
        int enb_rx3 = 1;
        if( MainActivity.mWorkMode == MainActivity.WORK_MODE_ONLY_RX2 )
            enb_rx3 = 0;
        else if( MainActivity.mWorkMode == MainActivity.WORK_MODE_ONLY_RX3 )
            enb_rx2 = 0;

        //绘制二次谐波
        if(enb_rx2==1) {
            if (power2DbFiltList4Disp.size() < 1)
                return;
            for (i = power2DbFiltList4Disp.size() - 1; i >= 0; i--) {
                x = (dataCount - (power2DbFiltList4Disp.size() - 1) + i) * chartRect.width() / dataCount + chartRect.left;
                if (x < chartRect.left)
                    break;
                power_temp = 2f + 3f * MainActivity.Gain2 *(power2DbFiltList4Disp.get(i) - MainActivity.TH_base2);
                if (power_temp <= 2f)
                    power_temp = 2f;
                else if (power_temp > 98f)
                    power_temp = 98f;

                y = chartRect.top + chartRect.height() * (1f - power_temp / MAX_POWER);

                if (pathRX2.isEmpty()) {
                    pathRX2.moveTo(x, y);
                } else {
                    pathRX2.lineTo(x, y);
                }
            }
        }

        //绘制三次谐波
        if(enb_rx3==1) {
            if (power3DbFiltList4Disp.size() < 1)
                return;
            for (i = power3DbFiltList4Disp.size() - 1; i >= 0; i--) {
                x = (dataCount - (power3DbFiltList4Disp.size() - 1) + i) * chartRect.width() / dataCount + chartRect.left;
                if (x < chartRect.left)
                    break;
                power_temp = 2f + 3f * MainActivity.Gain3 * (power3DbFiltList4Disp.get(i) - MainActivity.TH_base3);
                if (power_temp <= 2f)
                    power_temp = 2f;
                else if (power_temp > 98f)
                    power_temp = 98f;

                y = chartRect.top + chartRect.height() * (1f - power_temp / MAX_POWER);

                if (pathRX3.isEmpty()) {
                    pathRX3.moveTo(x, y);
                } else {
                    pathRX3.lineTo(x, y);
                }
            }
        }


        /*for (int i = dataPackageArrayList.size() - 1; i >= 0; i--) {
            int dataCount = MainActivity.mWorkMode == MainActivity.WORK_MODE_BOTH_RX2_RX3 ? MainActivity.maxDisplayLength * 2 : MainActivity.maxDisplayLength;
            float x = (dataCount - (dataPackageArrayList.size() - 1) + i) * chartRect.width() / dataCount + chartRect.left;
            if (x < chartRect.left)
                break;

            float y1 = 0, y2 = 0;
            //float y3 = chartRect.top + chartRect.height() * (1f - dataPackageArrayList.get(i).getSettingPower() / 10f);
            float y3 = chartRect.top + chartRect.height() * (1f -MainActivity.TH_base2 / MAX_POWER);

            if (pathBase.isEmpty()) {
                pathBase.moveTo(x, y3);
            } else {
                pathBase.lineTo(x, y3);
            }
            if (dataPackageArrayList.get(i).getWaveType() == 1) {//二次
                y1 = chartRect.top + chartRect.height() * (1f -dataPackageArrayList.get(i).getWavePower() / MAX_POWER);

                if (pathRX2.isEmpty()) {
                    pathRX2.moveTo(x, y1);
                } else {
                    pathRX2.lineTo(x, y1);
                }
            } else if (dataPackageArrayList.get(i).getWaveType() == 0) {//三次
                y2 = chartRect.top + chartRect.height() * (1f - (dataPackageArrayList.get(i).getWavePower() + MainActivity.TH_base2 - MainActivity.TH_base3) / MAX_POWER);

                if (pathRX3.isEmpty()) {
                    pathRX3.moveTo(x, y2);
                } else {
                    pathRX3.lineTo(x, y2);
                }
            }
        }*/

        //draw bar
        float margin = Utils.dp2px(13);
        paint.setColor(Color.GREEN);
        data1 = MainActivity.mPower / 10f;
        canvas.drawRoundRect(new RectF(barRect.left + margin, barRect.top + (barRect.height() - margin * 2) * (1 - data1) + margin, barRect.right - margin, barRect.bottom - margin), Utils.dp2px(10), Utils.dp2px(10), paint);

        //draw time line
        paint.setStyle(Paint.Style.STROKE);
        if (MainActivity.mWorkMode == MainActivity.WORK_MODE_ONLY_RX2) {
            paint.setColor(Color.RED);
            canvas.drawPath(pathRX2, paint);
            //paint.setColor(Color.BLUE);
            //canvas.drawPath(pathBase, paint);
        } else if (MainActivity.mWorkMode == MainActivity.WORK_MODE_ONLY_RX3) {
            paint.setColor(Color.YELLOW);
            canvas.drawPath(pathRX3, paint);
            //paint.setColor(Color.BLUE);
            //canvas.drawPath(pathBase, paint);
        } else if (MainActivity.mWorkMode == MainActivity.WORK_MODE_BOTH_RX2_RX3) {
            paint.setColor(Color.RED);
            canvas.drawPath(pathRX2, paint);
            paint.setColor(Color.YELLOW);
            canvas.drawPath(pathRX3, paint);
            //paint.setColor(Color.BLUE);
            //canvas.drawPath(pathBase, paint);
        } else {
            //do nothing
        }


    }

    private void drawFrame(Canvas canvas) {
        paint.setAntiAlias(false);
        paint.setStyle(Paint.Style.FILL);

        //draw bar
        paint.setColor(Color.GRAY);
        canvas.drawRect(barRect, paint);

        paint.setColor(Color.BLACK);
        float margin = Utils.dp2px(3);
        canvas.drawRect(new RectF(barRect.left + margin, barRect.top + margin, barRect.right - margin, barRect.bottom - margin), paint);

        paint.setColor(Color.GRAY);
        margin = Utils.dp2px(13);
        canvas.drawRoundRect(new RectF(barRect.left + margin, barRect.top + margin, barRect.right - margin, barRect.bottom - margin), Utils.dp2px(10), Utils.dp2px(10), paint);

        //draw bar legends
        float top = barRect.top + margin;
        float bottom = barRect.bottom - margin;
        float left = barRect.right - Utils.dp2px(5);
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

        //draw time interval
        paint.setColor(Color.WHITE);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(Utils.dp2px(22));
        String timeLength = MainActivity.maxDisplayLength / 50 + "秒时长";
        canvas.drawText(timeLength, (chartTopRect.left + chartTopRect.right) / 2, chartTopRect.bottom - Utils.dp2px(10), paint);

        //draw legends
        float everyWidth = chartBottomRect.width() / 17;
        float legendMargin = chartBottomRect.height() / 8;
        RectF legend1Rect = new RectF(chartBottomRect.left, chartBottomRect.top + legendMargin, chartBottomRect.left + everyWidth * 5, chartBottomRect.bottom - legendMargin);
        RectF legend2Rect = new RectF(chartBottomRect.left + everyWidth * 6, chartBottomRect.top + legendMargin, chartBottomRect.left + everyWidth * 11, chartBottomRect.bottom - legendMargin);
        RectF legend3Rect = new RectF(chartBottomRect.left + everyWidth * 12, chartBottomRect.top + legendMargin, chartBottomRect.left + everyWidth * 17, chartBottomRect.bottom - legendMargin);
        paint.setColor(Color.RED);
        canvas.drawRoundRect(legend1Rect, Utils.dp2px(5), Utils.dp2px(5), paint);
        paint.setColor(Color.YELLOW);
        canvas.drawRoundRect(legend2Rect, Utils.dp2px(5), Utils.dp2px(5), paint);
        paint.setColor(Color.BLUE);
        canvas.drawRoundRect(legend3Rect, Utils.dp2px(5), Utils.dp2px(5), paint);

        paint.setColor(Color.BLACK);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(Utils.dp2px(13));
        canvas.drawText("基准门限", (legend3Rect.left + legend3Rect.right) / 2, (legend3Rect.top + legend3Rect.bottom) / 2 + Utils.dp2px(5), paint);
        if (MainActivity.mWorkMode == MainActivity.WORK_MODE_BOTH_RX2_RX3 || MainActivity.mWorkMode == MainActivity.WORK_MODE_ONLY_RX2) {
            paint.setColor(Color.BLACK);
        } else {
            paint.setColor(Color.GRAY);
        }
        canvas.drawText("2次谐波", (legend1Rect.left + legend1Rect.right) / 2, (legend1Rect.top + legend1Rect.bottom) / 2 + Utils.dp2px(5), paint);
        if (MainActivity.mWorkMode == MainActivity.WORK_MODE_BOTH_RX2_RX3 || MainActivity.mWorkMode == MainActivity.WORK_MODE_ONLY_RX3) {
            paint.setColor(Color.BLACK);
        } else {
            paint.setColor(Color.GRAY);
        }
        canvas.drawText("3次谐波", (legend2Rect.left + legend2Rect.right) / 2, (legend2Rect.top + legend2Rect.bottom) / 2 + Utils.dp2px(5), paint);


        //draw time chart
        paint.setColor(Color.GRAY);
        canvas.drawRect(chartRect, paint);

    }
}
