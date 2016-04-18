
package application;

import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.objdetect.Objdetect;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

public class EyeOverlay
{
	private GraphicsContext	gc;
	private Image		eyeReplace;

	public EyeOverlay (Canvas overlayCanvas, Image eyeReplace)
	{
		this.gc = overlayCanvas.getGraphicsContext2D ();
		this.eyeReplace = eyeReplace;
	}

	public void overlayEyes (Mat frame)
	{
		Imgproc.cvtColor (frame, frame, Imgproc.COLOR_BGR2GRAY);
		Imgproc.equalizeHist (frame, frame);
		
		CascadeClassifier faceDetector = new CascadeClassifier ("D:/External Libraries/opencv/sources/data/haarcascades_cuda/haarcascade_frontalface_default.xml");
		CascadeClassifier eyeDetector  = new CascadeClassifier ("D:/External Libraries/opencv/sources/data/haarcascades_cuda/haarcascade_eye.xml");

		MatOfRect faceDetections = new MatOfRect ();
		MatOfRect eyeDetections = new MatOfRect ();
		
		faceDetector.detectMultiScale (frame, faceDetections, 1.1, 3, 0 | Objdetect.CASCADE_SCALE_IMAGE, new Size(30, 30), new Size (1000, 1000));

		System.out.println (String.format ("Detected %s faces", faceDetections.total()));

		gc.clearRect (0, 0, gc.getCanvas ().getWidth (), gc.getCanvas ().getHeight ());
		
		for (Rect faceRect : faceDetections.toArray ())
		{
			Mat face = frame.submat (faceRect);
			
			eyeDetector.detectMultiScale (face, eyeDetections, 1.1, 3,  0 | Objdetect.CASCADE_SCALE_IMAGE, new Size(30, 30), new Size (1000, 1000));
			
			for (Rect eyeRect : eyeDetections.toArray ())
			{
				gc.strokeRect (faceRect.x + eyeRect.x, faceRect.y + eyeRect.y, eyeRect.width, eyeRect.height);
//				gc.drawImage (eyeReplace, faceRect.x + eyeRect.x, faceRect.y + eyeRect.y, 100, 100);
			}
		}

	}
}
