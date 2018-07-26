package pl.edu.agh.wiet.zookeeperlab;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.util.Arrays;
import java.util.List;

public class DataWatcher implements Watcher, AsyncCallback.StatCallback, AsyncCallback.ChildrenCallback {

    private final ZooKeeper zookeeper;
    private final String znode;
    private final ZookeeperDataEventListener listener;
    private boolean dead;
    private byte[] previousData;
    private int childrenCount;

    public DataWatcher(ZooKeeper zookeeper, String znode, ZookeeperDataEventListener listener) {
        this.zookeeper = zookeeper;
        this.znode = znode;
        this.listener = listener;
        this.dead = false;
        this.childrenCount = 0;
        zookeeper.exists(znode, true, this, null);
        zookeeper.getChildren(znode, true, this, null);
    }

    @Override
    public void process(WatchedEvent event) {
        String path = event.getPath();
        if (event.getType() == Event.EventType.None) {
            switch (event.getState()) {
                case SyncConnected:
                    break;
                case Expired:
                    dead = true;
                    listener.close(KeeperException.Code.SessionExpired);
                    break;
            }
        } else {
            if (path != null && path.equals(znode)) {
                zookeeper.exists(znode, true, this, null);
                zookeeper.getChildren(znode, true, this, null);
            }


        }
    }

    @Override
    public void processResult(int returnCode, String path, Object ctx, Stat stat) {
        boolean exists;
        switch (returnCode) {
            case KeeperException.Code.Ok:
                exists = true;
                break;
            case KeeperException.Code.NoNode:
                exists = false;
                break;
            case KeeperException.Code.SessionExpired:
            case KeeperException.Code.NoAuth:
                dead = true;
                listener.close(returnCode);
                return;
            default:
                zookeeper.exists(znode, true, this, null);
                return;
        }

        byte data[] = null;
        if (exists) {
            try {
                data = zookeeper.getData(znode, false, null);
            } catch (KeeperException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                return;
            }
        }
        if ((data == null && previousData != null)
                || (data != null && !Arrays.equals(previousData, data))) {
            listener.changed(data);
            previousData = data;
        }
    }

    @Override
    public void processResult(int returnCode, String path, Object o, List<String> list) {
        final int chCount = list.size();
        if(childrenCount < chCount ) {
            System.out.print("\nChildren count: " + chCount);
        }
        this.childrenCount = chCount;

    }

    public boolean isDead() {
        return dead;
    }
}
