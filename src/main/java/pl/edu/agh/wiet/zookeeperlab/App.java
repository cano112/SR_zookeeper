package pl.edu.agh.wiet.zookeeperlab;

public class App {

    public static void main(String[] args) {
        if (args.length < 3) {
            System.err.println("USAGE: Executor hostPort znode program [args ...]");
            System.exit(2);
        }
        String hostPort = args[0];
        String znode = args[1];
        String executable[] = new String[args.length - 2];
        System.arraycopy(args, 2, executable, 0, executable.length);
        try {
            new ZookeeperRunner(hostPort, znode, executable).run();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
