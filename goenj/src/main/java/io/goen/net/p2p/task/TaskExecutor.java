package io.goen.net.p2p.task;

import io.goen.net.p2p.NodesCenter;
import io.goen.net.p2p.Sender;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TaskExecutor {
    private ScheduledExecutorService findExecutor = Executors.newSingleThreadScheduledExecutor();
    private ScheduledExecutorService checkExecutor = Executors.newSingleThreadScheduledExecutor();

    private NodesCenter nodesCenter;
    private Sender sender;

    public TaskExecutor(Sender sender, NodesCenter nodesCenter) {
        this.nodesCenter = nodesCenter;
        this.sender = sender;
    }

    public void start() {
        if (nodesCenter.findAble()) {
            findExecutor.scheduleWithFixedDelay(
                    new FindTask(sender, nodesCenter),
                    1, 30, TimeUnit.SECONDS);
        }

        if (nodesCenter.checkAble()) {
            checkExecutor.scheduleWithFixedDelay(
                    new CheckTask(sender, nodesCenter),
                    1, 1000, TimeUnit.MILLISECONDS);
        }
    }

    public void close() {
        findExecutor.shutdownNow();
        checkExecutor.shutdownNow();
    }
}
