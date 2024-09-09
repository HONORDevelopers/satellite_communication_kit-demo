/*
 * Copyright (c) Honor Device Co., Ltd. 2024-2024. All rights reserved.
 */

package com.example.satellite;

import android.app.PendingIntent;
import android.content.Context;
import android.util.Log;
import android.widget.TextView;

import com.hihonor.android.telephony.satellite.HnAvailableSatSim;
import com.hihonor.android.telephony.satellite.HnSatelliteManager;
import com.hihonor.android.telephony.satellite.HnSatellitePointingCallback;
import com.hihonor.android.telephony.satellite.HnSatellitePointingUpdates;
import com.hihonor.android.telephony.satellite.HnSatelliteRequestCallback;
import com.hihonor.android.telephony.satellite.HnSatelliteServiceState;
import com.hihonor.android.telephony.satellite.HnSatelliteSignalStrength;
import com.hihonor.android.telephony.satellite.HnSatelliteSmsManager;
import com.hihonor.android.telephony.satellite.HnSatelliteStateCallback;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 接口调用类，对卫星通信接口的调用进行封装
 *
 * @author Liu Penghui
 * @since 2024-08-20
 */
public class SatComKitDemo {
    private static final String TAG = "SatComKitDemo";
    private MainActivity mainActivity;
    private HnSatelliteManager mHnSatelliteManager;
    private HnSatelliteSmsManager mHnSatelliteSmsManager;
    private Context context;
    private ExecutorService threadPool;
    private HnSatelliteRequestCallback mHnSatelliteRequestCallback = new HnSatelliteRequestCallback() {
        @Override
        public void onRequestResult(boolean rst) {
            Log.i(TAG, "result: " + rst);
            mainActivity.getTextOutput().setText("requestSatelliteEnabled:\nresult: " + rst);
        }
    };
    private HnSatelliteStateCallback mHnSatelliteStateCallback = new HnSatelliteStateCallback() {
        @Override
        public void onServiceStateChanged(HnSatelliteServiceState hnSatelliteServiceState) {
            if (hnSatelliteServiceState == null) {
                Log.i(TAG, "onServiceStateChanged data null");
                mainActivity.addText("onServiceStateChanged data null");
                return;
            }
            /* 卫星服务状态，0为有服务，1为无服务，其他见HnSatelliteServiceState类中定义 */
            int satService = hnSatelliteServiceState.getState();
            TextView textServStat = mainActivity.findViewById(R.id.textServStatOutput);
            textServStat.setText(String.valueOf(satService));
        }

        @Override
        public void onSignalStrengthChanged(HnSatelliteSignalStrength hnSatelliteSignalStrength) {
            if (hnSatelliteSignalStrength == null) {
                Log.i(TAG, "onSignalStrengthChanged data null");
                mainActivity.addText("onSignalStrengthChanged data null");
                return;
            }
            int satSignal = hnSatelliteSignalStrength.getLevel(); /* 卫星信号格数 */
            TextView textSigLvl = mainActivity.findViewById(R.id.textSigLvlOutput);
            textSigLvl.setText(String.valueOf(satSignal));
        }
    };
    private HnSatellitePointingCallback mHnSatellitePointingCallback = new HnSatellitePointingCallback() {
        @Override
        public void onSatellitePointingUpdate(HnSatellitePointingUpdates satellitePointData,
            HnSatellitePointingUpdates phonePointData) {
            if (satellitePointData == null || phonePointData == null) {
                Log.i(TAG, "onSatellitePointingUpdate data null");
                mainActivity.addText("onSatellitePointingUpdate data null");
                return;
            }

            /* 卫星的仰角 */
            double satelliteEle = satellitePointData.getElevation();
            /* 卫星的方位角 */
            double satelliteAzi = satellitePointData.getAzimuth();
            /* 卫星的水平角 */
            double satelliteHor = satellitePointData.getHorizontal();
            /* 手机的仰角 */
            double phoneEle = phonePointData.getElevation();
            /* 手机的方位角 */
            double phoneAzi = phonePointData.getAzimuth();
            /* 手机的水平角 */
            double phoneHor = phonePointData.getHorizontal();

            TextView textSatEle = mainActivity.findViewById(R.id.textSatEleOutput);
            textSatEle.setText(String.valueOf(satelliteEle));
            TextView textSatAzi = mainActivity.findViewById(R.id.textSatAziOutput);
            textSatAzi.setText(String.valueOf(satelliteAzi));
            TextView textSatHor = mainActivity.findViewById(R.id.textSatHorOutput);
            textSatHor.setText(String.valueOf(satelliteHor));
            TextView textPhoEle = mainActivity.findViewById(R.id.textPhoEleOutput);
            textPhoEle.setText(String.valueOf(phoneEle));
            TextView textPhoAzi = mainActivity.findViewById(R.id.textPhoAziOutput);
            textPhoAzi.setText(String.valueOf(phoneAzi));
            TextView textPhoHor = mainActivity.findViewById(R.id.textPhoHorOutput);
            textPhoHor.setText(String.valueOf(phoneHor));
        }
    };

