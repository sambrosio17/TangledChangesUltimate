import com.intellij.openapi.util.Key;
import com.intellij.openapi.vcs.CheckinProjectPanel;
import com.intellij.openapi.vcs.changes.CommitContext;
import com.intellij.openapi.vcs.checkin.CheckinHandler;
import com.intellij.openapi.vcs.checkin.CheckinHandlerFactory;
import core.beans.Commit;
import core.beans.CommitChange;
import core.beans.Partition;
import core.beans.StagedCommit;
import core.executor.Extractor;
import core.executor.LocalExtractor;
import core.executor.RemoteExtractor;
import core.untangler.Untangler;
import core.utils.StringCostants;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.jetbrains.annotations.NotNull;
import window.CommitWindow;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;

public class CommitHandler extends CheckinHandlerFactory {
    List<Partition> result;
    StagedCommit staged;
    Git git;
    @Override
    public @NotNull CheckinHandler createHandler(@NotNull CheckinProjectPanel panel, @NotNull CommitContext commitContext) {

        final CheckinHandler ch= new CheckinHandler() {
            @Override
            public ReturnResult beforeCheckin() {

                //recupero del path della repository
                String repoPath=panel.getProject().getBasePath();
                Extractor extractor=new LocalExtractor(repoPath);
                HashMap<String, Commit> map=extractor.doExtract();

                git=null;
                try {

                    git = Git.open(new File(repoPath));
                } catch (IOException e) {
                    e.printStackTrace();
                    //poi faremo qualocosa
                }

                StagedCommit staged= new StagedCommit();

                try {
                    for(String path : git.status().call().getAdded()){
                        if(!path.contains(".java")) continue;
                        CommitChange change=new CommitChange(path, StringCostants.ADD);
                        staged.getChanges().add(change);
                    }
                    for(String path : git.status().call().getModified()){
                        if(!path.contains(".java")) continue;
                        CommitChange change=new CommitChange(path, StringCostants.MODIFY);
                        staged.getChanges().add(change);
                    }

                    for(String path : git.status().call().getRemoved()){
                        if(!path.contains(".java")) continue;
                        CommitChange change=new CommitChange(path, StringCostants.REMOVE);
                        staged.getChanges().add(change);
                    }
                    for(String path : git.status().call().getChanged()){
                        if(!path.contains(".java")) continue;
                        CommitChange change=new CommitChange(path, StringCostants.CHANGE);
                        staged.getChanges().add(change);
                    }
                } catch (GitAPIException e) {
                    e.printStackTrace();
                }

                int stopCondition= staged.getChanges().size()/3;
                Untangler untangler=new Untangler(staged,map,3);
                List<Partition> result=null;
                try {
                    result = untangler.doUntangle();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

                System.out.println("Repository: "+repoPath);
                //System.out.println("Commmit ID: "+commitId);
                System.out.println("La repository ha "+map.size()+" commit.");
                System.out.println("La stage area contiene "+staged.getChanges().size()+" file.");
                System.out.println("Partizioni Create: "+result.size());
                System.out.println(result);


                panel.getVirtualFiles().clear();
                new CommitWindow(panel.getProject(),result).setVisible(true);

                try {
                    git.reset().call();
                    System.out.println(git.status().call().getUncommittedChanges());
                } catch (GitAPIException e) {
                    e.printStackTrace();
                }

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
                        git.commit().setMessage("buonaseraaaaaaaa"+p.getId()).setAuthor("sasino", "s@outlook.it").call();
                    }
                }
                catch (Exception e){
                    e.printStackTrace();
                }



                return super.beforeCheckin();

            }

            @Override
            public void checkinSuccessful() {


                //super.checkinSuccessful();
            }
        };

        return ch;
    }
}
