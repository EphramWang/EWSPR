package android.serialport.reader.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.serialport.reader.model.DataPackage;
import android.serialport.reader.MainActivity;
import android.serialport.reader.utils.Utils;
import android.view.MotionEvent;
import android.view.View;

import java.util.LinkedList;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by ning on 17/8/31.
 */

public class ViewFrequencyChart extends View {

    public static final int X_AXIS_COUNT = 128;

    private int selection = 65;

    Context context;

    protected Paint paint = new Paint();

    private RectF mainRect;
    private RectF topRect;
    private RectF xAxisRect;
    private RectF legendRect1;
    private RectF chartRect1;
    private RectF legendRect2;
    private RectF chartRect2;


    public ViewFrequencyChart(Context context) {
        super(context);
        this.context = context;
    }

    @Override
    protected void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {
        super.onSizeChanged(width, height, oldWidth, oldHeight);
        if (width > 0 && height > 0) {
            mainRect = new RectF(Utils.dp2px(10), 0, width - Utils.dp2px(10), height);
            computeRect();
        }
    }

    private void computeRect() {
        float everyHeight = mainRect.height() / 5;
        float topBottomMargin = everyHeight / 2;
        topRect = new RectF(mainRect.left, mainRect.top + topBottomMargin, mainRect.right, mainRect.top + topBottomMargin + everyHeight);
        xAxisRect = new RectF(mainRect.left + mainRect.width() / 5, topRect.bottom, mainRect.right, topRect.bottom + everyHeight);
        legendRect1 = new RectF(mainRect.left, xAxisRect.bottom, mainRect.left + mainRect.width() / 5, xAxisRect.bottom + everyHeight);
        chartRect1 = new RectF(mainRect.left + mainRect.width() / 5, xAxisRect.bottom, mainRect.right, xAxisRect.bottom + everyHeight - 3);
        legendRect2 = new RectF(mainRect.left, legendRect1.bottom, mainRect.left + mainRect.width() / 5, legendRect1.bottom + everyHeight);
        chartRect2 = new RectF(mainRect.left + mainRect.width() / 5, chartRect1.bottom + 3, mainRect.right, chartRect1.bottom + everyHeight);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int rawX = (int) event.getX();
        int rawY = (int) event.getY();
        if (chartRect1.contains(rawX, rawY) || chartRect2.contains(rawX, rawY)) {
            selection = (int) ((rawX + 1 - chartRect1.left) * 128f / chartRect1.width());
        }
        if (selection < 1)
            selection = 1;
        if (selection > 128)
            selection = 128;
        postInvalidate();
        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawFrame(canvas);
        drawLabels(canvas);
        drawChart(canvas);
        invalidate();
    }

    private void drawChart(Canvas canvas) {
        //ArrayList<DataPackage> dataPackageArrayList = new ArrayList<>();
        //((MainActivity)context).dataPackageLinkedBlockingQueue.drainTo(dataPackageArrayList);
        CopyOnWriteArrayList<DataPackage> dataPackageArrayList = ((MainActivity)context).dataPackages4display;

        DataPackage rx2Pack = null;
        DataPackage rx3Pack = null;
        if (MainActivity.mWorkMode == MainActivity.WORK_MODE_BOTH_RX2_RX3) {
            if (dataPackageArrayList.size() > 1) {
                for (int i = dataPackageArrayList.size() - 1; (i > 0 && i > dataPackageArrayList.size() -3); i--) {
                    if (rx2Pack != null && rx3Pack != null)
                        break;
                    if (dataPackageArrayList.get(i).getWaveType() == 1) {
                        rx2Pack = dataPackageArrayList.get(i);
                    } else if (dataPackageArrayList.get(i).getWaveType() == 0) {
                        rx3Pack = dataPackageArrayList.get(i);
                    }
                }
            }

        } else if (MainActivity.mWorkMode == MainActivity.WORK_MODE_ONLY_RX2) {
            if (dataPackageArrayList.size() > 1)
                rx2Pack = dataPackageArrayList.get(dataPackageArrayList.size() - 1);
        } else if (MainActivity.mWorkMode == MainActivity.WORK_MODE_ONLY_RX3) {
            if (dataPackageArrayList.size() > 1)
                rx3Pack = dataPackageArrayList.get(dataPackageArrayList.size() - 1);
        }

        if (rx3Pack != null) {
            drawFreq(canvas, chartRect2, Color.YELLOW, rx3Pack);
        }
        if (rx2Pack != null) {
            drawFreq(canvas, chartRect1, Color.RED, rx2Pack);
        }

    }
    private void drawFreq(Canvas canvas, RectF rectF, int color, DataPackage dataPackage) {
        double[] spectrum = dataPackage.getHarmonicSpectrum();
        paint.setColor(color);
        for (int i = 0; i < spectrum.length; i++) {
            float x = rectF.left + rectF.width() * i / spectrum.length;
            float y = (float) (rectF.bottom - spectrum[i] / 100f * rectF.height());
            canvas.drawLine(x, rectF.bottom, x, y, paint);
        }
    }

