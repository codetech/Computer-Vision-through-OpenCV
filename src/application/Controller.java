
package application;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;

public class Controller
{

	@FXML private Button	startCamera;

	@FXML private ImageView	currentFrame;

	@FXML
	void initialize ()
	{
		assert startCamera != null : "fx:id=\"startCamera\" was not injected: check your FXML file 'Webcam.fxml'.";
		assert currentFrame != null : "fx:id=\"currentFrame\" was not injected: check your FXML file 'Webcam.fxml'.";

	}

}
