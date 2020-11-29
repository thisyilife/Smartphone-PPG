package calculHistogram;
import java.util.ArrayList;
import java.util.List;
import java.lang.Thread;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.highgui.HighGui;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.video.Video;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;



class CalcHist {
    public void run(String[] args) {
        System.loadLibrary("opencv_ffmpeg300_64");

        Mat videoMat = new Mat();
        boolean readFrame= true;
        VideoCapture capture = new VideoCapture("PATH OF THE VIDEO \\EXAMPLE.mp4");
        if( capture.isOpened()){
            // get some meta data about frame
            double fps = capture.get(Videoio.CAP_PROP_FPS);
            double frameCount = capture.get(Videoio.CAP_PROP_FRAME_COUNT);
            double h = capture.get(Videoio.CAP_PROP_FRAME_HEIGHT);
            double w = capture.get(Videoio.CAP_PROP_FRAME_WIDTH);
            double posFrames = capture.get(Videoio.CAP_PROP_POS_FRAMES);
            double posMsec = capture.get(Videoio.CAP_PROP_POS_MSEC);
            double speed = capture.get(Videoio.CAP_PROP_SPEED);
            System.out.println(fps);
            System.out.println(frameCount);
            int refreshHist = (int)((1/fps)*1000);
            while (readFrame){
                capture.read(videoMat);
                if( !videoMat.empty() ) {
                    // the calculation and the histogram of the blue and green components are commented on
                    System.out.println("Capture a frame");
                    List<Mat> bgrPlanes = new ArrayList<>();
                    Core.split(videoMat, bgrPlanes);
                    int histSize = 256;
                    float[] range = {0, 256}; //the upper boundary is exclusive
                    MatOfFloat histRange = new MatOfFloat(range);
                    boolean accumulate = false;
                    Mat bHist = new Mat(), gHist = new Mat(), rHist = new Mat();
                    //Imgproc.calcHist(bgrPlanes, new MatOfInt(0), new Mat(), bHist, new MatOfInt(histSize), histRange, accumulate);
                    //Imgproc.calcHist(bgrPlanes, new MatOfInt(1), new Mat(), gHist, new MatOfInt(histSize), histRange, accumulate);
                    Imgproc.calcHist(bgrPlanes, new MatOfInt(2), new Mat(), rHist, new MatOfInt(histSize), histRange, accumulate);
                    int histW = 512, histH = 400;
                    int binW = (int) Math.round((double) histW / histSize);
                    Mat histImage = new Mat( histH, histW, CvType.CV_8UC3, new Scalar( 0,0,0) );
                    //Core.normalize(bHist, bHist, 0, histImage.rows(), Core.NORM_MINMAX);
                    //Core.normalize(gHist, gHist, 0, histImage.rows(), Core.NORM_MINMAX);
                    Core.normalize(rHist, rHist, 0, histImage.rows(), Core.NORM_MINMAX);
                    //float[] bHistData = new float[(int) (bHist.total() * bHist.channels())];
                    //bHist.get(0, 0, bHistData);
                    // float[] gHistData = new float[(int) (gHist.total() * gHist.channels())];
                    // gHist.get(0, 0, gHistData);
                    float[] rHistData = new float[(int) (rHist.total() * rHist.channels())];
                    rHist.get(0, 0, rHistData);
                    for( int i = 1; i < histSize; i++ ) {
                       /* Imgproc.line(histImage, new Point(binW * (i - 1), histH - Math.round(bHistData[i - 1])),
                                new Point(binW * (i), histH - Math.round(bHistData[i])), new Scalar(255, 0, 0), 2);*/
                       /* Imgproc.line(histImage, new Point(binW * (i - 1), histH - Math.round(gHistData[i - 1])),
                                new Point(binW * (i), histH - Math.round(gHistData[i])), new Scalar(0, 255, 0), 2);*/
                        Imgproc.line(histImage, new Point(binW * (i - 1), histH - Math.round(rHistData[i - 1])),
                                new Point(binW * (i), histH - Math.round(rHistData[i])), new Scalar(0, 0, 255), 2);
                    }
                    HighGui.imshow( "Source image", videoMat );
                    HighGui.imshow( "calcHist Demo", histImage );
                    HighGui.waitKey(refreshHist);

                }
                else {
                    System.out.println("End capture frame");
                    readFrame = false;
                    HighGui.destroyAllWindows();
                }
            }
        }
        System.exit(0);

    }
}
public class CalcHistogram {
    public static void main(String[] args) {
        // Load the native OpenCV library
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        new CalcHist().run(args);
    }
}
