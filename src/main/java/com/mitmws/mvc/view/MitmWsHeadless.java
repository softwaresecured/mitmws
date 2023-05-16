package com.mitmws.mvc.view;

import com.mitmws.httpproxy.HttpProxy;
import com.mitmws.mvc.model.BreakpointModel;
import com.mitmws.mvc.thread.TrafficLogQueueProcessorThread;
import com.mitmws.projects.ProjectDataService;
import com.mitmws.projects.ProjectDataServiceException;

public class MitmWsHeadless {
    private String projectFile = null;

    public MitmWsHeadless(String projectFile) {
        this.projectFile = projectFile;
    }
    public void start() {
        try {
            ProjectDataService projectDataService = new ProjectDataService(projectFile);
            HttpProxy httpProxy = new HttpProxy(new BreakpointModel());
            TrafficLogQueueProcessorThread trafficLogQueueProcessorThread =  new TrafficLogQueueProcessorThread(httpProxy.getLogger(), projectDataService);
            httpProxy.startAll();
            Object lock = new Object();
            Thread headlessThread = new Thread(() -> {
                synchronized(lock) {
                    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                        System.out.println("Shutdown caught");
                        httpProxy.stopAll();
                        trafficLogQueueProcessorThread.shutdown();
                        try {
                            trafficLogQueueProcessorThread.join();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }));
                    httpProxy.startAll();
                    trafficLogQueueProcessorThread.start();
                    while (trafficLogQueueProcessorThread.isAlive()) {
                        try {
                            lock.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });

            headlessThread.start();
            headlessThread.join();

        } catch (ProjectDataServiceException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
