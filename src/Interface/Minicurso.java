package Interface;

import Chat.Controller;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Minicurso extends Application {
    
    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("Tela.fxml"));
        Parent root = loader.load();
        Controller controller = loader.getController();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setOnCloseRequest(e->{
            try {
                controller.serializar();
                if(controller.getChat() != null) {
                    controller.getChat().desconectar();
                }
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
        });
        stage.show();
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}
