package core.executor;

import core.beans.Commit;

import java.util.HashMap;

public interface Extractor {

    HashMap<String, Commit> doExtract();
    HashMap<String, Commit> getCommitList();
    void setCommitList(HashMap<String, Commit> commitList);
    String getRepoUrl();
    void setRepoUrl(String repoUrl);
}
