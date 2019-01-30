package sample;

import jdk.nashorn.internal.objects.NativeFunction;
import jdk.nashorn.internal.runtime.Undefined;

import javax.script.*;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

public class ScriptLoader {

    ScriptEngineManager factory;
    ScriptEngine engine ;
    LinkedList<String> scriptNames;


    public ScriptLoader(){
        this.factory = new ScriptEngineManager();
        this.engine = factory.getEngineByName("nashorn");
        this.scriptNames = new LinkedList<>();
    }


    private String evaluateScript(String scriptBody){

        try {


            //engine.put("x",scriptBody);
            engine.eval(scriptBody);
        } catch (ScriptException e) {
            return e.getMessage();
        }
        return null;
    }


    private void extractNames(){


        //engine.get("v1")

        Bindings bindings = engine.getBindings(ScriptContext.ENGINE_SCOPE);

        for (Map.Entry<String, Object> scopeEntry : bindings.entrySet()) {
            Object value = scopeEntry.getValue();
            String name = scopeEntry.getKey();
            System.out.println(name);
            System.out.println(value);




            try {
                //String func = (String) engine.get("x");
                System.out.println(engine.eval("v1()"));


            Set<String> allAttributes = bindings.keySet();

            Set<String> allFunctions = new HashSet<String>();
            for ( String attr : allAttributes ) {
                allFunctions.add(attr);
                if ( "function".equals( engine.eval("typeof " + attr) ) ) {

                }

                System.out.println(allFunctions);

            }

            } catch (ScriptException e) {
                e.printStackTrace();
            }




//            if (value instanceof NativeFunction) {
//                System.out.println(name);
//                //log.info("Function -> " + name);
//                NativeFunction function = NativeFunction.class.cast(value);
//                //DebuggableScript debuggableFunction = function.getDebuggableView();
//                //for (int i = 0; i < debuggableFunction.getParamAndVarCount(); i++) {
//                //    log.info("First level arg: " + debuggableFunction.getParamOrVarName(i));
//               // }
//            } else if (value instanceof Undefined
//                    || value instanceof String
//                    || value instanceof Number) {
//                //log.info("Global arg -> " + name);
//            }
        }

    }


    public String loadNewScript(String scriptBody) {

        String errorText = evaluateScript(scriptBody);

        if(errorText != null){
            return errorText;
        }else{

            extractNames();

            //engine.put("functionName",scriptBody);
            //scriptNames.add("functionName");



           // engine.eval("functionName")

            //TODO obsluga zapisu do listy
        }

    return null;
    }
}
