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

import android.app.Service;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.yanzhenjie.andserver.AndServer;
import com.yanzhenjie.andserver.Server;
import com.yanzhenjie.andserver.server.response.RequestDeleteHandler;
import com.yanzhenjie.andserver.server.response.RequestDownloadHandler;
import com.yanzhenjie.andserver.server.response.RequestDownloadPicHandler;
import com.yanzhenjie.andserver.server.response.RequestFileHandler;
import com.yanzhenjie.andserver.server.response.RequestLoginHandler;
import com.yanzhenjie.andserver.server.response.RequestUploadHandler;
import com.yanzhenjie.andserver.website.AssetsWebsite;

/**
 * <p>Server service.</p>
 */
public class CoreService extends Service {

    /**
     * AndServer.
     */
    private Server mServer;

    private AssetManager mAssetManager;

    @Override
    public void onCreate() {
        mAssetManager = getAssets();

        AndServer andServer = new AndServer.Build()
                .port(8081)
                .timeout(10 * 1000)
                .registerHandler("login", new RequestLoginHandler())
                .registerHandler("files", new RequestDownloadHandler())
                .registerHandler("pics", new RequestDownloadPicHandler())
                .registerHandler("download", new RequestFileHandler())
                .registerHandler("upload", new RequestUploadHandler())
                .registerHandler("delete", new RequestDeleteHandler())
                .website(new AssetsWebsite(mAssetManager, "web"))
//                .website(new StorageWebsite(new File(Environment.getExternalStorageDirectory(), "web").getAbsolutePath()))
                .listener(mListener)
                .build();

        // Create server.
        mServer = andServer.createServer();
    }

    /**
     * Server listener.
     */
    private Server.Listener mListener = new Server.Listener() {
        @Override
        public void onStarted() {
            ServerStatusReceiver.serverStart(CoreService.this);
        }

        @Override
        public void onStopped() {
            ServerStatusReceiver.serverStop(CoreService.this);
        }

        @Override
        public void onError(Exception e) {
            // Ports may be occupied.
        }
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startServer();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        stopServer(); // Stop server.

        if (mAssetManager != null)
            mAssetManager.close();
    }

    /**
     * Start server.
     */
    private void startServer() {
        if (mServer != null) {
            if (mServer.isRunning()) {
                ServerStatusReceiver.serverHasStarted(CoreService.this);
            } else {
                mServer.start();
            }
        }
    }

    /**
     * Stop server.
     */
    private void stopServer() {
        if (mServer != null) {
            mServer.stop();
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
