package Tester;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.highgui.VideoCapture;
import org.opencv.imgproc.Imgproc;

/**
 * Program to find constants in real time for Hough Circle Transform
 * 
 * Based on:
 * http://docs.opencv.org/2.4/doc/tutorials/imgproc/imgtrans/hough_circle
 * /hough_circle.html
 *
 * @author blevenson
 */

public class HoughTransform {
	private Mat gray = new Mat();
	private Mat img = new Mat(480, 640, 16);

	private int cannyDetectorThresh = 50;
	private int centerDetectorThresh = 85;
	
	private JLabel values;
	
	// Track image
	Mat greenHierarchy = new Mat();
	List<MatOfPoint> contours = new ArrayList<MatOfPoint>();

	private ArrayList<Rect> boundingRects = new ArrayList<Rect>();
	private ArrayList<Point> centerPoints = new ArrayList<Point>();

	private JLabel lblimage;
	private VideoCapture vid;

	public static void main(String[] args) {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

		HoughTransform tester = new HoughTransform();
		tester.initialize();
		while (!tester.isFinished()) {
			tester.execute();
		}
	}

	protected void initialize() {
		vid = new VideoCapture(0);
		vid.read(img);

		while (img.empty())
			vid.read(img);

		// Display Setup. For Testing
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		lblimage = new JLabel(new ImageIcon(toBufferedImage(img)));
		lblimage.setIcon(new ImageIcon(toBufferedImage(img)));

		JSlider cannySlider = new JSlider(JSlider.VERTICAL,1,100,cannyDetectorThresh);
		JSlider centerSlider = new JSlider(JSlider.VERTICAL,1,100,centerDetectorThresh);
		
		cannySlider.addChangeListener(new ChangeListener(){
			@Override
			public void stateChanged(ChangeEvent e) {
				JSlider source = (JSlider)e.getSource();
				cannyDetectorThresh = source.getValue();
				updateTextView();
			}
		});
		centerSlider.addChangeListener(new ChangeListener(){
			@Override
			public void stateChanged(ChangeEvent e) {
				JSlider source = (JSlider)e.getSource();
				centerDetectorThresh = source.getValue();
				updateTextView();
			}
		});
		
		JPanel mainPanel = new JPanel(new GridLayout());
		mainPanel.add(lblimage);
		
		JPanel fp = new JPanel();
		fp.add(cannySlider);
		fp.add(centerSlider);
		
		JPanel disp = new JPanel();
		values = new JLabel();
		values.setPreferredSize(new Dimension(1000, 100));
		disp.add(values, BorderLayout.SOUTH);
		
		frame.add(disp, BorderLayout.SOUTH);
		frame.add(fp, BorderLayout.EAST);
		
		frame.setSize(500, 500);
		frame.add(mainPanel);
		frame.setVisible(true);
	}

	protected void execute() {
		vid.read(img);

		if (img.empty())
			return;

		Imgproc.cvtColor(img, gray, Imgproc.COLOR_BGR2GRAY);


		// Blur image to reduce noise
		Imgproc.GaussianBlur(gray, gray, new Size(9, 9), 2, 2);

		Mat circles = new Mat();
		// Find circles
		Imgproc.HoughCircles(gray, circles, Imgproc.CV_HOUGH_GRADIENT, 1,
				gray.rows() / 8, cannyDetectorThresh, centerDetectorThresh, 0, 0);

		//printMatrix(circles);

		//Display circles on screen
		for (int i = 0; i < circles.cols(); i++) {
			Point center = new Point(Math.round(circles.get(0, i)[0]),
					Math.round(circles.get(0, i)[1]));
			int radius = (int) Math.round(circles.get(0, i)[2]);
			// circle center
			Core.circle(img, center, 3, new Scalar(0, 255, 0), -1, 8, 0);
			// circle outline
			Core.circle(img, center, radius, new Scalar(0, 0, 255), 3, 8, 0);
		}

		lblimage.setIcon(new ImageIcon(toBufferedImage(img)));

		// Cleanup
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
	
	private void updateTextView(){
    	values.setText("Threshold - Canny Detector: " + cannyDetectorThresh + "     " +
    				   "Threshold - Center detection: " + centerDetectorThresh);
    }

	private void printMatrix(Mat in) {
		for (int i = 0; i < in.rows(); i++) {
			for (int j = 0; j < in.cols(); j++) {
				System.out.print(" [ ");
				for (double x : in.get(i, j)) {
					System.out.print(x + " ");
				}
				System.out.print(" ] ");
			}
			System.out.println();
		}
	}
}
