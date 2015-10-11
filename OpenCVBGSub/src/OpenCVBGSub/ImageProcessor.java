package OpenCVBGSub;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.opencv.video.BackgroundSubtractorMOG2;
import org.opencv.video.Video;

public class ImageProcessor {
	
	private Mat INITIAL_FRAME;
	private Mat ACC_BG_IMAGE;
	private double MOG_LEARN_RATE = 0.01;
	private int MOG_HISTORY = 500;
	private float MOG_THRESHOLD = 32;
	private boolean MOG_DETECT_SHADOWS = false;
	
	public ImageProcessor(Mat initialFrame){
		super();
		this.INITIAL_FRAME = initialFrame;
		this.ACC_BG_IMAGE = new Mat();
	}

	public Mat processFrame(Mat inputFrame){
		
		BackgroundSubtractorMOG2 mog = Video.createBackgroundSubtractorMOG2(MOG_HISTORY, MOG_THRESHOLD, MOG_DETECT_SHADOWS);
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
		
		//mixture of gaussians
		//FAILS
		//mog.apply(inputFrame, foregroundImage, MOG_LEARN_RATE);
		
		
		//basic difference subtraction
		//Core.absdiff(INITIAL_FRAME, inputFrame, foregroundImage);
		
		//return foregroundImage;
	}
	
	private Mat convertToNegative(Mat normalImage){
		Mat negative = new Mat();
		Mat white = normalImage.clone();
		white.setTo(new Scalar(255.0));
		Core.subtract(white, normalImage, negative);
		return negative;
	}
}
