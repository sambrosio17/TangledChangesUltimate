package core.executor;

import core.beans.Commit;
import core.utils.RepoVisitor;
import org.repodriller.RepositoryMining;
import org.repodriller.filter.range.Commits;
import org.repodriller.scm.GitRemoteRepository;

import java.util.HashMap;

public class RemoteExtractor implements Extractor{

    HashMap<String, Commit> commitList;
    String repoUrl;

    public RemoteExtractor(String repoUrl){
        this.repoUrl=repoUrl;
        this.commitList=new HashMap<>();
    }


    @Override
    public HashMap<String, Commit> doExtract() {
        new RepositoryMining()
                .in(GitRemoteRepository.hostedOn(repoUrl).buildAsSCMRepository())
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
