
package application;

import java.io.ByteArrayInputStream;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.videoio.VideoCapture;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class Controller
{
	@FXML private Button				toggleCapture;
	@FXML private ImageView				currentFrame;

	private ScheduledExecutorService	timer;
	private VideoCapture				capture			= new VideoCapture ();
	private boolean						cameraActive	= false;

	@FXML
	private void initialize ()
	{
		assert toggleCapture != null : "fx:id=\"toggleCapture\" was not injected: check your FXML file 'Webcam.fxml'.";
		assert currentFrame != null : "fx:id=\"currentFrame\" was not injected: check your FXML file 'Webcam.fxml'.";
	}

	@FXML
	private void startCamera (ActionEvent e)
	{
		if (!this.cameraActive)
		{
			// start the video capture
			this.capture.open (0);

			// is the video stream available?
			if (this.capture.isOpened ())
			{
				this.cameraActive = true;

				// grab a frame every 16.6666 ms (60 frames/sec)
				Runnable frameGrabber = new Runnable ()
				{

					@Override
					public void run ()
					{
						Image imageToShow = grabFrame ();
						currentFrame.setImage (imageToShow);
					}
				};

				this.timer = Executors.newSingleThreadScheduledExecutor ();
				this.timer.scheduleAtFixedRate (frameGrabber, 0, 17, TimeUnit.MILLISECONDS);

				// update the button content
				this.toggleCapture.setText ("Stop Camera");
			}
			else
			{
				// log the error
				System.err.println ("Impossible to open the camera connection...");
			}
		}
		else
		{
			stopCamera ();
		}
	}

	public void stopCamera ()
	{
		// the camera is not active at this point
		this.cameraActive = false;
		// update again the button content
		this.toggleCapture.setText ("Start Camera");

		// stop the timer
		try
		{
			this.timer.shutdown ();
			this.timer.awaitTermination (33, TimeUnit.MILLISECONDS);
		}
		catch (InterruptedException exception)
		{
			// log the exception
			System.err.println (
					"Exception in stopping the frame capture, trying to release the camera now... " + exception);
		}

		// release the camera
		this.capture.release ();
		// clean the frame
		this.currentFrame.setImage (null);
	}

	private Image grabFrame ()
	{
		// init everything
		Image imageToShow = null;
		Mat frame = new Mat ();

		// check if the capture is open
		if (this.capture.isOpened ())
		{
			try
			{
				// read the current frame
				this.capture.read (frame);

				// if the frame is not empty, process it
				if (!frame.empty ())
				{
					// convert the image to gray scale
					// Imgproc.cvtColor(frame, frame, Imgproc.COLOR_BGR2RGB);
					// convert the Mat object (OpenCV) to Image (JavaFX)
					imageToShow = mat2Image (frame);
				}

			}
			catch (Exception e)
			{
				// log the error
				System.err.println ("Exception during the image elaboration: " + e);
			}
		}

		return imageToShow;
	}

	/**
	 * Convert a Mat object (OpenCV) in the corresponding Image for JavaFX
	 * 
	 * @param frame
	 *            the {@link Mat} representing the current frame
	 * @return the {@link Image} to show
	 */
	private Image mat2Image (Mat frame)
	{
		// create a temporary buffer
		MatOfByte buffer = new MatOfByte ();
		// encode the frame in the buffer
		Imgcodecs.imencode (".png", frame, buffer);
		// build and return an Image created from the image encoded in the
		// buffer
		return new Image (new ByteArrayInputStream (buffer.toArray ()));
	}

}
