
package application;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

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
			Parent root = FXMLLoader.load (getClass ().getResource ("Webcam.fxml"));
			primaryStage.setScene (new Scene (root));
			primaryStage.show ();
			
			Mat mat = Mat.eye(3, 3, CvType.CV_8UC1);
            System.out.println("mat = " + mat.dump());
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
