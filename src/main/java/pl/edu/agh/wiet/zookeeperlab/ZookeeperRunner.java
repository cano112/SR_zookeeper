package pl.edu.agh.wiet.zookeeperlab;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

import java.io.IOException;
import java.util.Arrays;

public class ZookeeperRunner implements Runnable, Watcher,  ZookeeperDataEventListener {
    private final DataWatcher watcher;
    private final ZooKeeper zookeeper;
    private final String executable[];
    private Process process;

    public ZookeeperRunner(String hostPort, String znode, String[] executable) throws IOException {
        System.out.println("Port: " + hostPort + "\nznode: " + znode + "\nexecutable: " + Arrays.toString(executable));
        this.executable = executable;
        this.zookeeper = new ZooKeeper(hostPort, 3000, this);

        if(zookeeper.getState().isConnected()) {
            System.out.println("Connected to Zookeeper");
            if(zookeeper.getState().isAlive()) {
                System.out.println("Zookeeper is alive");
            }
        }

        this.watcher = new DataWatcher(zookeeper, znode, this);
    }

    public void run() {
        System.out.println("Client started");
        new ZookeeperCliRunner(zookeeper).run();
        try {
            synchronized (this) {
                while(!watcher.isDead()) {
                    wait();
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void process(WatchedEvent watchedEvent) {
        watcher.process(watchedEvent);
    }

    public void changed(byte[] data) {
        stopProcess();
        if (data != null) {
            startProcess();
        }
    }

    private void stopProcess() {
        if (process != null) {
            System.out.println("Stopping process");
            process.destroy();
            try {
                process.waitFor();
                System.out.println("Process destroyed");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        process = null;
    }

    private void startProcess() {
        if(process == null) {
            try {
                System.out.println("Starting process");
                ProcessBuilder pb = new ProcessBuilder(executable);
                pb.redirectOutput(ProcessBuilder.Redirect.INHERIT);
                process = pb.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void close(int returnCode) {
        synchronized (this) {
            notifyAll();
        }
    }
}
