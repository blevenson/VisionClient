package Peg;

import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

public class TrackRects implements Runnable{
	
	private final String folderDirect = "C:/Users/Student/Downloads/GearVisionTarget";
	
	private int hueMin = 38;//53;
	private int satMin = 92;
	private int valueMin = 56;//111;
	private int hueMax = 96;
	private int satMax = 255;
	private int valueMax = 165;
	
	private final int SUM_THRESHOLD = 8;
	private final int LOW_THRESHOLD = 3;
	private final double MAGNITUDE_THRESH = 0.5;
	
	private final double FOCAL_LENGTH = 1892.8;//2.8; //pixels
	private final double CENTER_X = 320;
	private final double CENTER_Y = 240;
	
	public static void main(String[] args){
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		
		new Thread(new TrackRects()).start();
	}
	
	private JLabel lblimage;
	private Mat img = new Mat();
	private boolean nextImage = false;
	private int picCount;
	
	private int x = 0,
				y = 0;
	public void run(){
		img = Highgui.imread(folderDirect + "/untitled1.png");
		
		// Display Setup. For Testing
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		lblimage = new JLabel(new ImageIcon(toBufferedImage(img)));
		lblimage.setIcon(new ImageIcon(toBufferedImage(img)));
		
		JPanel mainPanel = new JPanel(new GridLayout());
		mainPanel.add(lblimage);
		mainPanel.addMouseListener(new MouseListener(){
			public void mouseClicked(MouseEvent e) {
			}

			public void mousePressed(MouseEvent e) {
			}

			public void mouseReleased(MouseEvent e) {
				nextImage = true;
			}

			public void mouseEntered(MouseEvent e) {
			}

			public void mouseExited(MouseEvent e) {
			}
		});
		
		frame.setSize(640, 480);
		frame.add(mainPanel);
		frame.setVisible(true);
		
		for(int i = 1; i <= 56; i++){			
			if(!new File(folderDirect + "/untitled" + i + ".png").exists()){
				continue;
			}
			img = Highgui.imread(folderDirect + "/untitled" + i + ".png", 1);
			
//			lblimage.setIcon(new ImageIcon(toBufferedImage(proccess(img))));
			lblimage.setIcon(new ImageIcon(toBufferedImage(proccess2(img))));
			//Force them to click to next
			while(!nextImage){
				try {
					Thread.sleep(100);
				} catch (InterruptedException e1) {}
			}
			nextImage = false;
		}
		System.exit(1);
	}
	
	private Mat proccess(Mat in){
		Mat output = in;
		
		Imgproc.cvtColor(img, img, Imgproc.COLOR_BGR2HSV);
		
		Core.inRange(img, new Scalar(hueMin, satMin, valueMin), new Scalar(hueMax, satMax, valueMax), img);
		
		Imgproc.blur(output, output, new Size(3,3));
		
		Imgproc.Canny(output, output, 100d, 300d);
		
		double[] sums = new double[img.width()];
		
		for(int i = 0; i < img.width(); i++){
			for(int j = 0; j < img.height(); j++){
				sums[i] += img.get(j, i)[0]/255.0;
			}
		}
//		System.out.println(Arrays.toString(sums));
		
		double[] mainPoints = new double[4];
		int index = 0;
		boolean droppedBelowThresh = true;
		//Find left most number
		for(int i = 0; i < sums.length; i++){
			if(sums[i] > SUM_THRESHOLD){
				if(droppedBelowThresh){
					if(index > 3){
						System.out.println("More than 4 important points found!");
						continue;
					}
					
					mainPoints[index] = i;
					index++;
					
					droppedBelowThresh = false;
				}
			}else if(sums[i] < LOW_THRESHOLD){
				droppedBelowThresh = true;
			}
		}
		
		System.out.println(Arrays.toString(mainPoints));
		for(double x : mainPoints){
			Core.line(output, new Point(x, 0), new Point(x, output.height()), new Scalar(255, 255, 255));
		}
		
		return output;
	}
	
