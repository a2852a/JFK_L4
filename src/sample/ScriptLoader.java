package sample;

import javafx.scene.text.Font;

import javax.script.*;
import java.io.*;
import java.util.*;

public class ScriptLoader {

    private ScriptEngineManager factory;
    private ScriptEngine engineNashorn;
    private ScriptEngine engineGroovy;
    private HashMap<String, ListItem> nashornFunctionMetaData;
    private HashMap<String, ListItem> groovyFunctionMetaData;
    private ScriptContext nashornContext;
    private ScriptContext groovyContext;
    private StringWriter stringWriter;
    private PrintWriter printWriter;
    private ScriptLangType scriptLangType;

    public Controller controller;

    public ScriptLoader(Controller controller) {
        this.factory = new ScriptEngineManager();
        this.controller = controller;
        this.engineGroovy = factory.getEngineByName("groovy");
        this.engineNashorn = factory.getEngineByName("nashorn");
        this.nashornFunctionMetaData = new HashMap<>();
        this.groovyFunctionMetaData = new HashMap<>();
        this.nashornContext = engineNashorn.getContext();
        this.groovyContext = engineGroovy.getContext();
        this.stringWriter = new StringWriter();
        this.printWriter = new PrintWriter(stringWriter, true);
        this.scriptLangType = ScriptLangType.NASHORN;
        nashornContext.setWriter(printWriter);
        nashornContext.setErrorWriter(printWriter);
        groovyContext.setWriter(printWriter);
        groovyContext.setErrorWriter(printWriter);
    }

    public void setScriptLangType(ScriptLangType scriptLangType) {
        this.scriptLangType = scriptLangType;
    }

    enum ScriptLangType {
        NASHORN(0),
        GROOVY(1);
        private int number;

        public int getNumber() {
            return number;
        }

        ScriptLangType(int number) {
            this.number = number;
        }
    }


    private String evaluateScript(String scriptBody) {

        try {
            if (scriptLangType == ScriptLangType.NASHORN) {
                engineNashorn.put("controller", controller);
                //controller.getTextArea().setFont(Font.font("Verdana", 100));
                engineNashorn.eval(scriptBody);
            } else {
                engineGroovy.put("textArea", controller.getTextArea());
                engineGroovy.eval(scriptBody);
            }
        } catch (ScriptException e) {
            return e.getMessage();
        }

        return null;
    }

    public HashMap<String, ListItem> getNashornFunctionMetaData() {
        return nashornFunctionMetaData;
    }

    private String getNames(String scriptBody) {

        scriptBody = scriptBody.trim().replaceAll(" +", " ");
        scriptBody = scriptBody.trim().replaceAll("\n+", "");

        int lastNameIndex = scriptBody.length();

        while (lastNameIndex > 0) {

            int startIndex = 0;
            int lastIndex = scriptBody.indexOf('}');

            lastNameIndex = scriptBody.indexOf(')') + 1;

            if (lastIndex < 0 || lastNameIndex < 0) return null;

            String functionDeclaration = scriptBody.substring(startIndex, lastNameIndex);


            String functionName = getFunctionName(functionDeclaration);

            String[] parameters = getParameterNames(functionDeclaration);

            List<String> parList;
            if (parameters == null) {
                parList = null;
            } else {
                parList = Arrays.asList(parameters);
                if (parList.size() > 2) return "too much params, max 2 allowed";
            }


            secureAdd(functionDeclaration, new ListItem(functionName, parList));

            scriptBody = scriptBody.substring(lastIndex + 1);

        }
        return null;
    }


    private String getFunctionName(String functionDeclaration) {

        int firstLetterIndex = functionDeclaration.indexOf(" ") + 1;
        int lastIndex = functionDeclaration.indexOf("(");


        return functionDeclaration.substring(firstLetterIndex, lastIndex);

    }

    private String[] getParameterNames(String functionDeclaration) {

        int startIndex = functionDeclaration.indexOf('(') + 1;
        int endIndex = functionDeclaration.indexOf(')');

        if (startIndex == 0 || endIndex < 0 || startIndex == endIndex) return null;
        else {

            String parametersRaw = functionDeclaration.substring(
                    startIndex,
                    endIndex

            );

            return parametersRaw.split(",");
        }
    }


    private void secureAdd(String newKey, ListItem newListItem) {
        if (scriptLangType == ScriptLangType.NASHORN) {
            for (String key : nashornFunctionMetaData.keySet()) {

                String functionName = nashornFunctionMetaData.get(key).getFunctionName();

                if (functionName.equals(newListItem.functionName)) {
                    nashornFunctionMetaData.remove(key);
                    nashornFunctionMetaData.put(newKey, newListItem);
                    return;
                }

            }

            nashornFunctionMetaData.put(newKey, newListItem);
        } else if (scriptLangType == ScriptLangType.GROOVY) {
            for (String key : groovyFunctionMetaData.keySet()) {

                String functionName = groovyFunctionMetaData.get(key).getFunctionName();

                if (functionName.equals(newListItem.functionName)) {
                    groovyFunctionMetaData.remove(key);
                    groovyFunctionMetaData.put(newKey, newListItem);
                    return;
                }

            }

            groovyFunctionMetaData.put(newKey, newListItem);
        }


    }


    public String invokeFunction(String functionKey, Object[] args) {
        String result;
        stringWriter.getBuffer().setLength(0);

        Invocable invocable = null;
        String functionName = null;

        if (scriptLangType == ScriptLangType.NASHORN) {
            invocable = (Invocable) engineNashorn;

            functionName = nashornFunctionMetaData.get(functionKey).getFunctionName();
        } else if (scriptLangType == ScriptLangType.GROOVY) {
            invocable = (Invocable) engineGroovy;

            functionName = groovyFunctionMetaData.get(functionKey).getFunctionName();
        }

        try {
            result = String.valueOf(invocable.invokeFunction(functionName, args));
        } catch (Exception e) {
            result = e.getMessage();
        }

        return stringWriter.toString() + result;
    }

    @Deprecated
    public void printAllListItems() {

        for (String key : nashornFunctionMetaData.keySet()) {
            ListItem item = nashornFunctionMetaData.get(key);
            System.out.println(item.getFunctionName());
            if (item.parameterNames == null) continue;
            System.out.println(item.parameterNames.size());
            for (String parName : item.parameterNames)
                System.out.println(parName);
        }


    }


    public List<String> getLoadedFunctions() {
        if (scriptLangType == ScriptLangType.NASHORN)
            return new LinkedList<>(nashornFunctionMetaData.keySet());
        else
            return new LinkedList<>(groovyFunctionMetaData.keySet());
    }


    public String loadNewScript(String scriptBody) {

        String errorText = evaluateScript(scriptBody);

        if (errorText != null) {
            return errorText;
        } else {
            return getNames(scriptBody);
        }
    }

    public HashMap<String, ListItem> getGroovyFunctionMetaData() {
        return groovyFunctionMetaData;
    }

    public HashMap<String, ListItem> getCurrentMeta() {
        if (scriptLangType == ScriptLangType.NASHORN)
            return nashornFunctionMetaData;
        else
            return groovyFunctionMetaData;
    }


}
