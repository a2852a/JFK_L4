package sample;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;

import java.net.URL;
import java.util.ResourceBundle;

public class Controller implements Initializable{

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

    private ScriptLoader scriptLoader;

    String functionKey;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        scriptLoader = new ScriptLoader();
    }

    @FXML
    private void addScriptAction(){

        String result = scriptLoader.loadNewScript(tab2ScriptTextArea.getText());
        if(result !=null)
            tab2ScriptTextArea.setText(result);
        scriptLoader.printAllListItems();

        ObservableList<String> items = FXCollections.observableArrayList(
                scriptLoader.getLoadedFunctions()
        );

        tab1ScriptListView.setItems(items);
        tab1ScriptListView.getSelectionModel().clearSelection();


        tab1ScriptListView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if(newValue != null)
                    functionKey = newValue;


                //tab1ResultTextArea.setText(scriptLoader.invokeFunction(newValue,null));
            }
        });


       // System.out.println(scriptLoader.getParametersName(tab2ScriptTextArea.getText(), "test1"));

    }

    @FXML
    private void invokeFunction(){
        String result = scriptLoader.invokeFunction(functionKey,null);
        tab1ResultTextArea.setText(result);

        System.out.println("TEST " + result);
    }


}
