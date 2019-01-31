package sample;

import javax.script.*;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ScriptLoader {

    ScriptEngineManager factory;
    ScriptEngine engine ;
    HashMap<String,ListItem> functionMetaData;

    public ScriptLoader(){
        this.factory = new ScriptEngineManager();
        this.engine = factory.getEngineByName("nashorn");
        this.functionMetaData = new HashMap<>();
    }


    private String evaluateScript(String scriptBody){

        try {
            engine.eval(scriptBody).toString();
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

            //System.out.println(lastNameIndex);

            int startIndex = 0;
            int lastIndex = scriptBody.indexOf('}');

            lastNameIndex = scriptBody.indexOf(')')+1;

            if(lastIndex < 0 || lastNameIndex < 0) return;

            String functionDeclaration = scriptBody.substring(startIndex, lastNameIndex);


            String functionName = getFunctionName(functionDeclaration);

            String[] parameters = getParameterNames(functionDeclaration);
            //if(parameters == null) break;
            //else{

            List<String> parList;
                if(parameters == null){
                    parList = null;
                }else{
                    parList = Arrays.asList(parameters);
                }
                secureAdd(functionDeclaration,new ListItem(functionName,parList));
                //secureAdd(new ListItem(functionDeclaration,Arrays.asList(parameters)));
            //}



           // System.out.println(Arrays.asList(parameters));

            scriptBody = scriptBody.substring(lastIndex + 1);

        }
    }


    private String getFunctionName(String functionDeclatation){

        int firstLetterIndex = functionDeclatation.indexOf(" ")+1;
        int lastIndex = functionDeclatation.indexOf("(");


        String functionName = functionDeclatation.substring(firstLetterIndex,lastIndex);

        return functionName;

    }

    private String[] getParameterNames(String functionDeclaration){

        int startIndex = functionDeclaration.indexOf('(')+1;
        int endIndex = functionDeclaration.indexOf(')');

        if(startIndex < 0 || endIndex < 0 || startIndex==endIndex) return null;
        else{

            String parametersRaw = functionDeclaration.substring(
                    startIndex,
                    endIndex

            );

            String[] parameters = parametersRaw.split(",");
            return parameters;
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

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);
        PrintStream old = System.out;
        System.setOut(ps);

        Invocable invocable = (Invocable) engine;
        String result ="ok";
        //System.out.println(functionKey + " " + functionMetaData.toString());
        String functionName = functionMetaData.get(functionKey).getFunctionName();

        try {
           result = String.valueOf(invocable.invokeFunction(functionName,args));
        } catch (Exception e) {
           result = e.getMessage();
        }

        System.out.flush();
        result = baos.toString() + "\n" + result;

        System.out.flush();
        System.setOut(old);


        return result;
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
        List<String> functionDeclarationList = new LinkedList<>();

        for(String key : functionMetaData.keySet()){
            functionDeclarationList.add(key);
        }

//        }
        return functionDeclarationList;
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
