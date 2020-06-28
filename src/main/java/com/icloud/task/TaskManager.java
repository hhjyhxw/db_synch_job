package com.icloud.task;

import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;

public class TaskManager implements TaskCallback {
    private Logger logger = Logger.getLogger(getClass());

    private ExecutorService executorService;

    private ConcurrentSkipListSet<Task> tasks = new ConcurrentSkipListSet<Task>();

    public void init() throws Exception {
        int poolSize = 8;
        this.executorService = Executors.newFixedThreadPool(poolSize);
        this.logger.info("任务处理线程池初始化完成，线程池大小：" + poolSize);
    }
    
    public void shutdown() throws InterruptedException {
        this.executorService.shutdown();
    }

    /**
     * 判断任务管理器是否空闲
     * 
     * @return
     */
    public boolean isIdle() {
        return this.tasks.isEmpty();
    }
    
    public int size() {
        return this.tasks.size();
    }

    public void addTask(Task task) {
        if (this.tasks.contains(task)) {
            return;
        }
        if (this.executorService == null) {
            this.logger.error("任务处理线程池未启动或已宕机！");
            return;
        }
        task.setTaskCallback(this);
        this.tasks.add(task);
        this.logger.info("添加任务：" + task + "，当前任务数：" + this.tasks.size());
        this.executorService.execute(task);
    }

    @Override
    public void taskFinished(Task task) {
        this.tasks.remove(task);
        this.logger.info("任务执行完成：" + task + "，剩余任务数：" + this.tasks.size());
    }
}
