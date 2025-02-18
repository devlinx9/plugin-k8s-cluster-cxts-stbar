package co.com.devlinx9.k8scontextstatusbar;

import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.nio.file.FileSystems;
import java.util.ArrayList;
import java.util.Map;
import java.util.logging.Logger;

public class KubeUtils {

    private static final Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    private static final String KUBECTL_CMD = "kubectl";

    /**
     * Load a Kubernetes config from ~/.kube/config
     */
    protected static KubeConfig loadKubeConfig() {
        String userHomeDir = System.getProperty("user.home");
        String configK8s = ".kube".concat(FileSystems.getDefault().getSeparator()).concat("config");

        Yaml yaml = new Yaml();
        Object config;
        try {
            config = yaml.load(
                    new FileReader(userHomeDir.concat(FileSystems.getDefault().getSeparator()).concat(configK8s))
                              );
        } catch (FileNotFoundException e) {
            throw new ContextK8sException("Unable to find ~/.kube/config");
        }

        Map<String, Object> configMap = (Map<String, Object>) config;
        String currentContext = (String) configMap.get("current-context");
        ArrayList<Object> contexts = (ArrayList<Object>) configMap.get("contexts");
        ArrayList<Object> clusters = (ArrayList<Object>) configMap.get("clusters");
        ArrayList<Object> users = (ArrayList<Object>) configMap.get("users");

        return new KubeConfig(currentContext, contexts, clusters, users);
    }

    /**
     * Update context by calling 'kubectl config use-context <context>'
     */
    protected static void updateContextK8s(String context) {
        // Ensure 'kubectl' is installed before actually running commands
        checkKubectlInstalled();

        runCommand(KUBECTL_CMD, "config", "use-context", context);
    }

    /**
     * Check if kubectl is installed by trying 'kubectl version --client'.
     * If the exit code is non-zero or an IOException is thrown,
     * we consider it not installed or not in PATH.
     */
    private static void checkKubectlInstalled() {
        try {
            runCommand(KUBECTL_CMD, "version", "--client");
            LOGGER.info("Detected that " + KUBECTL_CMD + " is installed and accessible.");
        } catch (ContextK8sException e) {
            throw new ContextK8sException(
                    "It seems '" + KUBECTL_CMD + "' is not installed or not in your PATH. " +
                    "Please install it or adjust your PATH environment variable.");
        }
    }

    /**
     * Utility method to run a shell command and capture/log its output.
     * Throws ContextK8sException on failure (non-zero exit or process error).
     */
    private static String runCommand(String... command) {
        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.redirectErrorStream(true);

        Process process = null;
        StringBuilder output = new StringBuilder();

        try {
            process = processBuilder.start();

            // Read command output
            try (BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = br.readLine()) != null) {
                    LOGGER.info("cmd output: " + line);
                    output.append(line).append(System.lineSeparator());
                }
            }

            // Wait for the process to complete
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                LOGGER.severe("'%s' failed with exit code %d".formatted(String.join(" ", command), exitCode));
                throw new ContextK8sException(
                        "'" + String.join(" ", command) + "' command failed with exit code " + exitCode
                );
            }
        } catch (IOException | InterruptedException e) {
            throw new ContextK8sException("Error executing command: " + String.join(" ", command));
        } finally {
            if (process != null) {
                process.destroy();
            }
        }

        return output.toString();
    }
}
