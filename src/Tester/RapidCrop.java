package Tester;

import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.highgui.Highgui;

public class RapidCrop implements Runnable{
	
	public static void main(String[] args){
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		
		new Thread(new RapidCrop()).start();
	}
	
	private JLabel lblimage;
	private Mat img = new Mat();
	private boolean saveImage;
	private boolean nextImage = false;
	private int picCount;
	
	private int x = 0,
				y = 0;
	public void run(){
		img = Highgui.imread("C://Users/Student/ImagePics/2016/RealField/RealFullField/0.jpg");
		
		// Display Setup. For Testing
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		lblimage = new JLabel(new ImageIcon(toBufferedImage(img)));
		lblimage.setIcon(new ImageIcon(toBufferedImage(img)));
		
		JPanel mainPanel = new JPanel(new GridLayout());
		mainPanel.add(lblimage);
		
		saveImage = false;
		mainPanel.addMouseListener(new MouseListener(){
			public void mouseClicked(MouseEvent e) {}
			public void mousePressed(MouseEvent e) {
				x = e.getX();
				y= e.getY();
				saveImage = true;
			}
			public void mouseReleased(MouseEvent e) {
				if(saveImage){
					if(e.getX() - x < 1 || e.getY() - y < 1)
						return;
					
					savePics(new Rect(x, y, e.getX() - x, e.getY() - y));
					saveImage = false;
					nextImage = true;
				}
			}
			public void mouseEntered(MouseEvent e) {}
			public void mouseExited(MouseEvent e) {}
		});
		
		frame.setSize(640, 480);
		frame.add(mainPanel);
		frame.setVisible(true);
		
		for(int i = 0; i < 543; i++){
			picCount = i;
			
			if(!new File("C://Users/Student/ImagePics/2016/RealField/RealFullField/" + i + ".jpg").exists()){
				continue;
			}
			img = Highgui.imread("C://Users/Student/ImagePics/2016/RealField/RealFullField/" + i + ".jpg", 1);
			lblimage.setIcon(new ImageIcon(toBufferedImage(img)));
			
			//Force them to pic a spot
			while(!nextImage){
				try {
					Thread.sleep(100);
				} catch (InterruptedException e1) {}
			}
			nextImage = false;
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
	
	public void savePics(Rect roi) {
		Highgui.imwrite("C://Users/Student/ImagePics/2016/bg/" + picCount + ".jpeg", img.clone().submat(roi));
		
		System.out.println("Saving: " + picCount);
	}
	
}
