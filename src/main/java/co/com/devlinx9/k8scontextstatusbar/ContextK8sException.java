package co.com.devlinx9.k8scontextstatusbar;

public class ContextK8sException extends RuntimeException{
    public ContextK8sException(Throwable cause) {
        super(cause);
    }

    public ContextK8sException(String message) {
        super(message);
    }
}