    /**
     * Constructor
     *
     * @param _mainActivity instance of class MainActivity
     */
    public SatComKitDemo(MainActivity _mainActivity) {
        mainActivity = _mainActivity;
        context = _mainActivity.getApplicationContext();
        mHnSatelliteManager = new HnSatelliteManager(context);
        mHnSatelliteSmsManager = new HnSatelliteSmsManager(context);
        threadPool = Executors.newCachedThreadPool();
    }

    /**
     * Request to enable satellite.
     * This operation will cost power and may incur charges
     *
     * @param enableSatellite true for enable and false for disable
     */
    public void requestSatelliteEnabled(boolean enableSatellite) {
        mHnSatelliteManager.requestSatelliteEnabled(enableSatellite, threadPool,
                mHnSatelliteRequestCallback);
    }

    /**
     * Registers for modem state changed from satellite modem.
     *
     * @return The result of the operation.
     */
    public int registerForSatelliteModemStateChanged() {
        return mHnSatelliteManager.registerForSatelliteModemStateChanged(threadPool,
                mHnSatelliteStateCallback);
    }

    /**
     * Unregisters for modem state changed from satellite modem.
     */
    public void unregisterForSatelliteModemStateChanged() {
        mHnSatelliteManager.unregisterForSatelliteModemStateChanged(mHnSatelliteStateCallback);
    }

    /**
     * Registers for satellite pointing info updates.
     *
     * @return The result of the operation.
     */
    public int registerForSatellitePointingUpdates() {
        return mHnSatelliteManager.registerForSatellitePointingUpdates(threadPool,
                mHnSatellitePointingCallback);
    }

    /**
     * Unregisters for satellite pointing info updates.
     */
    public void unregisterForSatellitePointingUpdates() {
        mHnSatelliteManager.unregisterForSatellitePointingUpdates(mHnSatellitePointingCallback);
    }

    /**
     * The satellite type of current device supported.
     *
     * @return satellite support type
     */
    public int getSatelliteSupportType() {
        return mHnSatelliteManager.getSatelliteSupportType();
    }

    /**
     * Get available satellite sim card information.
     *
     * @return Available satellite sim card information {@code HnAvailableSatSim}
     * default is an empty ArrayList.
     */
    public List<HnAvailableSatSim> getAvailableSatSimCards() {
        return mHnSatelliteManager.getAvailableSatSimCards();
    }

    /**
     * Set default satellite sim slot.
     *
     * @param slotId slot ID: 0 or 1
     */
    public void setSatelliteSlot(int slotId) {
        mHnSatelliteManager.setSatelliteSlot(slotId);
    }

    /**
     * Send a text message based satellite.
     *
     * @param destinationAddress the address to send the message to
     * @param scAddress is the service center address or null to use
     *  the current default SMSC
     * @param text the body of the message to send
     * @param sentIntent if not NULL this <code>PendingIntent</code> is
     *  broadcast when the message is successfully sent, or failed.
     * @param deliveryIntent if not NULL this <code>PendingIntent</code> is
     *  broadcast when the message is delivered to the recipient.
     */
    public void sendTextMessage(String destinationAddress, String scAddress, String text,
                                PendingIntent sentIntent, PendingIntent deliveryIntent) {
        mHnSatelliteSmsManager.sendTextMessage(destinationAddress, scAddress, text,
                sentIntent, deliveryIntent);
    }
}