	private Mat proccess2(Mat in){
		Mat output = in.clone();
		
		Imgproc.cvtColor(output, output, Imgproc.COLOR_BGR2HSV);
		
		Core.inRange(output, new Scalar(hueMin, satMin, valueMin), new Scalar(hueMax, satMax, valueMax), output);
		
		Imgproc.blur(output, output, new Size(3,3));
		
		Imgproc.Canny(output, output, 100d, 300d);
		
		Mat display = output.clone();
		
		Mat lines = new Mat();
		Imgproc.HoughLines(output, lines, 10, Math.PI/20, 10, 100, 2000);
		
		//Display Hough Lines
		Mat cardLines = new Mat();
		Imgproc.HoughLinesP(output, cardLines, 10, Math.PI/20, 10, 100, 2000);

		for(int i = 0; i < cardLines.cols(); i++){
			System.out.println(Arrays.toString(cardLines.get(0, i)));
			Core.line(display, new Point(cardLines.get(0, i)[0], cardLines.get(0, i)[1]), new Point(cardLines.get(0, i)[2], cardLines.get(0, i)[3]), new Scalar(0, 255, 255));
		}
		
		ArrayList<Mat> splitChannels = new ArrayList<Mat>();
		Core.split(lines, splitChannels);
		
		Mat angles = splitChannels.get(1);
		
		//Calculate gradient field
		int[] histogram = new int[90];
		for(int i = 0; i < angles.rows(); i++){
			int ang = (int)(angles.get(i, 0)[0]*180/Math.PI);
			if(Math.abs(ang) <= 45)
				histogram[ang + 45]++; 
		}
		
		int max = histogram[0];
		int angle = 0;
		for(int i = 0; i < histogram.length; i++){
			if(histogram[i] > max){
				max = histogram[i];
				angle = i;
			}
		}
		//Account for neg numbers in the array
		angle -= 45;
		
		System.out.println("Angle: " + angle);
				
//		printMat(lines);
//		System.out.println(lines.toString());
		
		//Rotate image x degrees
		Point src_center = new Point(output.cols()/2.0F, output.rows()/2.0F);
		Mat rot_mat = Imgproc.getRotationMatrix2D(src_center, angle, 1.0);
		Imgproc.warpAffine(output, output, rot_mat, output.size());
		

		double[] sums = new double[output.width()];
		
		for(int i = 0; i < output.width(); i++){
			for(int j = 0; j < output.height(); j++){
				sums[i] += output.get(j, i)[0]/255.0;
			}
		}
//		System.out.println(Arrays.toString(sums));
		
		double[] mainPoints = new double[4];
		int index = 0;
		boolean droppedBelowThresh = true;
		//Find left most number
		for(int i = 0; i < sums.length; i++){
			if(sums[i] > SUM_THRESHOLD){
				if(droppedBelowThresh){
					if(index > 3){
						System.out.println("More than 4 important points found!");
						continue;
					}
					
					mainPoints[index] = i;
					index++;
					
					droppedBelowThresh = false;
				}
			}else if(sums[i] < LOW_THRESHOLD){
				droppedBelowThresh = true;
			}
		}
		
		System.out.println(Arrays.toString(mainPoints));
		for(double x : mainPoints){
			Core.line(output, new Point(x, 0), new Point(x, output.height()), new Scalar(255, 255, 255));
		}
		if(!contains(mainPoints, 0)){
			Point centerPoint = new Point(mainPoints[0] + (mainPoints[3] - mainPoints[0])/2.0, CENTER_Y);
			Core.circle(output, centerPoint, 5, new Scalar(255,255,255));
			Core.putText(output, "Angle: " + Math.toDegrees(convertPointToAngle(centerPoint)), new Point(10, 40), 1, 1, new Scalar(255, 255, 255));
		}
		return output;
	}
	
	private boolean contains(double[] array, double value){
		for(double x : array){
			if(value == x)
				return true;
		}
		return false;
	}
	
	//Convert pixel point on screen, to heading error
	public double convertPointToAngle(Point point){
			double[] pixel = {point.x - CENTER_X, point.y - CENTER_Y, FOCAL_LENGTH};
			double[] center = {0, 0, FOCAL_LENGTH};
			
			double dot = dotProduct(pixel, center);
			
			//Find angle between vectors using dot product
			return ((point.x > CENTER_X)? 1 : -1) * Math.acos(dot/(magnitudeVector(pixel) * magnitudeVector(center)));
	}
	
	private double magnitudeVector(double[] vector){
		double total = 0;
		
		for(double d : vector){
			total += Math.pow(d, 2);
		}
		
		return Math.sqrt(total);
	}
	
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
	
	private void printMat(Mat input){
		for(int i = 0; i < input.rows(); i++){
			for(int j = 0; j < input.cols(); j++){
				System.out.print("[ ");
				for(int k = 0; k < input.channels(); k++){
					System.out.print(" " + input.get(i, j)[k]);
				}
				System.out.print("] ");
			}
			System.out.println();
		}
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
