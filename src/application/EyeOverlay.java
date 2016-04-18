
package application;

import static java.lang.Math.abs;

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
import javafx.scene.paint.Color;

public class EyeOverlay
{
	private GraphicsContext	gc;
	private Image			eyeOverlay;
	private Rect			lastFace;			// relative to frame
	private Rect			lastEyeL, lastEyeR;	// relative to face

	public EyeOverlay (Canvas overlayCanvas, Image eyeOverlay)
	{
		this.gc = overlayCanvas.getGraphicsContext2D ();
		this.eyeOverlay = eyeOverlay;
	}

	public void overlayEyes (Mat frame)
	{
		detectFace (frame);

		gc.clearRect (0, 0, gc.getCanvas ().getWidth (), gc.getCanvas ().getHeight ());

		if (lastFace != null)
		{
			gc.setStroke (Color.DARKGREEN);
			gc.strokeRect (lastFace.x, lastFace.y, lastFace.width, lastFace.height);

			Point centerFace = rectCenter (lastFace);
			gc.setStroke (Color.DARKMAGENTA);
			gc.strokeOval (centerFace.x - 10, centerFace.y - 10, 10, 10);

			if (lastEyeL != null)
			{
				gc.setStroke (Color.BLUE);
				gc.strokeRect (lastFace.x + lastEyeL.x, lastFace.y + lastEyeL.y, lastEyeL.width, lastEyeL.height);
//		gc.drawImage (eyeReplace, faceRect.x + eyeRect.x, faceRect.y + eyeRect.y, 100, 100);
			}
		
			if (lastEyeR != null)
			{
				gc.setStroke (Color.RED);
				gc.strokeRect (lastFace.x + lastEyeR.x, lastFace.y + lastEyeR.y, lastEyeR.width, lastEyeR.height);
			}
		}
	}
	
	private void detectFace(Mat frame)
	{
		Imgproc.cvtColor (frame, frame, Imgproc.COLOR_BGR2GRAY);
		Imgproc.equalizeHist (frame, frame);
		
		CascadeClassifier faceDetector = new CascadeClassifier ("D:/External Libraries/opencv/sources/data/haarcascades_cuda/haarcascade_frontalface_default.xml");
		CascadeClassifier eyeDetector  = new CascadeClassifier ("D:/External Libraries/opencv/sources/data/haarcascades_cuda/haarcascade_eye.xml");

		MatOfRect faceDetections = new MatOfRect ();
		MatOfRect eyeDetections = new MatOfRect ();
		
		faceDetector.detectMultiScale (frame, faceDetections, 1.1, 3, 0 | Objdetect.CASCADE_SCALE_IMAGE, new Size(30, 30), new Size (1000, 1000));

		System.out.println (String.format ("Detected %s faces", faceDetections.total ()));


		for (Rect faceRect : faceDetections.toArray ())
		{
			Mat face = frame.submat (faceRect);

			if (lastFace == null)
			{
				lastFace = faceRect;
			}
			else
			{
				lastFace = updateRect (faceRect, lastFace, frame.size (), 6, 7);
			}
			
			eyeDetector.detectMultiScale (face, eyeDetections, 1.1, 3,  0 | Objdetect.CASCADE_SCALE_IMAGE, new Size(30, 30), new Size (1000, 1000));
			
//			Rect[] eyes = eyeDetections.toArray ();
//			for (int i = 0; i < 2 && i < eyeDetections.total (); ++i)
			for(Rect eyeRect : eyeDetections.toArray ())
			{
//				Rect eyeRect = eyes[i];
				
				Point centerFace = rectCenter (faceRect);
				
				//my left, comps right
//				System.out.println ("Center: " + centerFace);
				if (faceRect.y + eyeRect.y + eyeRect.height < centerFace.y)
				{
					if (faceRect.x + eyeRect.x + eyeRect.width < centerFace.x)
					{
						if (lastEyeR == null)
						{
							lastEyeR = eyeRect;
						}
						else
						{
							lastEyeR = updateRect (eyeRect, lastEyeR, faceRect.size (), 6, 10);
//							System.out.println ("EYE_R: " + eyeRect);
						}
					}
					else if (faceRect.x + eyeRect.x > centerFace.x)
					{
						if (lastEyeL == null)
						{
							lastEyeL = eyeRect;
						}
						else
						{
							lastEyeL = updateRect (eyeRect, lastEyeL, faceRect.size (), 6, 10);
//							System.out.println ("EYE_L: " + eyeRect);
						}
					}
				}
			}
		}
	}
	
	public static Point rectCenter(Rect rect)
	{
		return new Point(rect.x + rect.width / 2.0, rect.y + rect.height / 2.0);
	}
	
	//Returns cur (could be cur as passed in or cur could be set to last)
	public static Rect updateRect(Rect cur, Rect last, Size frameSize , double maxDistFrameDivisor, double pixelThresholdToNotRevertCurToLast)
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
		
		return cur;
	}
}
