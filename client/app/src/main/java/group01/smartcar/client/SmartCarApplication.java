package group01.smartcar.client;

import android.app.Application;

import group01.smartcar.client.async.TaskExecutor;

public class SmartCarApplication extends Application {

    private static TaskExecutor taskExecutor;

    @Override
    public void onCreate() {
        super.onCreate();

        taskExecutor = TaskExecutor.getInstance();
    }

    public static TaskExecutor getTaskExecutor() {
        return taskExecutor;
    }

}
