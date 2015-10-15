package OpenCVBGSub;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;

public class OpenCVMonitor implements ChangeListener {
	
	private static int WINDOW_WIDTH = 680;
	private static int WINDOW_HEIGHT = 540;
	private static Mat INITIAL_FRAME;
	private static Mat NORMAL_FRAME;
	private static Mat FILTERED_FRAME;
	private static Mat DRAWN_ON_FRAME;
	
	private static int MOG_THRESHOLD_MIN = 0;
	private static int MOG_THRESHOLD_MAX = 32;
	private static int MOG_THRESHOLD_INIT = 16;
	
	ImageProcessor p;
	
	public OpenCVMonitor(){
		//load OpenCV lib
		System.loadLibrary( Core.NATIVE_LIBRARY_NAME );
		
		INITIAL_FRAME = new Mat();
		NORMAL_FRAME = new Mat();
		FILTERED_FRAME = new Mat();
		DRAWN_ON_FRAME = new Mat();
		p = new ImageProcessor();
	}
	
	public void Start() throws InterruptedException{
		
								
		//initialise windows
		//1. main window
		JFrame mainWindow = new JFrame("Original Image Window");
		mainWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		mainWindow.setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
		mainWindow.setLocation(0, 0);
		mainWindow.setVisible(true);
		
		//2. filtered frame image
		JFrame filteredWindow = new JFrame("Filtered Image Window");
		filteredWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		filteredWindow.setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
		filteredWindow.setLocation(WINDOW_WIDTH + 10, 0);
		filteredWindow.setVisible(true);
		
		//3. sliders
		JFrame sliderWindow = new JFrame("Threshold Sliders");
		sliderWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		sliderWindow.setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
		sliderWindow.setLocation(0, WINDOW_HEIGHT + 10);
		sliderWindow.setVisible(true);
		JSlider mogThresholdSlider = new JSlider(JSlider.HORIZONTAL,MOG_THRESHOLD_MIN, MOG_THRESHOLD_MAX, MOG_THRESHOLD_INIT);
		mogThresholdSlider.addChangeListener(this);

		//Turn on labels at major tick marks.
		mogThresholdSlider.setMajorTickSpacing(8);
		mogThresholdSlider.setMinorTickSpacing(1);
		mogThresholdSlider.setPaintTicks(true);
		//framesPerSecond.setPaintLabels(true);
		
		JPanel sliderPanel = new JPanel();
		sliderPanel.add(mogThresholdSlider);
		
		sliderWindow.setContentPane(sliderPanel);
		
		//initialise panel for each window
		CustomPanel mainPanel = new CustomPanel();
		mainWindow.setContentPane(mainPanel);
		CustomPanel filteredPanel = new CustomPanel();
		filteredWindow.setContentPane(filteredPanel);
		
		
		
		//initialise camera
		VideoCapture camera = new VideoCapture(0);
		
		//used to allow time to move out of camera field of view
		Thread.sleep(1000);
		
		//begin read and processing
		if(camera.isOpened()){
			
			//set initial frame
			camera.read(INITIAL_FRAME);
			
			if (!INITIAL_FRAME.empty()){
				
				//OLD
				//load image processor
				//ImageProcessor p = new ImageProcessor(INITIAL_FRAME);
				p.setInitialFrame(INITIAL_FRAME);
				
				//infinite loop for camera feed
				while(true){
					camera.read(NORMAL_FRAME);
					DRAWN_ON_FRAME = p.detectFullBody(NORMAL_FRAME);
					
					//bg subtraction
					FILTERED_FRAME = p.processFrameNegative(NORMAL_FRAME);
					//difference
					//FILTERED_FRAME = p.processFrameDifference(NORMAL_FRAME);
					
					//mog - FAILS AFTER 35 FRAMES
					//FILTERED_FRAME = p.processFrameMOG(NORMAL_FRAME);
					
					//buffer images
					mainPanel.convertToBufferedImage(DRAWN_ON_FRAME);
					filteredPanel.convertToBufferedImage(FILTERED_FRAME);
					
					//refresh panels
					mainPanel.repaint();
					filteredPanel.repaint();
					
					//Thread.sleep(10);
				}

			}
			else{
				System.out.println("Error capturing initial frame");
				return;
			}
		}
	}
	
	public void stateChanged(ChangeEvent e) {
	    JSlider source = (JSlider)e.getSource();
	    if (!source.getValueIsAdjusting()) {
	        int mogThreshold = (int)source.getValue();
	        p.setMogThreshold(mogThreshold);
	    }
	}
}
