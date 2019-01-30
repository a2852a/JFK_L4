package sample;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.util.LinkedList;

public class ScriptLoader {

    ScriptEngine engine ;
    LinkedList<String> scriptList;


    public ScriptLoader(){
        this.engine = new ScriptEngineManager().getEngineByName("nashorn");
        this.scriptList = new LinkedList<>();
    }


    private String evaluateScript(String scriptBody){


        try {
            engine.eval(scriptBody);
        } catch (ScriptException e) {
            return e.getMessage();
        }
        return null;
    }


    public String loadNewScript(String scriptBody) {

        String errorText = evaluateScript(scriptBody);

        if(errorText != null){
            return errorText;
        }else{
            //TODO obsluga zapisu do listy
        }

    return null;
    }
}
