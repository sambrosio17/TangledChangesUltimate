package window;

import Utils.StageExtractor;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vcs.CheckinProjectPanel;
import com.intellij.openapi.vcs.CommitMessageI;
import com.intellij.openapi.vcs.changes.CommitContext;
import com.intellij.openapi.vcs.checkin.CheckinHandler;
import com.intellij.openapi.vcs.checkin.CheckinHandlerFactory;
import com.intellij.openapi.vcs.ui.CommitMessage;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.RegisterToolWindowTask;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ToolWindowManager;
import core.beans.Commit;
import core.beans.CommitChange;
import core.beans.Partition;
import core.beans.StagedCommit;
import core.executor.Extractor;
import core.executor.LocalExtractor;
import core.executor.RemoteExtractor;
import core.untangler.Untangler;
import core.utils.StopConditionCalculator;
import core.utils.StringCostants;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.jetbrains.annotations.NotNull;


import javax.swing.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.*;

public class CommitHandler extends CheckinHandlerFactory {
    private final String toolName="PangeaUntangler";
    private List<Partition> result;
    private StagedCommit staged;
    private Git git;
    private StageExtractor stageExtractor;
    private List<String> nonJava;
    private String commitMessage;
    private CheckinProjectPanel pPanel;
    private ArrayList<String> commitMessages;
    private StopConditionCalculator stopConditionCalculator;

    @Override
    public @NotNull CheckinHandler createHandler(@NotNull CheckinProjectPanel panel, @NotNull CommitContext commitContext) {

        pPanel=panel;

        CommitHandler caller=this;
        final CheckinHandler ch = new CheckinHandler() {


            @Override
            public ReturnResult beforeCheckin() {


                String userdata=commitContext.getUserDataString();
                System.out.println(userdata);
                commitMessage= panel.getCommitMessage();
                //recupero del path della repository
                String repoPath = panel.getProject().getBasePath();
                Extractor extractor = new LocalExtractor(repoPath);
                HashMap<String, Commit> map = extractor.doExtract();
                result=null;
                try {
                    git=Git.open(new File(repoPath));
                    stageExtractor=new StageExtractor(repoPath,panel);
                    staged=stageExtractor.doExtract();
                    nonJava=stageExtractor.doExtractNonJava();
                    int stopCondition = new StopConditionCalculator(staged.getChanges().size()).doCalculate();
                    Untangler untangler = new Untangler(staged, map, stopCondition);
                    result = untangler.doUntangle();
                } catch (IOException | GitAPIException e) {
                    e.printStackTrace();
                }

                CommitWindow cw=new CommitWindow(panel.getProject(), result,caller);
                if(ToolWindowManager.getInstance(panel.getProject()).getToolWindow(toolName)!=null){
                    ToolWindowManager.getInstance(panel.getProject()).getToolWindow(toolName).remove();
                }
                ToolWindow tw=ToolWindowManager.getInstance(panel.getProject()).registerToolWindow(toolName,cw.mainPanel(), ToolWindowAnchor.BOTTOM);
                tw.show();
                //setta icona


                System.out.println("Repository: " + repoPath);
                //System.out.println("Commmit ID: "+commitId);
                System.out.println("La repository ha " + map.size() + " commit.");
                System.out.println("La stage area contiene " + staged.getChanges().size() + " file.");
                System.out.println("Partizioni Create: " + result.size());
                System.out.println(result);

                return ReturnResult.CLOSE_WINDOW;
            }





            @Override
            public void checkinSuccessful() {
                //super.checkinSuccessful();
            }
        };

        return ch;
    }

    public void doTangledCommit() throws GitAPIException {

        String userName=git.getRepository().getConfig().getString("user",null,"name");
        String userEmail=git.getRepository().getConfig().getString("user",null,"email");
        for (CommitChange c : staged.getChanges()) {
            if (c == null) continue;
            switch (c.getAction()) {
                case StringCostants.REMOVE:
                    git.rm().addFilepattern(c.getPath()).call();
                    break;
                case StringCostants.CHANGE:
                case StringCostants.MODIFY:
                case StringCostants.ADD:
                    git.add().addFilepattern(c.getPath()).call();
                    break;
                default:
                    break;
            }
        }
        for(String s: nonJava){
            git.add().addFilepattern(s).call();
        }
        git.commit().setMessage(commitMessage).setAuthor(userName, userEmail).call();
        ToolWindowManager.getInstance(pPanel.getProject()).getToolWindow(toolName).remove();
    }

    public void doUntagledCommit(){
        int i=0;
        try {
            for (Partition p : result) {
                for (String path : p.getPaths()) {
                    CommitChange c = staged.findOne(path);
                    if (c == null) continue;
                    switch (c.getAction()) {
                        case StringCostants.REMOVE:
                            git.rm().addFilepattern(path).call();
                            break;
                        case StringCostants.CHANGE:
                        case StringCostants.MODIFY:
                        case StringCostants.ADD:
                            git.add().addFilepattern(path).call();
                            break;
                        default:
                            break;
                    }
                }
                if(!commitMessages.get(i).isEmpty()){
                    git.commit().setMessage(commitMessages.get(i)).setAuthor("Untangler", "info@untangler.it").call();
                }
                else
                    git.commit().setMessage("UNTANGLED COMMIT #"+p.getId()+" -- "+ new Date()).setAuthor("Untangler", "info@untangler.it").call();
                i++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }



        try {
            //faccio commit dei file non java
            if(!nonJava.isEmpty()){
                //Aggiungo in un commit tutti i file non java
                for (String s : nonJava) {
                    git.add().addFilepattern(s).call();
                }
                git.commit().setMessage("UNTANGLED COMMIT [NON JAVA FILES]"+" -- "+ new Date()).setAuthor("Untangler", "info@untangler.it").call();
            }


        } catch (GitAPIException e) {
            e.printStackTrace();
        }

        ToolWindowManager.getInstance(pPanel.getProject()).getToolWindow(toolName).remove();
    }

    public void setMessages(ArrayList<String> messages){
        this.commitMessages=messages;
    }
}
