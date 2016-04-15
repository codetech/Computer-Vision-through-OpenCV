
package application;

import org.opencv.core.Core;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application
{

	static
	{
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
	}

	@Override
	public void start (Stage primaryStage)
	{
		try
		{
			FXMLLoader fxmlLoader = new FXMLLoader ();
			fxmlLoader.setLocation (getClass ().getResource ("Webcam.fxml"));
			Parent root = fxmlLoader.load ();
			Controller controller = fxmlLoader.<Controller>getController ();
			
			primaryStage.setScene (new Scene (root));
			primaryStage.show ();
			primaryStage.setOnCloseRequest ((e) -> controller.stopCamera());
		}
		catch (Exception e)
		{
			e.printStackTrace ();
		}
	}

	public static void main (String[] args)
	{
		launch (args);
	}
}
