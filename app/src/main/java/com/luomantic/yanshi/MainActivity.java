package com.luomantic.yanshi;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.method.DigitsKeyListener;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.RegexUtils;
import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.ScreenUtils;
import com.blankj.utilcode.util.StringUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.bumptech.glide.Glide;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.google.gson.Gson;
import com.luomantic.yanshi.adapter.SendWifiAdapter;
import com.luomantic.yanshi.adapter.WifiAdapter;
import com.luomantic.yanshi.bean.CardBean;
import com.luomantic.yanshi.bean.Program;
import com.luomantic.yanshi.net.lgsv_interface;
import com.luomantic.yanshi.net.makepacket;
import com.luomantic.yanshi.wifi.WIFIConManager;
import com.luomantic.yanshi.wifi.WIFIStaReceiver;
import com.zhihu.matisse.Matisse;
import com.zhihu.matisse.MimeType;
import com.zhihu.matisse.engine.impl.GlideEngine;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTextChanged;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

import static android.net.wifi.WifiManager.NETWORK_STATE_CHANGED_ACTION;
import static android.net.wifi.WifiManager.SCAN_RESULTS_AVAILABLE_ACTION;
import static android.net.wifi.WifiManager.WIFI_STATE_CHANGED_ACTION;
import static com.luomantic.yanshi.app.Constant.wifi_username;

