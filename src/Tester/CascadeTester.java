package Tester;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

public class CascadeTester {
	public void run() {
		CascadeClassifier faceDetector = new CascadeClassifier("C://Users/Student/ImagePics/classifer/cascade.xml");
//		CascadeClassifier faceDetector = new CascadeClassifier("C://Users/Student/ImagePics/data/cascade.xml");
//		CascadeClassifier faceDetector = new CascadeClassifier("C://Users/Student/ImagePics/data2/cascade.xml");
		
		Mat image = Highgui.imread("C://Users/Student/ImagePics/2016/img_1.jpeg");
		
		Imgproc.cvtColor(image, image, Imgproc.COLOR_RGB2GRAY);
		
		MatOfRect faceDetections = new MatOfRect();
		faceDetector.detectMultiScale(image, faceDetections);

		System.out.println(String.format("Detected %s objects",faceDetections.toArray().length));

		// Draw a bounding box around each object
		for (Rect rect : faceDetections.toArray()) {
			Core.rectangle(image, new Point(rect.x, rect.y), new Point(rect.x
					+ rect.width, rect.y + rect.height), new Scalar(0, 255, 0));
		}

		// Save the visualized detection.
		Highgui.imwrite("C://Users/Student/ImagePics/2016/Tester.jpeg", image);
	}

	public static void main(String[] args) {
		// Load the native library.
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		new CascadeTester().run();
	}
}
