package Tester;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.highgui.Highgui;

public class InfoCreater implements Runnable{
	
	public static void main(String[] args) {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		
		new Thread(new InfoCreater()).start();
    }
	
	private BufferedWriter writer;
	private Mat img = new Mat();
	public void run(){
		try{
			writer = new BufferedWriter(new FileWriter(new File("C://Users/Student/ImagePics/2016/positive/info.dat")));
			
			for(int i = 0; i < 543; i++){
				if(!new File("C://Users/Student/ImagePics/2016/positive/" + i + ".jpeg").exists()){
					continue;
				}
				img = Highgui.imread("C://Users/Student/ImagePics/2016/positive/" + i + ".jpeg", 1);	
	            writer.write(i + ".jpeg 1 0 0 " + img.width() + " " + img.height() + "\n");
			}
			writer.close();
		}catch(Exception e){
			System.out.println("Failed to write");
		}
	}
}