public class MainActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks,
        WIFIStaReceiver.WIFIStaListener, makepacket.ConnectStatusListener {

    private static final int REQUEST_CODE_CHOOSE_PHOTO_LEFT = 10;
    private static final int REQUEST_CODE_CHOOSE_PHOTO_RIGHT = 11;

    @BindView(R.id.img_left_show)
    ImageView imgLeftShow;

    @BindView(R.id.tv_name_show)
    TextView tvNameShow;

    @BindView(R.id.tv_info_show)
    TextView tvInfoShow;

    @BindView(R.id.img_right_show)
    ImageView imgRightShow;

    @BindView(R.id.et_name)
    EditText etName;

    @BindView(R.id.et_info)
    EditText etInfo;

    @BindView(R.id.bt_send)
    Button btSend;

    @BindView(R.id.bt_set)
    Button btSet;

    @BindView(R.id.rvWifi)
    RecyclerView rvWifi;

    private String[] permissions = {
            Manifest.permission.INTERNET,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.CHANGE_NETWORK_STATE,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.CHANGE_WIFI_STATE,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.CHANGE_WIFI_MULTICAST_STATE,
    };

    @SuppressLint("StaticFieldLeak")
    public static MainActivity mainActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ScreenUtils.setFullScreen(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        if (EasyPermissions.hasPermissions(this, permissions)) {
            initApp();
        } else {
            EasyPermissions.requestPermissions(this, "请求必要的权限,拒绝权限可能会无法使用app", 0, permissions);
        }
    }

    public List<CardBean> wifiList; // 接收到的wifi列表
    private List<CardBean> sendList; // 要发送的wifi列表
    private lgsv_interface lgsvInterface;
    public WifiAdapter adapter; // 接收到的卡号列表

    private void initApp() {
        mainActivity = this;
        lgsvInterface = new lgsv_interface();

        WifiManager manager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiManager.MulticastLock lock = manager.createMulticastLock("test wifi");

//        lgsvInterface.StartServer(new callbackserver(), lock);
        lgsvInterface.StartServer(this, lock);

        registerBroadcast();
        registerScanTask();

        rvWifi.setLayoutManager(new LinearLayoutManager(this));
        rvWifi.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        wifiList = new ArrayList<>();
        sendList = new ArrayList<>();

        adapter = new WifiAdapter(R.layout.item_wifi, wifiList);
        rvWifi.setAdapter(adapter);

        adapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                if (sendList.contains(wifiList.get(position))) {
                    sendList.remove(wifiList.get(position));
                    view.setBackgroundColor(Color.parseColor("#ffffff"));
                } else {
                    sendList.add(wifiList.get(position));
                    view.setBackgroundColor(getResources().getColor(R.color.commonBlue));
                }
            }
        });

        scannedWifiList = new ArrayList<>();
        tempList = new ArrayList<>();
        sendWifiAdapter = new SendWifiAdapter(R.layout.item_wifi_send, scannedWifiList);

        ipList = new ArrayList<>();
        cardList = new ArrayList<>();
    }

    private WIFIStaReceiver receiver; // 接收Wifi状态广播

    private void registerBroadcast() {
        if (receiver == null) {
            receiver = new WIFIStaReceiver(this);
            IntentFilter filter = new IntentFilter();
            filter.addAction(WIFI_STATE_CHANGED_ACTION);
            filter.addAction(NETWORK_STATE_CHANGED_ACTION);
            filter.addAction(SCAN_RESULTS_AVAILABLE_ACTION);
            registerReceiver(receiver, filter);
        }
    }

    private void unregisterBroadcast() {
        if (receiver != null) {
            unregisterReceiver(receiver);
            receiver = null;
        }
    }

    private Timer timer;

    /**
     * 注册自动扫描任务
     */
    private void registerScanTask() {
        if (timer == null) {
            timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    if (tempList != null && tempList.size() > 0) {
                        if (scannedWifiList != null) {
                            scannedWifiList.clear();
                            scannedWifiList.addAll(tempList);
                        }
                        tempList.clear();
                    }
                    Message message = handler.obtainMessage();
                    message.what = 0;
                    handler.sendMessage(message);
                    WIFIConManager.getInstance(MainActivity.this).getWifiManager().startScan();
                }
            }, 0, 12 * 1000);
        }
    }

    private void unregisterScanTask() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    @OnTextChanged(value = R.id.et_info, callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    public void etInfoTextChangedAfter(Editable editable) {
        tvInfoShow.setText(editable.toString());
    }

    @OnTextChanged(value = R.id.et_name, callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    public void etNameTextChangedAfter(Editable editable) {
        tvNameShow.setText(editable.toString());
    }

    @OnClick({R.id.img_left_show, R.id.img_right_show, R.id.bt_send, R.id.bt_set})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.img_left_show:
                selectPic(REQUEST_CODE_CHOOSE_PHOTO_LEFT);
                break;
            case R.id.img_right_show:
                selectPic(REQUEST_CODE_CHOOSE_PHOTO_RIGHT);
                break;
            case R.id.bt_send:
                sendProgram();
                break;
            case R.id.bt_set:
                showWifiSelectedDialog();
                break;
        }
    }

    private void sendWifi2Led() {
        if (null == wifi_username) {
            ToastUtils.showShort("请选择要发送的wifi");
        } else {
            final String username = wifi_username;
            final String password = etPassword.getText().toString();

            if (sendList.size() == 0) {
                ToastUtils.showShort("请选择Led后发送");
                return;
            }

            ToastUtils.showShort("发送wifi信息啦");

            executor.execute(new Runnable() {
                @Override
                public void run() {
                    for (int i = 0; i < sendList.size(); i++) {
                        int ret = lgsvInterface.SetHotInfo(cardList.get(i), ipList.get(i), username, password);
                        ToastUtils.showLong(ret);
                    }
                }
            });
        }
    }

    private List<String> scannedWifiList;
    private SendWifiAdapter sendWifiAdapter;
    private List<String> tempList; // 临时保存扫描到的wifi
    private RecyclerView rvSendWifi;
    private TextView tvWait;
    private EditText etPassword;

    private AlertDialog dialog;

    private void showWifiSelectedDialog() {
        for (int i = 0; i < sendList.size(); i++) {
            ipList.add(sendList.get(i).getIpAddress());
            cardList.add(sendList.get(i).getCardNum());
        }

        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        @SuppressLint("InflateParams")
        View view = inflater.inflate(R.layout.wifi_list_layout, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(view);
        dialog = builder.create();
        dialog.setCancelable(false);

        rvSendWifi = view.findViewById(R.id.rv_send_wifi);
        tvWait = view.findViewById(R.id.tv_wait);
        etPassword = view.findViewById(R.id.et_password);
        Button btSendLed = view.findViewById(R.id.bt_send_led);
        Button btSendLedCancel = view.findViewById(R.id.bt_send_led_cancel);
        btSendLed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendWifi2Led();
            }
        });
        btSendLedCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
            }
        });
        //noinspection NullableProblems
        etPassword.setKeyListener(new DigitsKeyListener() {
            @Override
            public int getInputType() {
                return InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_NORMAL;
            }

            @Override
            protected char[] getAcceptedChars() {
                return "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
            }
        });

        etPassword.setText(SPUtils.getInstance().getString("password", "12345"));
        etPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                SPUtils.getInstance().put("password", s.toString());
            }
        });

        rvSendWifi.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        rvSendWifi.setLayoutManager(new LinearLayoutManager(this));

        rvSendWifi.setAdapter(sendWifiAdapter);
        sendWifiAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                wifi_username = scannedWifiList.get(position);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        sendWifiAdapter.notifyDataSetChanged();
                    }
                });
            }
        });
        if (scannedWifiList == null || scannedWifiList.size() < 1) {
            rvSendWifi.setVisibility(View.GONE);
            tvWait.setVisibility(View.VISIBLE);
        }
        dialog.show();
    }

    private void selectPic(int requestCode) {
        Matisse.from(this)
                .choose(MimeType.of(MimeType.JPEG, MimeType.PNG))
                .countable(true)
                .maxSelectable(1)
                .gridExpectedSize(this.getResources().getDimensionPixelSize(R.dimen.grid_expected_size))
                .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
                .thumbnailScale(0.85f)
                .imageEngine(new GlideEngine())
                .forResult(requestCode);
    }

    private void sendProgram() {
        if (StringUtils.isEmpty(tvNameShow.getText().toString())) {
            ToastUtils.showShort("公司名字不能为空");
            return;
        }
        if (StringUtils.isEmpty(tvInfoShow.getText().toString())) {
            ToastUtils.showShort("描述信息不能为空");
            return;
        }
        if (sendList.size() == 0) {
            ToastUtils.showShort("请选择Led后发送");
            return;
        }

        ToastUtils.showShort("发送节目啦");
        startSend();
    }

    Executor executor = Executors.newCachedThreadPool();
    private List<String> ipList;
    private List<String> cardList;

    private void startSend() {
        for (int i = 0; i < sendList.size(); i++) {
            ipList.add(sendList.get(i).getIpAddress());
            cardList.add(sendList.get(i).getCardNum());
        }

        final Program program = new Program();
        program.setPlayCount(0);

        List<Program.TextAreaBean> textAreaBeanList = new ArrayList<>();
        Program.TextAreaBean textAreaBean = new Program.TextAreaBean();
        textAreaBean.setMsgInfo(tvNameShow.getText().toString());
        textAreaBean.setnX(32);
        textAreaBean.setnY(0);
        textAreaBean.setnW(128);
        textAreaBean.setnH(16);
        textAreaBean.setFontSize(1);
        textAreaBean.setInType(1);
        textAreaBean.setInSpeed(16);
        textAreaBean.setStayTime(200);
        Program.TextAreaBean textAreaBean1 = new Program.TextAreaBean();
        textAreaBean1.setnX(32);
        textAreaBean1.setnY(16);
        textAreaBean1.setnW(128);
        textAreaBean1.setnH(16);
        textAreaBean1.setFontSize(1);
        textAreaBean1.setInType(2);
        textAreaBean1.setInSpeed(4);
        textAreaBean1.setStayTime(1);
        textAreaBean1.setMsgInfo(tvInfoShow.getText().toString());
        textAreaBeanList.add(textAreaBean);
        textAreaBeanList.add(textAreaBean1);
        program.setTextArea(textAreaBeanList);

        List<Program.PicAreaBean> picAreaBeanList = new ArrayList<>();
        Program.PicAreaBean picAreaBean = new Program.PicAreaBean();  // 左图片
        picAreaBean.setNH(32);
        picAreaBean.setNX(160);
        picAreaBean.setNY(0);
        picAreaBean.setNW(32);
        picAreaBean.setInType(1);
        picAreaBean.setStayTime(200);
        picAreaBean.setFilePath(leftImgPath);
        Program.PicAreaBean picAreaBean1 = new Program.PicAreaBean(); // 右图片
        picAreaBean1.setNX(0);
        picAreaBean1.setNY(0);
        picAreaBean1.setNH(32);
        picAreaBean1.setNW(32);
        picAreaBean1.setInType(1);
        picAreaBean1.setStayTime(1);
        picAreaBean1.setFilePath(rightImgPath);
        picAreaBeanList.add(picAreaBean);
        picAreaBeanList.add(picAreaBean1);
        program.setPicArea(picAreaBeanList);

        program.setPlayCount(255);

        executor.execute(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < sendList.size(); i++) {
                    LogUtils.e(new Gson().toJson(program));
                    // 发送节目
                    int ret = lgsvInterface.SendShowInfoByJson(cardList.get(i), ipList.get(i), new Gson().toJson(program));
                    ToastUtils.showLong(ret);
                }
            }
        });
    }

    private String leftImgPath, rightImgPath;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null) {
            LogUtils.e(Matisse.obtainPathResult(data));
        }
        if (resultCode == RESULT_OK && null != data) {
            if (requestCode == REQUEST_CODE_CHOOSE_PHOTO_LEFT) {
                leftImgPath = Matisse.obtainPathResult(data).get(0);
                Glide.with(this).load(new File(leftImgPath)).into(imgLeftShow);
            } else if (requestCode == REQUEST_CODE_CHOOSE_PHOTO_RIGHT) {
                rightImgPath = Matisse.obtainPathResult(data).get(0);
                Glide.with(this).load(new File(rightImgPath)).into(imgRightShow);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        initApp();
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) { // 拒绝权限且点击了不再提示
            new AppSettingsDialog.Builder(this).build().show(); // 跳转应用设置页面
        } else {
            //noinspection ToArrayCallWithZeroLengthArrayArgument
            EasyPermissions.requestPermissions(this, "请求必要的权限,拒绝权限可能会无法使用app", 1, perms.toArray(new String[perms.size()]));
        }
    }

    @Override
    public void onWifiConnected() {

    }

    @Override
    public void onWifiDisconnected() {

    }

    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if (msg.what == 0) {
                if (scannedWifiList != null && scannedWifiList.size() > 0) {
                    if (tvWait != null && rvSendWifi != null) {
                        tvWait.setVisibility(View.GONE);
                        rvSendWifi.setVisibility(View.VISIBLE);
                    }
                }
                sendWifiAdapter.notifyDataSetChanged();
            }
            return false;
        }
    });

    @Override
    public void onWifiScanResultBack(String ssId) {
        if (!RegexUtils.isZh(ssId)) {
            tempList.add(ssId);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (isFinishing()) {
            unregisterBroadcast();
            unregisterScanTask();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterBroadcast();
        unregisterScanTask();
    }

    @Override
    public void showCardInfo(String cardNum, String ip, int online) { // 显示卡号的回调
        LogUtils.e(cardNum + "  " + ip + "  " + online);
        boolean isNewCard = false;

        for (int i = 0; i < wifiList.size(); i++) {
            if (wifiList.get(i).getCardNum().equals(cardNum)) {
                isNewCard = true;
                wifiList.get(i).setOnline(online);
            }
        }

        if (!isNewCard) {
            wifiList.add(new CardBean(cardNum, ip, online));
        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adapter.notifyDataSetChanged();
            }
        });
    }
}
