package org.king2.sl.data.pool;


import lombok.Data;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 上传内容任务的线程池
 */
@Data
public class UploadTaskPool {

    private UploadTaskPool() {
    }

    private static final UploadTaskPool UPLOAD_TASK_POOL = new UploadTaskPool();

    public static UploadTaskPool getInstance() {
        return UPLOAD_TASK_POOL;
    }

    private final ExecutorService pool = Executors.newFixedThreadPool(300);
}
