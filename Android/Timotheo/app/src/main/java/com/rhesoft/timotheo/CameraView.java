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

import android.content.Context;
import android.graphics.Color;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.ImageView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CameraView extends SurfaceView implements SurfaceHolder.Callback, Camera.PreviewCallback {
    private SurfaceHolder mHolder;
    private Camera mCamera;

    private int width = 320, height = 240; //size of the preview image
    private boolean isRunning = false; //if the robot is running
    private boolean isCalibrating = false; //if the application is calibrating the colors
    private boolean isFlashOn = false; //if the flash is on
    private List<ImageView> mBlocks = new ArrayList<ImageView>(); //list of buttons
    private int[] mBlocksMedian = new int[5]; //median value of each block

    private int mBlocksBiggest = 0; //biggest value of blocks
    private int mBlocksLowest = 255; //lowest value of blocks
    private int calibratingCounter = 0; //how many pictures was processed in the calibration

    private boolean canProcess = false; //if we can process the image
    private int colorDivisor = 126; //if > than colorDivisor then is black
    private MainActivity parent;

    public CameraView(Context context, Camera camera) {
        super(context);

        parent = (MainActivity) context;
        mCamera = camera;

        Camera.Parameters p = mCamera.getParameters();
        p.setPreviewSize(width, height);
        mCamera.setParameters(p);

        mCamera.setDisplayOrientation(90);
        //get the holder and set this class as the callback, so we can get camera data here
        mHolder = getHolder();
        mHolder.addCallback(this);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_NORMAL);
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        try {
            //when the surface is created, we can set the camera to draw images in this surfaceholder
            mCamera.setPreviewDisplay(surfaceHolder);
            mCamera.setPreviewCallback(this);
            mCamera.startPreview();

            mBlocks.add((ImageView) parent.findViewById(R.id.btnCalibrate));
            mBlocks.add((ImageView) parent.findViewById(R.id.btnFlash));
            mBlocks.add((ImageView) parent.findViewById(R.id.btnConnect));
            mBlocks.add((ImageView) parent.findViewById(R.id.btnStop));
            mBlocks.add((ImageView) parent.findViewById(R.id.btnStart));

        } catch (IOException e) {
            Log.d("ERROR", "Camera error on surfaceCreated " + e.getMessage());
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i2, int i3) {
        //before changing the application orientation, you need to stop the preview, rotate and then start it again
        if (mHolder.getSurface() == null)//check if the surface is ready to receive camera data
            return;

        try {
            mCamera.stopPreview();
        } catch (Exception e) {
            //this will happen when you are trying the camera if it's not running
        }

        //now, recreate the camera preview
        try {
            mCamera.setPreviewDisplay(mHolder);
            mCamera.setPreviewCallback(this);
            mCamera.startPreview();
        } catch (IOException e) {
            Log.d("ERROR", "Camera error on surfaceChanged " + e.getMessage());
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
    }

    @Override
    public void onPreviewFrame(byte[] bytes, Camera camera) {
        synchronized (this) {
            if (!isCalibrating && !canProcess && isRunning) {
                return; //if we are calibrating the camera, we'll analyze all pictures
                //but if we are running the robot(calibrating = false), we can only analyze it when
                //canProcess = true
                //with canProcess, we'll be able to choose how many pixels will be analyzed per second
            }

            int mBlocksQtd[] = new int[5]; //count number of pixels per block
            Arrays.fill(mBlocksMedian, 0); //we reset the block values

            int initialLine = 295; //where we start to analyze
            int finalLine = 300; //the final line to analyze
            int i, j = 5;

            for (i = 0; i < 240; i++) {
                if (i % 48 == 0) //we dived the 240 line in 5 blocks, each one with 48px
                    j--; //each 48px, we successfully read a block
                int value = 0;
                for (int k = initialLine; k < finalLine; k++) //for each line we read the value of the pixel
                    value += (bytes[k + 320 * i]) & 0xFF; //convert to a byte
                value /= (finalLine - initialLine); //get the median of this pixel

                mBlocksMedian[j] += value; //add the pixel median to the block
                mBlocksQtd[j]++; //add 1 to the number of pixels analyzed in each block.
                // Total will be 48, just use it to be easier to change the code :)
            }

            for (i = 0; i < 5; i++)
                mBlocksMedian[i] = mBlocksMedian[i] / mBlocksQtd[i]; //get the media value of each block

            if (isCalibrating) { //if this method is being called in a calibration, we need to
                doCalibrate(); //recalculate some variables
                return;
            }

            //if we are not processing, we need to display the camera results in the buttons
            for (i = 0; i < 5; i++)
                mBlocks.get(i).setBackgroundColor(mBlocksMedian[i] > colorDivisor ? Color.WHITE : Color.BLACK);

            //after each image processed, we'll need to wait until the next frame
            canProcess = false;
        }
    }

    private void doCalibrate() {
        //this method is called each frame
        int i;
        for (i = 0; i < 5; i++) {//so we check the lowest and biggest median of each block
            if (mBlocksMedian[i] > mBlocksBiggest)
                mBlocksBiggest = mBlocksMedian[i];
            if (mBlocksMedian[i] < mBlocksLowest)
                mBlocksLowest = mBlocksMedian[i];
        }

        calibratingCounter++;
        isCalibrating = true;//we do it 100 times, and then we call the stopCalibrate
        if (calibratingCounter == 100)
            stopCalibrate();

    }

    private void stopCalibrate() {
        //this method will calculated the colorDivisor as the median of the lowest and biggest
        colorDivisor = (mBlocksLowest + mBlocksBiggest) / 2;
        isCalibrating = false;

        Logger.Log("New color dividor: " + colorDivisor);
    }

    public void Calibrate() {
        Logger.Log("Calibrating...");
        mBlocksBiggest = 0;
        mBlocksLowest = 255;
        isCalibrating = true;
    }

    public void switchFlash() {
        Camera.Parameters p = mCamera.getParameters();
        if (isFlashOn)
            p.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
        else
            p.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
        isFlashOn = !isFlashOn;
        mCamera.setParameters(p);
        Logger.Log("Flash switched");
    }
}
