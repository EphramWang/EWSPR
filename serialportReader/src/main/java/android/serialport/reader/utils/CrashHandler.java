package android.serialport.reader.utils;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by ning on 17/12/18.
 */

public class CrashHandler implements Thread.UncaughtExceptionHandler {

        public static final String TAG = "CrashHandler";

        private static CrashHandler INSTANCE;

        private CrashHandler() {
        }

        public static CrashHandler getInstance() {
            if (INSTANCE == null)
                INSTANCE = new CrashHandler();
            return INSTANCE;
        }

        public static boolean isNull() {
            return INSTANCE == null;
        }

        /**
         * 初始化,注册Context对象, 获取系统默认的UncaughtException处理器, 设置该CrashHandler为程序的默认处理器
         */
        public void init() {
            Thread.setDefaultUncaughtExceptionHandler(this);
        }

        /**
         *
         * 保存错误日志到文件中 如果包混淆 则不保存文件
         *
         */
        private void writeMsgToSd(String message) {
            try {
                String pathName = Environment.getExternalStorageDirectory() + "/ewspr/";
                String fileName = "ewspr.txt";
                File path = new File(pathName);
                File file = new File(pathName + fileName);
                if (!path.exists()) {
                    path.mkdir();
                }
                if (!file.exists()) {
                    file.createNewFile();
                }

                FileOutputStream stream = new FileOutputStream(file, true);
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy年MM月dd日   HH:mm:ss     ");
                Date curDate = new Date(System.currentTimeMillis());// 获取当前时间
                String printMessage = formatter.format(curDate) + "Product Model: " + android.os.Build.MODEL + ",android: "
                        + android.os.Build.VERSION.RELEASE + "\n" + message + "\n\n";
                byte[] bytes = printMessage.getBytes();
                stream.write(bytes);
                stream.close();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }

        /**
         * 当UncaughtException发生时会转入该函数来处理
         */
        @Override
        public void uncaughtException(Thread thread, Throwable ex) {
            long time = System.currentTimeMillis();
            String message = ex.getMessage();

            StringBuilder sb = new StringBuilder();
            sb.append(message).append("\nStackTrace:\n").append(Log.getStackTraceString(ex));
            String crashMsg = sb.substring(0, Math.min(5000, sb.length()));

            try {
                writeMsgToSd(crashMsg);
            } catch (Exception ignored) {}

            // 关闭现有线程
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(10);
        }
    }