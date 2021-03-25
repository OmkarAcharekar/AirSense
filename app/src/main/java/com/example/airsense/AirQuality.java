package com.example.airsense;


import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.Interpreter;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.provider.MediaStore;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.airsense.ml.AirSenseModel;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import static org.opencv.imgproc.Imgproc.INTER_AREA;

public class AirQuality extends AppCompatActivity implements LocationListener{

    private TextView textView,textContrst;
    protected Interpreter tflite;
    public static final int REQUEST_IMAGE_CAPTURE = 100;
    private ImageView imageView;
    private Mat img,dest;
    private Button buttonTakePicture,features;
    private Uri file;
    int capacity = 10;
    Bitmap photo;
    double ent =0;
    LocationManager locationManager;

    public static final String TAG = "MainActivity";
    public static final int REQUEST_TAKE_PHOTO = 100;
    Uri photoURI;


    // Define the pic id
    private static final int pic_id = 123;

    // Define the button and imageview type variable
    Button camera_open_id;
    ImageView click_image_id;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_air_quality);
        if (ContextCompat.checkSelfPermission(AirQuality.this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(AirQuality.this,new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION
            },100);
        }
        try {
            tflite = new Interpreter(loadModelFile());
        }catch (Exception ex){
            ex.printStackTrace();
        }

        getLocation();

        camera_open_id = (Button)findViewById(R.id.camera_button);
        click_image_id = (ImageView)findViewById(R.id.click_image);
        features = (Button)findViewById(R.id.buttonfeatures);
        Window window = AirQuality.this.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.setStatusBarColor(ContextCompat.getColor(AirQuality.this, R.color.black));

        camera_open_id.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v)
            {


                Intent camera_intent
                        = new Intent(MediaStore
                        .ACTION_IMAGE_CAPTURE);

                startActivityForResult(camera_intent, pic_id);
            }
        });

        features.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(photo != null){
                    float pred1 = opener();
                    String pred = String.format("%.2f",pred1);
                Intent intent = new Intent(AirQuality.this, AQI.class);
                intent.putExtra("prediction",pred);
                startActivity(intent);
                readImageFromResources();

            }}
        });

        OpenCVLoader.initDebug();


    }

    @SuppressLint("MissingPermission")
    private void getLocation() {

        try {
            locationManager = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,5000,500, AirQuality.this);

        }catch (Exception e){
            e.printStackTrace();
        }

    }


    public float opener() {

        readImageFromResources();
        transmission();
        float contr=  contrast();
        float contrcal = contr/75.f;
        float entro = entropy();
        float entrocal = -(entro);
        float entrocalf = entrocal/68.f;



        float[] intArray = new float[]{0.6666667f , contrcal, entrocalf, 0.5638298f , 0.30468348f,
                0.80529412f, 0.04074074f, 0.98727275f, 2.f        , 0.90486034f};

        Toast.makeText(getApplicationContext(),"contr"+ contr +"Entro" + entro,Toast.LENGTH_SHORT).show();

        float prediction=doInference(intArray);
        return  prediction;

    }

    private float doInference(float[] inputString) {
        float[] inputVal=new float[10];
        inputVal[0]=inputString[0];
        inputVal[1]=inputString[1];
        inputVal[2]=inputString[2];
        inputVal[3]=inputString[3];
        inputVal[4]=inputString[4];
        inputVal[5]=inputString[5];
        inputVal[6]=inputString[6];
        inputVal[7]=inputString[7];
        inputVal[8]=inputString[8];
        inputVal[9]=inputString[9];
        float[][] output=new float[1][1];
        tflite.run(inputVal,output);
        float inferredValue=output[0][0];
        return  inferredValue;
    }

    private MappedByteBuffer loadModelFile() throws IOException{
        AssetFileDescriptor fileDescriptor=this.getAssets().openFd("AirSenseModel1.tflite");
        FileInputStream inputStream=new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel=inputStream.getChannel();
        long startOffset=fileDescriptor.getStartOffset();
        long declareLength=fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY,startOffset,declareLength);
    }



    // This method will help to retrieve the image
    protected void onActivityResult(int requestCode,
                                    int resultCode,
                                    Intent data) {


        // Match the request 'pic id with requestCode
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == pic_id) {

            // BitMap is data structure of image file
            // which stor the image in memory
            photo = (Bitmap) data.getExtras()
                    .get("data");

            // Set the image in imageview for display
            click_image_id.setImageBitmap(photo);
        }
    }


    private Mat readImageFromResources() {
        img = new Mat();

        Utils.bitmapToMat(photo,img);
        //Toast.makeText(getApplicationContext(),"Image is loaded",Toast.LENGTH_SHORT).show();

        return img;


    }
    private void showImage(Mat img){
        Bitmap bm = Bitmap.createBitmap(img.cols(), img.rows(),Bitmap.Config.RGB_565);
        Utils.matToBitmap(img, bm);

        //click_image_id.setImageBitmap(bm);
    }
    private void transmission(){

        //to resize image
        dest = new Mat();
        Size scaleSize = new Size(256,256);
        Imgproc.resize(img,dest, scaleSize , 0, 0, INTER_AREA);
        //Toast.makeText(getApplicationContext(),"get size"+dest.size(),Toast.LENGTH_LONG).show();


        //convert bgr to rgb
        Imgproc.cvtColor(dest, dest, Imgproc.COLOR_BGR2RGB);
       // Toast.makeText(getApplicationContext(),"get size"+img.size(),Toast.LENGTH_LONG).show();


        //store values of r,g,b
        List<Mat> lRgb = new ArrayList<Mat>(3);
        Core.split(img, lRgb);
        Mat mR = lRgb.get(0);
        Mat mG = lRgb.get(1);
        Mat mB = lRgb.get(2);
        //Toast.makeText(getApplicationContext(),"RED:"+mR+"Green:"+mG+"Blue:"+mB,Toast.LENGTH_LONG).show();
//        textView.setText("Red"+mR.dump());
//        Log.i(TAG,"Red"+mR.dump());

        //convert rgb to hsv
        Mat hsvImage = new Mat();
        Imgproc.cvtColor(dest,hsvImage,Imgproc.COLOR_RGB2HSV);
        //Toast.makeText(getApplicationContext(),"Image is in HSV",Toast.LENGTH_SHORT).show();

        //get hsv values
        List<Mat> Rgb = new ArrayList<Mat>(3);
        Core.split(hsvImage, Rgb);
        Mat mH = Rgb.get(0);
        Mat mS = Rgb.get(1);
        Mat mV = Rgb.get(2);
        //Toast.makeText(getApplicationContext(),"H: "+mH+"S:"+mS+"V:"+mV,Toast.LENGTH_LONG).show();
        // Log.i(TAG,"H: "+mH+"S:"+mS+"V:"+mV);
//        textView.setText("Hsv image"+hsvImage.dump());


        //to make image smooth
        Mat v_mask = new Mat();
        int kernel_size=3;
        Mat ker = new Mat();
        Mat kernel = Mat.ones(kernel_size,kernel_size, CvType.CV_32F);
        Core.multiply(kernel,new Scalar(1/(double)(kernel_size*kernel_size)),ker);

        Imgproc.filter2D(mV, v_mask, -1, ker);


        //threshold
        Mat build = new Mat();
        Imgproc.threshold(v_mask, build, 230, 255, Imgproc.THRESH_BINARY);


        //sky
        Mat sky = new Mat();
        Core.bitwise_and(dest,dest,sky,build);


        //skyMask
        Mat skyMask = new Mat();
        Core.bitwise_not(build,skyMask);

        //building
        Mat building = new Mat();
        Core.bitwise_and(dest,dest,building,skyMask);


        //to find air light
        Core.MinMaxLocResult minMaxLocResult = Core.minMaxLoc(v_mask);
        float al = (float) minMaxLocResult.maxVal;



        //to find transition map
        Mat C = dest.clone();
        C.convertTo(C, CvType.CV_64FC3);
        double[] a = new double[(int) (dest.total()*dest.channels())];
        C.get(0,0,a);
        for(int i = 0; i < a.length; i ++){
            a[i] = a[i]/al;
        }
        C.put(0,0,a);

//
//
        //to find r,g,b values
        List<Mat> Rgb3 = new ArrayList<Mat>(3);

        Core.split(C, Rgb3);
        Mat iR = Rgb3.get(0);
        Mat iG = Rgb3.get(1);
        Mat iB = Rgb3.get(2);
        Mat dc = iR.clone();
        int rows = iR.rows();
        int cols = iR.cols();

        for (int i = 0; i < dest.rows(); i++) {
            for (int j = 0; j < dest.cols(); j++) {
                double min = Math.min(iR.get(i, j)[0], Math.min(iG.get(i, j)[0], iB.get(i, j)[0]));
                dc.put(i, j, min);
            }
        }

        Mat b1 = new Mat();

        double krnlRatio = 1;
        int krnlSz = Double.valueOf(Math.max(Math.max(rows * krnlRatio, cols * krnlRatio), 3.0)).intValue();
        Mat kere = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(krnlSz, krnlSz), new Point(-1, -1));
        Log.i(TAG,"susi"+kere.size()+" "+kere.channels()+" "+kere.type());
        Log.i(TAG,"dc"+dc.size()+" "+dc.channels());
        Imgproc.erode(dc, b1, kere);

