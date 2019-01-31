package sample;

import javax.script.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ScriptLoader {

    ScriptEngineManager factory;
    ScriptEngine engine ;
    LinkedList<ListItem> functionMetaData;


    public ScriptLoader(){
        this.factory = new ScriptEngineManager();
        this.engine = factory.getEngineByName("nashorn");
        this.functionMetaData = new LinkedList<>();
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

        int lastNameIndex = scriptBody.length();

        while(lastNameIndex > 0) {

            //System.out.println(lastNameIndex);

            int startIndex = 0;
            int lastIndex = scriptBody.indexOf('}');

            lastNameIndex = scriptBody.indexOf(')')+1;

            String functionDeclaration = scriptBody.substring(startIndex, lastNameIndex);


            String[] parameters = getParameterNames(functionDeclaration);
            if(parameters == null) break;
            else{
                secureAdd(new ListItem(functionDeclaration,Arrays.asList(parameters)));
            }


           // System.out.println(Arrays.asList(parameters));

            scriptBody = scriptBody.substring(lastIndex + 1);

        }
    }

    private String[] getParameterNames(String functionDeclaration){

        int startIndex = functionDeclaration.indexOf('(')+1;
        int endIndex = functionDeclaration.indexOf(')');

        if(startIndex < 0 || endIndex < 0) return null;
        else{

            String parametersRaw = functionDeclaration.substring(
                    startIndex,
                    endIndex

            );

            String[] parameters = parametersRaw.split(",");
            return parameters;
        }
    }



    @Deprecated
    private List getFunctionName(String scriptBody){
        List<String> allMatches = new ArrayList<>();
        String scriptBodyTmp = scriptBody.trim().replaceAll(" +", " ");
        Matcher m = Pattern.compile("function [$A-Za-z_][0-9a-zA-Z_$]*")
                .matcher(scriptBodyTmp);
        while (m.find()) {
            String functionName = m.group().split(" ")[1];
            //getParametersName(scriptBody,functionName);

            String func = "(function(reComments, reParams, reNames) {\n" +
                    "  getParamNames = function(fn) {\n" +
                    "    return ((fn + '').replace(reComments, '').match(reParams) || [0, ''])[1].match(reNames) || [];\n" +
                    "  };\n" +
                    "})(\n" +
                    "  /\\/\\*[\\s\\S]*?\\*\\/|\\/\\/.*?[\\r\\n]/g,\n" +
                    "  /\\(([\\s\\S]*?)\\)/,\n" +
                    "  /[$\\w]+/g\n" +
                    ");";



            try {
                engine.eval(func);
                Invocable invocable = (Invocable) engine;
                String results =  invocable.invokeFunction("getParamNames",functionName).toString();

                System.out.println(Arrays.asList(results));

            } catch (ScriptException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }

            //allMatches.add(m.group().split(" ")[1]);
        }

        return allMatches;
    }

    @Deprecated
    private void getParametersName(String scriptBody, String functionName){
        String scriptBodyTmp = scriptBody.trim().replaceAll(" +", "");
        int index = scriptBodyTmp.indexOf(functionName);
        try {

            String result = scriptBodyTmp.substring(index + functionName.length() + 1, scriptBodyTmp.indexOf(')'));


            String[] parameters = result.split(",");

            System.out.println(Arrays.asList(parameters).toString());

            secureAdd(new ListItem(functionName,Arrays.asList(parameters)));
            //functionMetaData.add(new ListItem(functionName,Arrays.asList(parameters)));
        }catch (StringIndexOutOfBoundsException e){
           // System.out.println("TUTAJ");
            secureAdd(new ListItem(functionName,null));
            //functionMetaData.add(new ListItem(functionName,null));
        }


        //return Arrays.asList(parameters);
    }

    private void secureAdd(ListItem newListItem){

        for(ListItem listItem : functionMetaData){
            if(listItem.functionName.equals(newListItem.functionName)){
                functionMetaData.remove(listItem);
                functionMetaData.add(newListItem);
                return;
            }
        }

        functionMetaData.add(newListItem);

    }




    public void printAllListItems(){

        for(ListItem listItem : functionMetaData){
            System.out.println(listItem.getFunctionName());
            if(listItem.parameterNames == null) return;
            for(String parName : listItem.parameterNames)
                System.out.println(parName);
        }



    }


    public List<String> getLoadedFunctions(){
        List<String> functionDeclarationList = new LinkedList<>();
        for(ListItem item : functionMetaData){
            functionDeclarationList.add(item.functionName);
        }
        return functionDeclarationList;
    }


    public String loadNewScript(String scriptBody) {

        String errorText = evaluateScript(scriptBody);

        if(errorText != null){
            return errorText;
        }else{
            getNames(scriptBody);
           // getFunctionName(scriptBody);
           // String function

            //engine.put("functionName",scriptBody);
            //functionMetaData.add("functionName");



           // engine.eval("functionName")

            //TODO obsluga zapisu do listy
        }

    return null;
    }
}
