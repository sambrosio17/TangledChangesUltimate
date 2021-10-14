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
import core.beans.Commit;
import core.beans.CommitChange;
import core.beans.Partition;
import core.beans.StagedCommit;
import core.executor.Extractor;
import core.executor.LocalExtractor;
import core.executor.RemoteExtractor;
import core.untangler.Untangler;
import core.utils.StringCostants;
import org.eclipse.jgit.api.CheckoutCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.kotlin.ir.interpreter.Return;
import window.CommitWindow;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.*;

public class CommitHandler extends CheckinHandlerFactory {
    private List<Partition> result;
    private StagedCommit staged;
    private Git git;
    private StageExtractor stageExtractor;
    private List<String> nonJava;
    private String commitMessage;
    @Override
    public @NotNull CheckinHandler createHandler(@NotNull CheckinProjectPanel panel, @NotNull CommitContext commitContext) {

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
                    int stopCondition = staged.getChanges().size() / 3;
                    Untangler untangler = new Untangler(staged, map, 3);
                    result = untangler.doUntangle();
                } catch (IOException | GitAPIException e) {
                    e.printStackTrace();
                }

                new CommitWindow(panel.getProject(), result,caller).setVisible(true);

                System.out.println("Repository: " + repoPath);
                //System.out.println("Commmit ID: "+commitId);
                System.out.println("La repository ha " + map.size() + " commit.");
                System.out.println("La stage area contiene " + staged.getChanges().size() + " file.");
                System.out.println("Partizioni Create: " + result.size());
                System.out.println(result);




                /*
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
                        git.commit().setMessage("N:" + new Date().toString() + p.getId()).setAuthor("sasino", "s@outlook.it").call();
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
                        git.commit().setMessage("NON JAVA FILE").setAuthor("sax", "sax").call();
                    }


                } catch (GitAPIException e) {
                    e.printStackTrace();
                }
                 */

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
    }

    public void doUntagledCommit(){
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
                git.commit().setMessage("UNTANGLED COMMIT #"+p.getId()+" -- "+new Date().toString()).setAuthor("Untangler", "info@untangler.it").call();
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
                git.commit().setMessage("UNTANGLED COMMIT [NON JAVA FILES]"+" -- "+new Date().toString()).setAuthor("Untangler", "info@untangler.it").call();
            }


        } catch (GitAPIException e) {
            e.printStackTrace();
        }
    }
}
