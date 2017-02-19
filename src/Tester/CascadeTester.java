package Tester;

import java.awt.GridLayout;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

public class CascadeTester 	{
	
	public void run() {
		// Display Setup. For Testing
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	
//		CascadeClassifier faceDetector = new CascadeClassifier("C://Users/Student/ImagePics/classifer/cascade.xml");
//		Mat image = Highgui.imread("C://Users/Student/ImagePics/2016/img_1.jpeg");
//		CascadeClassifier faceDetector = new CascadeClassifier("/Users/Blevenson/Desktop/Robotics/ImageProcessing/2016/data/cascade.xml");
		CascadeClassifier faceDetector = new CascadeClassifier("/Users/Blevenson/Desktop/Robotics/ImageProcessing/classifier_real_335/classifier_real_335/cascade.xml");
		Mat image = Highgui.imread("/Users/Blevenson/Desktop/Robotics/ImageProcessing/2016/positive/30.jpeg");
		
		JLabel lblimage = new JLabel(new ImageIcon(toBufferedImage(image)));
		lblimage.setIcon(new ImageIcon(toBufferedImage(image)));
		JPanel mainPanel = new JPanel(new GridLayout());
		mainPanel.add(lblimage);
		frame.setSize(640, 480);
		frame.add(mainPanel);
		frame.setVisible(true);
		
		MatOfRect faceDetections = new MatOfRect();
		
		for(int i = 0; i <= 20; i++){
			image = Highgui.imread("/Users/Blevenson/Desktop/Robotics/ImageProcessing/2016/RealField/RealFullField/" + i +".jpg");
			if(image.empty())
				continue;
//			Imgproc.cvtColor(image, image, Imgproc.COLOR_RGB2GRAY);
			faceDetector.detectMultiScale(image, faceDetections);
		
			System.out.println(String.format("Detected %s objects " + i,faceDetections.toArray().length));
			
			// Draw a bounding box around each object
			for (Rect rect : faceDetections.toArray()) {
				Core.rectangle(image, new Point(rect.x, rect.y), new Point(rect.x
						+ rect.width, rect.y + rect.height), new Scalar(0, 255, 0));
			}
			
			lblimage.setIcon(new ImageIcon(toBufferedImage(image)));
			
			try{
				Thread.sleep(700);
			}catch(Exception e){}
		}
		// Draw a bounding box around each object
		for (Rect rect : faceDetections.toArray()) {
			Core.rectangle(image, new Point(rect.x, rect.y), new Point(rect.x
					+ rect.width, rect.y + rect.height), new Scalar(0, 255, 0));
		}

		// Save the visualized detection.
		Highgui.imwrite("/Users/Blevenson/Desktop/Robotics/ImageProcessing/2016/Tester.jpeg", image);
		lblimage.setIcon(new ImageIcon(toBufferedImage(image)));
	}

	public static void main(String[] args) {
		// Load the native library.
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		new CascadeTester().run();
	}
	
	public Image toBufferedImage(Mat m) {
		int type = BufferedImage.TYPE_BYTE_GRAY;
		if (m.channels() > 1) {
			type = BufferedImage.TYPE_3BYTE_BGR;
		}
		int bufferSize = m.channels() * m.cols() * m.rows();
		byte[] b = new byte[bufferSize];
		m.get(0, 0, b); // get all the pixels
		BufferedImage image = new BufferedImage(m.cols(), m.rows(), type);
		final byte[] targetPixels = ((DataBufferByte) image.getRaster()
				.getDataBuffer()).getData();
		System.arraycopy(b, 0, targetPixels, 0, b.length);
		return image;
	}
}
