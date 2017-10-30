package android.serialport.reader;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.serialport.reader.model.DataPackage;
import android.serialport.reader.model.ReceivedData;
import android.serialport.reader.test.MainMenu;
import android.serialport.reader.utils.DataConstants;
import android.serialport.reader.views.ViewBarChart;
import android.serialport.reader.views.ViewFrequencyChart;
import android.serialport.reader.views.ViewSettings;
import android.serialport.reader.model.CommandPackage;
import android.serialport.reader.utils.Utils;
import android.serialport.reader.views.BatteryView;
import android.serialport.reader.views.ViewMain;
import android.serialport.reader.views.ViewTimeChart;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by ning on 17/8/31.
 */

public class MainActivity extends SerialPortActivity implements View.OnClickListener {

    //界面类型
    public static final int VIEW_TYPE_HOME = 1;
    public static final int VIEW_TYPE_BAR_CHART = 2;
    public static final int VIEW_TYPE_TIME_CHART = 3;
    public static final int VIEW_TYPE_FREQUENCY_CHART = 4;
    public static final int VIEW_TYPE_SETTINGS = 5;

    //工作模式
    public static final byte WORK_MODE_STANDBY = 0x00;
    public static final byte WORK_MODE_ONLY_RX2 = 0x01;
    public static final byte WORK_MODE_ONLY_RX3 = 0x02;
    public static final byte WORK_MODE_BOTH_RX2_RX3 = 0x03;

    private int currentViewType = 1;//当前界面类型

    private TextView tvTime;

    private LinearLayout llCenterLayout;

    //底部按钮
    private ImageView btBottomLeft;
    private ImageView btBottomCenter;
    private ImageView btBottomRight;
    //顶部按钮
    private ImageView btTopLight;
    private ImageView btTopCapture;
    private BatteryView topBattery;
    //几个切换的view
    private View llMainView;
    private View llBarChartView;
    private View llTimeChartView;
    private View llFreqChartView;
    private View llSettingsView;

    //系统的一些参数
    public static byte mSensitivity = 0x05;//灵敏度设置
    public static byte mWorkMode = WORK_MODE_BOTH_RX2_RX3;//工作状态
    public static byte mPower = 0x00;//功率 发射基波 uint8
    public static byte mSZBZPL = 0x00;//数字本振频率
    public static byte mSZFDZY = 0x00;//数字放大增益

    public static boolean isCheckSum = false;//是否校验
    public static String filePath = "/datapack";//数据包保存的路径
    public static String screenshotPath = "/datapackScreenShot";//截图文件存放文件夹的路径
    public static int datapackNumToSaveInFile = 500;//多少个数据包保存到一个文件
    public static int maxDisplayLength = 500; //时域波形显示长度

    private static int currentVolume = 0;

    //数据
    public CopyOnWriteArrayList<DataPackage> dataPackages4display = new CopyOnWriteArrayList<>();//显示的缓存

    public LinkedBlockingQueue<ReceivedData> receivedDataLinkedBlockingQueue = new LinkedBlockingQueue<>();//读取到的串口数据缓冲区
    public LinkedBlockingQueue<DataPackage> dataPackageLinkedBlockingQueue = new LinkedBlockingQueue<>();//读取到的数据


    ReadSerialPortThread readSerialPortThread;
    SaveDataPackToStorageThread saveDataThread;

