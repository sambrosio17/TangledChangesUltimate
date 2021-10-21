package core.utils;

public class StopConditionCalculator {

    private int numberOfFiles;

    public StopConditionCalculator(int numberOfFiles){
        this.numberOfFiles=numberOfFiles;
    }

    public int doCalculate(){

        if(numberOfFiles<3){
            return 1;
        }
        return (numberOfFiles/3)+1;
    }
}
