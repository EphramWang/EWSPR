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

import com.yanzhenjie.andserver.RequestHandler;
import com.yanzhenjie.andserver.util.HttpRequestParser;

import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.HttpContext;

import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * <p>Login Handler.</p>
 */
public class RequestDownloadHandler implements RequestHandler {

    @Override
    public void handle(HttpRequest request, HttpResponse response, HttpContext context) throws HttpException, IOException {
        Map<String, String> params = HttpRequestParser.parse(request);
        String dir = params.get("dir");
        String subDirName = "";
        if (dir!= null && dir.length() > 0) {
            subDirName = "/" + dir;
        }

        String sStart = "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "    <meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\"/>\n" +
                "    <meta name=\"viewport\"\n" +
                "          content=\"width=device-width,initial-scale=1.0,minimum-scale=1.0,maximum-scale=1.0,user-scalable=no\"/>\n" +
                "    <meta name=\"format-detection\" content=\"telephone=no\"/>\n" +
                "    <title>文件下载</title>\n" +
                "</head>\n" +
                "<body>\n" +
                "\n" +
                "<div>\n" +
                " 文件下载<br/>\n"
//                "<a href=\"/web/error.html\">Jump to error.html</a>" + "<br/>\n" +
//                "<a href=\"/upload\">/upload</a>" + "<br/>\n" +
//                "<a href=\"/download?filename=xiaomiPush.txt\">/download?filename=xiaomiPush.txt</a>" + "<br/>\n" +
//                "</div>\n" +
//                "\n" +
//                "</body>\n" +
//                "</html>"
                ;

        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + MainActivity.filePath + subDirName);
        if (file.exists() && file.isDirectory()) {
            String[] filelist = file.list();
            for (int i = 0; i < filelist.length; i++) {
                File readfile = new File(file.getAbsolutePath() + "/" + filelist[i]);
                if (!readfile.isDirectory()) {
                    System.out.println("path=" + readfile.getPath());
                    System.out.println("absolutepath=" + readfile.getAbsolutePath());
                    System.out.println("name=" + readfile.getName());
                    sStart += "<a href=\"/download?dir=" + dir + "&&filename=" + readfile.getName() + "\">" + readfile.getName() + "</a> \n";
                    sStart += "<a href=\"/delete?dir=" +readfile.getParent().substring(readfile.getParent().lastIndexOf('/') + 1) + "&&filename=" + readfile.getName() + "\">" + "删除文件" + "</a> <br/>\n";
                } else {
                    sStart += "<a href=\"/files?dir=" + readfile.getName() + "\">" + readfile.getName() + "</a> \n";
                    sStart += "<a href=\"/delete?dir=" + readfile.getName() + "\">" + "删除文件夹" + "</a> <br/>\n";
                }
            }
        }

        sStart += "</div>\n" +
                "\n" +
                "</body>\n" +
                "</html>";

        StringEntity stringEntity = new StringEntity(sStart, "utf-8");
        stringEntity.setContentType("text/html");
        response.setEntity(stringEntity);
    }
}