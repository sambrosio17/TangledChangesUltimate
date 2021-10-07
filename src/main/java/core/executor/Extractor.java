package core.executor;

import core.beans.Commit;

import java.util.HashMap;

public interface Extractor {

    public HashMap<String, Commit> doExtract();
    public HashMap<String, Commit> getCommitList();
    public void setCommitList(HashMap<String, Commit> commitList);
    public String getRepoUrl();
    public void setRepoUrl(String repoUrl);
}
