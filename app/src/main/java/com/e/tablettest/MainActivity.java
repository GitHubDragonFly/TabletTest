package com.e.tablettest;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.sun.jna.Platform;

import org.libplctag.Tag;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity implements SetTags,AdapterView.OnItemSelectedListener,ABTaskCallback,MBTaskCallback,WriteTaskCallback,GetCLGXTagsTaskCallback {
    private static final int intButtonWriteABTag1 = R.id.btnWriteABTag1;
    private static final int intButtonWriteABTag2 = R.id.btnWriteABTag2;
    private static final int intButtonWriteABTag3 = R.id.btnWriteABTag3;
    private static final int intButtonWriteABTag4 = R.id.btnWriteABTag4;
    private static final int intButtonWriteABTag5 = R.id.btnWriteABTag5;
    private static final int intButtonWriteABTag6 = R.id.btnWriteABTag6;
    private static final int intButtonWriteABTag7 = R.id.btnWriteABTag7;
    private static final int intButtonWriteMBTag1 = R.id.btnWriteMBTag1;
    private static final int intButtonWriteMBTag2 = R.id.btnWriteMBTag2;
    private static final int intButtonWriteMBTag3 = R.id.btnWriteMBTag3;
    private static final int intButtonWriteMBTag4 = R.id.btnWriteMBTag4;
    private static final int intButtonWriteMBTag5 = R.id.btnWriteMBTag5;
    private static final int intButtonWriteMBTag6 = R.id.btnWriteMBTag6;
    private static final int intButtonWriteMBTag7 = R.id.btnWriteMBTag7;

    private static final int intSpinnerABCPU = R.id.spinnerABCPU;
    private static final int intSpinnerCLGXTags = R.id.spinnerCLGXTags;
    private static final int intSpinnerBooleanDisplay = R.id.spinnerBooleanDisplay;

    public static ABTaskCallback ABtaskCallback;
    public static MBTaskCallback MBtaskCallback;
    public static GetCLGXTagsTaskCallback GetCLGXTagstaskCallback;
    public static WriteTaskCallback WritetaskCallback;
    public static SetTags setTags;

    private final Tag Master = new Tag();

    public static int version_major;
    public static int version_minor;
    public static int version_patch;

    // Variables used to control unintentional multiple taps of the same button
    public static boolean gaugeScreenABOpen, popupScreenABOpen, gaugeScreenMBOpen, popupScreenMBOpen;
    private boolean clearingTags, abActive, otherActive, getCLGXTagsRunning;

    private static final String TAG = "Main Activity";

    public static String abGaugeAddress = "", abCPU = "", abIPAddress = "", abPath = "", timeout = "";
    public static String mbGaugeAddress = "", mbIPAddress = "", mbUnitID = "";
    public static String cpu = "", callerName = "", boolDisplay = "";
    public static boolean cbSwapBytesChecked, cbSwapWordsChecked;

    AsyncReadTaskAB myTaskAB = null;
    AsyncReadTaskModbus myTaskMB = null;
    AsyncTaskGetCLGXTags myTaskGetCLGXTags = null;
    AsyncWriteTaskAB myWriteTaskAB = null;
    AsyncWriteTaskModbus myWriteTaskMB = null;

    EditText tvABx, tvAB1, tvAB2, tvAB3, tvAB4, tvAB5, tvAB6, tvAB7, tvMBx, tvMB1, tvMB2, tvMB3, tvMB4, tvMB5, tvMB6, tvMB7;
    EditText etABx, etAB1, etAB2, etAB3, etAB4, etAB5, etAB6, etAB7, etABGauge, etMBx, etMB1, etMB2, etMB3, etMB4, etMB5, etMB6, etMB7, etMBGauge;
    EditText etABIP, etABPath, etMBIP, etMBUnitID, etTimeout, etProgram;
    ToggleButton btnAB, btnMB;
    TextView lblWriteMessage;
    Button btnClearABTags, btnGetCLGXTags, btnWriteABCaller, btnWriteAB1, btnWriteAB2, btnWriteAB3, btnWriteAB4, btnWriteAB5, btnWriteAB6, btnWriteAB7, btnABGauge;
    Button btnClearMBTags, btnWriteMBCaller, btnWriteMB1, btnWriteMB2, btnWriteMB3, btnWriteMB4, btnWriteMB5, btnWriteMB6, btnWriteMB7, btnMBGauge;
    Spinner spinABCPU, spinMBCPU, spinCLGXTags, spinBooleanDisplay;
    CheckBox cbSwapBytes, cbSwapWords;

    ColorStateList textColor;
    TextWatcher tcListener;
    View.OnFocusChangeListener ofcl;

    public static class ABAddressInfo
    {
        EditText etABTag;
        EditText etABTagValue;
        String caller;

        ABAddressInfo(){}
    }

    private final List<ABAddressInfo> ABAddressList = new ArrayList<>();

    public static class MBAddressInfo
    {
        EditText etMBTag;
        EditText etMBTagValue;
        String caller;

        MBAddressInfo(){}
    }

    private static final List<MBAddressInfo> MBAddressList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Hide status bar
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        // Keep the screen turned on
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_main);

        spinABCPU = findViewById(R.id.spinnerABCPU);
        spinABCPU.setOnItemSelectedListener(this);

        spinMBCPU = findViewById(R.id.spinnerModbus);

        spinCLGXTags = findViewById(R.id.spinnerCLGXTags);
        spinCLGXTags.setOnItemSelectedListener(this);

        spinBooleanDisplay = findViewById(R.id.spinnerBooleanDisplay);
        spinBooleanDisplay.setOnItemSelectedListener(this);

        tcListener = new TextWatcher(){
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                if (!clearingTags && !callerName.equals("")){
                    // Enable or disable the Read/Write display boxes
                    boolean textHasValue = !charSequence.toString().equals("");

                    switch (callerName){
                        case "etABTag1":
                            tvAB1.setEnabled(textHasValue);
                            abActive = true;
                            otherActive = false;
                            break;
                        case "etABTag2":
                            tvAB2.setEnabled(textHasValue);
                            abActive = true;
                            otherActive = false;
                            break;
                        case "etABTag3":
                            tvAB3.setEnabled(textHasValue);
                            abActive = true;
                            otherActive = false;
                            break;
                        case "etABTag4":
                            tvAB4.setEnabled(textHasValue);
                            abActive = true;
                            otherActive = false;
                            break;
                        case "etABTag5":
                            tvAB5.setEnabled(textHasValue);
                            abActive = true;
                            otherActive = false;
                            break;
                        case "etABTag6":
                            tvAB6.setEnabled(textHasValue);
                            abActive = true;
                            otherActive = false;
                            break;
                        case "etABTag7":
                            tvAB7.setEnabled(textHasValue);
                            abActive = true;
                            otherActive = false;
                            break;
                        case "etABTagGauge":
                            btnABGauge.setEnabled(textHasValue);

                            if (textHasValue)
                                btnABGauge.setBackground(ContextCompat.getDrawable(MainActivity.this.getBaseContext(), android.R.drawable.button_onoff_indicator_on));
                            else
                                btnABGauge.setBackground(ContextCompat.getDrawable(MainActivity.this.getBaseContext(), android.R.drawable.button_onoff_indicator_off));

                            abActive = true;
                            otherActive = false;
                            break;
                        case "tvABTagValue1":
                            btnWriteAB1.setEnabled(textHasValue);

                            if (textHasValue)
                                btnWriteAB1.setBackground(ContextCompat.getDrawable(MainActivity.this, android.R.drawable.button_onoff_indicator_on));
                            else
                                btnWriteAB1.setBackground(ContextCompat.getDrawable(MainActivity.this, android.R.drawable.button_onoff_indicator_off));

                            otherActive = true;
                            break;
                        case "tvABTagValue2":
                            btnWriteAB2.setEnabled(textHasValue);

                            if (textHasValue)
                                btnWriteAB2.setBackground(ContextCompat.getDrawable(MainActivity.this, android.R.drawable.button_onoff_indicator_on));
                            else
                                btnWriteAB2.setBackground(ContextCompat.getDrawable(MainActivity.this, android.R.drawable.button_onoff_indicator_off));

                            otherActive = true;
                            break;
                        case "tvABTagValue3":
                            btnWriteAB3.setEnabled(textHasValue);

                            if (textHasValue)
                                btnWriteAB3.setBackground(ContextCompat.getDrawable(MainActivity.this, android.R.drawable.button_onoff_indicator_on));
                            else
                                btnWriteAB3.setBackground(ContextCompat.getDrawable(MainActivity.this, android.R.drawable.button_onoff_indicator_off));

                            otherActive = true;
                            break;
                        case "tvABTagValue4":
                            btnWriteAB4.setEnabled(textHasValue);

                            if (textHasValue)
                                btnWriteAB4.setBackground(ContextCompat.getDrawable(MainActivity.this, android.R.drawable.button_onoff_indicator_on));
                            else
                                btnWriteAB4.setBackground(ContextCompat.getDrawable(MainActivity.this, android.R.drawable.button_onoff_indicator_off));

                            otherActive = true;
                            break;
                        case "tvABTagValue5":
                            btnWriteAB5.setEnabled(textHasValue);

                            if (textHasValue)
                                btnWriteAB5.setBackground(ContextCompat.getDrawable(MainActivity.this, android.R.drawable.button_onoff_indicator_on));
                            else
                                btnWriteAB5.setBackground(ContextCompat.getDrawable(MainActivity.this, android.R.drawable.button_onoff_indicator_off));

                            otherActive = true;
                            break;
                        case "tvABTagValue6":
                            btnWriteAB6.setEnabled(textHasValue);

                            if (textHasValue)
                                btnWriteAB6.setBackground(ContextCompat.getDrawable(MainActivity.this, android.R.drawable.button_onoff_indicator_on));
                            else
                                btnWriteAB6.setBackground(ContextCompat.getDrawable(MainActivity.this, android.R.drawable.button_onoff_indicator_off));

                            otherActive = true;
                            break;
                        case "tvABTagValue7":
                            btnWriteAB7.setEnabled(textHasValue);

                            if (textHasValue)
                                btnWriteAB7.setBackground(ContextCompat.getDrawable(MainActivity.this, android.R.drawable.button_onoff_indicator_on));
                            else
                                btnWriteAB7.setBackground(ContextCompat.getDrawable(MainActivity.this, android.R.drawable.button_onoff_indicator_off));

                            otherActive = true;
                            break;
                        case "etMBTag1":
                            tvMB1.setEnabled(textHasValue);
                            abActive = false;
                            otherActive = false;
                            break;
                        case "etMBTag2":
                            tvMB2.setEnabled(textHasValue);
                            abActive = false;
                            otherActive = false;
                            break;
                        case "etMBTag3":
                            tvMB3.setEnabled(textHasValue);
                            abActive = false;
                            otherActive = false;
                            break;
                        case "etMBTag4":
                            tvMB4.setEnabled(textHasValue);
                            abActive = false;
                            otherActive = false;
                            break;
                        case "etMBTag5":
                            tvMB5.setEnabled(textHasValue);
                            abActive = false;
                            otherActive = false;
                            break;
                        case "etMBTag6":
                            tvMB6.setEnabled(textHasValue);
                            abActive = false;
                            otherActive = false;
                            break;
                        case "etMBTag7":
                            tvMB7.setEnabled(textHasValue);
                            abActive = false;
                            otherActive = false;
                            break;
                        case "etMBTagGauge":
                            btnMBGauge.setEnabled(textHasValue);

                            if (textHasValue)
                                btnMBGauge.setBackground(ContextCompat.getDrawable(MainActivity.this.getBaseContext(), android.R.drawable.button_onoff_indicator_on));
                            else
                                btnMBGauge.setBackground(ContextCompat.getDrawable(MainActivity.this.getBaseContext(), android.R.drawable.button_onoff_indicator_off));

                            abActive = false;
                            otherActive = false;
                            break;
                        case "tvMBTagValue1":
                            btnWriteMB1.setEnabled(textHasValue);

                            if (textHasValue)
                                btnWriteMB1.setBackground(ContextCompat.getDrawable(MainActivity.this, android.R.drawable.button_onoff_indicator_on));
                            else
                                btnWriteMB1.setBackground(ContextCompat.getDrawable(MainActivity.this, android.R.drawable.button_onoff_indicator_off));

                            otherActive = true;
                            break;
                        case "tvMBTagValue2":
                            btnWriteMB2.setEnabled(textHasValue);

                            if (textHasValue)
                                btnWriteMB2.setBackground(ContextCompat.getDrawable(MainActivity.this, android.R.drawable.button_onoff_indicator_on));
                            else
                                btnWriteMB2.setBackground(ContextCompat.getDrawable(MainActivity.this, android.R.drawable.button_onoff_indicator_off));

                            otherActive = true;
                            break;
                        case "tvMBTagValue3":
                            btnWriteMB3.setEnabled(textHasValue);

                            if (textHasValue)
                                btnWriteMB3.setBackground(ContextCompat.getDrawable(MainActivity.this, android.R.drawable.button_onoff_indicator_on));
                            else
                                btnWriteMB3.setBackground(ContextCompat.getDrawable(MainActivity.this, android.R.drawable.button_onoff_indicator_off));

                            otherActive = true;
                            break;
                        case "tvMBTagValue4":
                            btnWriteMB4.setEnabled(textHasValue);

                            if (textHasValue)
                                btnWriteMB4.setBackground(ContextCompat.getDrawable(MainActivity.this, android.R.drawable.button_onoff_indicator_on));
                            else
                                btnWriteMB4.setBackground(ContextCompat.getDrawable(MainActivity.this, android.R.drawable.button_onoff_indicator_off));

                            otherActive = true;
                            break;
                        case "tvMBTagValue5":
                            btnWriteMB5.setEnabled(textHasValue);

                            if (textHasValue)
                                btnWriteMB5.setBackground(ContextCompat.getDrawable(MainActivity.this, android.R.drawable.button_onoff_indicator_on));
                            else
                                btnWriteMB5.setBackground(ContextCompat.getDrawable(MainActivity.this, android.R.drawable.button_onoff_indicator_off));

                            otherActive = true;
                            break;
                        case "tvMBTagValue6":
                            btnWriteMB6.setEnabled(textHasValue);

                            if (textHasValue)
                                btnWriteMB6.setBackground(ContextCompat.getDrawable(MainActivity.this, android.R.drawable.button_onoff_indicator_on));
                            else
                                btnWriteMB6.setBackground(ContextCompat.getDrawable(MainActivity.this, android.R.drawable.button_onoff_indicator_off));

                            otherActive = true;
                            break;
                        case "tvMBTagValue7":
                            btnWriteMB7.setEnabled(textHasValue);

                            if (textHasValue)
                                btnWriteMB7.setBackground(ContextCompat.getDrawable(MainActivity.this, android.R.drawable.button_onoff_indicator_on));
                            else
                                btnWriteMB7.setBackground(ContextCompat.getDrawable(MainActivity.this, android.R.drawable.button_onoff_indicator_off));

                            otherActive = true;
                            break;
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (!otherActive){
                    if (abActive)
                        ButtonClearABTagsEnableDisable();
                    else
                        ButtonClearMBTagsEnableDisable();
                }
            }
        };

        ofcl = new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (hasFocus)
                    callerName = getResources().getResourceEntryName(view.getId());
            }
        };

        lblWriteMessage = findViewById(R.id.labelWriteMessage);

        tvAB1 = findViewById(R.id.tvABTagValue1);
        tvAB1.setOnFocusChangeListener(ofcl);
        tvAB1.addTextChangedListener(tcListener);
        tvAB2 = findViewById(R.id.tvABTagValue2);
        tvAB2.setOnFocusChangeListener(ofcl);
        tvAB2.addTextChangedListener(tcListener);
        tvAB3 = findViewById(R.id.tvABTagValue3);
        tvAB3.setOnFocusChangeListener(ofcl);
        tvAB3.addTextChangedListener(tcListener);
        tvAB4 = findViewById(R.id.tvABTagValue4);
        tvAB4.setOnFocusChangeListener(ofcl);
        tvAB4.addTextChangedListener(tcListener);
        tvAB5 = findViewById(R.id.tvABTagValue5);
        tvAB5.setOnFocusChangeListener(ofcl);
        tvAB5.addTextChangedListener(tcListener);
        tvAB6 = findViewById(R.id.tvABTagValue6);
        tvAB6.setOnFocusChangeListener(ofcl);
        tvAB6.addTextChangedListener(tcListener);
        tvAB7 = findViewById(R.id.tvABTagValue7);
        tvAB7.setOnFocusChangeListener(ofcl);
        tvAB7.addTextChangedListener(tcListener);
        tvMB1 = findViewById(R.id.tvMBTagValue1);
        tvMB1.setOnFocusChangeListener(ofcl);
        tvMB1.addTextChangedListener(tcListener);
        tvMB2 = findViewById(R.id.tvMBTagValue2);
        tvMB2.setOnFocusChangeListener(ofcl);
        tvMB2.addTextChangedListener(tcListener);
        tvMB3 = findViewById(R.id.tvMBTagValue3);
        tvMB3.setOnFocusChangeListener(ofcl);
        tvMB3.addTextChangedListener(tcListener);
        tvMB4 = findViewById(R.id.tvMBTagValue4);
        tvMB4.setOnFocusChangeListener(ofcl);
        tvMB4.addTextChangedListener(tcListener);
        tvMB5 = findViewById(R.id.tvMBTagValue5);
        tvMB5.setOnFocusChangeListener(ofcl);
        tvMB5.addTextChangedListener(tcListener);
        tvMB6 = findViewById(R.id.tvMBTagValue6);
        tvMB6.setOnFocusChangeListener(ofcl);
        tvMB6.addTextChangedListener(tcListener);
        tvMB7 = findViewById(R.id.tvMBTagValue7);
        tvMB7.setOnFocusChangeListener(ofcl);
        tvMB7.addTextChangedListener(tcListener);

        textColor = tvAB1.getTextColors();

        etAB1 = findViewById(R.id.etABTag1);
        etAB1.addTextChangedListener(tcListener);
        etAB2 = findViewById(R.id.etABTag2);
        etAB2.addTextChangedListener(tcListener);
        etAB3 = findViewById(R.id.etABTag3);
        etAB3.addTextChangedListener(tcListener);
        etAB4 = findViewById(R.id.etABTag4);
        etAB4.addTextChangedListener(tcListener);
        etAB5 = findViewById(R.id.etABTag5);
        etAB5.addTextChangedListener(tcListener);
        etAB6 = findViewById(R.id.etABTag6);
        etAB6.addTextChangedListener(tcListener);
        etAB7 = findViewById(R.id.etABTag7);
        etAB7.addTextChangedListener(tcListener);
        etABGauge = findViewById(R.id.etABTagGauge);
        etABGauge.addTextChangedListener(tcListener);
        etMB1 = findViewById(R.id.etMBTag1);
        etMB1.addTextChangedListener(tcListener);
        etMB2 = findViewById(R.id.etMBTag2);
        etMB2.addTextChangedListener(tcListener);
        etMB3 = findViewById(R.id.etMBTag3);
        etMB3.addTextChangedListener(tcListener);
        etMB4 = findViewById(R.id.etMBTag4);
        etMB4.addTextChangedListener(tcListener);
        etMB5 = findViewById(R.id.etMBTag5);
        etMB5.addTextChangedListener(tcListener);
        etMB6 = findViewById(R.id.etMBTag6);
        etMB6.addTextChangedListener(tcListener);
        etMB7 = findViewById(R.id.etMBTag7);
        etMB7.addTextChangedListener(tcListener);
        etMBGauge = findViewById(R.id.etMBTagGauge);
        etMBGauge.addTextChangedListener(tcListener);

        btnWriteAB1 = findViewById(R.id.btnWriteABTag1);
        btnWriteAB2 = findViewById(R.id.btnWriteABTag2);
        btnWriteAB3 = findViewById(R.id.btnWriteABTag3);
        btnWriteAB4 = findViewById(R.id.btnWriteABTag4);
        btnWriteAB5 = findViewById(R.id.btnWriteABTag5);
        btnWriteAB6 = findViewById(R.id.btnWriteABTag6);
        btnWriteAB7 = findViewById(R.id.btnWriteABTag7);
        btnABGauge = findViewById(R.id.btnABGauge);
        btnWriteMB1 = findViewById(R.id.btnWriteMBTag1);
        btnWriteMB2 = findViewById(R.id.btnWriteMBTag2);
        btnWriteMB3 = findViewById(R.id.btnWriteMBTag3);
        btnWriteMB4 = findViewById(R.id.btnWriteMBTag4);
        btnWriteMB5 = findViewById(R.id.btnWriteMBTag5);
        btnWriteMB6 = findViewById(R.id.btnWriteMBTag6);
        btnWriteMB7 = findViewById(R.id.btnWriteMBTag7);
        btnMBGauge = findViewById(R.id.btnMBGauge);

        btnAB = findViewById(R.id.toggleButtonAB);
        btnMB = findViewById(R.id.toggleButtonMB);
        btnGetCLGXTags = findViewById(R.id.btnGetCLGXTags);

        btnClearABTags = findViewById(R.id.btnClearABTags);
        btnClearMBTags = findViewById(R.id.btnClearMBTags);

        etABIP = findViewById(R.id.etABIPAddress);
        etABPath = findViewById(R.id.etABPath);
        etProgram = findViewById(R.id.etCLGXProgram);
        etTimeout = findViewById(R.id.etTimeout);
        etMBIP = findViewById(R.id.etMBIPAddress);
        etMBUnitID = findViewById(R.id.etMBUnitID);

        cbSwapBytes = findViewById(R.id.cbSwapBytes);
        cbSwapWords = findViewById(R.id.cbSwapWords);

        cbSwapBytesChecked = cbSwapBytes.isChecked();
        cbSwapWordsChecked = cbSwapWords.isChecked();

        ABtaskCallback = this;
        MBtaskCallback = this;
        GetCLGXTagstaskCallback = this;
        WritetaskCallback = this;
        setTags = this;

        version_major = Master.getIntAttribute(0, "version_major", 0);
        version_minor = Master.getIntAttribute(0, "version_minor", 0);
        version_patch = Master.getIntAttribute(0, "version_patch", 0);

        String plctagVersion = "libplctag v" + version_major + "." + version_minor + "." + version_patch;
        String platformArch = "Platform Arch : " + Platform.ARCH;

        EditText et0 = findViewById(R.id.txtLibplctagVersion);
        et0.setText(plctagVersion);
        EditText et1 = findViewById(R.id.txtPlatformArch);
        et1.setText(platformArch);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (myTaskAB != null){
            myTaskAB.cancel(true);
            myTaskAB = null;
        }

        if (myTaskMB != null){
            myTaskMB.cancel(true);
            myTaskMB = null;
        }

        if (myWriteTaskAB != null){
            myWriteTaskAB.cancel(true);
            myWriteTaskAB = null;
        }

        if (myWriteTaskMB != null){
            myWriteTaskMB.cancel(true);
            myWriteTaskMB = null;
        }

        if (myTaskGetCLGXTags != null){
            myTaskGetCLGXTags.cancel(true);
            myTaskGetCLGXTags = null;
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int pos,long id) {
        switch(parent.getId()){
            case intSpinnerABCPU:
                if (spinABCPU.getSelectedItem().toString().equals("controllogix") ||
                        spinABCPU.getSelectedItem().toString().equals("logixpccc") ||
                        spinABCPU.getSelectedItem().toString().equals("njnx")){
                    btnGetCLGXTags.setEnabled(true);
                    btnGetCLGXTags.setBackground(ContextCompat.getDrawable(this, android.R.drawable.button_onoff_indicator_on));
                    spinCLGXTags.setEnabled(true);
                    etProgram.setEnabled(true);
                    String ipAddress = "192.168.1.21";
                    etABIP.setText(ipAddress);
                    etABPath.setText("1,3");
                } else {
                    btnGetCLGXTags.setEnabled(false);
                    btnGetCLGXTags.setBackground(ContextCompat.getDrawable(this, android.R.drawable.button_onoff_indicator_off));
                    spinCLGXTags.setEnabled(false);
                    etProgram.setEnabled(false);
                    String ipAddress = "192.168.1.10";
                    etABIP.setText(ipAddress);
                    etABPath.setText("");
                }
                break;
            case intSpinnerCLGXTags:
                if (!(spinCLGXTags.getSelectedItem().toString().startsWith("*") || spinCLGXTags.getSelectedItem().toString().startsWith("Failed") ||
                        spinCLGXTags.getSelectedItem().toString().equals("Controller + Program Tags"))) {

                    ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("Selected CLGX Tag", spinCLGXTags.getSelectedItem().toString());
                    clipboard.setPrimaryClip(clip);
                }
                break;
            case intSpinnerBooleanDisplay:
                boolDisplay = spinBooleanDisplay.getSelectedItem().toString();
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
    }

    public void ButtonClearABTagsEnableDisable(){
        if ((!(etAB1.getInputType() == InputType.TYPE_NULL) && !etAB1.getText().toString().equals("")) ||
                (!(etAB2.getInputType() == InputType.TYPE_NULL) && !etAB2.getText().toString().equals("")) ||
                (!(etAB3.getInputType() == InputType.TYPE_NULL) && !etAB3.getText().toString().equals("")) ||
                (!(etAB4.getInputType() == InputType.TYPE_NULL) && !etAB4.getText().toString().equals("")) ||
                (!(etAB5.getInputType() == InputType.TYPE_NULL) && !etAB5.getText().toString().equals("")) ||
                (!(etAB6.getInputType() == InputType.TYPE_NULL) && !etAB6.getText().toString().equals("")) ||
                (!(etAB7.getInputType() == InputType.TYPE_NULL) && !etAB7.getText().toString().equals("")) ||
                !etABGauge.getText().toString().equals("")){

            btnClearABTags.setEnabled(true);
            btnClearABTags.setBackground(ContextCompat.getDrawable(MainActivity.this, android.R.drawable.button_onoff_indicator_on));
        } else {
            if (btnClearABTags.isEnabled()){
                btnClearABTags.setEnabled(false);
                btnClearABTags.setBackground(ContextCompat.getDrawable(MainActivity.this, android.R.drawable.button_onoff_indicator_off));
            }
        }
    }

    public void ButtonClearMBTagsEnableDisable(){
        if ((!(etMB1.getInputType() == InputType.TYPE_NULL) && !etMB1.getText().toString().equals("")) ||
                (!(etMB2.getInputType() == InputType.TYPE_NULL) && !etMB2.getText().toString().equals("")) ||
                (!(etMB3.getInputType() == InputType.TYPE_NULL) && !etMB3.getText().toString().equals("")) ||
                (!(etMB4.getInputType() == InputType.TYPE_NULL) && !etMB4.getText().toString().equals("")) ||
                (!(etMB5.getInputType() == InputType.TYPE_NULL) && !etMB5.getText().toString().equals("")) ||
                (!(etMB6.getInputType() == InputType.TYPE_NULL) && !etMB6.getText().toString().equals("")) ||
                (!(etMB7.getInputType() == InputType.TYPE_NULL) && !etMB7.getText().toString().equals("")) ||
                !etMBGauge.getText().toString().equals("")){

            btnClearMBTags.setEnabled(true);
            btnClearMBTags.setBackground(ContextCompat.getDrawable(MainActivity.this, android.R.drawable.button_onoff_indicator_on));
        } else {
            if (btnClearMBTags.isEnabled()){
                btnClearMBTags.setEnabled(false);
                btnClearMBTags.setBackground(ContextCompat.getDrawable(MainActivity.this, android.R.drawable.button_onoff_indicator_off));
            }
        }
    }

    public void sendMessageGetCLGXTags(View v){
        if (!getCLGXTagsRunning){
            getCLGXTagsRunning = true;

            String[] params = new String[3];
            String ipaddress, path, program, timeout;

            ipaddress = etABIP.getText().toString();
            path = etABPath.getText().toString();
            program = etProgram.getText().toString();
            timeout = etTimeout.getText().toString();

            ipaddress = ipaddress.replace(" ", "");
            path = path.replace(" ", "");
            program = program.replace(" ", "");
            timeout = timeout.replace(" ", "");

            if (TextUtils.isEmpty(ipaddress) || TextUtils.isEmpty(path) || TextUtils.isEmpty(program) || !TextUtils.isDigitsOnly(timeout)){
                return;
            }

            params[0] = "gateway=" + ipaddress + "&path=" + path;
            params[1] = program;
            params[2] = timeout;

            if (myTaskGetCLGXTags == null) {
                myTaskGetCLGXTags = new AsyncTaskGetCLGXTags();
            } else {
                return;
            }

            ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, getResources().getStringArray(R.array.ab_tags_please_wait));
            dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            dataAdapter.notifyDataSetChanged();
            spinCLGXTags.setAdapter(dataAdapter);

            myTaskGetCLGXTags.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, params);

            btnGetCLGXTags.setEnabled(false);
            btnGetCLGXTags.setBackground(ContextCompat.getDrawable(this, android.R.drawable.button_onoff_indicator_off));

            Log.v(TAG, "GetCLGXTags Task is " + myTaskGetCLGXTags.getStatus().toString());
        }
    }

    @SuppressWarnings("unchecked")
    public void sendMessageToggleAB(View v) throws InterruptedException {
        if (btnAB.getText().equals(btnAB.getTextOn())){
            if (myTaskAB == null) {
                myTaskAB = new AsyncReadTaskAB();
            } else {
                btnAB.setText(btnAB.getTextOff());
                return;
            }

            if (TextUtils.isEmpty(etAB1.getText()) && TextUtils.isEmpty(etAB2.getText()) &&
                    TextUtils.isEmpty(etAB3.getText()) && TextUtils.isEmpty(etAB4.getText()) &&
                    TextUtils.isEmpty(etAB5.getText()) && TextUtils.isEmpty(etAB6.getText()) &&
                    TextUtils.isEmpty(etAB7.getText())){

                btnAB.setText(btnAB.getTextOff());
                myTaskAB = null;
                return;
            }

            ArrayList<ArrayList<String>> params = new ArrayList<>();
            ArrayList<String> tagItems = new ArrayList<>(), plcAddresses = new ArrayList<>(), callerIDs = new ArrayList<>();

            String cpu, ipaddress, path, timeout;

            cpu = spinABCPU.getSelectedItem().toString();
            ipaddress = etABIP.getText().toString();
            path = etABPath.getText().toString();
            timeout = etTimeout.getText().toString();

            ipaddress = ipaddress.replace(" ", "");
            path = path.replace(" ", "");
            timeout = timeout.replace(" ", "");

            if (TextUtils.isEmpty(ipaddress) || !TextUtils.isDigitsOnly(timeout)){
                myTaskAB = null;
                return;
            }

            tagItems.add("gateway=" + ipaddress + "&path=" + path + "&cpu=" + cpu);
            tagItems.add(timeout);

            params.add(tagItems);

            if (!TextUtils.isEmpty(etAB1.getText())){
                plcAddresses.add(etAB1.getText().toString());
                callerIDs.add("tvABTagValue1");
                ABAddressInfo ABinfo = new ABAddressInfo();
                ABinfo.etABTag = etAB1;
                ABinfo.etABTagValue = tvAB1;
                ABinfo.caller = "tvABTagValue1";
                ABAddressList.add(ABinfo);
            }

            if (!TextUtils.isEmpty(etAB2.getText())){
                plcAddresses.add(etAB2.getText().toString());
                callerIDs.add("tvABTagValue2");
                ABAddressInfo ABinfo = new ABAddressInfo();
                ABinfo.etABTag = etAB2;
                ABinfo.etABTagValue = tvAB2;
                ABinfo.caller = "tvABTagValue2";
                ABAddressList.add(ABinfo);
            }

            if (!TextUtils.isEmpty(etAB3.getText())){
                plcAddresses.add(etAB3.getText().toString());
                callerIDs.add("tvABTagValue3");
                ABAddressInfo ABinfo = new ABAddressInfo();
                ABinfo.etABTag = etAB3;
                ABinfo.etABTagValue = tvAB3;
                ABinfo.caller = "tvABTagValue3";
                ABAddressList.add(ABinfo);
            }

            if (!TextUtils.isEmpty(etAB4.getText())){
                plcAddresses.add(etAB4.getText().toString());
                callerIDs.add("tvABTagValue4");
                ABAddressInfo ABinfo = new ABAddressInfo();
                ABinfo.etABTag = etAB4;
                ABinfo.etABTagValue = tvAB4;
                ABinfo.caller = "tvABTagValue4";
                ABAddressList.add(ABinfo);
            }

            if (!TextUtils.isEmpty(etAB5.getText())){
                plcAddresses.add(etAB5.getText().toString());
                callerIDs.add("tvABTagValue5");
                ABAddressInfo ABinfo = new ABAddressInfo();
                ABinfo.etABTag = etAB5;
                ABinfo.etABTagValue = tvAB5;
                ABinfo.caller = "tvABTagValue5";
                ABAddressList.add(ABinfo);
            }

            if (!TextUtils.isEmpty(etAB6.getText())){
                plcAddresses.add(etAB6.getText().toString());
                callerIDs.add("tvABTagValue6");
                ABAddressInfo ABinfo = new ABAddressInfo();
                ABinfo.etABTag = etAB6;
                ABinfo.etABTagValue = tvAB6;
                ABinfo.caller = "tvABTagValue6";
                ABAddressList.add(ABinfo);
            }

            if (!TextUtils.isEmpty(etAB7.getText())){
                plcAddresses.add(etAB7.getText().toString());
                callerIDs.add("tvABTagValue7");
                ABAddressInfo ABinfo = new ABAddressInfo();
                ABinfo.etABTag = etAB7;
                ABinfo.etABTagValue = tvAB7;
                ABinfo.caller = "tvABTagValue7";
                ABAddressList.add(ABinfo);
            }

            for (ABAddressInfo abi: ABAddressList){
                abi.etABTag.setInputType(InputType.TYPE_NULL);
                abi.etABTag.setClickable(false);
                callerName = abi.caller;
                abi.etABTagValue.setText("");
                abi.etABTagValue.setEnabled(false);
                abi.etABTagValue.removeTextChangedListener(tcListener);
            }

            spinABCPU.setEnabled(false);
            etABIP.setEnabled(false);
            etABPath.setEnabled(false);
            etTimeout.setEnabled(false);

            params.add(plcAddresses);
            params.add(callerIDs);

            myTaskAB.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, params);

            btnAB.setBackground(ContextCompat.getDrawable(this, android.R.drawable.button_onoff_indicator_on));
            callerName = "";
            ButtonClearABTagsEnableDisable();

            Log.v(TAG, "AB Task is " + myTaskAB.getStatus().toString());
        } else {
            if (myTaskAB != null){
                myTaskAB.cancel(true);

                // Add check for isCancelled to prevent any subsequent UI update
                while (!myTaskAB.isCancelled()){
                    TimeUnit.MILLISECONDS.sleep(5);
                }

                myTaskAB = null;
            }

            for (ABAddressInfo abi: ABAddressList){
                abi.etABTag.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
                abi.etABTag.setClickable(true);
                abi.etABTagValue.setEnabled(true);
                abi.etABTagValue.addTextChangedListener(tcListener);
                abi.etABTagValue.setTextColor(textColor);
                callerName = abi.caller;
                abi.etABTagValue.setText("");
            }

            ABAddressList.clear();

            spinABCPU.setEnabled(true);
            etABIP.setEnabled(true);
            etABPath.setEnabled(true);
            if (btnMB.getText().equals(btnMB.getTextOff())){
                etTimeout.setEnabled(true);
            }

            btnAB.setBackground(ContextCompat.getDrawable(this, android.R.drawable.button_onoff_indicator_off));
            ButtonClearABTagsEnableDisable();

            Log.v(TAG, "AB Task is Cancelled");
        }
    }

    @SuppressWarnings("unchecked")
    public void sendMessageToggleMB(View v) throws InterruptedException {
        if (btnMB.getText().equals(btnMB.getTextOn())){
            if (myTaskMB == null) {
                myTaskMB = new AsyncReadTaskModbus();
            } else {
                btnMB.setText(btnMB.getTextOff());
                return;
            }

            if (TextUtils.isEmpty(etMB1.getText()) && TextUtils.isEmpty(etMB2.getText()) &&
                    TextUtils.isEmpty(etMB3.getText()) && TextUtils.isEmpty(etMB4.getText()) &&
                    TextUtils.isEmpty(etMB5.getText()) && TextUtils.isEmpty(etMB6.getText()) &&
                    TextUtils.isEmpty(etMB7.getText())){

                btnMB.setText(btnMB.getTextOff());
                myTaskMB = null;
                return;
            }

            ArrayList<ArrayList<String>> params = new ArrayList<>();
            ArrayList<String> tagItems = new ArrayList<>(), plcAddresses = new ArrayList<>(), callerIDs = new ArrayList<>();

            String ipaddress, unitID, timeout;

            ipaddress = etMBIP.getText().toString();
            unitID = etMBUnitID.getText().toString();
            timeout = etTimeout.getText().toString();

            ipaddress = ipaddress.replace(" ", "");
            unitID = unitID.replace(" ", "");
            timeout = timeout.replace(" ", "");

            if (TextUtils.isEmpty(ipaddress) || !TextUtils.isDigitsOnly(timeout)){
                myTaskMB = null;
                return;
            }

            tagItems.add("gateway=" + ipaddress + "&path=" + unitID);
            tagItems.add(timeout);

            params.add(tagItems);

            if (!TextUtils.isEmpty(etMB1.getText())){
                plcAddresses.add(etMB1.getText().toString());
                callerIDs.add("tvMBTagValue1");
                MBAddressInfo MBinfo = new MBAddressInfo();
                MBinfo.etMBTag = etMB1;
                MBinfo.etMBTagValue = tvMB1;
                MBinfo.caller = "tvMBTagValue1";
                MBAddressList.add(MBinfo);
            }

            if (!TextUtils.isEmpty(etMB2.getText())){
                plcAddresses.add(etMB2.getText().toString());
                callerIDs.add("tvMBTagValue2");
                MBAddressInfo MBinfo = new MBAddressInfo();
                MBinfo.etMBTag = etMB2;
                MBinfo.etMBTagValue = tvMB2;
                MBinfo.caller = "tvMBTagValue2";
                MBAddressList.add(MBinfo);
            }

            if (!TextUtils.isEmpty(etMB3.getText())){
                plcAddresses.add(etMB3.getText().toString());
                callerIDs.add("tvMBTagValue3");
                MBAddressInfo MBinfo = new MBAddressInfo();
                MBinfo.etMBTag = etMB3;
                MBinfo.etMBTagValue = tvMB3;
                MBinfo.caller = "tvMBTagValue3";
                MBAddressList.add(MBinfo);
            }

            if (!TextUtils.isEmpty(etMB4.getText())){
                plcAddresses.add(etMB4.getText().toString());
                callerIDs.add("tvMBTagValue4");
                MBAddressInfo MBinfo = new MBAddressInfo();
                MBinfo.etMBTag = etMB4;
                MBinfo.etMBTagValue = tvMB4;
                MBinfo.caller = "tvMBTagValue4";
                MBAddressList.add(MBinfo);
            }

            if (!TextUtils.isEmpty(etMB5.getText())){
                plcAddresses.add(etMB5.getText().toString());
                callerIDs.add("tvMBTagValue5");
                MBAddressInfo MBinfo = new MBAddressInfo();
                MBinfo.etMBTag = etMB5;
                MBinfo.etMBTagValue = tvMB5;
                MBinfo.caller = "tvMBTagValue5";
                MBAddressList.add(MBinfo);
            }

            if (!TextUtils.isEmpty(etMB6.getText())){
                plcAddresses.add(etMB6.getText().toString());
                callerIDs.add("tvMBTagValue6");
                MBAddressInfo MBinfo = new MBAddressInfo();
                MBinfo.etMBTag = etMB6;
                MBinfo.etMBTagValue = tvMB6;
                MBinfo.caller = "tvMBTagValue6";
                MBAddressList.add(MBinfo);
            }

            if (!TextUtils.isEmpty(etMB7.getText())){
                plcAddresses.add(etMB7.getText().toString());
                callerIDs.add("tvMBTagValue7");
                MBAddressInfo MBinfo = new MBAddressInfo();
                MBinfo.etMBTag = etMB7;
                MBinfo.etMBTagValue = tvMB7;
                MBinfo.caller = "tvMBTagValue7";
                MBAddressList.add(MBinfo);
            }

            for (MBAddressInfo mbi: MBAddressList){
                mbi.etMBTag.setInputType(InputType.TYPE_NULL);
                mbi.etMBTag.setClickable(false);
                callerName = mbi.caller;
                mbi.etMBTagValue.setText("");
                mbi.etMBTagValue.setEnabled(false);
                mbi.etMBTagValue.removeTextChangedListener(tcListener);
            }

            spinMBCPU.setEnabled(false);
            etMBIP.setEnabled(false);
            etMBUnitID.setEnabled(false);
            etTimeout.setEnabled(false);
            cbSwapBytes.setEnabled(false);
            cbSwapWords.setEnabled(false);

            params.add(plcAddresses);
            params.add(callerIDs);

            myTaskMB.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, params);

            btnMB.setBackground(ContextCompat.getDrawable(this, android.R.drawable.button_onoff_indicator_on));
            callerName = "";
            ButtonClearMBTagsEnableDisable();

            Log.v(TAG, "Modbus Task is " + myTaskMB.getStatus().toString());
        } else {
            if (myTaskMB != null){
                myTaskMB.cancel(true);

                // Add check for isCancelled to prevent any subsequent UI update
                while (!myTaskMB.isCancelled()){
                    TimeUnit.MILLISECONDS.sleep(5);
                }

                myTaskMB = null;
            }

            for (MBAddressInfo mbi: MBAddressList){
                mbi.etMBTag.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
                mbi.etMBTag.setClickable(true);
                mbi.etMBTagValue.setEnabled(true);
                mbi.etMBTagValue.addTextChangedListener(tcListener);
                mbi.etMBTagValue.setTextColor(textColor);
                callerName = mbi.caller;
                mbi.etMBTagValue.setText("");
            }

            MBAddressList.clear();

            spinMBCPU.setEnabled(true);
            etMBIP.setEnabled(true);
            etMBUnitID.setEnabled(true);
            if (btnAB.getText().equals(btnAB.getTextOff())){
                etTimeout.setEnabled(true);
            }
            cbSwapBytes.setEnabled(true);
            cbSwapWords.setEnabled(true);

            btnMB.setBackground(ContextCompat.getDrawable(this, android.R.drawable.button_onoff_indicator_off));
            ButtonClearMBTagsEnableDisable();

            Log.v(TAG, "Modbus Task is Cancelled");
        }
    }

    public void sendMessageWriteAB(View v) {
        if (myWriteTaskAB == null) {
            // Clear the label indicating write success/failure
            ((TextView)findViewById(R.id.labelWriteMessage)).setText("");

            myWriteTaskAB = new AsyncWriteTaskAB();
        } else {
            return;
        }

        String cpu, ipaddress, path, timeout;

        cpu = spinABCPU.getSelectedItem().toString();
        ipaddress = etABIP.getText().toString();
        path = etABPath.getText().toString();
        timeout = etTimeout.getText().toString();

        ipaddress = ipaddress.replace(" ", "");
        path = path.replace(" ", "");
        timeout = timeout.replace(" ", "");

        if (TextUtils.isEmpty(ipaddress) || (TextUtils.isEmpty(path) && (cpu.equals("controllogix") || cpu.equals("logixpccc") || cpu.equals("njnx"))) || !TextUtils.isDigitsOnly(timeout)){
            myWriteTaskAB = null;
            return;
        }

        String[] params = new String[4];

        if (cpu.equals("controllogix") || cpu.equals("logixpccc") || cpu.equals("njnx")){
            params[0] = "gateway=" + ipaddress + "&path=" + path + "&cpu=" + cpu;
        } else {
            params[0] = "gateway=" + ipaddress + "&cpu=" + cpu;
        }

        params[1] = timeout;

        switch(v.getId()){
            case intButtonWriteABTag1:
                if (TextUtils.isEmpty(etAB1.getText()) || TextUtils.isEmpty(tvAB1.getText())){
                    myWriteTaskAB = null;
                    return;
                } else {
                    params[2] = etAB1.getText().toString();
                    etABx = etAB1;
                    params[3] = tvAB1.getText().toString();
                    tvABx = tvAB1;
                }
                break;
            case intButtonWriteABTag2:
                if (TextUtils.isEmpty(etAB2.getText()) || TextUtils.isEmpty(tvAB2.getText())){
                    myWriteTaskAB = null;
                    return;
                } else {
                    params[2] = etAB2.getText().toString();
                    etABx = etAB2;
                    params[3] = tvAB2.getText().toString();
                    tvABx = tvAB2;
                }
                break;
            case intButtonWriteABTag3:
                if (TextUtils.isEmpty(etAB3.getText()) || TextUtils.isEmpty(tvAB3.getText())){
                    myWriteTaskAB = null;
                    return;
                } else {
                    params[2] = etAB3.getText().toString();
                    etABx = etAB3;
                    params[3] = tvAB3.getText().toString();
                    tvABx = tvAB3;
                }
                break;
            case intButtonWriteABTag4:
                if (TextUtils.isEmpty(etAB4.getText()) || TextUtils.isEmpty(tvAB4.getText())){
                    myWriteTaskAB = null;
                    return;
                } else {
                    params[2] = etAB4.getText().toString();
                    etABx = etAB4;
                    params[3] = tvAB4.getText().toString();
                    tvABx = tvAB4;
                }
                break;
            case intButtonWriteABTag5:
                if (TextUtils.isEmpty(etAB5.getText()) || TextUtils.isEmpty(tvAB5.getText())){
                    myWriteTaskAB = null;
                    return;
                } else {
                    params[2] = etAB5.getText().toString();
                    etABx = etAB5;
                    params[3] = tvAB5.getText().toString();
                    tvABx = tvAB5;
                }
                break;
            case intButtonWriteABTag6:
                if (TextUtils.isEmpty(etAB6.getText()) || TextUtils.isEmpty(tvAB6.getText())){
                    myWriteTaskAB = null;
                    return;
                } else {
                    params[2] = etAB6.getText().toString();
                    etABx = etAB6;
                    params[3] = tvAB6.getText().toString();
                    tvABx = tvAB6;
                }
                break;
            case intButtonWriteABTag7:
                if (TextUtils.isEmpty(etAB7.getText()) || TextUtils.isEmpty(tvAB7.getText())){
                    myWriteTaskAB = null;
                    return;
                } else {
                    params[2] = etAB7.getText().toString();
                    etABx = etAB7;
                    params[3] = tvAB7.getText().toString();
                    tvABx = tvAB7;
                }
                break;
        }

        lblWriteMessage.setText(getResources().getStringArray(R.array.ab_tags_please_wait)[0]);

        // Disable access or clearing of the corresponding text boxes
        etABx.setInputType(InputType.TYPE_NULL);
        tvABx.setInputType(InputType.TYPE_NULL);

        btnWriteABCaller = (Button)v;
        btnWriteABCaller.setEnabled(false);
        btnWriteABCaller.setBackground(ContextCompat.getDrawable(this, android.R.drawable.button_onoff_indicator_off));

        myWriteTaskAB.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, params);

        // If this is the only tag then set the correct state of the Clear AB Tags button
        ButtonClearABTagsEnableDisable();

        Log.v(TAG, "AB Write Task is " + myWriteTaskAB.getStatus().toString());
    }

    public void sendMessageWriteMB(View v) {
        ((TextView)findViewById(R.id.labelWriteMessage)).setText("");

        if (myWriteTaskMB == null) {
            myWriteTaskMB = new AsyncWriteTaskModbus();
        } else {
            return;
        }

        String ipaddress, unitID, timeout;

        ipaddress = etMBIP.getText().toString();
        unitID = etMBUnitID.getText().toString();
        timeout = etTimeout.getText().toString();

        ipaddress = ipaddress.replace(" ", "");
        unitID = unitID.replace(" ", "");
        timeout = timeout.replace(" ", "");

        if (TextUtils.isEmpty(ipaddress) || Integer.parseInt(unitID) < 1 || Integer.parseInt(unitID) > 247 || !TextUtils.isDigitsOnly(timeout)){
            myWriteTaskMB = null;
            return;
        }

        String[] params = new String[4];

        params[0] = "gateway=" + ipaddress + "&path=" + unitID;
        params[1] = timeout;

        switch(v.getId()){
            case intButtonWriteMBTag1:
                if (TextUtils.isEmpty(etMB1.getText()) || TextUtils.isEmpty(tvMB1.getText())){
                    myWriteTaskMB = null;
                    return;
                } else {
                    params[2] = etMB1.getText().toString();
                    etMBx = etMB1;
                    params[3] = tvMB1.getText().toString();
                    tvMBx = tvMB1;
                }
                break;
            case intButtonWriteMBTag2:
                if (TextUtils.isEmpty(etMB2.getText()) || TextUtils.isEmpty(tvMB2.getText())){
                    myWriteTaskMB = null;
                    return;
                } else {
                    params[2] = etMB2.getText().toString();
                    etMBx = etMB2;
                    params[3] = tvMB2.getText().toString();
                    tvMBx = tvMB2;
                }
                break;
            case intButtonWriteMBTag3:
                if (TextUtils.isEmpty(etMB3.getText()) || TextUtils.isEmpty(tvMB3.getText())){
                    myWriteTaskMB = null;
                    return;
                } else {
                    params[2] = etMB3.getText().toString();
                    etMBx = etMB3;
                    params[3] = tvMB3.getText().toString();
                    tvMBx = tvMB3;
                }
                break;
            case intButtonWriteMBTag4:
                if (TextUtils.isEmpty(etMB4.getText()) || TextUtils.isEmpty(tvMB4.getText())){
                    myWriteTaskMB = null;
                    return;
                } else {
                    params[2] = etMB4.getText().toString();
                    etMBx = etMB4;
                    params[3] = tvMB4.getText().toString();
                    tvMBx = tvMB4;
                }
                break;
            case intButtonWriteMBTag5:
                if (TextUtils.isEmpty(etMB5.getText()) || TextUtils.isEmpty(tvMB5.getText())){
                    myWriteTaskMB = null;
                    return;
                } else {
                    params[2] = etMB5.getText().toString();
                    etMBx = etMB5;
                    params[3] = tvMB5.getText().toString();
                    tvMBx = tvMB5;
                }
                break;
            case intButtonWriteMBTag6:
                if (TextUtils.isEmpty(etMB6.getText()) || TextUtils.isEmpty(tvMB6.getText())){
                    myWriteTaskMB = null;
                    return;
                } else {
                    params[2] = etMB6.getText().toString();
                    etMBx = etMB6;
                    params[3] = tvMB6.getText().toString();
                    tvMBx = tvMB6;
                }
                break;
            case intButtonWriteMBTag7:
                if (TextUtils.isEmpty(etMB7.getText()) || TextUtils.isEmpty(tvMB7.getText())){
                    myWriteTaskMB = null;
                    return;
                } else {
                    params[2] = etMB7.getText().toString();
                    etMBx = etMB7;
                    params[3] = tvMB7.getText().toString();
                    tvMBx = tvMB7;
                }
                break;
        }

        lblWriteMessage.setText(getResources().getStringArray(R.array.ab_tags_please_wait)[0]);

        // Disable access or clearing of the corresponding text boxes
        etMBx.setInputType(InputType.TYPE_NULL);
        tvMBx.setInputType(InputType.TYPE_NULL);

        btnWriteMBCaller = (Button)v;
        btnWriteMBCaller.setEnabled(false);
        btnWriteMBCaller.setBackground(ContextCompat.getDrawable(this, android.R.drawable.button_onoff_indicator_off));

        myWriteTaskMB.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, params);

        // If this is the only tag then set the correct state of the Clear MB Tags button
        ButtonClearMBTagsEnableDisable();

        Log.v(TAG, "Modbus Write Task is " + myWriteTaskMB.getStatus().toString());
    }

    public void sendMessageClearABTags(View v) {
        clearingTags = true;

        if (!(etAB1.getInputType() == InputType.TYPE_NULL) && !etAB1.getText().toString().equals("")){
            etAB1.setText("");
            tvAB1.setText("");
            tvAB1.setEnabled(false);
            btnWriteAB1.setEnabled(false);
            btnWriteAB1.setBackground(ContextCompat.getDrawable(this, android.R.drawable.button_onoff_indicator_off));
        }

        if (!(etAB2.getInputType() == InputType.TYPE_NULL) && !etAB2.getText().toString().equals("")){
            etAB2.setText("");
            tvAB2.setText("");
            tvAB2.setEnabled(false);
            btnWriteAB2.setEnabled(false);
            btnWriteAB2.setBackground(ContextCompat.getDrawable(this, android.R.drawable.button_onoff_indicator_off));
        }

        if (!(etAB3.getInputType() == InputType.TYPE_NULL) && !etAB3.getText().toString().equals("")){
            etAB3.setText("");
            tvAB3.setText("");
            tvAB3.setEnabled(false);
            btnWriteAB3.setEnabled(false);
            btnWriteAB3.setBackground(ContextCompat.getDrawable(this, android.R.drawable.button_onoff_indicator_off));
        }

        if (!(etAB4.getInputType() == InputType.TYPE_NULL) && !etAB4.getText().toString().equals("")){
            etAB4.setText("");
            tvAB4.setText("");
            tvAB4.setEnabled(false);
            btnWriteAB4.setEnabled(false);
            btnWriteAB4.setBackground(ContextCompat.getDrawable(this, android.R.drawable.button_onoff_indicator_off));
        }

        if (!(etAB5.getInputType() == InputType.TYPE_NULL) && !etAB5.getText().toString().equals("")){
            etAB5.setText("");
            tvAB5.setText("");
            tvAB5.setEnabled(false);
            btnWriteAB5.setEnabled(false);
            btnWriteAB5.setBackground(ContextCompat.getDrawable(this, android.R.drawable.button_onoff_indicator_off));
        }

        if (!(etAB6.getInputType() == InputType.TYPE_NULL) && !etAB6.getText().toString().equals("")){
            etAB6.setText("");
            tvAB6.setText("");
            tvAB6.setEnabled(false);
            btnWriteAB6.setEnabled(false);
            btnWriteAB6.setBackground(ContextCompat.getDrawable(this, android.R.drawable.button_onoff_indicator_off));
        }

        if (!(etAB7.getInputType() == InputType.TYPE_NULL) && !etAB7.getText().toString().equals("")){
            etAB7.setText("");
            tvAB7.setText("");
            tvAB7.setEnabled(false);
            btnWriteAB7.setEnabled(false);
            btnWriteAB7.setBackground(ContextCompat.getDrawable(this, android.R.drawable.button_onoff_indicator_off));
        }

        if (!(etABGauge.getInputType() == InputType.TYPE_NULL) && !etABGauge.getText().toString().equals("")){
            etABGauge.setText("");
            btnABGauge.setEnabled(false);
            btnABGauge.setBackground(ContextCompat.getDrawable(this, android.R.drawable.button_onoff_indicator_off));
        }

        ButtonClearABTagsEnableDisable();

        clearingTags = false;
    }

    public void sendMessageClearMBTags(View v) {
        clearingTags = true;

        if (!(etMB1.getInputType() == InputType.TYPE_NULL) && !etMB1.getText().toString().equals("")){
            etMB1.setText("");
            tvMB1.setText("");
            tvMB1.setEnabled(false);
            btnWriteMB1.setEnabled(false);
            btnWriteMB1.setBackground(ContextCompat.getDrawable(this, android.R.drawable.button_onoff_indicator_off));
        }

        if (!(etMB2.getInputType() == InputType.TYPE_NULL) && !etMB2.getText().toString().equals("")){
            etMB2.setText("");
            tvMB2.setText("");
            tvMB2.setEnabled(false);
            btnWriteMB2.setEnabled(false);
            btnWriteMB2.setBackground(ContextCompat.getDrawable(this, android.R.drawable.button_onoff_indicator_off));
        }

        if (!(etMB3.getInputType() == InputType.TYPE_NULL) && !etMB3.getText().toString().equals("")){
            etMB3.setText("");
            tvMB3.setText("");
            tvMB3.setEnabled(false);
            btnWriteMB3.setEnabled(false);
            btnWriteMB3.setBackground(ContextCompat.getDrawable(this, android.R.drawable.button_onoff_indicator_off));
        }

        if (!(etMB4.getInputType() == InputType.TYPE_NULL) && !etMB4.getText().toString().equals("")){
            etMB4.setText("");
            tvMB4.setText("");
            tvMB4.setEnabled(false);
            btnWriteMB4.setEnabled(false);
            btnWriteMB4.setBackground(ContextCompat.getDrawable(this, android.R.drawable.button_onoff_indicator_off));
        }

        if (!(etMB5.getInputType() == InputType.TYPE_NULL) && !etMB5.getText().toString().equals("")){
            etMB5.setText("");
            tvMB5.setText("");
            tvMB5.setEnabled(false);
            btnWriteMB5.setEnabled(false);
            btnWriteMB5.setBackground(ContextCompat.getDrawable(this, android.R.drawable.button_onoff_indicator_off));
        }

        if (!(etMB6.getInputType() == InputType.TYPE_NULL) && !etMB6.getText().toString().equals("")){
            etMB6.setText("");
            tvMB6.setText("");
            tvMB6.setEnabled(false);
            btnWriteMB6.setEnabled(false);
            btnWriteMB6.setBackground(ContextCompat.getDrawable(this, android.R.drawable.button_onoff_indicator_off));
        }

        if (!(etMB7.getInputType() == InputType.TYPE_NULL) && !etMB7.getText().toString().equals("")){
            etMB7.setText("");
            tvMB7.setText("");
            tvMB7.setEnabled(false);
            btnWriteMB7.setEnabled(false);
            btnWriteMB7.setBackground(ContextCompat.getDrawable(this, android.R.drawable.button_onoff_indicator_off));
        }

        if (!(etMBGauge.getInputType() == InputType.TYPE_NULL) && !etMBGauge.getText().toString().equals("")){
            etMBGauge.setText("");
            btnMBGauge.setEnabled(false);
            btnMBGauge.setBackground(ContextCompat.getDrawable(this, android.R.drawable.button_onoff_indicator_off));
        }

        ButtonClearMBTagsEnableDisable();

        clearingTags = false;
    }

    public void sendMessagePopUpAddressAB(View v)
    {
        if (!popupScreenABOpen){
            popupScreenABOpen = true;

            callerName = getResources().getResourceEntryName(v.getId());
            cpu = spinABCPU.getSelectedItem().toString();

            Intent intent = new Intent(MainActivity.this, PopUpAddressAB.class);
            startActivity(intent);
        }
    }

    public void sendMessagePopUpAddressMB(View v)
    {
        if (!popupScreenMBOpen){
            popupScreenMBOpen = true;

            callerName = getResources().getResourceEntryName(v.getId());

            Intent intent = new Intent(MainActivity.this, PopUpAddressMB.class);
            startActivity(intent);
        }
    }

    public void sendMessageGaugeAB(View v)
    {
        if (!gaugeScreenABOpen){
            gaugeScreenABOpen = true;

            abCPU = spinABCPU.getSelectedItem().toString();
            abIPAddress = etABIP.getText().toString();
            abPath = etABPath.getText().toString();
            timeout = etTimeout.getText().toString();
            abGaugeAddress = etABGauge.getText().toString();

            Intent intent = new Intent(MainActivity.this, GaugeActivityAB.class);
            startActivity(intent);
        }
    }

    public void sendMessageGaugeMB(View v)
    {
        if (!gaugeScreenMBOpen){
            gaugeScreenMBOpen = true;

            mbIPAddress = etMBIP.getText().toString();
            mbUnitID = etMBUnitID.getText().toString();
            timeout = etTimeout.getText().toString();
            mbGaugeAddress = etMBGauge.getText().toString();

            Intent intent = new Intent(MainActivity.this, GaugeActivityMB.class);
            startActivity(intent);
        }
    }

    public void sendMessageScreenClean(View v)
    {
        Intent intent = new Intent(MainActivity.this, ScreenClean.class);
        startActivity(intent);
    }

    public void sendMessageCheckBoxSwapBytesWords(View v)
    {
        CheckBox cb = (CheckBox)v;

        if (cb.isChecked()){
            if (cb.getText().equals("Swap Bytes")){
                cbSwapBytesChecked = true;
            } else {
                cbSwapWordsChecked = true;
            }
        } else {
            if (cb.getText().equals("Swap Bytes")){
                cbSwapBytesChecked = false;
            } else {
                cbSwapWordsChecked = false;
            }
        }
    }

    @Override
    public void UpdateABUI(String callerID, String value) {
        TextView tv = findViewById(getResources().getIdentifier(callerID, "id", getPackageName()));

        if (value.startsWith("err") || value.equals("pending")){
            tv.setTextColor(Color.RED);
        } else {
            tv.setTextColor(textColor);
        }
        tv.setText(value);
    }

    @Override
    public void UpdateMBUI(String callerID, String value) {
        TextView tv = findViewById(getResources().getIdentifier(callerID, "id", getPackageName()));

        if (value.startsWith("err") || value.equals("pending")){
            tv.setTextColor(Color.RED);
        } else {
            tv.setTextColor(textColor);
        }
        tv.setText(value);
    }

    @Override
    public void UpdateGetCLGXTagsUI(List<String> values) {
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, values);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dataAdapter.notifyDataSetChanged();
        spinCLGXTags.setAdapter(dataAdapter);

        if (myTaskGetCLGXTags != null){
            myTaskGetCLGXTags.cancel(true);
            myTaskGetCLGXTags = null;
        }

        btnGetCLGXTags.setEnabled(true);
        btnGetCLGXTags.setBackground(ContextCompat.getDrawable(this, android.R.drawable.button_onoff_indicator_on));

        getCLGXTagsRunning = false;
    }

    @Override
    public void UpdateTags(String callerId, String value) {
        ((EditText)findViewById(getResources().getIdentifier(callerId, "id", getPackageName()))).setText(value);

        switch (callerId){
            case "etABTag1":
                callerName = "tvABTagValue1";
                tvAB1.setText("");
                break;
            case "etABTag2":
                callerName = "tvABTagValue2";
                tvAB2.setText("");
                break;
            case "etABTag3":
                callerName = "tvABTagValue3";
                tvAB3.setText("");
                break;
            case "etABTag4":
                callerName = "tvABTagValue4";
                tvAB4.setText("");
                break;
            case "etABTag5":
                callerName = "tvABTagValue5";
                tvAB5.setText("");
                break;
            case "etABTag6":
                callerName = "tvABTagValue6";
                tvAB6.setText("");
                break;
            case "etABTag7":
                callerName = "tvABTagValue7";
                tvAB7.setText("");
                break;
            case "etABTagGauge":
                ButtonClearABTagsEnableDisable();
                break;
            case "etMBTag1":
                callerName = "tvMBTagValue1";
                tvMB1.setText("");
                break;
            case "etMBTag2":
                callerName = "tvMBTagValue2";
                tvMB2.setText("");
                break;
            case "etMBTag3":
                callerName = "tvMBTagValue3";
                tvMB3.setText("");
                break;
            case "etMBTag4":
                callerName = "tvMBTagValue4";
                tvMB4.setText("");
                break;
            case "etMBTag5":
                callerName = "tvMBTagValue5";
                tvMB5.setText("");
                break;
            case "etMBTag6":
                callerName = "tvMBTagValue6";
                tvMB6.setText("");
                break;
            case "etMBTag7":
                callerName = "tvMBTagValue7";
                tvMB7.setText("");
                break;
            case "etMBTagGauge":
                ButtonClearMBTagsEnableDisable();
                break;
        }
    }

    @Override
    public void WriteUpdateUI(String value) {
        if (value.startsWith("AB")){
            if (myWriteTaskAB != null){
                myWriteTaskAB.cancel(true);
                myWriteTaskAB = null;
            }

            // Enable access to corresponding text boxes
            etABx.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
            tvABx.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);

            btnWriteABCaller.setEnabled(true);
            btnWriteABCaller.setBackground(ContextCompat.getDrawable(this, android.R.drawable.button_onoff_indicator_on));

            ButtonClearABTagsEnableDisable();
        } else {
            if (myWriteTaskMB != null){
                myWriteTaskMB.cancel(true);
                myWriteTaskMB = null;
            }

            // Enable access to corresponding text boxes
            etMBx.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
            tvMBx.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);

            btnWriteMBCaller.setEnabled(true);
            btnWriteMBCaller.setBackground(ContextCompat.getDrawable(this, android.R.drawable.button_onoff_indicator_on));

            ButtonClearMBTagsEnableDisable();
        }

        lblWriteMessage.setText(value.substring(3));
    }
}
