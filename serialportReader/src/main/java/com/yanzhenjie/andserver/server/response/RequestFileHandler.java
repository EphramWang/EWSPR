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
package com.yanzhenjie.andserver.server.response;

import android.os.Environment;
import android.serialport.reader.MainActivity;
import android.util.Log;

import com.yanzhenjie.andserver.RequestHandler;
import com.yanzhenjie.andserver.util.HttpRequestParser;

import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.entity.FileEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.HttpContext;

import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.Map;

/**
 * <p>Returns a file.</p>
 */
public class RequestFileHandler implements RequestHandler {
    @Override
    public void handle(HttpRequest request, HttpResponse response, HttpContext context) throws HttpException, IOException {
        Map<String, String> params = HttpRequestParser.parse(request);
        Log.i("AndServer", "Params: " + params.toString());

        String filename = URLDecoder.decode(params.get("filename"), "utf-8");
        String dir = URLDecoder.decode(params.get("dir"), "utf-8");

        String fileStr;
        if (dir.equals("pics123"))
            fileStr = Environment.getExternalStorageDirectory().getAbsolutePath()+ MainActivity.screenshotPath + "/" + filename;
        else
            fileStr =Environment.getExternalStorageDirectory().getAbsolutePath()+ MainActivity.filePath + "/" + dir + "/" + filename;
        File file = new File(fileStr);
        if (file.exists()) {
            response.setStatusCode(200);

            long contentLength = file.length();
            response.setHeader("ContentLength", Long.toString(contentLength));
            //response.setEntity(new FileEntity(file, HttpRequestParser.getMimeType(file.getName())));
            response.setEntity(new FileEntity(file, "multipart/form-data"));
            response.setHeader("Content-Disposition","attachment;filename=\"" + filename + "\"");
        } else {
            response.setStatusCode(404);
            response.setEntity(new StringEntity(""));
        }
    }


}
