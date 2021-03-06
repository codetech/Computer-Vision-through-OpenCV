
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
import javafx.scene.paint.Color;
import javafx.scene.transform.Rotate;

//Based on http://synaptitude.me/blog/smooth-face-tracking-using-opencv/

public class EyeOverlay
{
	public final static String	HAAR_CASCADES_FOLDER	= "D:/External Libraries/opencv/sources/data/haarcascades_cuda";

	private GraphicsContext		gc;
	private Image				eyeOverlay;
	private Rect				lastFace;																				// relative
																														// to
																														// frame
	private Rect				lastEyeL, lastEyeR;																		// relative
																														// to
																														// face

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

			// Point centerFace = rectCenter (lastFace);
			// gc.setStroke (Color.DARKMAGENTA);
			// gc.strokeOval (centerFace.x - 10, centerFace.y - 10, 10, 10);

			double aspectRatio = eyeOverlay.getWidth () / eyeOverlay.getHeight ();

			double wL, wR, hL, hR, xL, xR, yL, yR;
			Point eyeCenterL, eyeCenter, eyeCenterR;

			wL = 0;
			wR = 0;
			hL = 0;
			hR = 0;
			xL = 0;
			xR = 0;
			yL = 0;
			yR = 0;

			eyeCenterL = null;
			eyeCenter = null;
			eyeCenterR = null;

			if (lastEyeL != null)
			{
				// gc.setStroke (Color.BLUE);
				// gc.strokeRect (lastFace.x + lastEyeL.x, lastFace.y +
				// lastEyeL.y, lastEyeL.width, lastEyeL.height);

				eyeCenterL = rectCenter (lastEyeL);

				wL = 6 * lastEyeL.width;
				hL = wL / aspectRatio;

				xL = lastFace.x + eyeCenterL.x - wL / 2 + 8;
				yL = lastFace.y + eyeCenterL.y + lastEyeL.height - hL;

			}

			if (lastEyeR != null)
			{
				// gc.setStroke (Color.RED);
				// gc.strokeRect (lastFace.x + lastEyeR.x, lastFace.y +
				// lastEyeR.y, lastEyeR.width, lastEyeR.height);

				eyeCenterR = rectCenter (lastEyeR);

				wR = 6 * lastEyeR.width;
				hR = wR / aspectRatio;

				xR = lastFace.x + eyeCenterR.x - wR / 2 + 8;
				yR = lastFace.y + eyeCenterR.y + lastEyeR.height - hR;

			}

			if (eyeCenterL != null && eyeCenterR != null)
			{
				eyeCenter = new Point ((eyeCenterL.x + eyeCenterR.x) / 2.0, (eyeCenterL.y + eyeCenterR.y) / 2.0);

				double angleL = toDegrees (atan2 (eyeCenterL.y - eyeCenter.y, eyeCenterL.x - eyeCenter.x));
				double angleR = toDegrees (atan2 (eyeCenter.y - eyeCenterR.y, eyeCenter.x - eyeCenterR.x));

				drawRotatedImage (gc, eyeOverlay, xL, yL, angleL, eyeCenterL.x, eyeCenterL.x, wL, hL);
				drawRotatedImage (gc, eyeOverlay, xR, yR, angleR, eyeCenterR.x, eyeCenterR.x, wR, hR);
			}
			else if (eyeCenterL != null)
			{
				gc.drawImage (eyeOverlay, xL, yL, wL, hL);
			}
			else if (eyeCenterR != null)
			{
				gc.drawImage (eyeOverlay, xR, yR, wR, hR);
			}
		}
	}

	private void detectFace (Mat frame)
	{
		Imgproc.cvtColor (frame, frame, Imgproc.COLOR_BGR2GRAY);
		Imgproc.equalizeHist (frame, frame);

		CascadeClassifier faceDetector = new CascadeClassifier (
				HAAR_CASCADES_FOLDER + "/haarcascade_frontalface_default.xml");
		CascadeClassifier eyeDetector = new CascadeClassifier (HAAR_CASCADES_FOLDER + "/haarcascade_eye.xml");

		MatOfRect faceDetections = new MatOfRect ();
		MatOfRect eyeDetections = new MatOfRect ();

		faceDetector.detectMultiScale (frame,
				faceDetections,
				1.1,
				3,
				Objdetect.CASCADE_SCALE_IMAGE,
				new Size (30, 30),
				new Size (1000, 1000));

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

			eyeDetector.detectMultiScale (face,
					eyeDetections,
					1.1,
					3,
					Objdetect.CASCADE_SCALE_IMAGE,
					new Size (30, 30),
					new Size (1000, 1000));

			for (Rect eyeRect : eyeDetections.toArray ())
			{

				Point centerFace = rectCenter (faceRect);

				// my left, comps right
				if (faceRect.y + eyeRect.y + eyeRect.height < centerFace.y && eyeRect.area () < faceRect.area () / 6)
				{
					if (faceRect.x + eyeRect.x + eyeRect.width < centerFace.x)
					{
						if (lastEyeR == null)
						{
							lastEyeR = eyeRect;
						}
						else if (eyeRect.area () / lastEyeL.area () < 1.3 && eyeRect.area () / lastEyeL.area () > 0.7)
						{
							lastEyeR = updateRect (eyeRect, lastEyeR, faceRect.size (), 6, 14);
						}
					}
					else if (faceRect.x + eyeRect.x > centerFace.x)
					{
						if (lastEyeL == null)
						{
							lastEyeL = eyeRect;
						}
						else if (eyeRect.area () / lastEyeR.area () < 1.3 && eyeRect.area () / lastEyeR.area () > 0.7)
						{
							lastEyeL = updateRect (eyeRect, lastEyeL, faceRect.size (), 6, 14);
						}
					}
				}
			}
		}
	}

	public static Point rectCenter (Rect rect)
	{
		return new Point (rect.x + rect.width / 2.0, rect.y + rect.height / 2.0);
	}

	// Returns cur (could be cur as passed in or cur could be set to last)
	public static Rect updateRect (Rect cur, Rect last, Size frameSize, double maxDistFrameDivisor,
			double pixelThresholdToNotRevertCurToLast)
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

	// http://stackoverflow.com/a/18262938/5900241
	// angle in degrees
	public static void rotate (GraphicsContext gc, Double angle, double pivotX, double pivotY)
	{
		Rotate r = new Rotate (angle, pivotX, pivotY);
		gc.setTransform (r.getMxx (), r.getMyx (), r.getMxy (), r.getMyy (), r.getTx (), r.getTy ());
	}

	// based on http://stackoverflow.com/a/18262938/5900241
	// angle in degrees
	private void drawRotatedImage (GraphicsContext gc, Image image, double x, double y, double angle, double pivotX,
			double pivotY, double width, double height)
	{
		// saves the current state on stack, including the current transform
		gc.save ();
		rotate (gc, angle, pivotX, pivotY);
		gc.drawImage (image, x, y, width, height);
		// back to original state (before rotation)
		gc.restore ();
	}
}
