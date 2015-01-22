import java.awt.BorderLayout;
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
import org.opencv.core.CvType;
import org.opencv.core.Mat;
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

	private int picCount = 0;
	//Green Reflector
		//min
		public int HMIN = 40;
		private int SMIN = 150;
		private int VMIN = 120;
		//Max
		private int HMAX = 72;
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
	
	private final boolean PRINT = true;

	public static void main(String[] args) {
		new NetworkTablesDesktopClient().run();
	}

	public void run() {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		VideoCapture vid = new VideoCapture(1);

		NetworkTable.setClientMode();
		NetworkTable.setIPAddress("roborio-766.local");
		table = NetworkTable.getTable("dataTable");

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
		 * work, or is not called, the commands during auto don't get hungup.
		 */
		//Put sliders
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
			//img = Highgui.imread("file:///Users/Blevenson/git/VisionClient/RefPics/rawMove_0.jpeg");

			// Imgproc.threshold(img, pic, 100, 255, Imgproc.THRESH_BINARY);
			// Imgproc.cvtColor(img, pic, Imgproc.COLOR_RGB2HSV);
			// ArrayList<Mat> channels = new ArrayList<Mat>();
			// Core.split(pic, channels);
			// satImg = channels.get(1);
			// Imgproc.medianBlur(satImg , satImg , 11);
			// Imgproc.adaptiveThreshold(satImg , satImg , 255,
			// Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, 401, -10);

			Imgproc.cvtColor(img, hsv, Imgproc.COLOR_BGR2HSV);
			// Imgproc.threshold(hsv, satImg, 250, 800, Imgproc.THRESH_BINARY);
			Core.inRange(hsv, new Scalar(HMIN, SMIN, VMIN), new Scalar(HMAX, SMAX, VMAX), satImg);

			lblimage.setIcon(new ImageIcon(toBufferedImage(img)));

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
		Highgui.imwrite("C://Users/Student/ImagePics/rawRing_" + picCount + ".jpeg", img);
		picCount++;
		
		if(picCount >= 10)done = true;
		
		System.out.println("Looping: " + picCount);
	}
}
