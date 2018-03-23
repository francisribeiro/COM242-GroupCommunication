/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Chat;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;

/**
 *
 * @author vinic
 */
public class Controller implements Initializable, Serializable {

    private Chat chat;
    private ArrayList<Grupos> grupos;
    private ArrayList<Button> btnGrupos;

    @FXML
    private Button btnEnviar;
    @FXML
    private Button btnAnexo;
    @FXML
    private Button desconectar;
    @FXML
    private Button btnAddGrupo;
    @FXML
    private Label labelGrupo;
    @FXML
    private Label title;
    @FXML
    private TextArea textArea;
    @FXML
    private TextField textField;
    @FXML
    private VBox vBoxSelecaoGrupos;
    @FXML
    private Pane paneGrupos;
    @FXML
    private Pane areadeChat;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        btnEnviar.getStyleClass().add("btn-primary");
        btnAnexo.getStyleClass().add("btn-primary");
        btnAddGrupo.getStyleClass().add("btn-primary");
        desconectar.getStyleClass().add("btn-danger");
        textArea.getStyleClass().add("text-field");
        textField.getStyleClass().add("text-field");
        labelGrupo.getStyleClass().add("h4");
        title.getStyleClass().add("h3");
        paneGrupos.getStyleClass().add("pane");
        areadeChat.getStyleClass().add("pane");
        lerGrupos();
        criarBotoesGrupos();
    }

    @FXML
    private void handleJoinGroup(Button button) throws Exception {
        for (int i = 0; i < btnGrupos.size(); i++) {
            if (button.equals(btnGrupos.get(i))) {
                JPasswordField senha = new JPasswordField();
                int okCxl = JOptionPane.showConfirmDialog(
                        null, senha, "Entre com a senha do grupo",
                        JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
                if (okCxl == JOptionPane.OK_OPTION) {
                    String senhaDigitada = new String(senha.getPassword());
                    if (senhaDigitada.equals(grupos.get(i).getSenha())) {
                        areadeChat.setDisable(false);
                        areadeChat.setVisible(true);
                        paneGrupos.setDisable(true);
                        chat = new Chat(this);
                        chat.start(grupos.get(i).getNome());
                    } else {
                        JOptionPane.showMessageDialog(senha, "Senha inválida");
                    }
                }
                break;
            }
        }
    }

    @FXML
    private void handleButtonDesconectar() {
        areadeChat.setVisible(false);
        paneGrupos.setDisable(false);
        textArea.setText("");
        chat.desconectar();
        chat = null;
    }

    @FXML
    private void handleButtonEnviar() throws Exception {
        String msg = textField.getText();
        if(!msg.isEmpty()) {
            chat.enviarMsg(msg);
        }
        textField.setText("");
    }

    @FXML
    private void handleAddGrupo() {
        Stage stage = new Stage();
        Label nome = new Label("Nome do Grupo");
        nome.setMaxSize(250, 200);
        Label senha = new Label("Senha do Grupo");
        senha.setMaxSize(250, 200);
        TextField nomeGrupo = new TextField();
        nomeGrupo.setMaxSize(250, 200);
        PasswordField senhaGrupo = new PasswordField();
        senhaGrupo.setMaxSize(250, 200);
        Button button = new Button("Cadastrar");
        button.setOnAction((ActionEvent event) -> {
            Grupos grupo = new Grupos(nomeGrupo.getText(), senhaGrupo.getText());
            grupos.add(grupo);
            stage.close();
            vBoxSelecaoGrupos.getChildren().clear();
            criarBotoesGrupos();
            event.consume();
        });
        VBox root = new VBox();
        root.getChildren().add(nome);
        root.getChildren().add(nomeGrupo);
        root.getChildren().add(senha);
        root.getChildren().add(senhaGrupo);
        root.getChildren().add(button);
        stage.setScene(new Scene(root, 300, 250));
        stage.show();
    }

    @FXML
    private void handleButtonAnexo() throws Exception {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("");
        File file = fileChooser.showOpenDialog(null);
        if (file != null) {
            chat.enviarAnexo(file);
        }
    }

    private void criarBotoesGrupos() {
        btnGrupos = new ArrayList<>();
        vBoxSelecaoGrupos.getChildren().clear();
        for (Grupos grupo : grupos) {
            Button button = new Button(grupo.getNome());
            button.setMinSize(252, 40);
            button.getStyleClass().add("btn-info");
            button.setOnAction((event) -> {
                try {
                    handleJoinGroup(button);
                } catch (Exception ex) {
                    System.out.println(ex.getMessage());
                }
            });
            btnGrupos.add(button);
            vBoxSelecaoGrupos.getChildren().add(button);
        }
    }

    public void serializar() throws Exception {
        try {
            FileOutputStream arquivo = new FileOutputStream("grupos.ser");
            ObjectOutputStream out = new ObjectOutputStream(arquivo);
            out.writeObject(grupos);
            out.close();
            arquivo.close();
        } catch (IOException exc) {
            throw new Exception("Arquivo Grupos não encontrado!");
        }
    }

    private void lerGrupos() {
        try {
            FileInputStream arquivo = new FileInputStream("grupos.ser");
            ObjectInputStream in = new ObjectInputStream(arquivo);
            grupos = (ArrayList<Grupos>) in.readObject();
        } catch (IOException | ClassNotFoundException ex) {
            grupos = new ArrayList<>();
        }
    }

    public Grupos getGrupo(String nome) {
        for (Grupos grupo : grupos) {
            if (grupo.getNome().equals(nome)) {
                return grupo;
            }
        }
        return null;
    }

    public ArrayList<Grupos> getGrupos() {
        return grupos;
    }

    public Chat getChat() {
        return chat;
    }

    public ArrayList<Button> getBtnGrupos() {
        return btnGrupos;
    }

    public VBox getSelecaoGrupos() {
        return vBoxSelecaoGrupos;
    }

    public Button getBtnAnexo() {
        return btnAnexo;
    }

    public Button getBtnEnviar() {
        return btnEnviar;
    }

    public TextArea getTextArea() {
        return textArea;
    }

}
