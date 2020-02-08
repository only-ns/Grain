package com.only.grain.manage;

import org.csource.common.MyException;
import org.csource.fastdfs.ClientGlobal;
import org.csource.fastdfs.StorageClient;
import org.csource.fastdfs.TrackerClient;
import org.csource.fastdfs.TrackerServer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;


@RunWith(SpringRunner.class)
@SpringBootTest

public class GrainManageWebApplicationTests {

    @Test
    public void contextLoads() {
        String tracker = GrainManageWebApplicationTests.class.getResource("/tracker.conf").getPath();
        try {
            ClientGlobal.init(tracker);
        } catch (IOException e) {
            System.err.println("文件找不到");
            e.printStackTrace();
        } catch (MyException e) {
            System.err.println("配置错误");
            e.printStackTrace();
        }

        TrackerClient trackerClient = new TrackerClient();

        // 获得一个trackerServer的实例
        TrackerServer trackerServer = null;
        try {
            trackerServer = trackerClient.getConnection();
        } catch (Exception e) {
            System.err.println("trackerServer连接失败!");
            e.printStackTrace();
        }

        // 通过tracker获得一个Storage链接客户端
        StorageClient storageClient = new StorageClient(trackerServer,null);
        System.out.println(storageClient);

        String[] uploadInfos = new String[0];
        try {
            uploadInfos = storageClient.upload_file("C:\\Users\\only\\Desktop\\2.jpg", "jpg", null);
        } catch (Exception e) {
            System.err.println("上传失败!");
            e.printStackTrace();
        }

        String url = "http://192.168.65.102";

        for (String uploadInfo : uploadInfos) {
            url += "/"+uploadInfo;

            //url = url + uploadInfo;
        }

        System.out.println(url);
    }

}
