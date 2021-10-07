package cli;



import core.beans.Commit;
import core.beans.Partition;
import core.executor.Extractor;
import core.executor.RemoteExtractor;
import core.untangler.Untangler;

import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) throws FileNotFoundException, UnsupportedEncodingException {

        Scanner in=new Scanner(System.in);
        System.out.println("Insert Repository Url: ");
        String repoUrl=in.nextLine();
        System.out.println("Insert Commit ID: ");
        String commitId=in.nextLine();

        repoUrl="https://github.com/sambrosio17/TangledChangesV2.git";
        commitId="ef0520ff6af5db408a0cb6b7a299dea378b0d6c7";
        Extractor extractor=new RemoteExtractor(repoUrl);
        HashMap<String, Commit> list=extractor.doExtract();

        System.out.println("Your repository has: "+list.size()+" commit(s)");
        System.out.println("Commit: "+commitId+" has: "+list.get(commitId).getChanges().size()+" file(s)");

        System.out.println("How many untangled commit would you like to create?");
        int stopCondition=Integer.parseInt("3");

        Untangler untangler=new Untangler(list.get(commitId),list,stopCondition);
        List<Partition> result= untangler.doUntangle();

        System.out.println("Created untangled commmit(s): "+ result.size());
        System.out.println("Proposed untangled commit(s): ");

        System.out.println("*************************");
        for(int i=0; i<result.size(); i++){
            Partition p=result.get(i);
            System.out.println("Untangled Commit #"+i);
            System.out.println("Paths: ");
            for(String path: p.getPaths()){
                System.out.println("\t"+path);
            }
            System.out.println("*************************");
        }


    }
}
