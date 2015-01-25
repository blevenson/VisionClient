import java.awt.BorderLayout;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

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

import edu.wpi.first.wpilibj.networktables.NetworkTable;

public class NetworkTablesDesktopClient {

	/*
	 * Instance variables because I might have separate methods to turn the
	 * image into real motor values...It would be very messy if it was all in
	 * one method...Gross.
	 */
	private NetworkTable table;
	private double leftMotorC = 0d;
	private double rightMotorC = 0d;
	private boolean done;
	
	private Point left;
	private Point right;

	private int picCount = 0;
	//Green Reflector
		//min
		public int HMIN = 90;
		private int SMIN = 100;
		private int VMIN = 250;
		//Max
		private int HMAX = 255;
		private int SMAX = 255;
		private int VMAX = 255;
	//Box Tracer
		//min
		public int B_HMIN = 20;
		private int B_SMIN = 80;
		private int B_VMIN = 125;
		//Max
		private int B_HMAX = 100;
		private int B_SMAX = 255;
		private int B_VMAX = 255;
	
	private final boolean PRINT = false;

	public static void main(String[] args) {
		new NetworkTablesDesktopClient().run();
	}

	public void run() {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		VideoCapture vid = new VideoCapture(0);

		NetworkTable.setClientMode();
		NetworkTable.setIPAddress("roborio-766.local");
		table = NetworkTable.getTable("dataTable");
		
		left = new Point();
		right = new Point();

		Mat img = new Mat();
		vid.read(img);

		// Display Setup. For Testing
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		JLabel lblimage = new JLabel(new ImageIcon(toBufferedImage(img)));
		lblimage.setIcon(new ImageIcon(toBufferedImage(img)));
		JPanel mainPanel = new JPanel(new BorderLayout());
		mainPanel.add(lblimage);
		frame.add(mainPanel);
		frame.setSize(500, 500);
		frame.setVisible(true);

		Mat pic = new Mat();

		/*
		 * Done starts as true, so that if the image processing here does not
		 * work, or is not called, the commands during auto don't get hung-up.
		 */
		//Put sliders8
		table.putNumber("HMIN", HMIN);
		table.putNumber("SMIN", SMIN);
		table.putNumber("VMIN", VMIN);
		
		table.putNumber("HMAX", HMAX);
		table.putNumber("SMAX", SMAX);
		table.putNumber("VMAX", VMAX);
		
		table.putBoolean("done", false);
		done = (table.getBoolean("done"));

		Mat hsv = new Mat();
		Mat satImg = new Mat();
		
		while (!done) {

			vid.read(img);
			
			Imgproc.cvtColor(img, hsv, Imgproc.COLOR_BGR2HSV);
			
			//Real Values
			Core.inRange(hsv, new Scalar(HMIN, SMIN, VMIN), new Scalar(HMAX, SMAX, VMAX), satImg);
			//Core.inRange(hsv, new Scalar(B_HMIN, B_SMIN, B_VMIN), new Scalar(B_HMAX, B_SMAX, B_VMAX), satImg);
			
			Imgproc.erode(satImg, satImg, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(1,1)));
			Imgproc.dilate(satImg, satImg, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(8,8)));
			
			//Track image
			List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
			Mat hierarchy = new Mat();
			Imgproc.findContours(satImg, contours, hierarchy, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
			Imgproc.drawContours(satImg, contours, -1, new Scalar(255,255,0));
			
			if(contours.size() > 0)
			{
				left = new Point(Imgproc.boundingRect(contours.get(0)).x, Imgproc.boundingRect(contours.get(0)).y);
				right = new Point(Imgproc.boundingRect(contours.get(0)).x, Imgproc.boundingRect(contours.get(0)).y);
			}
			
			for(MatOfPoint point : contours)
			{
				Rect rectangle = Imgproc.boundingRect(point);
				//System.out.println("(" + rectangle.x + " " + rectangle.y + ")");
				//Find farthest left and right corners of tote
				if(rectangle.x < left.x)
					left.x = rectangle.x;
				if(rectangle.y < left.y)
					left.y = rectangle.y;
				
				if((rectangle.x + rectangle.width) > right.x)
					right.x = rectangle.x + rectangle.width;
				if((rectangle.y + rectangle.height) > right.y)
					right.y = rectangle.y + rectangle.height;
				
//				Core.rectangle(satImg, new Point(rectangle.x, rectangle.y),
//						new Point(rectangle.x + rectangle.width, 
//						rectangle.y + rectangle.height), new Scalar(255,255,0));
			}
			

			Core.rectangle(satImg, left, right, new Scalar(255,255,0));
			
			lblimage.setIcon(new ImageIcon(toBufferedImage(satImg)));

			if(PRINT)savePics(img);
			
			updateSliderValues();

			table.putNumber("leftMotor", leftMotorC);
			table.putNumber("rightMotor", rightMotorC);
			// System.out.println("Left: " + leftMotorC + " Right: " + rightMotorC);

		}
	}

	private void updateSliderValues() {
		//Update values
		HMIN = (int)(table.getNumber("HMIN"));
		SMIN = (int)(table.getNumber("SMIN"));
		VMIN = (int)(table.getNumber("VMIN"));
		HMAX = (int)(table.getNumber("HMAX"));
		SMAX = (int)(table.getNumber("SMAX"));
		VMAX = (int)(table.getNumber("VMAX"));
		
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
		try {
			Thread.sleep(1000);
		} catch (InterruptedException ex) {
			System.out.println("0 noses.....I 2 tyd to swep");
		}
		// change address for your computer
		Highgui.imwrite("C://Users/Student/ImagePics/filteredBox_" + picCount + ".jpeg", img);
		picCount++;
		
		if(picCount >= 10)done = true;
		
		System.out.println("Looping: " + picCount);
	}
}
