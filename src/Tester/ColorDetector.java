package Tester;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.highgui.VideoCapture;
import org.opencv.imgproc.Imgproc;

import Lib.AxisCamera;
 
/*
 * javaFX window that displays a camera view and has three sliders to adjust the view to find an object
 */
public class ColorDetector implements Runnable {
	
	private int hueMax = 0;
	private int satMax = 189;
	private int valueMax = 219;
	private int hueMin = 103;
	private int satMin = 255;
	private int valueMin = 255;
	
	private JLabel values;
	
	private AxisCamera cam;
	
	private static final boolean USING_AXIS_CAMERA = false;
	
	public void run(){
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        
        cam = new AxisCamera("10.7.66.11");
      
		VideoCapture vid = new VideoCapture(0);
		
		Mat img = new Mat();
		vid.read(img);
//		img = Highgui.imread("C:/Users/Student/Downloads/GearVisionTarget/untitled8.png",1);
		
		
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		JLabel lblimage = new JLabel(new ImageIcon(toBufferedImage(img)));
		lblimage.setIcon(new ImageIcon(toBufferedImage(img)));
		JPanel mainPanel = new JPanel(new BorderLayout());
		mainPanel.add(lblimage);
		frame.add(mainPanel);
		frame.setSize(500, 500);
		frame.setVisible(true);
		
		JSlider hMin = new JSlider(JSlider.VERTICAL,0,255,hueMin);
		JSlider sMin = new JSlider(JSlider.VERTICAL,0,255,satMin);
		JSlider vMin = new JSlider(JSlider.VERTICAL,0,255,valueMin);
		JSlider hMax = new JSlider(JSlider.VERTICAL,0,255,hueMax);
		JSlider sMax = new JSlider(JSlider.VERTICAL,0,255,satMax);
		JSlider vMax = new JSlider(JSlider.VERTICAL,0,255,valueMax);
		
		hMin.addChangeListener(new ChangeListener(){
			@Override
			public void stateChanged(ChangeEvent e) {
				JSlider source = (JSlider)e.getSource();
				hueMin = source.getValue();
				updateTextView();
			}
		});
		sMin.addChangeListener(new ChangeListener(){
			@Override
			public void stateChanged(ChangeEvent e) {
				JSlider source = (JSlider)e.getSource();
				satMin = source.getValue();
				updateTextView();
			}
		});
		vMin.addChangeListener(new ChangeListener(){
			@Override
			public void stateChanged(ChangeEvent e) {
				JSlider source = (JSlider)e.getSource();
				valueMin = source.getValue();
				updateTextView();
			}
		});
		hMax.addChangeListener(new ChangeListener(){
			@Override
			public void stateChanged(ChangeEvent e) {
				JSlider source = (JSlider)e.getSource();
				hueMax = source.getValue();
				updateTextView();
			}
		});
		sMax.addChangeListener(new ChangeListener(){
			@Override
			public void stateChanged(ChangeEvent e) {
				JSlider source = (JSlider)e.getSource();
				satMax = source.getValue();
				updateTextView();
			}
		});
		vMax.addChangeListener(new ChangeListener(){
			@Override
			public void stateChanged(ChangeEvent e) {
				JSlider source = (JSlider)e.getSource();
				valueMax = source.getValue();
				updateTextView();
			}
		});
		
		//Container to  hold sliders
		JPanel fp = new JPanel();
		fp.add(hMin);
		fp.add(sMin);
		fp.add(vMin);
		fp.add(hMax);
		fp.add(sMax);
		fp.add(vMax);
		
		
		JPanel disp = new JPanel();
		values = new JLabel();
		values.setPreferredSize(new Dimension(1000, 100));
		disp.add(values, BorderLayout.SOUTH);
		
		frame.add(disp, BorderLayout.SOUTH);
		frame.add(fp, BorderLayout.EAST);
		
		Mat hsv = new Mat();
		Mat yellowImg = new Mat();
        
//		Imgproc.cvtColor(img, hsv, Imgproc.COLOR_BGR2HSV);
		while(true){
			if(USING_AXIS_CAMERA)
				img = cam.getImage();
			else
				vid.read(img);
			if(img.empty())continue;
			
			System.out.println(img.type());
			
			Imgproc.cvtColor(img, hsv, Imgproc.COLOR_BGR2HSV);
				
			Core.inRange(hsv, new Scalar(hueMin, satMin, valueMin), new Scalar(hueMax, satMax, valueMax), yellowImg);
				
			lblimage.setIcon(new ImageIcon(toBufferedImage(yellowImg)));
	
			//vid.read(img);
		}
		
    }
 
    public static void main(String[] args) {
        new Thread(new ColorDetector()).start();
    }
    
    private void updateTextView(){
    	values.setText("Hue Min: " + hueMin + "     " +
    				   "Saturation Min: " + satMin + "     " +
    				   "Value Min: " + valueMin + "     " +
    				   "Hue Max: " + hueMax + "     " +
    				   "Saturation Max: " + satMax + "     " +
    				   "Value Max: " + valueMax);
    }
    
    public static Image toBufferedImage(Mat m){
        int type = BufferedImage.TYPE_BYTE_GRAY;
        if ( m.channels() > 1 ) {
            type = BufferedImage.TYPE_3BYTE_BGR;
        }
        int bufferSize = m.channels()*m.cols()*m.rows();
        byte [] b = new byte[bufferSize];
        m.get(0,0,b); // get all the pixels
        BufferedImage image = new BufferedImage(m.cols(),m.rows(), type);
        final byte[] targetPixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        System.arraycopy(b, 0, targetPixels, 0, b.length);  
        return image;
  	}
}