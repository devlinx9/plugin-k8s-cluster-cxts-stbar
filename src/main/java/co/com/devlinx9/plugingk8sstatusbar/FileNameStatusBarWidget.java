package co.com.devlinx9.plugingk8sstatusbar;

import com.intellij.ide.IdeBundle;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorManagerEvent;
import com.intellij.openapi.fileEditor.impl.FileEditorManagerImpl;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.ListPopup;
import com.intellij.openapi.ui.popup.util.BaseListPopupStep;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.newvfs.VfsPresentationUtil;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.StatusBarWidget;
import com.intellij.openapi.wm.impl.status.EditorBasedWidget;
import com.intellij.ui.popup.list.ListPopupImpl;
import com.intellij.util.Consumer;
import com.intellij.util.SlowOperations;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

class FileNameStatusBarWidget extends EditorBasedWidget implements StatusBarWidget.MultipleTextValuesPresentation {
    private String text;

    public FileNameStatusBarWidget(@NotNull Project project) {
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
        String file = "/home/palfonso/.kube/config";
        String currentContext = "None";

        try (BufferedReader br
                     = new BufferedReader(new FileReader(file))) {

            String line;
            while ((line = br.readLine()) != null) {
                if (line.contains("current-context: ")) {
                    currentContext = line.replace("current-context: ", "");
                    break;
                }
            }

        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return currentContext;
    }

    private String getFileTitle(VirtualFile file) {
        return SlowOperations.allowSlowOperations(() -> VfsPresentationUtil.getUniquePresentableNameForUI(myProject, file));
    }

    @Override
    public @Nullable("null means the widget is unable to show the popup") ListPopup getPopupStep() {
        return new ListPopupImpl(myProject, new RecentFilesPopupStep(myProject));
    }

    @Override
    public @Nullable @NlsContexts.StatusBarText String getSelectedValue() {
        return text;
    }

    private static List<String> getSelectionHistory(FileEditorManagerImpl fileEditorManager) {

        return List.of("papsdf", "asdfasdfasdf");
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
