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
import org.opencv.highgui.VideoCapture;
import org.opencv.imgproc.Imgproc;

import edu.wpi.first.wpilibj.networktables.NetworkTable;

public class NetworkTablesDesktopClient {

	/*
	 * Instance variables because I might have separate methods to turn the
	 * image into real motor values...It would be very messy if it was all in
	 * one method...Gross.
	 */
	private double leftMotorC = 0d;
	private double rightMotorC = 0d;

	public static void main(String[] args) {
		new NetworkTablesDesktopClient().run();
	}

	public void run() {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		VideoCapture vid = new VideoCapture(1);
		
		NetworkTable.setClientMode();
		NetworkTable.setIPAddress("roborio-766.local");
		NetworkTable table = NetworkTable.getTable("dataTable");
		
		Mat img = new Mat();
		vid.read(img);
		
		//Display Setup.   For Testing
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
		table.putBoolean("done", false);
		boolean done = (table.getBoolean("done"));
		
		Mat hsv = new Mat();
		Mat satImg = new Mat();
		while (!done) {
//			try {
//				Thread.sleep(1000);
//			} catch (InterruptedException ex) {
//				System.out.println("0 noses.....I 2 tyd to swep");
//			}

			// scan image and then set the motor values
			// update done
			vid.read(img);
			//Imgproc.threshold(img, pic, 100, 255, Imgproc.THRESH_BINARY);
//			Imgproc.cvtColor(img, pic,  Imgproc.COLOR_RGB2HSV);
//			ArrayList<Mat> channels = new ArrayList<Mat>();
//			Core.split(pic, channels);
//			satImg = channels.get(1);
//			Imgproc.medianBlur(satImg , satImg , 11);
//			Imgproc.adaptiveThreshold(satImg , satImg , 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, 401, -10);
			
		    Imgproc.cvtColor(img, hsv, Imgproc.COLOR_RGB2HSV);
		    //Imgproc.threshold(hsv, satImg, 250, 800, Imgproc.THRESH_BINARY);
		    Core.inRange(hsv, new Scalar(49, 100, 100), new Scalar(55,255,255), satImg);
		    
		     
		    
		    lblimage.setIcon(new ImageIcon(toBufferedImage(satImg)));
		    
		    
		    
		    table.putNumber("leftMotor", leftMotorC);
			table.putNumber("rightMotor", rightMotorC);
			//System.out.println("Left: " + leftMotorC + " Right: " + rightMotorC);

		}
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
}
