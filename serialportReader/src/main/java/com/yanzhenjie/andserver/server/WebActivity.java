/*
 * Copyright © Yan Zhenjie. All Rights Reserved
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.yanzhenjie.andserver.server;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.serialport.reader.R;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.yanzhenjie.loading.dialog.LoadingDialog;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
//import com.yanzhenjie.nohttp.tools.NetUtil;

/**
 */
public class WebActivity extends Activity implements View.OnClickListener {

    private Intent mService;
    /**
     * Accept and server status.
     */
    private ServerStatusReceiver mReceiver;

    /**
     * Show message
     */
    private TextView mTvMessage;

    private LoadingDialog mDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web);
        findViewById(R.id.btn_start).setOnClickListener(this);
        findViewById(R.id.btn_stop).setOnClickListener(this);

        mTvMessage = (TextView) findViewById(R.id.tv_message);

        // AndServer run in the service.
        mService = new Intent(this, CoreService.class);
        mReceiver = new ServerStatusReceiver(this);
        mReceiver.register();

        //copy assets file to external storage
        copyFilesFromAssets(WebActivity.this, "web", Environment.getExternalStorageDirectory().getAbsolutePath() + "/web");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mReceiver.unRegister();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.btn_start: {
                showDialog();
                startService(mService);
                break;
            }
            case R.id.btn_stop: {
                stopService(mService);
            }
        }
    }

    /**
     * Start notify.
     */
    public void serverStart() {
        closeDialog();
        String message = "server_start_succeed";

//        String ip = NetUtil.getLocalIPAddress();
        String ip = "localhost";
        if (!TextUtils.isEmpty(ip)) {
            message += ("\nhttp://" + ip + ":8080/\n"
                    + "http://" + ip + ":8080/login\n"
                    + "http://" + ip + ":8080/upload\n"
                    + "http://" + ip + ":8080/web/index.html\n"
                    + "http://" + ip + ":8080/web/error.html\n"
                    + "http://" + ip + ":8080/web/login.html\n"
                    + "http://" + ip + ":8080/web/image/image.jpg");
        }
        mTvMessage.setText(message);
    }

    /**
     * Started notify.
     */
    public void serverHasStarted() {
        closeDialog();
        Toast.makeText(this, "server_started", Toast.LENGTH_SHORT).show();
    }

    /**
     * Stop notify.
     */
    public void serverStop() {
        closeDialog();
        mTvMessage.setText("server_stop_succeed");
    }

    private void showDialog() {
        if (mDialog == null)
            mDialog = new LoadingDialog(this);
        if (!mDialog.isShowing()) mDialog.show();
    }

    private void closeDialog() {
        if (mDialog != null && mDialog.isShowing()) mDialog.dismiss();
    }

    public static void copyFilesFromAssets(Context context, String assetsPath, String savePath){
        try {
            String fileNames[] = context.getAssets().list(assetsPath);// 获取assets目录下的所有文件及目录名
            if (fileNames.length > 0) {// 如果是目录
                File file = new File(savePath);
                file.mkdirs();// 如果文件夹不存在，则递归
                for (String fileName : fileNames) {
                    copyFilesFromAssets(context, assetsPath + "/" + fileName,
                            savePath + "/" + fileName);
                }
            } else {// 如果是文件
                InputStream is = context.getAssets().open(assetsPath);
                File saveFile = new File(savePath);
                if (saveFile.exists()) {
                    saveFile.delete();
                }
                FileOutputStream fos = new FileOutputStream(saveFile);
                byte[] buffer = new byte[1024];
                int byteCount = 0;
                while ((byteCount = is.read(buffer)) != -1) {// 循环从输入流读取
                    // buffer字节
                    fos.write(buffer, 0, byteCount);// 将读取的输入流写入到输出流
                }
                fos.flush();// 刷新缓冲区
                is.close();
                fos.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
