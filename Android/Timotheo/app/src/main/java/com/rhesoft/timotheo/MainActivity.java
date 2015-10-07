/*
    This file is part of Timótheo.

    Timótheo is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Timótheo is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Timótheo.  If not, see <http://www.gnu.org/licenses/>.
*/
package com.rhesoft.timotheo;

import android.app.Activity;
import android.hardware.Camera;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;

import com.rhesoft.timotheo.CameraView;


public class MainActivity extends Activity implements View.OnClickListener{
    private Camera mCamera = null;
    private CameraView mCameraView = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Logger l = Logger.getInstance(this); //create the logger instance

        Logger.Log("Starting Timótheo...");
        try{
            Logger.Log("\tCreating the camera...");
            mCamera = Camera.open();//you can use open(int) to use different cameras
        } catch (Exception e){
            Logger.LogError("\tFailed to get camera: " + e.getMessage());
        }

        Logger.Log("\tDone!\n\tCreating the camera preview...");
        if(mCamera != null) {
            mCameraView = new CameraView(this, mCamera);//create a SurfaceView to show camera data
            FrameLayout camera_view = (FrameLayout)findViewById(R.id.camera_view);
            camera_view.addView(mCameraView);//add the SurfaceView to the layout
        }
        Logger.Log("\tDone!");
        //btn to close the application
        ImageButton imgClose = (ImageButton)findViewById(R.id.imgClose);
        imgClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                System.exit(0);
            }
        });
        Logger.Log("Timótheo started successfully!");

        findViewById(R.id.btnCalibrate).setOnClickListener(this);
        findViewById(R.id.btnFlash).setOnClickListener(this);
        findViewById(R.id.btnConnect).setOnClickListener(this);
        findViewById(R.id.btnStop).setOnClickListener(this);
        findViewById(R.id.btnStart).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btnCalibrate:
                mCameraView.Calibrate();
                break;
            case R.id.btnFlash:
                mCameraView.switchFlash();
                break;
            case R.id.btnConnect:
                break;
            case R.id.btnStop:
                break;
            case R.id.btnStart:
                break;
        }
    }
}
