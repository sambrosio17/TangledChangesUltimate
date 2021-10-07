package core.executor;



import core.beans.Commit;
import core.utils.RepoVisitor;
import org.repodriller.RepositoryMining;
import org.repodriller.filter.range.Commits;
import org.repodriller.scm.GitRemoteRepository;
import org.repodriller.scm.GitRepository;

import java.util.HashMap;

public class LocalExtractor implements Extractor{

    HashMap<String, Commit> commitList;
    String repoUrl;

    public LocalExtractor(String repoUrl){
        this.repoUrl=repoUrl;
    }

    public HashMap<String, Commit> doExtract(){

        new RepositoryMining()
                .in(GitRepository.singleProject(repoUrl))
                .through(Commits.all())
                .process(new RepoVisitor(this))
                .mine();

        return commitList;
    }

    public HashMap<String, Commit> getCommitList() {
        return commitList;
    }

    public void setCommitList(HashMap<String, Commit> commitList) {
        this.commitList = commitList;
    }

    public String getRepoUrl() {
        return repoUrl;
    }

    public void setRepoUrl(String repoUrl) {
        this.repoUrl = repoUrl;
    }
}
