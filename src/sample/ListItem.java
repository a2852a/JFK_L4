package sample;

import java.util.List;

public class ListItem {

    String functionName;
    List<String> parameterNames;

    public ListItem(String functionName, List<String> parameterNames){
        this.functionName = functionName;
        this.parameterNames = parameterNames;
    }

    public ListItem(){

    }


    public String getFunctionName() {
        return functionName;
    }

    public void setFunctionName(String functionName) {
        this.functionName = functionName;
    }


    public List<String> getParameterNames() {
        return parameterNames;
    }

    public void setParameterNames(List<String> parameterNames) {
        this.parameterNames = parameterNames;
    }





}
