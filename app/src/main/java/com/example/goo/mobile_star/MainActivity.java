package com.example.goo.mobile_star;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.graphics.Paint;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import static android.content.ContentValues.TAG;
import com.bumptech.glide.Glide;


public class MainActivity extends AppCompatActivity
        implements ActivityCompat.OnRequestPermissionsResultCallback,SensorEventListener {

    View orionView, libraView, pegasusView;
    ImageView lena;
    //TextView myCoor;
    int check = 1;//

    private static final String TAG = "android_camera_example";
    private static final int PERMISSIONS_REQUEST_CODE = 100;
    String[] REQUIRED_PERMISSIONS  = {Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private static final int CAMERA_FACING = Camera.CameraInfo.CAMERA_FACING_BACK; // Camera.CameraInfo.CAMERA_FACING_FRONT

    private SurfaceView surfaceView;
    private CameraPreview mCameraPreview;
    private View mLayout;  // Snackbar 사용하기 위해서는 View가 필요합니다.
    // (참고로 Toast에서는 Context가 필요했습니다.)

    private ImageView mDrawable;
    HorizontalScrollView h_sv;
    ScrollView v_sv;
    public static int x;
    public static int y;
    int acc_x = 0;
    int acc_y = 0;
    //same for every image
    private double MARGIN_RATIO = 0.03;

    // DIFFERENT in every image
    private int IMAGE_WIDTH ;
    private int IMAGE_HEIGHT ;
    private int SCROLL_START = 500;
    // user input (DIFFERENT scroll speed vary in every android device)
    //0.5 , 1.0 , 1.5 , 2.0 , 2.5 (VERY SLOW, SLOW, MODERATE, FAST, VERY FAST)
    private double TIMES_FASTER = 2.0;


    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private Sensor mMagneticField;
    private float mAzimut,mPitch,mRoll;
    private TextView mResultView;
    private float mAzimutArray[];
    private final static int COM_DATA_SIZE = 10;
    private int comindex = 0;
    private boolean full = false;

    private float mPitchArray[];
    private final static int COM_DATA_SIZE_P = 10;
    private int comindex_p = 0;

    float[] mGravity;
    float[] mGeomagnetic;
    private Paint paint;
    //AnimatedView animatedView = null;
    String result;

    CheckBox ver,hor;
    private static int LOAD_IMAGE_RESULTS = 1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ActionBar ab = getSupportActionBar() ;
        ab.hide();
        //이미지-터치 연결
        lena = (ImageView) findViewById(R.id.kkori);
        lena.setOnTouchListener(myTouch);
        //myCoor=(TextView) findViewById(R.id.myCoor);/
        // 상태바를 안보이도록 합니다.
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // 화면 켜진 상태를 유지합니다.
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mLayout = findViewById(R.id.layout_main);
        surfaceView = findViewById(R.id.camera_preview_main);


        // 런타임 퍼미션 완료될때 까지 화면에서 보이지 않게 해야합니다.
        surfaceView.setVisibility(View.GONE);

        Button button = findViewById(R.id.button_main_capture);
        button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mCameraPreview.takePicture();
            }
        });



        if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {

            int cameraPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
            int writeExternalStoragePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);


            if ( cameraPermission == PackageManager.PERMISSION_GRANTED
                    && writeExternalStoragePermission == PackageManager.PERMISSION_GRANTED) {
                startCamera();


            }else {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[0])
                        || ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[1])) {

                    Snackbar.make(mLayout, "이 앱을 실행하려면 카메라와 외부 저장소 접근 권한이 필요합니다.",
                            Snackbar.LENGTH_INDEFINITE).setAction("확인", new View.OnClickListener() {

                        @Override
                        public void onClick(View view) {

                            ActivityCompat.requestPermissions( MainActivity.this, REQUIRED_PERMISSIONS,
                                    PERMISSIONS_REQUEST_CODE);
                        }
                    }).show();


                } else {
                    // 2. 사용자가 퍼미션 거부를 한 적이 없는 경우에는 퍼미션 요청을 바로 합니다.
                    // 요청 결과는 onRequestPermissionResult에서 수신됩니다.
                    ActivityCompat.requestPermissions( this, REQUIRED_PERMISSIONS,
                            PERMISSIONS_REQUEST_CODE);
                }

            }

        } else {

            final Snackbar snackbar = Snackbar.make(mLayout, "디바이스가 카메라를 지원하지 않습니다.",
                    Snackbar.LENGTH_INDEFINITE);
            snackbar.setAction("확인", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    snackbar.dismiss();
                }
            });
            snackbar.show();
        }

        h_sv = (HorizontalScrollView) findViewById(R.id.horizontal_scrollview);
        h_sv.setScrollX(SCROLL_START);
        mDrawable = (ImageView) findViewById(R.id.kkori);

        v_sv = (ScrollView) findViewById(R.id.vertical_scrollview);
        v_sv.setScrollY(SCROLL_START);

        ver=(CheckBox) findViewById(R.id.cb_ver);
        hor=(CheckBox) findViewById(R.id.cb_hori);

        ver.setChecked(false);
        hor.setChecked(false);

        //get display width, height
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int pxWidth  = displayMetrics.widthPixels;
        int pxHeight = displayMetrics.heightPixels;
        Log.d(TAG, "onCreate: displayMetrics.widthPixels  = "+displayMetrics.widthPixels);
        Log.d(TAG, "onCreate: displayMetrics.heightPixels = "+displayMetrics.heightPixels);

        //get image size
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;


        ////////////////////   put image name  ////////////////////
        BitmapFactory.decodeResource(getResources(), R.drawable.starlong, options);
        ///////////////////////////////////////////////////////////
        int img_width = options.outWidth;
        int img_height = options.outHeight;
        Log.d(TAG, "onCreate: BitmapDrawable img_height = "+img_height);
        Log.d(TAG, "onCreate: BitmapDrawable img_width  = "+img_width);
        IMAGE_WIDTH = img_width;
        IMAGE_HEIGHT = img_height;
        //resize to fit display
        float resizeW = (pxHeight*IMAGE_WIDTH)/IMAGE_HEIGHT;
        Log.d(TAG, "override: resizeW = "+resizeW);
        int resizeW_int = (int) resizeW;
        Log.d(TAG, "override: resizeW_int = "+resizeW_int);
        Log.d(TAG, "override: pxHeight= "+pxHeight);


        ////////////////////   put image name  ////////////////////
        //Glide.with(this).load(R.drawable.sunglasses_emoji).override(resizeW_int, pxHeight).into(mDrawable);
        Glide.with(this).load(R.drawable.starlong).override(6200, 3500).into(mDrawable);
        new Handler().postDelayed(new Runnable()
        {
            @Override
            public void run()
            {
                Log.d(TAG, "override===============================================");
                Log.d(TAG, "override:getWidth = "+mDrawable.getWidth());
                Log.d(TAG, "override:getHeight = "+mDrawable.getHeight());

            }
        }, 3000);

        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mMagneticField = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        mAzimutArray = new float[COM_DATA_SIZE];
        mPitchArray = new float[COM_DATA_SIZE];

        Button bt_gal = (Button)findViewById(R.id.bt_gal);
        bt_gal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Create the Intent for Image Gallery.
                Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

                // Start new activity with the LOAD_IMAGE_RESULTS to handle back the results when image is picked from the Image Gallery.
                startActivityForResult(intent, LOAD_IMAGE_RESULTS);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Here we need to check if the activity that was triggers was the Image Gallery.
        // If it is the requestCode will match the LOAD_IMAGE_RESULTS value.
        // If the resultCode is RESULT_OK and there is some data we know that an image was picked.
        if (requestCode == LOAD_IMAGE_RESULTS && resultCode == RESULT_OK && data != null) {
            // Let's read picked image data - its URI
            Uri pickedImage = data.getData();
            // Let's read picked image path using content resolver
            String[] filePath = { MediaStore.Images.Media.DATA };
            Cursor cursor = getContentResolver().query(pickedImage, filePath, null, null, null);
            cursor.moveToFirst();
            String imagePath = cursor.getString(cursor.getColumnIndex(filePath[0]));

            // Now we need to set the GUI ImageView data with data read from the picked file.

            Intent intent = new Intent(MainActivity.this,GallaryActivity.class);
            intent.putExtra("IMGpath", imagePath);
            MainActivity.this.startActivity(intent);

            // At the end remember to close the cursor or you will end with the RuntimeException!
            cursor.close();
        }
    }



    void startCamera(){

        // Create the Preview view and set it as the content of this Activity.
        mCameraPreview = new CameraPreview(this, this, CAMERA_FACING, surfaceView);

    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grandResults) {

        if ( requestCode == PERMISSIONS_REQUEST_CODE && grandResults.length == REQUIRED_PERMISSIONS.length) {

            boolean check_result = true;

            for (int result : grandResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    check_result = false;
                    break;
                }
            }

            if ( check_result ) {

                startCamera();
            }
            else {

                if (ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[0])
                        || ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[1])) {

                    Snackbar.make(mLayout, "퍼미션이 거부되었습니다. 앱을 다시 실행하여 퍼미션을 허용해주세요. ",
                            Snackbar.LENGTH_INDEFINITE).setAction("확인", new View.OnClickListener() {

                        @Override
                        public void onClick(View view) {

                            finish();
                        }
                    }).show();

                }else {


                            startCamera();

                    }
                }
            }

        }

    @Override
    protected void onResume() {
        super.onResume();
        //sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mMagneticField, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        //sensorManager.unregisterListener(this);
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onAccuracyChanged(Sensor arg0, int arg1) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        // TODO Auto-generated method stub
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
            mGravity = event.values;
        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
            mGeomagnetic = event.values;
        if (mGravity != null && mGeomagnetic != null) {
            float R[] = new float[9];
            float I[] = new float[9];
            boolean success = SensorManager.getRotationMatrix(R, I, mGravity, mGeomagnetic);
            if (success) {
                float orientation[] = new float[3];
                SensorManager.getOrientation(R, orientation);
                mAzimut = (float)Math.toDegrees(orientation[0]);
                mPitch = (float)Math.toDegrees(orientation[1]);
                mRoll = (float)Math.toDegrees(orientation[2]);


                result = "Azimut:"+mAzimut+"\n"+"Pitch:"+mPitch+"\n"+"Roll:"+mRoll;
                Log.e("ssibal",result);
                Log.e("Acc","z:"+mGravity[2]);

                // animatedView.invalidate();
                changeAzimut(mAzimut);
                changePitch(mPitch);
                if(hor.isChecked())
                    scrollviewX();
                if(ver.isChecked())
                    scrollviewY();

            }
        }
    }
    //180사이인 방위값을 0~360도로 저장
    private void changeAzimut(float mAzimut) {
        // mAzimut -180 ~ +180
        if( mAzimut < 0 ) mAzimut=mAzimut+360.0f;
        mAzimutArray[comindex++]=mAzimut;
        if( COM_DATA_SIZE <= comindex ){
            comindex = 0;
            full = true;
        }
    }

    private void changePitch(float mPitch) {
        // mAzimut -180 ~ +180
        if(mGravity[2]>=0) mPitch=mPitch+90.0f;
        if( mGravity[2]<0 ) mPitch = 180.0f+(90-mPitch);
        mPitchArray[comindex_p++]=mPitch;
        if( COM_DATA_SIZE <= comindex_p ){
            comindex_p = 0;
        }
    }

    private float getChangedAzimut() {
        if( full ) {
            int i;
            float min = 1000, max = -1000;
            int mini = -1,maxi = -1,count = 0;
            float sum = 0.0f;
            float arr []=new float[COM_DATA_SIZE];

            float firstValue = mAzimutArray[0];
            arr[0] = mAzimutArray[0];

            for (i = 1; i < COM_DATA_SIZE; i++) {
                if (firstValue>mAzimutArray[i]) {
                    if(firstValue-mAzimutArray[i]>180){
                        arr[i]=mAzimutArray[i]+360;
                    }else{
                        arr[i]=mAzimutArray[i];
                    }
                }else{
                    if(mAzimutArray[i]-firstValue>180){
                        arr[i]=mAzimutArray[i]-360;
                    }else{
                        arr[i]=mAzimutArray[i];
                    }
                }

            }

            for (i = 0; i < COM_DATA_SIZE; i++) {
                if (arr[i] < min) {
                    min = arr[i];
                    mini = i;
                }
                if (arr[i] > max) {
                    max = arr[i];
                    maxi = i;
                }
            }
            for (i = 0; i < COM_DATA_SIZE; i++) {
                if( mini == i || maxi == i ) continue;
                count ++;
                sum += arr[i];
            }
            float dir = sum/count;
            for(;;) {
                if (dir>=0 && dir<=360) break;
                if (dir < 0) dir += 360;
                if (dir > 360) dir -= 360;
            }
            return dir;
        }else{
            return mAzimutArray[comindex];
        }
    }

    void scrollviewX() {
        float aziX = 6200 * (getChangedAzimut() / 360.0f);
        int int_aziX = (int) aziX;
        h_sv.smoothScrollTo(int_aziX, 0);
        Log.d(TAG, "===int x============" + int_aziX);
    }

    void scrollviewY() {
        float pitY = 3500 * (mPitchArray[comindex_p] / 360.0f);
        int int_aziY = (int) pitY;
        v_sv.smoothScrollTo(0, int_aziY);
        Log.d(TAG, "===int y============" + mPitchArray[comindex_p]);
    }

    //상세
    private View.OnTouchListener myTouch = new View.OnTouchListener(){
       @Override
        public boolean onTouch(View v, MotionEvent event) {
            float x, y;

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    x = event.getX();
                    y = event.getY();
                    //오리온
                    if((170.0<x && x<600.0) && (1590.0<y && y<2160.0)) check=2;
                    //천칭
                    else if((4050.0<x && x<4330.0) && (2130.0<y && y<2470.0)) check=3;
                    //페가수스
                    else if((1800.0<x && x<2500.0) && (1350.0<y && y<1800.0)) check=4;
                    else check=1;
                    //오리온
                    if(check==2)
                    {
                        orionView = (View) View.inflate(MainActivity.this, R.layout.orion_myth, null);
                        AlertDialog.Builder orion_dlg = new AlertDialog.Builder(MainActivity.this);
                        orion_dlg.setView(orionView);
                        orion_dlg.setPositiveButton("닫기", null);
                        orion_dlg.setNegativeButton("공유하기", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                textShare(2);
                            }
                        });
                        orion_dlg.show();
                    }
                    //천칭
                    if(check==3)
                    {
                        libraView = (View) View.inflate(MainActivity.this, R.layout.libra_myth, null);
                        AlertDialog.Builder libra_dlg = new AlertDialog.Builder(MainActivity.this);
                        libra_dlg.setView(libraView);
                        libra_dlg.setPositiveButton("닫기", null);
                        libra_dlg.setNegativeButton("공유하기", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                textShare(3);
                            }
                        });
                        libra_dlg.show();
                    }
                    //페가수스
                    if(check==4)
                    {
                        pegasusView = (View) View.inflate(MainActivity.this, R.layout.pegasus_myth, null);
                        AlertDialog.Builder pegasus_dlg = new AlertDialog.Builder(MainActivity.this);
                        pegasus_dlg.setView(pegasusView);
                        pegasus_dlg.setPositiveButton("닫기", null);
                        pegasus_dlg.setNegativeButton("공유하기", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                textShare(4);
                            }
                        });
                        pegasus_dlg.show();
                    }
                    //myCoor.setText("x: "+x+"   /   y: "+y+"   /   check: "+check);
                    break;
                case MotionEvent.ACTION_MOVE:
                    break;
                case MotionEvent.ACTION_UP:
                    break;
            }
            return false;
        }
    };

    private void textShare(int i){
        try {
            String message = "";
            switch (i){
                case 2:
                    message = "오리온 자리\n"+getString(R.string.orion_t);
                    break;
                case 3:
                    message = "천칭 자리\n"+getString(R.string.libra_t);
                    break;
                case 4:
                    message = "페가수스 자리\n"+getString(R.string.pegasus_t);
                    break;
            }
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_TEXT, message );
            intent.setPackage("com.kakao.talk");
            startActivity(intent);
        } catch (ActivityNotFoundException e) {

        }
    }
}


