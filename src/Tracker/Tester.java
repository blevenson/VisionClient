package Tracker;

import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.highgui.VideoCapture;
import org.opencv.imgproc.Imgproc;

import Lib.AxisCamera;

/**
 * Driving subsystem
 *
 * @author blevenson
 */

public class Tester {
	private Mat hsv = new Mat();
	private Mat binaryImage = new Mat();
	private Mat img = new Mat(480,640, 16);
	
	private final int TRACK_MODE = 4;
	private final int PIXEL_WIDTH = 351;
	//inches
	private final double DISTANCE = 45;
	private final double TAPE_WIDTH = 20.5;
	private  final double FOCAL_LENGTH = (PIXEL_WIDTH * DISTANCE) / TAPE_WIDTH;
		
	//Track image
	Mat greenHierarchy = new Mat();
	List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
	
	private ArrayList<Rect> boundingRects = new ArrayList<Rect>();
	private ArrayList<Point> centerPoints = new ArrayList<Point>();
	private Rect biggestRect = new Rect();
	
	private Point trackPoint;
	
	private int picCount = 0;
	//Box Tracer
		//min
		public int B_HMIN = 73;
		private int B_SMIN = 182;
		private int B_VMIN = 159;
		//Max
		private int B_HMAX = 106;
		private int B_SMAX = 255;
		private int B_VMAX = 255;
		
	private JLabel lblimage;
	private VideoCapture vid;
	
	private Point followPoint = new Point();
	private Point largestCenter;
	private AxisCamera cam; 
	
	private boolean takePhoto;
	
	public static void main(String[] args){
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		
		Tester tester = new Tester();
		tester.initialize();
		while(!tester.isFinished()){
			tester.execute();
		}
	}
	
	protected void initialize(){
//		vid = new VideoCapture(0);
//		vid.read(img);
		cam = new AxisCamera("169.254.2.2");
		
		img = cam.getImage();
		
		while(img.empty())
			img = cam.getImage();
		
		// Display Setup. For Testing
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		lblimage = new JLabel(new ImageIcon(toBufferedImage(img)));
		lblimage.setIcon(new ImageIcon(toBufferedImage(img)));
		
		JPanel mainPanel = new JPanel(new GridLayout());
		mainPanel.add(lblimage);
		
		takePhoto = false;
		mainPanel.addMouseListener(new MouseListener(){
			public void mouseClicked(MouseEvent e) {}
			public void mousePressed(MouseEvent e) {
				takePhoto = true;
			}
			public void mouseReleased(MouseEvent e) {}
			public void mouseEntered(MouseEvent e) {}
			public void mouseExited(MouseEvent e) {}
		});
		
		frame.setSize(500, 500);
		frame.add(mainPanel);
		frame.setVisible(true);
	}

	protected void execute() {
		img = cam.getImage();

		if (img.empty()) return;
		
		Imgproc.cvtColor(img, hsv, Imgproc.COLOR_BGR2HSV);
		
		if(takePhoto){
			savePics(img);
			takePhoto = false;
		}
		
		//Real Values
		Core.inRange(hsv, new Scalar(B_HMIN, B_SMIN, B_VMIN), new Scalar(B_HMAX, B_SMAX, B_VMAX), binaryImage);
		
		Imgproc.erode(binaryImage, binaryImage, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(5,5)));
		Imgproc.dilate(binaryImage, binaryImage, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(5,5)));
		Imgproc.drawContours(img, contours, -1, new Scalar(0,0,255));
				
		Imgproc.findContours(binaryImage, contours, greenHierarchy, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_TC89_KCOS);
		
		if(contours.size() == 0){
			System.out.println("No Conters");
			return;
		}
		
		for(MatOfPoint point : contours)
			boundingRects.add(Imgproc.boundingRect(point));
		
		for(Rect r : boundingRects){
			//Create the arraylist with the center points
			centerPoints.add(new Point(r.x + r.width/2, r.y + r.height/2));
			
			if(r.width * r.height > biggestRect.height * biggestRect.width){
				biggestRect = r;
				largestCenter = centerPoints.get(centerPoints.size() - 1);
			}
		}

		if(centerPoints.size() < 1)return;
		followPoint.y = binaryImage.height()/2;
//		followPoint.x = binaryImage.width()/2;
		if(TRACK_MODE == 4)
			followPoint.x = binaryImage.width() / 2;
		else if(centerPoints.size() == 1 || TRACK_MODE > 2 || contours.size() > 3)
			followPoint.x = largestCenter.x;
		else if(centerPoints.size() == 3){
			switch(TRACK_MODE){
				case 0:
					followPoint.x = 0;
					break;
				case 1:
					followPoint.x = binaryImage.width()/2;
					break;
				case 2:
					followPoint.x = binaryImage.width();
					break;
			}
		}
		trackPoint = findClossestPoint(centerPoints, followPoint);
		
		Core.rectangle(img, new Point(trackPoint.x-10, trackPoint.y-10), new Point(trackPoint.x + 10, trackPoint.y + 10), new Scalar(255, 0, 100));
		
		for(Rect r : boundingRects){
			if(r.contains(trackPoint)){
				Core.putText(img, "distance: " + (TAPE_WIDTH * FOCAL_LENGTH) / r.width, new Point(10, 20), 1, 1, new Scalar(0, 255, 255));
//				System.out.println("distance: " + (TAPE_WIDTH * FOCAL_LENGTH) / r.width);s
				//System.out.println("Angle: " + Math.atan((TAPE_WIDTH)/(TAPE_WIDTH * FOCAL_LENGTH) / r.width));
				//System.out.println("Angle: " + Math.toDegrees(2 * Math.atan((followPoint.y - trackPoint.y)/FOCAL_LENGTH)));
			}
		}
				
		
		//System.out.println("xErr: " + (binaryImage.width()/2 - trackPoint.x) + " yErr: " + (binaryImage.height()/2 - trackPoint.y));
		lblimage.setIcon(new ImageIcon(toBufferedImage(img)));
		
		//Cleanup
		centerPoints.clear();
		boundingRects.clear();
		contours.clear();
	}

	protected boolean isFinished() {
		return false;
	}

	protected void end() {

	}

	protected void interrupted() {
		end();
	}
	
	private Point findClossestPoint(ArrayList<Point> points, Point followPoint){
		//Find the center point from all the contours
		trackPoint = points.get(0);
		for(Point p : points){
			if(Math.sqrt(Math.pow(followPoint.x - trackPoint.x ,2) + Math.pow(followPoint.y - trackPoint.y ,2)) > Math.sqrt(Math.pow(binaryImage.width()/2 - p.x ,2) + Math.pow(binaryImage.height()/2 - p.y ,2)))
				trackPoint = p;
		}
		return trackPoint;
	}
	
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
	
	public void savePics(Mat img) {
//		try {
//			Thread.sleep(1000);
//		} catch (InterruptedException ex) {
//			System.out.println("0 noses.....I 2 tyd to swep");
//		}
		// change address for your computers
		Highgui.imwrite("C://Users/Student/ImagePics/img_" + picCount + ".jpeg", img);
		picCount++;
		
		System.out.println("Saving: " + picCount);
	}
}