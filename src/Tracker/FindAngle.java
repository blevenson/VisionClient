package Tracker;

import java.awt.GridLayout;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.highgui.VideoCapture;
import org.opencv.imgproc.Imgproc;

/**
 * Calculates the angle needed to turn to center a pixel
 * on the center of the image
 * 
 * @author Brett Levenson
 *
 * Resources:
 * Finding focal length: http://answers.opencv.org/question/17076/conversion-focal-distance-from-mm-to-pixels/
 *
 */

public class FindAngle {
	//Constants
	public final double FOCAL_LENGTH = 1892.8;//2.8; //pixels
	public final double CENTER_X = 640;
	public final double CENTER_Y = 360;
	
	private VideoCapture vid;
	
	//Images
	private Mat img = new Mat(480,640, 16);
	private Mat hsv = new Mat();
	private Mat binaryImage = new Mat();
	
	private JLabel lblimage;
	
	private List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
	private ArrayList<Rect> boundingRects = new ArrayList<Rect>();
	
	//Color detection
	//min
	public int B_HMIN = 90;
	private int B_SMIN = 183;
	private int B_VMIN = 105;
	//Max
	private int B_HMAX = 142;
	private int B_SMAX = 255;
	private int B_VMAX = 190;
	
	public static void main(String[] args){
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		
		FindAngle angleFinder = new FindAngle();
		angleFinder.init();
		while(true){
			angleFinder.run();
		}
	}
	
	public void init(){
		vid = new VideoCapture(0);
		
		while(img.empty())
			vid.read(img);
		
		// Display Setup. For Testing
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		lblimage = new JLabel(new ImageIcon(toBufferedImage(img)));
		lblimage.setIcon(new ImageIcon(toBufferedImage(img)));
		
		JPanel mainPanel = new JPanel(new GridLayout());
		mainPanel.add(lblimage);
		
		frame.setSize(500, 500);
		frame.add(mainPanel);
		frame.setVisible(true);
	}
	
	public void run(){
		//Grab the image from the camera
		vid.read(img);

		//Exit loop if the image grabbed is empty
		if (img.empty()) return;
		
		//Turn the color image into and HSV image
		Imgproc.cvtColor(img, hsv, Imgproc.COLOR_BGR2HSV);
		
		//Find the contours using colors
		findContours();
		
		//Return if no contours detected
		if(contours.size() == 0){
			System.out.println("No Conters");
			return;
		}
		
		//Add bounding boxes to each contour
		for(MatOfPoint point : contours)
			boundingRects.add(Imgproc.boundingRect(point));
		
		//Find average center of all bounding rectangles
		Point centerPoint = findAvgOfRects(boundingRects);
		
		//Add centerPoint to display Image
		Core.circle(img, centerPoint, 10, new Scalar(0, 0, 0));
		Core.circle(img, new Point(CENTER_X, CENTER_Y), 10, new Scalar(0, 0, 0));

		//Display angle to turn robot
		Core.putText(img, "Angle: " + Math.toDegrees(convertPointToAngle(centerPoint)), new Point(10, 40), 1, 1, new Scalar(0, 255, 255));
		
		//Display image
		lblimage.setIcon(new ImageIcon(toBufferedImage(img)));
		
		//Clean up
		boundingRects.clear();
		contours.clear();
	}
	
	//Uses the Constant color values to find contours in the image
	public void findContours(){
		Mat greenHierarchy = new Mat();
		
		//Real Values
		Core.inRange(hsv, new Scalar(B_HMIN, B_SMIN, B_VMIN), new Scalar(B_HMAX, B_SMAX, B_VMAX), binaryImage);
		
		Imgproc.erode(binaryImage, binaryImage, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(5,5)));
		Imgproc.dilate(binaryImage, binaryImage, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(5,5)));
		Imgproc.drawContours(img, contours, -1, new Scalar(0,0,255));
				
		Imgproc.findContours(binaryImage, contours, greenHierarchy, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_TC89_KCOS);
	}
	
	//Finds average center of rectangles
	public Point findAvgOfRects(ArrayList<Rect> boxes){
		double totX = 0;
		double totY = 0;
		
		for(Rect r : boxes){
			//Location of center of rectangles
			totX += r.x + r.width/2.0;
			totY += r.y + r.height/2.0;
		}
		
		return new Point(totX / boxes.size(), totY / boxes.size());
	}
	
	//Convert pixel point on screen, to heading error
	public double convertPointToAngle(Point point){
		double[] pixel = {point.x - CENTER_X, point.y - CENTER_Y, FOCAL_LENGTH};
		double[] center = {0, 0, FOCAL_LENGTH};
		
		double dot = dotProduct(pixel, center);
		
		//Find angle between vectors using dot product
		return ((point.x > CENTER_X)? 1 : -1) * Math.acos(dot/(magnitudeVector(pixel) * magnitudeVector(center)));
	}
	
	// A*B = Ax*Bx + Ay*By + Az*Bz + ...
	private double dotProduct(double[] a, double[] b){
	    if(a.length != b.length){
	    	System.out.println("Error: vectors must be the same dimension");
	    	return 0;
	    }
	
		double total = 0;
		for(int i = 0; i < a.length; i++){
			total += a[i] * b[i];
		}
		
		return total;
	}
	
	//	|A| = Ax^2 + Ay^2 + Az^2 + ...
	private double magnitudeVector(double[] vector){
		double total = 0;
		
		for(double d : vector){
			total += Math.pow(d, 2);
		}
		
		return Math.sqrt(total);
	}
	
	//Convert Matrix image to BufferedImage so it can be displayed
	public static Image toBufferedImage(Mat m) {
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
