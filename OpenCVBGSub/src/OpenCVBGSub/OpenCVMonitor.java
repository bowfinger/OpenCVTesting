package OpenCVBGSub;

import javax.swing.JFrame;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;

public class OpenCVMonitor {
	
	private static int WINDOW_WIDTH = 680;
	private static int WINDOW_HEIGHT = 540;
	private static Mat INITIAL_FRAME;
	private static Mat NORMAL_FRAME;
	private static Mat BGSUB_FRAME;
	
	public OpenCVMonitor(){
		//load OpenCV lib
		System.loadLibrary( Core.NATIVE_LIBRARY_NAME );
		
		INITIAL_FRAME = new Mat();
		NORMAL_FRAME = new Mat();
		BGSUB_FRAME = new Mat();
	}
	
	public void Start() throws InterruptedException{
		
								
		//initialise windows
		//1. main window
		JFrame mainWindow = new JFrame("Original Image");
		mainWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		mainWindow.setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
		mainWindow.setLocation(0, 0);
		mainWindow.setVisible(true);
		
		//2. bg subtract window
		JFrame bgSubWindow = new JFrame("BG Subtract Window");
		bgSubWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		bgSubWindow.setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
		bgSubWindow.setLocation(WINDOW_WIDTH + 10, 0);
		bgSubWindow.setVisible(true);
		
		//initialise panel for each window
		CustomPanel mainPanel = new CustomPanel();
		mainWindow.setContentPane(mainPanel);
		CustomPanel bgSubPanel = new CustomPanel();
		bgSubWindow.setContentPane(bgSubPanel);
		
		
		
		//initialise camera
		VideoCapture camera = new VideoCapture(0);
		
		//used to allow time to move out of camera field of view
		Thread.sleep(5000);
		
		//begin read and processing
		if(camera.isOpened()){
			
			//set initial frame
			camera.read(INITIAL_FRAME);
			
			if (!INITIAL_FRAME.empty()){
				
				//load image processor
				ImageProcessor p = new ImageProcessor(INITIAL_FRAME);
				
				//infinite loop for camera feed
				while(true){
					camera.read(NORMAL_FRAME);
					BGSUB_FRAME = p.processFrame(NORMAL_FRAME);
					
					//buffer images
					mainPanel.convertToBufferedImage(NORMAL_FRAME);
					bgSubPanel.convertToBufferedImage(BGSUB_FRAME);
					
					//refresh panels
					mainPanel.repaint();
					bgSubPanel.repaint();
				}

			}
			else{
				System.out.println("Error capturing initial frame");
				return;
			}
		}
		
	}
}
