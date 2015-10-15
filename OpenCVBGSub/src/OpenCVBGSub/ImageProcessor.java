package OpenCVBGSub;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.video.BackgroundSubtractorMOG2;
import org.opencv.video.Video;

public class ImageProcessor {
	
	private Mat INITIAL_FRAME;
	private Mat ACC_BG_IMAGE;
	private double MOG_LEARN_RATE = 0.01;
	private int MOG_HISTORY = 500;
	private float MOG_THRESHOLD = 16;
	private boolean MOG_DETECT_SHADOWS = false;
	private CascadeClassifier BODY_CASCADE;
	
	public ImageProcessor(){
		//this.INITIAL_FRAME = new Mat();
		this.ACC_BG_IMAGE = new Mat();
		this.BODY_CASCADE = new CascadeClassifier("C:/Users/Jamie/Downloads/opencv/sources/data/haarcascades/haarcascade_fullbody.xml");
		
		if(this.BODY_CASCADE.empty()){
			System.out.println("Failed to load Cascade Classifier (BODY)");
			return;
		}else{
			System.out.println("Cascade Classifier (BODY) loaded successfully.");
		}
	}
	
	public void setInitialFrame(Mat initialFrame){
		this.INITIAL_FRAME = initialFrame;
	}
	
	public void setMogThreshold(float value){
		this.MOG_THRESHOLD = value;
	}
	
	//old constructor
	public ImageProcessor(Mat initialFrame){
		this.INITIAL_FRAME = initialFrame;
		this.ACC_BG_IMAGE = new Mat();
	}

	public Mat processFrameNegative(Mat inputFrame){
			
		Mat foregroundImage = new Mat();
		Mat foregroundThreshold = new Mat();
		Mat grayScaleImage = new Mat();
		
		//average subtraction
		Imgproc.cvtColor(inputFrame, grayScaleImage, Imgproc.COLOR_BGR2GRAY);
		
		if(ACC_BG_IMAGE.empty()){
			grayScaleImage.convertTo(ACC_BG_IMAGE, CvType.CV_32F);
		}
				
		ACC_BG_IMAGE.convertTo(INITIAL_FRAME, CvType.CV_8U);
		
		Core.absdiff(INITIAL_FRAME, grayScaleImage, foregroundImage);
		Imgproc.threshold(foregroundImage, foregroundThreshold, MOG_THRESHOLD, 255, Imgproc.THRESH_BINARY_INV);
		Mat inputFloating = new Mat();
		grayScaleImage.convertTo(inputFloating, CvType.CV_32F);
		Imgproc.accumulateWeighted(inputFloating, ACC_BG_IMAGE, MOG_LEARN_RATE, foregroundThreshold);
		
		return convertToNegative(foregroundThreshold);
	}
	
	public Mat processFrameDifference(Mat inputFrame){
		Mat foregroundImage = new Mat();
		//basic difference subtraction
		Core.absdiff(INITIAL_FRAME, inputFrame, foregroundImage);
				
		return foregroundImage;
	}
	
	public Mat processFrameMOG(Mat inputFrame){
		BackgroundSubtractorMOG2 mog = Video.createBackgroundSubtractorMOG2();
		Mat foregroundImage = new Mat();
		
		//mixture of gaussians
		//FAILS
		//mog.apply(inputFrame, foregroundImage);
		mog.apply(inputFrame, foregroundImage, MOG_LEARN_RATE);
		
		Imgproc.erode(foregroundImage, foregroundImage, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(2,2)));
		Imgproc.dilate(foregroundImage, foregroundImage, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(2,2)));
				
		return foregroundImage;
	}
	
	public Mat detectFullBody(Mat inputFrame){
		Mat grayScaleImage = new Mat(inputFrame.height(), inputFrame.width(), CvType.CV_8UC1);
		MatOfRect bodies = new MatOfRect();
		
		Imgproc.cvtColor(inputFrame, grayScaleImage, Imgproc.COLOR_BGRA2GRAY);
		Imgproc.equalizeHist( grayScaleImage, grayScaleImage );
		
		BODY_CASCADE.detectMultiScale(grayScaleImage, bodies);
		
		//test
		if (!bodies.empty()){
			System.out.println("No of people in frame: " + bodies.toArray().length);
		}
		
		for(Rect rect:bodies.toArray())  
        {  
             //Point center= new Point(rect.x + rect.width*0.5, rect.y + rect.height*0.5 );  
             Imgproc.rectangle(inputFrame, rect.tl(), rect.br(), new Scalar( 255, 0, 255 ), 4, 8, 0);
             //Imgproc.ellipse( inputFrame, center, new Size( rect.width*0.5, rect.height*0.5), 0, 0, 360, new Scalar( 255, 0, 255 ), 4, 8, 0 );
        }
		return inputFrame;
	}
	
	private Mat convertToNegative(Mat normalImage){
		Mat negative = new Mat();
		Mat white = normalImage.clone();
		white.setTo(new Scalar(255.0));
		Core.subtract(white, normalImage, negative);		
		
		return negative;
	}
}
