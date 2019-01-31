package sample;

import javax.script.*;
import java.io.*;
import java.util.*;

public class ScriptLoader {

    private ScriptEngineManager factory;
    private ScriptEngine engine ;
    private HashMap<String,ListItem> functionMetaData;
    private ScriptContext context;
    private StringWriter stringWriter;
    private PrintWriter printWriter;

    public ScriptLoader(){
        this.factory = new ScriptEngineManager();
        this.engine = factory.getEngineByName("nashorn");
        this.functionMetaData = new HashMap<>();
        this.context = engine.getContext();
        this.stringWriter = new StringWriter();
        this.printWriter = new PrintWriter(stringWriter, true);
        context.setWriter(printWriter);
        context.setErrorWriter(printWriter);
    }


    private String evaluateScript(String scriptBody){

        try {
            engine.eval(scriptBody);
        } catch (ScriptException e) {
            return e.getMessage();
        }

        return null;
    }

    private void getNames(String scriptBody){

        scriptBody = scriptBody.trim().replaceAll(" +", " ");
        scriptBody = scriptBody.trim().replaceAll("\n+", "");

        int lastNameIndex = scriptBody.length();

        while(lastNameIndex > 0) {

            int startIndex = 0;
            int lastIndex = scriptBody.indexOf('}');

            lastNameIndex = scriptBody.indexOf(')')+1;

            if(lastIndex < 0 || lastNameIndex < 0) return;

            String functionDeclaration = scriptBody.substring(startIndex, lastNameIndex);


            String functionName = getFunctionName(functionDeclaration);

            String[] parameters = getParameterNames(functionDeclaration);

            List<String> parList;
                if(parameters == null){
                    parList = null;
                }else{
                    parList = Arrays.asList(parameters);
                }
                secureAdd(functionDeclaration,new ListItem(functionName,parList));

            scriptBody = scriptBody.substring(lastIndex + 1);

        }
    }


    private String getFunctionName(String functionDeclatation){

        int firstLetterIndex = functionDeclatation.indexOf(" ")+1;
        int lastIndex = functionDeclatation.indexOf("(");


        return functionDeclatation.substring(firstLetterIndex,lastIndex);

    }

    private String[] getParameterNames(String functionDeclaration){

        int startIndex = functionDeclaration.indexOf('(')+1;
        int endIndex = functionDeclaration.indexOf(')');

        if(startIndex == 0 || endIndex < 0 || startIndex == endIndex) return null;
        else{

            String parametersRaw = functionDeclaration.substring(
                    startIndex,
                    endIndex

            );

            return parametersRaw.split(",");
        }
    }


    private void secureAdd(String newKey, ListItem newListItem){

        for(String key : functionMetaData.keySet()){

            String functionName = functionMetaData.get(key).getFunctionName();

            if(functionName.equals(newListItem.functionName)){
                functionMetaData.remove(key);
                functionMetaData.put(newKey,newListItem);
                return;
            }

        }

        functionMetaData.put(newKey,newListItem);


    }


    public String invokeFunction(String functionKey, Object[] args){
        String result;
        stringWriter.getBuffer().setLength(0);

        Invocable invocable = (Invocable) engine;

        String functionName = functionMetaData.get(functionKey).getFunctionName();

        try {
           result = String.valueOf(invocable.invokeFunction(functionName,args));
        } catch (Exception e) {
           result = e.getMessage();
        }

        return stringWriter.toString() + result;
    }



    public void printAllListItems(){

        for(String key : functionMetaData.keySet()){
            ListItem item = functionMetaData.get(key);
            System.out.println(item.getFunctionName());
            if(item.parameterNames == null) continue;
            System.out.println(item.parameterNames.size());
           for(String parName : item.parameterNames)
                System.out.println(parName);
        }


    }


    public List<String> getLoadedFunctions(){

        return new LinkedList<>(functionMetaData.keySet());
    }


    public String loadNewScript(String scriptBody) {

        String errorText = evaluateScript(scriptBody);

        if(errorText != null){
            return errorText;
        }else{
            getNames(scriptBody);

            //TODO obsluga zapisu do listy
        }

    return null;
    }
}
