package co.com.devlinx9.plugingk8sstatusbar;

import java.util.List;

public class Context {
   private String cluster;
   private String user;
   private String name;

    public String getCluster() {
        return cluster;
    }

    public void setCluster(String cluster) {
        this.cluster = cluster;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
