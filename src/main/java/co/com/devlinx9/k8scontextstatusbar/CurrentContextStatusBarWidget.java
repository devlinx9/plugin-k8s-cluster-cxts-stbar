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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.logging.Logger;

import static co.com.devlinx9.k8scontextstatusbar.KubeUtils.loadKubeConfig;
import static co.com.devlinx9.k8scontextstatusbar.KubeUtils.updateContextK8s;
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
    }
}