    Timer timer = new Timer();
    TimerTask timerTask = new TimerTask() {
        public void run() {
            EventBus.getDefault().post(new updateTimeEvent());
            //query battery
            EventBus.getDefault().post(new MainActivity.sendDataEvent(DataConstants.getControlCommandBytes(DataConstants.command_send_battery, (byte)0x00)));
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_main);
        tvTime = (TextView) findViewById(R.id.top_time_tv);
        tvTime.setOnClickListener(this);
        topBattery = (BatteryView) findViewById(R.id.top_battery);
        topBattery.setOnClickListener(this);

        llCenterLayout = (LinearLayout) findViewById(R.id.center_layout);

        //底部按钮
        btBottomLeft = (ImageView) findViewById(R.id.bottom_button_left);
        btBottomCenter = (ImageView) findViewById(R.id.bottom_button_center);
        btBottomRight = (ImageView) findViewById(R.id.bottom_button_right);
        btBottomLeft.setOnClickListener(this);
        btBottomCenter.setOnClickListener(this);
        btBottomRight.setOnClickListener(this);
        //顶部按钮
        btTopLight = (ImageView) findViewById(R.id.top_light_button);
        btTopCapture = (ImageView) findViewById(R.id.top_capture_button);
        btTopLight.setOnClickListener(this);
        btTopCapture.setOnClickListener(this);

        initViews();

        llCenterLayout.addView(llMainView);

        setCurrentTime();

        readSerialPortThread = new ReadSerialPortThread();
        readSerialPortThread.start();
        saveDataThread = new SaveDataPackToStorageThread();
        saveDataThread.start();

        EventBus.getDefault().register(this);

        timer.schedule(timerTask, 10000, 10000);

        initParams();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onEvent(Object event) { //接收方法  在发关事件的线程接收
        if (event instanceof sendDataEvent) {
            sendBytes(((sendDataEvent) event).sendBytes);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(Object event) {
        if (event instanceof receiveCommandPackageEvent) {
            processCommandPackage(((receiveCommandPackageEvent) event).commandPackage);
        } else if (event instanceof updateTimeEvent) {
            setCurrentTime();
        }
    }

    @Override
    protected void onDataReceived(byte[] buffer, int size) {
        //Log.e("www", "onDataReceived " + buffer + " size" + size + " buffer.length" + buffer.length);
        try {
            receivedDataLinkedBlockingQueue.put(new ReceivedData(buffer, size, System.currentTimeMillis()));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    public void sendBytes(byte[] bytes) {
        Log.e("www", "sendBytes " + bytes + " bytes.length" + bytes.length);
        try {
            if (mOutputStream != null) {
                mOutputStream.write(bytes);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initViews() {
        llMainView = new ViewMain(getBaseContext(), this);
        llBarChartView = new ViewBarChart(this);
        llTimeChartView = new ViewTimeChart(this);
        llFreqChartView = new ViewFrequencyChart(this);
        llSettingsView = new ViewSettings(this);
    }

    private void refreshView(int viewType) {
        if (viewType != currentViewType) {
            llCenterLayout.removeAllViews();
            switch (viewType) {
                case VIEW_TYPE_HOME:
                    if (llMainView != null) {
                        llCenterLayout.addView(llMainView);
                    }
                    break;
                case VIEW_TYPE_BAR_CHART:
                    if (llBarChartView != null) {
                        llCenterLayout.addView(llBarChartView);
                    }
                    break;
                case VIEW_TYPE_TIME_CHART:
                    if (llTimeChartView != null) {
                        llCenterLayout.addView(llTimeChartView);
                    }
                    break;
                case VIEW_TYPE_FREQUENCY_CHART:
                    if (llFreqChartView != null) {
                        llCenterLayout.addView(llFreqChartView);
                    }
                    break;
                case VIEW_TYPE_SETTINGS:
                    if (llSettingsView != null) {
                        llCenterLayout.addView(llSettingsView);
                    }
                    break;
            }
            currentViewType = viewType;
            refreshBottomView(viewType);
        }
    }

    private void refreshBottomView(int viewType) {
        switch (viewType) {
            case VIEW_TYPE_HOME:
            case VIEW_TYPE_SETTINGS:
                //右下按钮设置成 设置
                btBottomRight.setImageResource(R.drawable.settings);
                break;
            case VIEW_TYPE_BAR_CHART:
            case VIEW_TYPE_TIME_CHART:
            case VIEW_TYPE_FREQUENCY_CHART:
                //右下按钮设置成 音量加减
                btBottomRight.setImageResource(R.drawable.music);
                break;
        }
    }

    public void setCurrentTime() {
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm");
        Date curDate = new Date(System.currentTimeMillis());
        String str = formatter.format(curDate);
        tvTime.setText(str);
    }

    private void initParams() {
        String packageName = getPackageName();
        SharedPreferences sp = getSharedPreferences(packageName + "_preferences", MODE_PRIVATE);

        int sensitivity = Integer.decode(sp.getString("SENSITIVITY", "5"));
        mSensitivity = (byte) sensitivity;
        int power = Integer.decode(sp.getString("POWER", "5"));
        mPower = (byte) power;
        int SZBZPL = Integer.decode(sp.getString("SZBZPL", "0"));
        mSZBZPL = (byte) SZBZPL;
        int SZFDZY = Integer.decode(sp.getString("SZFDZY", "0"));
        mSZFDZY = (byte) SZFDZY;

        EventBus.getDefault().post(new MainActivity.sendDataEvent(DataConstants.getControlCommandBytes(DataConstants.command_send_workmode, mWorkMode)));
        EventBus.getDefault().post(new MainActivity.sendDataEvent(DataConstants.getControlCommandBytes(DataConstants.command_send_sensitivity, mSensitivity)));
        EventBus.getDefault().post(new MainActivity.sendDataEvent(DataConstants.getControlCommandBytes(DataConstants.command_send_power, mPower)));
        EventBus.getDefault().post(new MainActivity.sendDataEvent(DataConstants.getControlCommandBytes(DataConstants.command_send_szbzpl, mSZBZPL)));
        EventBus.getDefault().post(new MainActivity.sendDataEvent(DataConstants.getControlCommandBytes(DataConstants.command_send_szfdzy, mSZFDZY)));

        isCheckSum = sp.getString("checksum", "0").equals("校验");
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.bottom_button_left:
                setVolume();
                break;
            case R.id.bottom_button_center:
                refreshView(VIEW_TYPE_HOME);
                break;
            case R.id.bottom_button_right:
                if (currentViewType == VIEW_TYPE_HOME || currentViewType == VIEW_TYPE_SETTINGS)
                    startActivity(new Intent(MainActivity.this, PrefActivity.class));
                else
                    ShowVolumeSelect();
                break;
            case R.id.top_light_button:
                setBrightness();
                break;
            case R.id.top_capture_button:
                saveCurrentImage();
                break;
            case R.id.img1:
                refreshView(VIEW_TYPE_BAR_CHART);
                break;
            case R.id.img2:
                refreshView(VIEW_TYPE_TIME_CHART);
                break;
            case R.id.img3:
                refreshView(VIEW_TYPE_FREQUENCY_CHART);
                break;
            case R.id.img4:
                refreshView(VIEW_TYPE_SETTINGS);
                break;
            case R.id.top_time_tv:
            case R.id.top_battery:
                startActivity(new Intent(MainActivity.this, MainMenu.class));
                break;
        }
    }

    /**
     * 按亮度按钮循环设置亮度
     */
    private void setBrightness() {
        Activity context = MainActivity.this;
        WindowManager.LayoutParams lp = context.getWindow().getAttributes();
        float currentBrightness = lp.screenBrightness;
        // 0, 64, 128, 192, 256
        float nextBrightness = currentBrightness + 0.2f;
        if (nextBrightness > 1f)
            nextBrightness -= 1f;
        setBrightness(nextBrightness);
    }

    /**
     * 设置亮度
     * 通过WindowManager去设置当前界面的亮度
     * 亮度值是0~1，数据类型为float型。
     * @param brightness
     */
    private void setBrightness(float brightness) {
        Activity context = MainActivity.this;
        WindowManager.LayoutParams lp = context.getWindow().getAttributes();
        lp.screenBrightness = brightness;
        context.getWindow().setAttributes(lp);
    }

    /**
     * 设置亮度 unused
     * 通过修改系统数据库来设置亮度
     * @param brightness
     */
    public void setBrightnessAll(int brightness) {
        Activity activity = MainActivity.this;
        Uri uri = Settings.System.getUriFor(Settings.System.SCREEN_BRIGHTNESS);
        Settings.System.putInt(activity.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, brightness);
        activity.getContentResolver().notifyChange(uri, null);
    }

    private void setVolume() {
        AudioManager audioManager = (AudioManager) MainActivity.this.getSystemService(Context.AUDIO_SERVICE);
        int currentVol = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        int maxVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

        if (currentVol == 0) {
            setVolume(currentVolume);
        } else {
            currentVolume = currentVol;
            setVolume(0);
        }
    }

    /**
     * 调整音量
     * @param volume
     */
    private void setVolume(int volume) {
        AudioManager audioManager = (AudioManager) MainActivity.this.getSystemService(Context.AUDIO_SERVICE);
        //audioManager.adjustVolume(AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI);
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume, AudioManager.FLAG_SHOW_UI);
    }

    public void setVolumeUp() {
        AudioManager audioManager = (AudioManager) MainActivity.this.getSystemService(Context.AUDIO_SERVICE);
        int currentVol = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        int maxVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

        int newVol = currentVol + maxVol / 10;
        if (newVol > maxVol)
            newVol = maxVol;

        setVolume(newVol);
    }

    public void setVolumeDown() {
        AudioManager audioManager = (AudioManager) MainActivity.this.getSystemService(Context.AUDIO_SERVICE);
        int currentVol = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        int maxVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

        int newVol = currentVol - maxVol / 10;
        if (newVol < 0)
            newVol = 0;

        setVolume(newVol);
    }

    public void ShowVolumeSelect() {
        AudioManager audioManager = (AudioManager) MainActivity.this.getSystemService(Context.AUDIO_SERVICE);
        int currentVol = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        int maxVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

        setVolume(currentVol);
    }

    /**
     * 静音 unused
     * @param mute
     */
    private void muteVolume(boolean mute) {
        AudioManager audioManager = (AudioManager) MainActivity.this.getSystemService(Context.AUDIO_SERVICE);
        audioManager.setStreamMute(AudioManager.STREAM_MUSIC, mute);
    }

    /**
     * 截屏
     */
    private void saveCurrentImage() {
        //获取当前屏幕的大小
        //int width = getWindow().getDecorView().getRootView().getWidth();
        //int height = getWindow().getDecorView().getRootView().getHeight();
        //找到当前页面的跟布局
        View view =  getWindow().getDecorView().getRootView();
        //设置缓存
        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache();
        //从缓存中获取当前屏幕的图片
        Bitmap temBitmap = view.getDrawingCache();

        //输出到sd卡
        File dir = new File(Environment.getExternalStorageDirectory() + screenshotPath );
        if (!dir.exists())
            dir.mkdir();
        File file = new File(Environment.getExternalStorageDirectory() + screenshotPath + "/" + System.currentTimeMillis() + ".png");
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
            FileOutputStream foStream = new FileOutputStream(file);
            temBitmap.compress(Bitmap.CompressFormat.PNG, 80, foStream);
            foStream.flush();
            foStream.close();

            Toast.makeText(getBaseContext(), "截屏成功!", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.i("Show", e.toString());
        }

        view.destroyDrawingCache();//释放缓存占用的资源
    }


    private void saveDataPackagesToStorage(ArrayList<DataPackage> listToBeSaved) {
        Log.e("www", "saveDataPackagesToStorage, length=" + listToBeSaved.size());
        String dirName = Environment.getExternalStorageDirectory() + filePath;
        File dir = new File(dirName);
        if (!dir.exists())
            dir.mkdir();

        Date date = new Date(listToBeSaved.get(0).timestamp);
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
        String dateString = formatter.format(date);
        String subDirName = dateString.substring(0, 16);
        File subDir = new File(dirName + "/" + subDirName);
        if (!subDir.exists())
            subDir.mkdir();

        String filename =  dirName + "/" + subDirName + "/" + dateString + ".dat";

        File file = new File(filename);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(file);
                for (DataPackage dataPack: listToBeSaved) {
                    fos.write(dataPack.dataBytes);
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (fos != null)
                        fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void processCommandPackage(CommandPackage commandPackage) {
        byte commandId = commandPackage.dataBytes[1];
        byte commandContent = commandPackage.dataBytes[4];
        switch (commandId) {
            case (byte) 0x80://自检结果上报 0：自检成功
                if (Utils.getUnsignedByte(commandContent) == 0)
                    Toast.makeText(MainActivity.this, "自检成功", Toast.LENGTH_SHORT).show();
                break;
            case (byte) 0x81://灵敏度设置ACK  0：设置成功
                if (Utils.getUnsignedByte(commandContent) == 0)
                    Toast.makeText(MainActivity.this, "灵敏度设置成功", Toast.LENGTH_SHORT).show();
                break;
            case (byte) 0x82://工作模式设置ACK  0：设置成功
                if (Utils.getUnsignedByte(commandContent) == 0)
                    Toast.makeText(MainActivity.this, "工作模式设置成功", Toast.LENGTH_SHORT).show();
                break;
            case (byte) 0x83://读取电量上报  1-10有效，表示电量格数，10表示电量充足，3以下(包含3)提示低电量
                if (Utils.getUnsignedByte(commandContent) <= 3)
                    Toast.makeText(MainActivity.this, "低电量", Toast.LENGTH_SHORT).show();
                topBattery.setPower(Utils.getUnsignedByte(commandContent) * 10);
                break;
            case (byte) 0x85://功率设置ACK   0：设置成功
                if (Utils.getUnsignedByte(commandContent) == 0)
                    Toast.makeText(MainActivity.this, "功率设置成功", Toast.LENGTH_SHORT).show();
                break;
            case (byte) 0x86://数字本振频率设置ACK  0：设置成功
                if (Utils.getUnsignedByte(commandContent) == 0)
                    Toast.makeText(MainActivity.this, "数字本振频率设置成功", Toast.LENGTH_SHORT).show();
                break;
            case (byte) 0x87://数字放大增益设置ACK  0：设置成功
                if (Utils.getUnsignedByte(commandContent) == 0)
                    Toast.makeText(MainActivity.this, "数字放大增益设置成功", Toast.LENGTH_SHORT).show();
                break;
            case (byte) 0x88://测量数据主动上报,非指令
                break;
        }
    }

    class ReadSerialPortThread extends Thread {
        @Override
        public void run() {
            while (true) {
                try {
                    ReceivedData receivedData = receivedDataLinkedBlockingQueue.take();
                    if (receivedData.size == DataConstants.COMMAND_FRAME_LENGTH
                            && receivedData.buffer[0] == DataConstants.FRAME_HEAD
                            && receivedData.buffer[6] == DataConstants.FRAME_TAIL) { //指令帧
                        CommandPackage commandPackage = new CommandPackage((receivedData));
                        Log.e("www", "ReadSerialPortThread  received CommandPackage..." +  " dataBytes.length" + commandPackage.dataBytes.length);
                        //processCommandPackage(commandPackage);
                        EventBus.getDefault().post(new receiveCommandPackageEvent(commandPackage));
                    } else if (receivedData.size > 0 && receivedData.buffer[0] == DataConstants.FRAME_HEAD && receivedData.buffer[1] == DataConstants.command_receive_data) { //数据帧开头
                        DataPackage dataPackage = new DataPackage(receivedData.timestamp);
                        int copyIndex = 0;
                        System.arraycopy(receivedData.buffer, 0, dataPackage.dataBytes, copyIndex, receivedData.size);//复制第一段数据
                        copyIndex += receivedData.size;
                        byte[] dataLen = new byte[2];
                        dataLen[0] = receivedData.buffer[2];
                        dataLen[1] = receivedData.buffer[3];
                        int length = Utils.byteArrayToShort(dataLen, 0);
                        length = DataConstants.DATA_FRAME_LENGTH;//写死数据帧长度
                        Log.e("www", "ReadSerialPortThread  length" +  length);
                        while (copyIndex < length) {//循环读取数据包后面的部分
                            ReceivedData otherReceivedData = receivedDataLinkedBlockingQueue.take();
                            System.arraycopy(otherReceivedData.buffer, 0, dataPackage.dataBytes, copyIndex, otherReceivedData.size);//复制一段数据
                            copyIndex += otherReceivedData.size;
                        }
                        if (dataPackage.dataBytes[0] == DataConstants.FRAME_HEAD
                                && dataPackage.dataBytes[DataConstants.DATA_FRAME_LENGTH - 1] == DataConstants.FRAME_TAIL
                                && dataPackage.dataBytes[1] == DataConstants.command_receive_data) {
                            //校验合格的数据包
                            dataPackageLinkedBlockingQueue.put(dataPackage);//数据包保存到队列
                            if (dataPackages4display.size() > maxDisplayLength + 1) {
                                dataPackages4display.remove(0);
                            }
                            dataPackages4display.add(dataPackage);
                            Log.e("www", "ReadSerialPortThread  received DataPackage..." + " dataBytes.length" + dataPackage.dataBytes.length);
                        }
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (IndexOutOfBoundsException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    class SaveDataPackToStorageThread extends Thread {
        @Override
        public void run() {
            while (true) {
                if (dataPackageLinkedBlockingQueue.size() > datapackNumToSaveInFile * 2) {
                    //保存到外部
                    ArrayList<DataPackage> listToBeSaved = new ArrayList<>(datapackNumToSaveInFile);
                    dataPackageLinkedBlockingQueue.drainTo(listToBeSaved, datapackNumToSaveInFile);
                    saveDataPackagesToStorage(listToBeSaved);
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static class sendDataEvent {
        public byte[] sendBytes;
        public sendDataEvent(byte[] sendBytes) {
            this.sendBytes = sendBytes;
        }
    }

    public static class receiveCommandPackageEvent {
        public CommandPackage commandPackage;
        public receiveCommandPackageEvent(CommandPackage commandPackage) {
            this.commandPackage = commandPackage;
        }
    }

    public static class updateTimeEvent {
    }
}
