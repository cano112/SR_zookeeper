package pl.edu.agh.wiet.zookeeperlab;

public interface ZookeeperDataEventListener {
    void changed(byte[] data);
    void close(int returnCode);
}
