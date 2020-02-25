package org.king2.sl.sso.pool;


import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 线程池POOL
 */
public class TaskPool {

    public static final ExecutorService TASK_POOL = Executors.newFixedThreadPool(300);
}
