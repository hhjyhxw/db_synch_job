package com.icloud.task;

import org.apache.log4j.Logger;

public abstract class Task implements Comparable<Task>, Runnable {
    private static Logger logger = Logger.getLogger(Task.class);
    private TaskCallback taskCallback;

    public abstract void doTask();
    
    public abstract void finished();

    public void run() {
        try {
            this.doTask();
            this.finished();
        } catch (Exception e) {
            logger.error("任务执行出错！", e);
        } finally {
            if (this.getTaskCallback() != null) {
                this.getTaskCallback().taskFinished(this);
            }
        }
    }

    public TaskCallback getTaskCallback() {
        return taskCallback;
    }

    public void setTaskCallback(TaskCallback taskCallback) {
        this.taskCallback = taskCallback;
    }
}
