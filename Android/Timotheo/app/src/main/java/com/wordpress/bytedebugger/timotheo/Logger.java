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
package com.wordpress.bytedebugger.timotheo;

import android.util.Log;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;

/**
 * This class will manage the Log messages
 */
public class Logger {
    private static Logger __logger = null;
    protected static final String TAG = "[Logger]";
    private static TextView mTxtLogger;
    private static ScrollView mScrollLogger;

    /**
     * Ensure that we have only one Logger instance running
     * @param parent MainActivity
     * @return Logger
     */
    public static Logger getInstance(MainActivity parent) {
        return (__logger == null) ? new Logger(parent) : __logger;
    }

    public static Logger getInstance() {
        return (__logger == null) ? new Logger(null) : __logger;
    }

    private Logger(MainActivity parent) {
        mTxtLogger = (TextView)parent.findViewById(R.id.txtLogger);
        mScrollLogger = (ScrollView)parent.findViewById(R.id.scrLogger);
    }

    /**
     * Log the message, display it in the TextView area and in the logcat
     * @param txt
     */
    public static void Log(String txt) {
        Log.d(TAG, txt);
        mTxtLogger.append(txt + "\n");

        mScrollLogger.post(new Runnable() {
            @Override
            public void run() {
                mScrollLogger.fullScroll(View.FOCUS_DOWN);
            }
        });
    }

    /**
     * Log a error message, show it in the TextView, and in the logcat
     * @param txt
     */
    public static void LogError(String txt) {
        Log.e(TAG, txt);
        mTxtLogger.append("[ERROR] " + txt + "\n");

        mScrollLogger.post(new Runnable() {
            @Override
            public void run() {
                mScrollLogger.fullScroll(View.FOCUS_DOWN);
            }
        });
    }
}