//
//
//
//
        Mat tr = dc.clone();
        Mat dr = new Mat();
        dr = Mat.ones(dc.rows(),dc.cols(),CvType.CV_8UC1);
        Mat trList = dr.clone();
        tr.convertTo(tr, CvType.CV_64FC3);
        trList.convertTo(trList,CvType.CV_64FC3);
        double[] b = new double[(int) (tr.total()*tr.channels())];
        double [] l = new double[(int)(trList.total()*trList.channels())];
        tr.get(0,0,b);
        trList.get(0,0,l);
        for(int i = 0; i < b.length; i++){
            l[i] = 1-b[i];
        }
        trList.put(0,0,l);


        Mat img_norm = new Mat();
        Core.normalize(trList,img_norm,0,255,Core.NORM_MINMAX,CvType.CV_8UC1);

        showImage(img_norm);


    }

    public float contrast(){


        dest = new Mat();
        Size scaleSize = new Size(256,256);
        Imgproc.resize(img,dest, scaleSize , 0, 0, INTER_AREA);
        //Toast.makeText(getApplicationContext(),"get size"+dest.size(),Toast.LENGTH_LONG).show();


        //convert bgr to gray
        Imgproc.cvtColor(dest, dest, Imgproc.COLOR_BGR2GRAY);
        //Toast.makeText(getApplicationContext(),"get size"+img.size(),Toast.LENGTH_LONG).show();

        float s =0;
        float ss =0;

        Mat con = dest.clone();
        con.convertTo(con, CvType.CV_64FC3);
        double[] a = new double[(int) (dest.total()*dest.channels())];
        con.get(0,0,a);
        for(int i = 0; i < a.length; i ++){
            s = (int) (s+a[i]);
        }
        con.put(0,0,s);

        float avg ;
        avg = (s/(256*256));

        Mat co = dest.clone();
        co.convertTo(co, CvType.CV_64FC3);
        double[] b = new double[(int) (dest.total()*dest.channels())];
        co.get(0,0,b);
        for(int i = 0; i < b.length; i++){
            ss = (float) (ss + (float)((b[i]-avg)*(b[i]-avg)));
        }
        co.put(0,0,ss);

        float contra;
        contra = (float) Math.sqrt(ss/(256*256));
        //Toast.makeText(getApplicationContext(),"Constrast: "+contra,Toast.LENGTH_LONG).show();
        return contra;
    }

    public float entropy(){
        //to resize image
        dest = new Mat();
        Size scaleSize = new Size(256,256);
        Imgproc.resize(img,dest, scaleSize , 0, 0, INTER_AREA);
        //Toast.makeText(getApplicationContext(),"get size"+dest.size(),Toast.LENGTH_LONG).show();


        //convert bgr to gray
        Imgproc.cvtColor(dest, dest, Imgproc.COLOR_BGR2GRAY);
        //Toast.makeText(getApplicationContext(),"get size"+img.size(),Toast.LENGTH_LONG).show();

        //to calculate histogram
        MatOfInt histSize = new MatOfInt(256);

        Mat hist = new Mat(dest.size(),dest.type());



        ArrayList<Mat> list = new ArrayList<Mat>();
        list.add(dest);


        Imgproc.calcHist(list, new MatOfInt(0), new Mat(), hist, histSize, new MatOfFloat(0, 256));
        Core.normalize(hist,hist);

        //to find entropy
//        double ent = 0;
//        Mat h = hist.clone();
//        double[] b = new double[(int)(hist.channels()*hist.total())];
//        for(int i=5;i<256;i++) {
//            try {
//                ent += b[i] * Math.log(b[i]);
//            } catch (Exception e) {
//                Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_LONG).show();
//            }
//            textView.setText("degfu= "+ent);
//        }

        double total = 0;

        for (int row = 0; row < hist.rows(); row++) {
            double[] val = hist.get(row, 0);
            for(int p=0;p<val.length;p++) {
                try {
                    if(val[p]!= 0.0){
                        ent += val[p]*(Math.log(val[p])/Math.log(2));}
                }catch (Exception e){
                    //Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_LONG).show();
                }
            }

        }
        return (float) ent;

//        values(hist);
    }




    public void values(Mat img){
        List<Float> value = new ArrayList<>();

        for(int i=0;i<img.rows();i++){
            for(int j=0;j<img.cols();j++){
                value.add((float)img.get(i,j)[0]);
            }
        }

    }

    @Override
    public void onLocationChanged(Location location) {
        Toast.makeText(AirQuality.this, ""+location.getLatitude()+","+location.getLongitude(), Toast.LENGTH_SHORT).show();
        try {
            Geocoder geocoder = new Geocoder(AirQuality.this, Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(),location.getLongitude(),1);
            String address = addresses.get(0).getAddressLine(0);
            Toast.makeText(this, ""+ address, Toast.LENGTH_SHORT).show();



        }catch (Exception e){
            e.printStackTrace();
        }

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }






}