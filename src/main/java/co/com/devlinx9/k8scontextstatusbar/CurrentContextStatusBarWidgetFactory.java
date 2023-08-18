package co.com.devlinx9.k8scontextstatusbar;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.StatusBarWidget;
import com.intellij.openapi.wm.impl.status.widget.StatusBarEditorBasedWidgetFactory;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

public class CurrentContextStatusBarWidgetFactory extends StatusBarEditorBasedWidgetFactory {
    @Override
    public @NonNls @NotNull String getId() {
        return "ClusterK8s";
    }

    @Override
    public @Nls @NotNull String getDisplayName() {
        return "Cluster k8s";
    }

    @Override
    public @NotNull StatusBarWidget createWidget(@NotNull Project project) {
        return new CurrentContextStatusBarWidget(project);
    }
}
