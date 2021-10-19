package cli;



import core.beans.Commit;
import core.beans.Partition;
import core.executor.Extractor;
import core.executor.RemoteExtractor;
import core.untangler.Untangler;
import org.apache.commons.cli.*;

import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;

public class Main {

    public static void main(String[] args) throws FileNotFoundException, UnsupportedEncodingException {

        CommandLineParser parser=new DefaultParser();


        Option s =new Option("s","show",true,"show info about repo");
        Options options=new Options();
        options.addOption("s","show",true,"show info about repository");
        options.addOption("d","detect",true,"start detection");
        options.addOption("h","help",false,"show help");

        try{
            CommandLine cl= parser.parse(options,args);
            if(cl.hasOption("h")){
                System.out.println("Usage: $programName [-h] [-s repositoryUrl commitId] [-d repositoryUrl commitId stopCondition]");
                System.out.println("\t\t\t\t\t -h --help\t\t\t\t\t\t\t\t\t\t\t\t to show help");
                System.out.println("\t\t\t\t\t -s --show \t\trepositoryUrl commitId\t\t\t\t\t to show generic information about repositoryUrl");
                System.out.println("\t\t\t\t\t -d --detect \trepositoryUrl commitId stopCondition\t to start detection");
                return;
            }
            if(cl.hasOption("s")){
                String repoUrl=args[1];
                String commitId=args[2];
                System.out.println(repoUrl);
                Extractor extractor=new RemoteExtractor(repoUrl);
                HashMap<String, Commit> list=extractor.doExtract();

                System.out.println("Your repository has: "+list.size()+" commit(s)");
                System.out.println("Commit: "+commitId+" has: "+list.get(commitId).getChanges().size()+" file(s)");
                return;
            }
            if(cl.hasOption("d")){
                String repoUrl=args[1];
                String commitId=args[2];
                String stop=args[3];

                int stopCondition=Integer.parseInt(stop);

                Extractor extractor=new RemoteExtractor(repoUrl);
                HashMap<String, Commit> list=extractor.doExtract();

                System.out.println("Your repository has: "+list.size()+" commit(s)");
                System.out.println("Commit: "+commitId+" has: "+list.get(commitId).getChanges().size()+" file(s)");

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

                return;
            }


        }catch (Exception e){
            e.printStackTrace();
            System.out.println("No option with this name");
            return;
        }


    }
}
