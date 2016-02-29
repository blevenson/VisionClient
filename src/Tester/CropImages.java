package Tester;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

public class CropImages implements Runnable{
	
	//Box Tracer
			//min
			public int B_HMIN = 82;
			private int B_SMIN = 180;
			private int B_VMIN = 96;
			//Max
			private int B_HMAX = 95;
			private int B_SMAX = 255;
			private int B_VMAX = 255;
	
	public static void main(String[] args) {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		
		new Thread(new CropImages()).start();
    }
	
	private Mat binaryImage = new Mat();
	List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
	private ArrayList<Rect> boundingRects = new ArrayList<Rect>();
	Mat greenHierarchy = new Mat();
	Mat img = new Mat();
	
	int imageNumber = 0;
	
	public void run(){
		for(int i = 0; i < 543; i++){
			if(!new File("C://Users/Student/ImagePics/2016/RealField/RealFullField/" + i + ".jpg").exists()){
				System.out.println("No");
				continue;
			}
			img = Highgui.imread("C://Users/Student/ImagePics/2016/RealField/RealFullField/" + i + ".jpg", 1);
			
			Imgproc.cvtColor(img, binaryImage, Imgproc.COLOR_BGR2HSV);
			
			Core.inRange(binaryImage, new Scalar(B_HMIN, B_SMIN, B_VMIN), new Scalar(B_HMAX, B_SMAX, B_VMAX), binaryImage);
			
			Imgproc.erode(binaryImage, binaryImage, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(5,5)));
			Imgproc.dilate(binaryImage, binaryImage, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(5,5)));
					
			Imgproc.findContours(binaryImage, contours, greenHierarchy, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_TC89_KCOS);
			
			if(contours.size() == 0){
				System.out.println("No Conters");
				continue;
			}

			for(MatOfPoint point : contours)
				boundingRects.add(Imgproc.boundingRect(point));

			for(Rect r : boundingRects){
				Highgui.imwrite("C://Users/Student/ImagePics/2016/positive/" + imageNumber + ".jpeg", img.submat(r));
				imageNumber++;
			}
		}
	}
}
