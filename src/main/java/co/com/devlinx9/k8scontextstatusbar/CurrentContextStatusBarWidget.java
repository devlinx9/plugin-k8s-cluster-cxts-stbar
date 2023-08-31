package co.com.devlinx9.k8scontextstatusbar;

import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.ListPopup;
import com.intellij.openapi.ui.popup.PopupStep;
import com.intellij.openapi.ui.popup.util.BaseListPopupStep;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.StatusBarWidget;
import com.intellij.openapi.wm.impl.status.EditorBasedWidget;
import com.intellij.ui.popup.list.ListPopupImpl;
import com.intellij.util.concurrency.AppExecutorUtil;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.yaml.snakeyaml.Yaml;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.nio.file.FileSystems;
import java.util.*;
import java.util.logging.Logger;

import static java.util.concurrent.TimeUnit.SECONDS;

class CurrentContextStatusBarWidget extends EditorBasedWidget implements StatusBarWidget.MultipleTextValuesPresentation {

    private static final Logger LOGGER =
            Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);


    private String text;

    CurrentContextStatusBarWidget(@NotNull Project project) {
        super(project);
    }

    @Override
    public @NonNls
    @NotNull String ID() {
        return "currentContextK8s";
    }

    @Override
    public @Nullable WidgetPresentation getPresentation() {
        return this;
    }

    @Override
    public @Nullable
    @NlsContexts.Tooltip String getTooltipText() {
        return null;
    }

    @Override
    public void install(@NotNull StatusBar statusBar) {
        super.install(statusBar);
        DumbService.getInstance(getProject()).runWhenSmart(this::update);

        AppExecutorUtil.getAppScheduledExecutorService().scheduleWithFixedDelay(() -> {
            LOGGER.info("Updating k8s context...");
            update();
            LOGGER.info("finished update");
        }, 15, 45L, SECONDS);
    }

    public void update() {
        text = getCurrentContextFromKubeFile();
        if (myStatusBar != null) {
            myStatusBar.updateWidget(ID());
        }
    }

    private String getCurrentContextFromKubeFile() {
        var kubeConfig = loadKubeConfig();
        return kubeConfig.getCurrentContext();
    }

    /**
     * Load a Kubernetes config from a Reader
     */
    private static KubeConfig loadKubeConfig() {
        String userHomeDir = System.getProperty("user.home");
        String configK8s = ".kube".concat(FileSystems.getDefault().getSeparator()).concat("config");
        Yaml yaml = new Yaml();
        Object config;
        try {
            config = yaml.load(new FileReader(userHomeDir.concat(FileSystems.getDefault().getSeparator()).concat(configK8s)));
        } catch (FileNotFoundException e) {
            throw new ContextK8sException(e);
        }
        Map<String, Object> configMap = (Map<String, Object>) config;

        String currentContext = (String) configMap.get("current-context");
        ArrayList<Object> contexts = (ArrayList<Object>) configMap.get("contexts");
        ArrayList<Object> clusters = (ArrayList<Object>) configMap.get("clusters");
        ArrayList<Object> users = (ArrayList<Object>) configMap.get("users");

        return new KubeConfig(currentContext, contexts, clusters, users);
    }

    private List<String> getAllContextFromKubeFile() {
        var kubeConfig = loadKubeConfig();
        text = kubeConfig.getCurrentContext();
        if (myStatusBar != null) {
            myStatusBar.updateWidget(ID());
        }
        List<String> contextsNames = new ArrayList<>();
        kubeConfig.getContexts().forEach(context -> contextsNames.add(((LinkedHashMap<?, ?>) context).get("name").toString()));
        return contextsNames;
    }

    @Override
    public @Nullable("null means the widget is unable to show the popup") ListPopup getPopup() {
        return new ListPopupImpl(getProject(), new ContextsPopupStep());
    }

    @Override
    public @Nullable
    @NlsContexts.StatusBarText String getSelectedValue() {
        return text;
    }

    private class ContextsPopupStep extends BaseListPopupStep<String> {

        ContextsPopupStep() {
            super("K8s Contexts", getAllContextFromKubeFile());
        }

        @Override
        public @Nullable PopupStep<?> onChosen(String selectedValue, boolean finalChoice) {
            updateContextK8s(selectedValue);
            update();
            return super.onChosen(selectedValue, finalChoice);
        }

        private void updateContextK8s(String context) {
            String s;
            Process p;
            try {
                p = Runtime.getRuntime().exec("kubectl config use-context ".concat(context));
                BufferedReader br = new BufferedReader(
                        new InputStreamReader(p.getInputStream()));
                while ((s = br.readLine()) != null)
                    LOGGER.info("line: " + s);
                p.waitFor();
                int result = p.exitValue();
                if (result != 0) {
                    LOGGER.info("kubectl command result: " + result);
                    throw new ContextK8sException("kubectl command failed");
                }
                p.destroy();
            } catch (Exception e) {
                throw new ContextK8sException(e);
            }
        }
    }


}
