
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
 * <p>Returns a file.</p>
 */
public class RequestDeleteHandler implements RequestHandler {
    @Override
    public void handle(HttpRequest request, HttpResponse response, HttpContext context) throws HttpException, IOException {
        Map<String, String> params = HttpRequestParser.parse(request);
        String dirName = params.get("dir");

        String filename = params.get("filename");
        //String filename = URLDecoder.decode(params.get("filename"), "utf-8");

        if (filename == null || filename.length() == 0) { //is directory
            File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + MainActivity.filePath + "/" + dirName);
            if (deleteFile(file)) {
                response.setStatusCode(200);
                response.setEntity(new StringEntity("success!"));
            } else {
                response.setStatusCode(404);
                response.setEntity(new StringEntity("error!"));
            }

        } else {//is file

            String fileStr;
            if (MainActivity.screenshotPath.contains(dirName)) {
                fileStr = Environment.getExternalStorageDirectory().getAbsolutePath() + MainActivity.screenshotPath + "/" + filename;
            } else {
                fileStr = Environment.getExternalStorageDirectory().getAbsolutePath() + MainActivity.filePath + "/" + dirName + "/" + filename;
            }
            File file = new File(fileStr);
            if (file.exists()) {
                response.setStatusCode(200);
                file.delete();

                response.setEntity(new StringEntity("success!"));

            } else {
                response.setStatusCode(404);
                response.setEntity(new StringEntity("File not found."));
            }

        }
    }

    //递归删除文件夹
    private boolean deleteFile(File file) {
        if (file.exists()) {//判断文件是否存在
            if (file.isFile()) {//判断是否是文件
                return  file.delete();//删除文件
            } else if (file.isDirectory()) {//否则如果它是一个目录
                File[] files = file.listFiles();//声明目录下所有的文件 files[];
                for (int i = 0;i < files.length;i ++) {//遍历目录下所有的文件
                    this.deleteFile(files[i]);//把每个文件用这个方法进行迭代
                }
                return file.delete();//删除文件夹
            }
        } else {
            System.out.println("File not found");
            return false;
        }
        return false;
    }


}
