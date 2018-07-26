package pl.edu.agh.wiet.zookeeperlab;

import com.scalified.tree.TreeNode;
import com.scalified.tree.multinode.ArrayMultiTreeNode;
import org.apache.zookeeper.ZooKeeper;

import java.util.List;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;

public class ZookeeperCliRunner implements Runnable {

    private final ZooKeeper zookeeper;
    private TreeNode<String> root;
    private AtomicInteger nodesDiscovered;
    private AtomicInteger nodesVisited;

    public ZookeeperCliRunner(ZooKeeper zookeeper) {
        this.zookeeper = zookeeper;
        this.nodesDiscovered = new AtomicInteger(0);
        this.nodesVisited = new AtomicInteger(0);
    }

    @Override
    public void run() {
        if(zookeeper == null) {
            throw new RuntimeException("Zookeeper is null");
        }

        Scanner scanner = new Scanner(System.in);

        while(true) {
            String line = scanner.nextLine();
            if(line.equals("exit")) {
                System.exit(0);
            }

            if(line.equals("tree")) {
                printNodeTree();
                root = null;
            }
        }
    }

    private void printNodeTree() {
        zookeeper.getChildren("/", false, this::childNodesCallback, null);
    }

    public void childNodesCallback(int returnCode, String path, Object ctx, List<String> list) {
        nodesDiscovered.addAndGet(list.size());
        nodesVisited.incrementAndGet();

        if(nodesDiscovered.get() == nodesVisited.get()) {
            System.out.println(root.toString());
        }

        if(root == null) {
            root = new ArrayMultiTreeNode<>(path);
            nodesDiscovered.incrementAndGet();
        }

        TreeNode<String> parent = root.find(path);
        if(parent != null) {
            list.forEach(child -> {
                synchronized (parent) {
                    parent.add(new ArrayMultiTreeNode<>(buildPath(path, child)));
                }
                zookeeper.getChildren(buildPath(path, child), false, this::childNodesCallback, null);
            });
        }
    }

    private String buildPath(String parentPath, String child) {
        if(parentPath.endsWith("/")) {
            return parentPath + child;
        }
        return parentPath + "/" + child;
    }
}
