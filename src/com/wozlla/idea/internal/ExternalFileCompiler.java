package com.wozlla.idea.internal;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.wozlla.idea.IExternalFileCompiler;
import com.wozlla.idea.WozllaIDEAPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.List;

public class ExternalFileCompiler implements IExternalFileCompiler, ProjectComponent {

    private Project project;
    private VirtualFile compileShellFile;

    private List<ICompilerListener> listeners = new ArrayList<ICompilerListener>();

    public ExternalFileCompiler(Project project) {
        this.project = project;
    }

    @Override
    public void addCompilerListener(ICompilerListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeCompilerListener(ICompilerListener listener) {
        listeners.remove(listener);
    }

    protected void dispatchError(String errorMsg) {
        for(ICompilerListener listener : listeners) {
            listener.onError(errorMsg);
        }
    }

    protected void dispatchSuccess() {
        for(ICompilerListener listener : listeners) {
            listener.onSuccess();
        }
    }

    public void initCompiler() {
        VirtualFile shellFile = project.getBaseDir().findFileByRelativePath(WozllaIDEAPlugin.COMPILE_SHELL_PATH);
        if(shellFile == null) {
            throw new RuntimeException("Fail to init compiler: compile file not exists");
        }
        String path = shellFile.getPath();
        try {
            Process chmod = Runtime.getRuntime().exec("chmod 777 " + path);
            chmod.waitFor();
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
        compileShellFile = shellFile;
    }

    public void compile() {
        ApplicationManager.getApplication().runWriteAction(new Runnable() {
            @Override
            public void run() {
                try {
                    String shellPath = compileShellFile.getPath();
                    Process ps = Runtime.getRuntime().exec(shellPath);
                    InputStreamReader ir = new InputStreamReader(ps.getInputStream());
                    LineNumberReader input = new LineNumberReader(ir);
                    String line;
                    ps.waitFor();
                    String result = "";
                    while ((line = input.readLine()) != null) {
                        result += line + "\n\r";
                    }
                    VirtualFile editorDir = project.getBaseDir().findChild(WozllaIDEAPlugin.EDITOR_DIR_NAME);
                    if (editorDir != null) {
                        editorDir.refresh(true, true);
                    }
                    dispatchSuccess();
                } catch(Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    @Override
    public void projectOpened() {
        VirtualFile identifyFile = project.getBaseDir().findFileByRelativePath(WozllaIDEAPlugin.IDENTIFY_FILE_PATH);
        System.out.println("Open " + identifyFile);
        if(identifyFile != null) {
            WozllaIDEAPlugin.isWozllaProject = true;
            initCompiler();
        }
    }

    @Override
    public void projectClosed() {

    }

    @Override
    public void initComponent() {

    }

    @Override
    public void disposeComponent() {

    }

    @NotNull
    @Override
    public String getComponentName() {
        return ExternalFileCompiler.class.getName();
    }
}
