package co.com.devlinx9.plugingk8sstatusbar;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.intellij.ide.IdeBundle;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorManagerEvent;
import com.intellij.openapi.fileEditor.impl.FileEditorManagerImpl;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.ListPopup;
import com.intellij.openapi.ui.popup.util.BaseListPopupStep;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.StatusBarWidget;
import com.intellij.openapi.wm.impl.status.EditorBasedWidget;
import com.intellij.ui.popup.list.ListPopupImpl;
import com.intellij.util.Consumer;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;

import java.awt.event.MouseEvent;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

class CurrentContextStatusBarWidget extends EditorBasedWidget implements StatusBarWidget.MultipleTextValuesPresentation {
    private String text;

    public CurrentContextStatusBarWidget(@NotNull Project project) {
        super(project);
    }

    @Override
    public void dispose() {
        super.dispose();
    }

    @Override
    public @NonNls @NotNull String ID() {
        return "currentContext";
    }

    @Override
    public @Nullable WidgetPresentation getPresentation() {
        return this;
    }

    @Override
    public @Nullable @NlsContexts.Tooltip String getTooltipText() {
        return null;
    }

    @Override
    public @Nullable Consumer<MouseEvent> getClickConsumer() {
        return null;
    }

    @Override
    public void install(@NotNull StatusBar statusBar) {
        super.install(statusBar);
        DumbService.getInstance(myProject).runWhenSmart(() -> update());
    }

    @Override
    public void selectionChanged(@NotNull FileEditorManagerEvent event) {
        update();
    }


    private void update() {
        text = getCurrentContextFromKubeFile();
        myStatusBar.updateWidget(ID());
    }

    private String getCurrentContextFromKubeFile() {
        var kubeConfig = loadKubeConfig();
        return kubeConfig.getCurrentContext();
    }

    /**
     * Load a Kubernetes config from a Reader
     */
    public static KubeConfig loadKubeConfig() {
        Yaml yaml = new Yaml(new SafeConstructor());
        Object config = null;
        try {
            config = yaml.load(new FileReader("/home/palfonso/.kube/config"));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        Map<String, Object> configMap = (Map<String, Object>) config;

        String currentContext = (String) configMap.get("current-context");
        ArrayList<Object> contexts = (ArrayList<Object>) configMap.get("contexts");
        ArrayList<Object> clusters = (ArrayList<Object>) configMap.get("clusters");
        ArrayList<Object> users = (ArrayList<Object>) configMap.get("users");
//        Object preferences = configMap.get("preferences");

        KubeConfig kubeConfig = new KubeConfig(currentContext, contexts, clusters, users);
        System.out.println(kubeConfig);
        return kubeConfig;
    }

    private List<String> getAllContextFromKubeFile() {
        var kubeConfig = loadKubeConfig();
        text = kubeConfig.getCurrentContext();
        myStatusBar.updateWidget(ID());
        List<String> contextsNames = new ArrayList<>();
        kubeConfig.getContexts().forEach(context -> {
            contextsNames.add(((LinkedHashMap) context).get("name").toString());
        });
        return contextsNames;
    }

    @Override
    public @Nullable("null means the widget is unable to show the popup") ListPopup getPopupStep() {
        return new ListPopupImpl(myProject, new RecentFilesPopupStep(myProject));
    }

    @Override
    public @Nullable @NlsContexts.StatusBarText String getSelectedValue() {
        return text;
    }

    private List<String> getSelectionHistory(FileEditorManagerImpl fileEditorManager) {

        return getAllContextFromKubeFile();
    }

    private class RecentFilesPopupStep extends BaseListPopupStep<String> {
        private final FileEditorManager fileEditorManager;

        public RecentFilesPopupStep(Project project) {
            this((FileEditorManagerImpl) FileEditorManagerImpl.getInstance(project));
        }

        private RecentFilesPopupStep(FileEditorManagerImpl fileEditorManager) {
            super(IdeBundle.message("title.popup.recent.files"), getSelectionHistory(fileEditorManager));
            this.fileEditorManager = fileEditorManager;
        }
    }
}
