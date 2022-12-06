package co.com.devlinx9.plugingk8sstatusbar;

import java.util.List;

public class KubeConfig {
    public KubeConfig(String currentContext, List<Object> contexts, List<Object> clusters, List<Object> users) {
        this.currentContext = currentContext;
        this.contexts = contexts;
        this.clusters = clusters;
        this.users = users;
    }

    private String currentContext;
    private List<Object> contexts;
    private List<Object> clusters;
    private List<Object> users;

    public String getCurrentContext() {
        return currentContext;
    }

    public void setCurrentContext(String currentContext) {
        this.currentContext = currentContext;
    }

    public List<Object> getContexts() {
        return contexts;
    }

    public void setContexts(List<Object> contexts) {
        this.contexts = contexts;
    }

    public List<Object> getClusters() {
        return clusters;
    }

    public void setClusters(List<Object> clusters) {
        this.clusters = clusters;
    }
}
