/*
 * Copyright (c) Honor Device Co., Ltd. 2024-2024. All rights reserved.
 */

package com.example.satellite;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.hihonor.android.telephony.satellite.HnAvailableSatSim;

import org.w3c.dom.Text;

import java.util.List;

/**
 * 测试apk的主活动，包含各控件功能的实现方法
 *
 * @author Liu Penghui
 * @since 2024-08-20
 */
public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final String SATELLITE_MESSAGE_SENT_ACTION = "com.android.satellit.sms.sent";
    private static final String SATELLITE_MESSAGE_DELIVERY_ACTION = "com.android.satellit.sms.delivery";
    private TextView textOutput;
    private EditText editInput;
    private IntentReceiver intentReceiver;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_2);

        /*
         * 初始化输入输出控件
         */
        textOutput = findViewById(R.id.textOutput);
        textOutput.setMovementMethod(ScrollingMovementMethod.getInstance());
        textOutput.setOnTouchListener((view, motionEvent) -> {
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN
                    || motionEvent.getAction() == MotionEvent.ACTION_MOVE) {
                if (textOutput.canScrollVertically(1)
                        || textOutput.canScrollVertically(-1)) {
                    view.getParent().requestDisallowInterceptTouchEvent(true);
                }
            }
            if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                view.getParent().requestDisallowInterceptTouchEvent(false);
            }
            return false;
        });

        editInput = findViewById(R.id.editInput);
        editInput.setMovementMethod(ScrollingMovementMethod.getInstance());
        editInput.setOnTouchListener((view, motionEvent) -> {
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN
                    || motionEvent.getAction() == MotionEvent.ACTION_MOVE) {
                if (editInput.canScrollVertically(1)
                        || editInput.canScrollVertically(-1)) {
                    view.getParent().requestDisallowInterceptTouchEvent(true);
                }
            }
            if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                view.getParent().requestDisallowInterceptTouchEvent(false);
            }
            return false;
        });

        /*
         * 创建并注册广播接收，用于接收发送短信的结果
         */
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(SATELLITE_MESSAGE_SENT_ACTION);
        intentFilter.addAction(SATELLITE_MESSAGE_DELIVERY_ACTION);

        intentReceiver = new IntentReceiver();
        registerReceiver(intentReceiver, intentFilter, RECEIVER_EXPORTED);

        /*
         * 创建接口调用类实例
         */
        SatComKitDemo satComKitDemo = new SatComKitDemo(MainActivity.this);

        /*
         * 在执行下列步骤的过程中，如果想要清空输出框的显示内容，可以点击该按钮
         */
        Button btnClearButton = findViewById(R.id.btnClearButton);
        btnClearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                textOutput.setText("");
                editInput.setText("");
                editInput.setHint("输入参数......");
            }
        });

        /*
         * 步骤1：判断当前设备是否支持卫星通信，支持则调用步骤2，不支持则不再执行后续操作
         * 通过比较设备支持的卫星类型与需要的卫星类型是否一致，来判断是否支持卫星通信
         */
        Button btnSatSupportType = findViewById(R.id.btnSatSupportType);
        btnSatSupportType.setOnClickListener(view -> {
            int rst = satComKitDemo.getSatelliteSupportType();
            textOutput.setText("getSatelliteSupportType: " + "\nresult: " + rst);
        });

        /*
         * 步骤2.1：获取设备中支持卫星通信的sim card
         * 如果存在多张sim card，则由用户进行选择，执行步骤2.1,设置默认卫星卡
         * 如果不存在可用sim card，则不执行后续步骤；
         */
        Button btnSatSimCards = findViewById(R.id.btnSatSimCards);
        btnSatSimCards.setOnClickListener(view -> {
            List<HnAvailableSatSim> availableSatSimList = satComKitDemo.getAvailableSatSimCards();
            StringBuilder rst = new StringBuilder();
            if (!availableSatSimList.isEmpty()) {
                for (HnAvailableSatSim mHnAvailableSatSim: availableSatSimList) {
                    rst.append("mSlotId: ")
                        .append(mHnAvailableSatSim.getSlotId())
                        .append("; mSupportSatType: ")
                        .append(mHnAvailableSatSim.getSatelliteSupportType())
                        .append("; mOperator: ")
                        .append(mHnAvailableSatSim.getSimOperator())
                        .append("; mPhoneNumber: ")
                        .append(mHnAvailableSatSim.getPhoneNumber())
                        .append("\n");
                }
            } else {
                rst.append("List<HnAvailableSatSim> availableSatSimList is empty!");
            }
            textOutput.setText("getAvailableSatSimCards:\n" + rst);
        });

        /*
         * 步骤2.2：设置默认卫星卡
         */
        Button btnSatSetSlot = findViewById(R.id.btnSatSetSlot);
        btnSatSetSlot.setOnClickListener(view -> {
            String textInput = editInput.getText().toString();
            if ("".equals(textInput)) {
                Toast.makeText(getApplicationContext(), "需要在输入框输入参数！",
                        Toast.LENGTH_SHORT).show();
                editInput.setHint("请在此处输入参数：(int) slotId");
                return;
            }
            String[] strList = textInput.split(" ");
            if (strList.length > 1) {
                Toast.makeText(getApplicationContext(), "输入参数个数大于1！",
                        Toast.LENGTH_SHORT).show();
                editInput.setText("");
                return;
            }
            int slotId = Integer.parseInt(strList[0]);
            textOutput.setText("setSatelliteSlot:");
            satComKitDemo.setSatelliteSlot(slotId);
            editInput.setText("");
            editInput.setHint("输入参数......");
        });

        /*
         * 步骤3：调用异步接口，注册对星、搜星数据回调
         * 在对星、搜星数据发生变化时，会主动上报，在开发者界面进行显示，方便用户进行对星
         * 建议在接收到对星数据后，再执行步骤4、5
         */
        Button btnSatPointing = findViewById(R.id.btnSatPointing);
        btnSatPointing.setOnClickListener(view -> {
            textOutput.setText("registerForSatellitePointingUpdates: ");
            int rst = satComKitDemo.registerForSatellitePointingUpdates();
            addText("result: " + rst);
        });

        /*
         * 步骤4：调用异步接口，注册卫星服务状态和信号状态的回调
         * 在卫星服务状态或信号状态发生变化时，会主动上报，在开发者界面进行显示
         */
        Button btnSatModem = findViewById(R.id.btnSatModem);
        btnSatModem.setOnClickListener(view -> {
            textOutput.setText("registerForSatelliteModemStateChanged: ");
            int rst = satComKitDemo.registerForSatelliteModemStateChanged();
            addText("result: " + rst);
        });

        /*
         * 步骤5：调用异步接口，使能卫星，并注册其回调，通过回调，返回使能结果
         * 针对天通卫星，建议先进行搜星对星的操作，接收到搜星、对星数据后，调用请求卫星使能的接口
         * 在接收到卫星成功使能后，调用步骤6，否则无效；卫星使能失败，可进行重试
         * 如果持续失败，怀疑是权限问题，可联系荣耀方
         */
        Button btnSatEnabled = findViewById(R.id.btnSatEnabled);
        btnSatEnabled.setOnClickListener(view -> {
            String textInput = editInput.getText().toString();
            if ("".equals(textInput)) {
                Toast.makeText(getApplicationContext(), "需要在输入框输入参数！",
                        Toast.LENGTH_SHORT).show();
                editInput.setHint("请在此处输入参数：\n(boolean) enableSatellite");
                return;
            }
            String[] strList = textInput.split(" ");
            if (strList.length != 1) {
                Toast.makeText(getApplicationContext(), "输入参数个数不等于1！",
                        Toast.LENGTH_SHORT).show();
                editInput.setText("");
                return;
            }
            boolean enableSatellite = Boolean.parseBoolean(strList[0]);
            satComKitDemo.requestSatelliteEnabled(enableSatellite);
            editInput.setText("");
            editInput.setHint("输入参数......");
        });

        /*
         * 步骤6：在保证卫星服务状态为连接后，可调用卫星短信发送的接口，进行短信的发送。
         */
        Button btnSatMessage = findViewById(R.id.btnSatMessage);
        btnSatMessage.setOnClickListener(view -> {
            String textInput = editInput.getText().toString();
            if ("".equals(textInput)) {
                Toast.makeText(getApplicationContext(), "需要在输入框输入参数!",
                        Toast.LENGTH_SHORT).show();
                editInput.setHint("请在此处输入参数：(以空格进行区分)\n" +
                        "(String) destinationAddress scAddress text");
                return;
            }
            String[] strList = textInput.split(" ");
            if (strList.length != 3) {
                Toast.makeText(getApplicationContext(), "输入参数个数不等于3!",
                        Toast.LENGTH_SHORT).show();
                editInput.setText("");
                return;
            }
            String destinationAddress = strList[0];
            String scAddress = strList[1];
            String text = strList[2];

            Context context = getApplicationContext();
            Intent sentNormIntent = new Intent(SATELLITE_MESSAGE_SENT_ACTION);
            sentNormIntent.setPackage(context.getPackageName());
            Intent deliveryNormIntent = new Intent(SATELLITE_MESSAGE_DELIVERY_ACTION);
            deliveryNormIntent.setPackage(context.getPackageName());

            PendingIntent sentIntent = PendingIntent.getBroadcast(context, 0,
                    sentNormIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);
            PendingIntent deliveryIntent = PendingIntent.getBroadcast(context, 1,
                    deliveryNormIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);

            textOutput.setText("sendTextMessage: ");
            satComKitDemo.sendTextMessage(destinationAddress, scAddress, text,
                    sentIntent, deliveryIntent);
            editInput.setText("");
            editInput.setHint("输入参数......");
        });

        /*
         * 步骤7：在不需要进行卫星紧急救援时，调用去注册卫星服务状态和信号状态的接口即可。
         */
        Button btnSatUnModem = findViewById(R.id.btnSatUnModem);
        btnSatUnModem.setOnClickListener(view -> {
            textOutput.setText("unregisterForSatelliteModemStateChanged: ");
            satComKitDemo.unregisterForSatelliteModemStateChanged();
        });

        /*
         * 步骤8：在不需要进行卫星紧急救援时，调用去注册对星、搜星数据的接口即可。
         */
        Button btnSatUnPointing = findViewById(R.id.btnSatUnPointing);
        btnSatUnPointing.setOnClickListener(view -> {
            textOutput.setText("unregisterForSatellitePointingUpdates: ");
            satComKitDemo.unregisterForSatellitePointingUpdates();
        });

        /*
         * 步骤9：在不需要进行卫星紧急救援时，调用去使能卫星的接口即可，接口同步骤5，入参为false。
         */
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(intentReceiver);
    }

    class IntentReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent == null) {
                Log.d(TAG, "received null intent");
                addText("received null intent");
                return;
            }

            final String action = intent.getAction();
            if (action == null) {
                Log.d(TAG, "received null intent action");
                addText("received null intent action");
                return;
            }

            if (SATELLITE_MESSAGE_DELIVERY_ACTION.equals(action)) {
                int resultCode = getResultCode();
                addText("message delivery resultCode: " + resultCode);
            }

            if (SATELLITE_MESSAGE_SENT_ACTION.equals(action)) {
                int resultCode = getResultCode();
                addText("message sent resultCode: " + resultCode);
            }
        }
    }

    /**
     * 在输出框中追加显示内容
     *
     * @param strText 追加内容
     * @return 主活动类的输出框实例成员
     */
    public synchronized void addText(final String strText) {
        String lastText = textOutput.getText().toString().trim();
        textOutput.setText(lastText + '\n' + strText);
    }

    /**
     * 获取输出框实例
     *
     * @return 主活动类的输出框实例成员
     */
    public TextView getTextOutput() {
        return textOutput;
    }
}
