package core.utils;

import core.beans.CommitChange;
import core.executor.Extractor;
import org.repodriller.domain.Commit;
import org.repodriller.domain.Modification;
import org.repodriller.persistence.PersistenceMechanism;
import org.repodriller.scm.CommitVisitor;
import org.repodriller.scm.SCMRepository;

import java.util.HashMap;

public class RepoVisitor implements CommitVisitor {

    Extractor caller;

    public RepoVisitor(Extractor caller){
        this.caller=caller;
    }

    @Override
    public void process(SCMRepository scmRepository, Commit commit, PersistenceMechanism persistenceMechanism) {

        HashMap<String, core.beans.Commit> list=caller.getCommitList();

        for(Modification m : commit.getModifications()) {

            if(!m.getFileName().contains(".java")) continue;

            if(list.containsKey(commit.getHash())){
                CommitChange change=new CommitChange();
                change.setPath(m.getFileName());
                change.setAction(m.getType().toString());
                list.get(commit.getHash()).getChanges().add(change);
            }
            else{
                core.beans.Commit currentCommit= new core.beans.Commit();
                currentCommit.setId(commit.getHash());
                currentCommit.setAuthor(commit.getAuthor().getName());
                CommitChange currentChange=new CommitChange();
                currentChange.setPath(m.getFileName());
                currentChange.setAction(m.getType().toString());
                currentCommit.getChanges().add(currentChange);
                list.put(currentCommit.getId(),currentCommit);
            }

        }
        caller.setCommitList(list);

    }
}
