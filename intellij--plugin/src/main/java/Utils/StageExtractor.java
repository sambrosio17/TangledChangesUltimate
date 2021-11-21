package Utils;

import com.intellij.openapi.vcs.CheckinProjectPanel;
import com.intellij.openapi.vfs.VirtualFile;
import core.beans.CommitChange;
import core.beans.StagedCommit;
import core.utils.StringCostants;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import java.util.*;
import java.io.File;
import java.io.IOException;

public class StageExtractor {

    private final CheckinProjectPanel panel;
    private final StagedCommit staged;
    private final String repoPath;
    private final Git git;

    public StageExtractor(String repoPath, CheckinProjectPanel panel) throws IOException {
        staged=new StagedCommit();
        this.repoPath=repoPath;
        this.panel=panel;
        git= Git.open(new File(repoPath));
    }

    public StagedCommit doExtract() throws GitAPIException {

        for (String path : git.status().call().getAdded()) {
            if (!path.contains(".java")) continue;
            CommitChange change = new CommitChange(path, StringCostants.ADD);
            staged.getChanges().add(change);
        }
        for (String path : git.status().call().getModified()) {
            if (!path.contains(".java")) continue;
            CommitChange change = new CommitChange(path, StringCostants.MODIFY);
            staged.getChanges().add(change);
        }

        for (String path : git.status().call().getRemoved()) {
            if (!path.contains(".java")) continue;
            CommitChange change = new CommitChange(path, StringCostants.REMOVE);
            staged.getChanges().add(change);
        }
        for (String path : git.status().call().getChanged()) {
            if (!path.contains(".java")) continue;
            CommitChange change = new CommitChange(path, StringCostants.CHANGE);
            staged.getChanges().add(change);
        }

        return staged;

    }

    public List<String> doExtractNonJava() throws GitAPIException {
        List<String> nonJava=new ArrayList<>();
        for(VirtualFile vf  : panel.getVirtualFiles()){
            if(vf.getPath().contains(".java")) continue;
            nonJava.add(vf.getPath().replace(repoPath+"/",""));
        }
        git.reset().call();
        return nonJava;
    }
}
