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
		
		Mat display = output.clone();
		
		Imgproc.blur(output, output, new Size(3,3));
		
		Imgproc.Canny(output, output, 100d, 300d);
		
		Mat lines = new Mat();
		Imgproc.HoughLines(output, lines, 1, Math.PI/180, 10);
		
		ArrayList<Mat> splitChannels = new ArrayList<Mat>();
		Core.split(lines, splitChannels);
		
		Mat angles = splitChannels.get(1);
		
		//Calculate gradient field
		int[] histogram = new int[360];
		for(int i = 0; i < angles.rows(); i++){
			histogram[(int)(angles.get(i, 0)[0]*180/Math.PI)]++; 
		}
		
		int max = histogram[0];
		int angle = 0;
		for(int i = 0; i < histogram.length; i++){
			if(histogram[i] > max){
				max = histogram[i];
				angle = i;
			}
		}
		
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
		
				
		/*
		//Horizontal Edge detector
		double[] horSums = new double[horiz.width()];
		
		for(int i = 0; i < horiz.width(); i++){
			for(int j = 0; j < horiz.height(); j++){
				horSums[i] += horiz.get(j, i)[0]/255.0;
			}
		}
		
		//Rotate image 90 degrees
		Point src_center = new Point(vertical.cols()/2.0F, vertical.rows()/2.0F);
		Mat rot_mat = Imgproc.getRotationMatrix2D(src_center, 90, 1.0);
		Imgproc.warpAffine(output, vertical, rot_mat, vertical.size());
		
		//Find vertical edges
		double[] vertSums = new double[vertical.width()];
		
		for(int i = 0; i < vertical.width(); i++){
			for(int j = 0; j < vertical.height(); j++){
				vertSums[i] += vertical.get(j, i)[0]/255.0;
			}
		}
//		System.out.println(Arrays.toString(horSums));
//		System.out.println(Arrays.toString(vertSums));
		
		
		//Calculate gradient field
		int[] histogram = new int[360];
		for(int i = 0; i < horSums.length; i++){
			for(int j = 0; j < vertSums.length; j++){
				//Make sure value isn't zero
				if(Math.sqrt(Math.pow(horSums[i], 2) + Math.pow(vertSums[i], 2)) > MAGNITUDE_THRESH)
					histogram[(int)Math.toDegrees(Math.atan2(vertSums[i], horSums[j]))]++;
			}
		}
		
		int max = histogram[0];
		int angle = 0;
		for(int i = 0; i < histogram.length; i++){
			if(histogram[i] > max){
				max = histogram[i];
				angle = i;
			}
		}
//		System.out.println(Arrays.toString(histogram));
		
		System.out.println("Angle: " + angle);
		
//		double[] mainPoints = new double[4];
//		int index = 0;
//		boolean droppedBelowThresh = true;
//		//Find left most number
//		for(int i = 0; i < sums.length; i++){
//			if(sums[i] > SUM_THRESHOLD){
//				if(droppedBelowThresh){
//					if(index > 3){
//						System.out.println("More than 4 important points found!");
//						continue;
//					}
//					
//					mainPoints[index] = i;
//					index++;
//					
//					droppedBelowThresh = false;
//				}
//			}else{
//				droppedBelowThresh = true;
//			}
//		}
//		
//		System.out.println(Arrays.toString(mainPoints));
//		for(double x : mainPoints){
//			Core.line(output, new Point(x, 0), new Point(x, output.height()), new Scalar(255, 255, 255));
//		}
		
		*/
		return display;
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
