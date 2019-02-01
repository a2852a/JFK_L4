package sample;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Font;

import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;

public class Controller implements Initializable {

    @FXML
    private ListView<String> tab1ScriptListView;

    @FXML
    private Button tab1ExecuteButton;

    @FXML
    private TextArea tab1ResultTextArea;

    @FXML
    private TextArea tab2ScriptTextArea;

    @FXML
    private Button tab2AddScriptButton;

    @FXML
    private RadioButton tab2NashornRadioButton;

    @FXML
    private RadioButton tab2GroovyRadioButton;

    @FXML
    private TextField tab1Par1TextField;

    @FXML
    private TextField tab1Par2TextField;

    @FXML
    private AnchorPane anchor1;


    private ScriptLoader scriptLoader;

    String functionKey;

    int paramsCount;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        scriptLoader = new ScriptLoader(this);
        ToggleGroup toggleGroup = new ToggleGroup();
        tab2NashornRadioButton.setToggleGroup(toggleGroup);
        tab2GroovyRadioButton.setToggleGroup(toggleGroup);
        tab2NashornRadioButton.setSelected(true);
        paramsCount = 0;
        tab1Par1TextField.setDisable(true);
        tab1Par2TextField.setDisable(true);
        tab1ResultTextArea.setFont(Font.font("Verdana", 20));

        tab1ExecuteButton.setDisable(true);

    }


    @FXML
    private void addScriptAction() {

        String result = scriptLoader.loadNewScript(tab2ScriptTextArea.getText());
        if (result != null)
            tab2ScriptTextArea.setText(result);

        reloadList();

//        ObservableList<String> items = FXCollections.observableArrayList(
//                scriptLoader.getLoadedFunctions()
//        );
//
//        tab1ScriptListView.setItems(items);
//        tab1ScriptListView.getSelectionModel().clearSelection();
//
//
//        tab1ScriptListView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
//            @Override
//            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
//                if (newValue != null)
//                    functionKey = newValue;
//            }
//        });
//
//        tab1ScriptListView.getSelectionModel().clearSelection();
//        tab1Par1TextField.setDisable(true);
//        tab1Par2TextField.setDisable(true);

    }


    private void reloadList(){
        ObservableList<String> items = FXCollections.observableArrayList(
                scriptLoader.getLoadedFunctions()
        );

        tab1ScriptListView.setItems(items);
        tab1ScriptListView.getSelectionModel().clearSelection();
        tab1ExecuteButton.setDisable(true);


        tab1ScriptListView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (newValue != null)
                    functionKey = newValue;
            }
        });


        tab1ScriptListView.getSelectionModel().clearSelection();

        tab1ResultTextArea.setText("");

        tab1Par1TextField.setDisable(true);
        tab1Par1TextField.setText("");

        tab1Par2TextField.setDisable(true);
        tab1Par2TextField.setText("");



    }


    public Object toObject(Class clazz, String value) {

        try {
            if (Boolean.class.isAssignableFrom(value.getClass())) return Boolean.parseBoolean(value);
            if (Byte.class.isAssignableFrom(clazz)) return Byte.parseByte(value);
            if (Short.class.isAssignableFrom(clazz)) return Short.parseShort(value);
            if (Integer.class.isAssignableFrom(clazz)) return Integer.parseInt(value);
            if (Long.class.isAssignableFrom(clazz)) return Long.parseLong(value);
            if (Float.class.isAssignableFrom(clazz)) return Float.parseFloat(value);
            if (Double.class.isAssignableFrom(clazz)) return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return value;
        }
        return value;
    }

    private Object stringToObject(String string){

        Class[] classes = new Class[]{Boolean.class, Byte.class, Short.class, Integer.class, Long.class, Float.class, Double.class};

        for (Class classC : classes) {
            Object obj = toObject(classC, string);
            if (!(obj instanceof String)) return obj;
        }

        return string;
    }


    public Object convert(String value) {


        char firstChar = value.charAt(0);
        char lastChar = value.charAt(value.length() - 1);

        if ((firstChar == '\"' && lastChar == '\"') || (firstChar == '\'' && lastChar == '\''))
            return value.substring(1, value.length() - 1);

        return stringToObject(value);

    }


    @FXML
    private void invokeFunction() {

        Object args[] = null;

        String par1 = tab1Par1TextField.getText();
        String par2 = tab1Par2TextField.getText();


        if ((paramsCount==1&&(par1.isEmpty()))||
                (paramsCount==2&&(par1.isEmpty() || par2.isEmpty()))
        ) {
            tab1ResultTextArea.setText("Insufficient parameters");
            return;
        }


        switch (paramsCount) {

            case 1: {
                args = new Object[]{convert(par1)};
                break;
            }
            case 2: {
                args = new Object[]{convert(par1), convert(par2)};
                break;
            }

        }


        String result = scriptLoader.invokeFunction(functionKey, args);
        tab1ResultTextArea.setText(result);

    }

    @FXML
    void radioButtonSelected(ActionEvent event) {
        RadioButton radioButton = (RadioButton) event.getSource();
        String text = radioButton.getText();
        switch (text) {
            case "Nashorn":
                scriptLoader.setScriptLangType(ScriptLoader.ScriptLangType.NASHORN);
                break;
            case "Groovy":
                scriptLoader.setScriptLangType(ScriptLoader.ScriptLangType.GROOVY);
                break;
        }

        reloadList();

    }

    void setFields() {
        tab1Par1TextField.setText("");
        tab1Par2TextField.setText("");
        tab1ExecuteButton.setDisable(false);

        String functionKey = tab1ScriptListView.getSelectionModel().getSelectedItem();
        if(functionKey == null) return;
        List<String> params = scriptLoader.getCurrentMeta().get(functionKey).parameterNames;
        paramsCount = 0;
        if (params != null) paramsCount = params.size();
        if (paramsCount == 1) {
            tab1Par1TextField.setDisable(false);
            tab1Par2TextField.setDisable(true);
        } else if (paramsCount == 2) {
            tab1Par1TextField.setDisable(false);
            tab1Par2TextField.setDisable(false);
        } else if (paramsCount == 0) {
            tab1Par1TextField.setDisable(true);
            tab1Par2TextField.setDisable(true);
        }
    }

    TextArea getTextArea() {
        return tab1ResultTextArea;
    }

    @FXML
    void functionSelected() {
        setFields();
    }

    @FXML
    void functionSelectedKey() {
        setFields();
    }


    public List<Node> giveAllElements(){

        List<Node> childrenList = anchor1.getChildren();


        return childrenList;

    }

}
