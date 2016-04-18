
package application;

import static java.lang.Math.*;

import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
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
	private Image			eyeOverlay;
	private Rect			lastFace;
	private Rect			lastEyeL, lastEyeR;

	public EyeOverlay (Canvas overlayCanvas, Image eyeOverlay)
	{
		this.gc = overlayCanvas.getGraphicsContext2D ();
		this.eyeOverlay = eyeOverlay;
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

		System.out.println (String.format ("Detected %s faces", faceDetections.total ()));

		gc.clearRect (0, 0, gc.getCanvas ().getWidth (), gc.getCanvas ().getHeight ());

		for (Rect faceRect : faceDetections.toArray ())
		{
			Mat face = frame.submat (faceRect);

			if (lastFace == null)
			{
				lastFace = faceRect;
				break;
			}
			else
			{
				updateRect (faceRect, lastFace, frame.size (), 6, 7);
			}

			gc.strokeRect (faceRect.x, faceRect.y, faceRect.width, faceRect.height);
			
			eyeDetector.detectMultiScale (face, eyeDetections, 1.1, 3,  0 | Objdetect.CASCADE_SCALE_IMAGE, new Size(30, 30), new Size (1000, 1000));
			
			for (Rect eyeRect : eyeDetections.toArray ())
			{
				Point centerFace = rectCenter (faceRect);
				
				gc.strokeOval (centerFace.x - 10, centerFace.y - 10, 10, 10);
				
				if (lastFace == null)
				{
					lastFace = faceRect;
					break;
				}
				
				//my left, comps right
				if(eyeRect.x > centerFace.x)
				{
					if(lastEyeR == null)
					{
						lastEyeR = eyeRect;
					}
					else
					{
						updateRect (eyeRect, lastEyeR, frame.size (), 10, 15);
						System.out.println ("EYE_R: " + eyeRect);
					}
				}
				else
				{
					if(lastEyeL == null)
					{
						lastEyeL = eyeRect;
					}
					else
					{
						updateRect(eyeRect, lastEyeL, frame.size (), 10, 15);
						System.out.println ("EYE_L: " + eyeRect);
					}
				}
				
				gc.strokeRect (faceRect.x + eyeRect.x, faceRect.y + eyeRect.y, eyeRect.width, eyeRect.height);
//				gc.drawImage (eyeReplace, faceRect.x + eyeRect.x, faceRect.y + eyeRect.y, 100, 100);
			}
		}

	}
	
	public static Point rectCenter(Rect rect)
	{
		return new Point(rect.x + rect.width / 2.0, rect.y + rect.height / 2.0);
	}
	
	public static void updateRect(Rect cur, Rect last, Size frameSize , double maxDistFrameDivisor, double pixelThresholdToNotRevertCurToLast)
	{
		if (abs (cur.x - last.x) < frameSize.width / maxDistFrameDivisor
				&& abs (cur.y - last.y) < frameSize.height / maxDistFrameDivisor)
		{

			// Check to see if the user moved enough to update position
			if (abs (cur.x - last.x) < pixelThresholdToNotRevertCurToLast
					&& abs (cur.y - last.y) < pixelThresholdToNotRevertCurToLast)
			{
				cur = last;
			}
		}
	}
}