    private void drawFrame(Canvas canvas) {
        paint.setAntiAlias(false);
        paint.setStyle(Paint.Style.FILL);

        //draw background
        paint.setColor(Color.BLACK);
        canvas.drawRect(mainRect, paint);

        paint.setColor(Color.GRAY);
        canvas.drawRect(chartRect1, paint);
        canvas.drawRect(chartRect2, paint);

    }

    private void drawLabels(Canvas canvas) {
        //top freq
        paint.setColor(Color.WHITE);
        paint.setTextAlign(Paint.Align.RIGHT);
        paint.setTextSize(Utils.dp2px(22));
        float selectFreq = (selection - 65) * 0.5f;
        String add = selectFreq >= 0 ? "+" : "-";
        canvas.drawText(add + selectFreq + "KHz", topRect.right - Utils.dp2px(10), topRect.bottom - Utils.dp2px(10), paint);

        paint.setTextSize(Utils.dp2px(11));
        paint.setTextAlign(Paint.Align.LEFT);
        canvas.drawText("-32KHz", xAxisRect.left, xAxisRect.bottom - Utils.dp2px(10), paint);
        paint.setTextAlign(Paint.Align.RIGHT);
        canvas.drawText("+32KHz", xAxisRect.right, xAxisRect.bottom - Utils.dp2px(10), paint);

        //draw vertical line for selection
        paint.setColor(Color.BLUE);
        float selectX = chartRect1.left + chartRect1.width() * selection / X_AXIS_COUNT;
        canvas.drawLine(selectX, chartRect1.top, selectX, chartRect2.bottom, paint);

        //legends
        float margin = Utils.dp2px(5);
        RectF newLegendRect1 = new RectF(legendRect1.left + margin, legendRect1.top + margin, legendRect1.right - margin, legendRect1.bottom - margin);
        RectF newLegendRect2 = new RectF(legendRect2.left + margin, legendRect2.top + margin, legendRect2.right - margin, legendRect2.bottom - margin);
        paint.setColor(Color.RED);
        canvas.drawRoundRect(newLegendRect1, Utils.dp2px(10), Utils.dp2px(10), paint);
        paint.setColor(Color.YELLOW);
        canvas.drawRoundRect(newLegendRect2, Utils.dp2px(10), Utils.dp2px(10), paint);

        paint.setColor(Color.BLACK);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(Utils.dp2px(13));
        if (MainActivity.mWorkMode == MainActivity.WORK_MODE_BOTH_RX2_RX3 || MainActivity.mWorkMode == MainActivity.WORK_MODE_ONLY_RX2) {
            paint.setColor(Color.BLACK);
        } else {
            paint.setColor(Color.GRAY);
        }
        canvas.drawText("2次谐波", (newLegendRect1.left + newLegendRect1.right) / 2, (newLegendRect1.top + newLegendRect1.bottom) / 2 + Utils.dp2px(10), paint);
        if (MainActivity.mWorkMode == MainActivity.WORK_MODE_BOTH_RX2_RX3 || MainActivity.mWorkMode == MainActivity.WORK_MODE_ONLY_RX3) {
            paint.setColor(Color.BLACK);
        } else {
            paint.setColor(Color.GRAY);
        }
        canvas.drawText("3次谐波", (newLegendRect2.left + newLegendRect2.right) / 2, (newLegendRect2.top + newLegendRect2.bottom) / 2 + Utils.dp2px(10), paint);
    }
}
