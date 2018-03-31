package Interface;

import Chat.Controller;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.InetAddress;

public class Minicurso extends Application {

    private Controller controller;

    @Override
    public void start(Stage stage) throws Exception {
        System.setProperty("java.net.preferIPv4Stack" , "true");
        String localhost = InetAddress.getLocalHost().toString().split("/", 2)[1];
        System.setProperty("jgroups.bind_addr" , localhost);
        FXMLLoader loader = new FXMLLoader(getClass().getResource("Tela.fxml"));
        Parent root = loader.load();
        controller = loader.getController();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    @Override
    public void stop() {
        try {
            controller.serializar();
            if(controller.getChat() != null) {
                controller.getChat().desconectar();
            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}
