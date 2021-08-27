import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.Locale;

public class Main extends Application {

	@Override
	public void start(Stage primaryStage) throws Exception {
		Parent root = FXMLLoader.load(getClass().getClassLoader().getResource("views/mainFrame.fxml"));
		primaryStage.setTitle("Прогнозирование по Хольту-Винтерсу");
		primaryStage.setScene(new Scene(root, 1200, 800));
		primaryStage.show();
	}

	public static void main(String[] args) {
		Locale.setDefault(Locale.US);
		launch(args);
	}
}
