package co.com.devlinx9.k8scontextstatusbar;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
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
}
