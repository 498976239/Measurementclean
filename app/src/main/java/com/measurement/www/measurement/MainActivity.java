package com.measurement.www.measurement;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.bean.entity.Bean;
import com.measurement.www.measurement.dbmanager.CommonUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    //intent需要的code
    private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
    private static final int REQUEST_CONNECT_DEVICE_INSECURE = 2;
    private static final int REQUEST_ENABLE_BT = 3;
    private static final int REQUEST_QUERY = 4;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothChatService mChatService;
    private List<Bean> mList;
    private Toolbar toolbar;
    private int temp;
    private byte[] compare;
    private DrawerLayout mDrawer;
    private NavigationView mNavigationView;
    private Button mButton;
    private CommonUtils mCommonUtils;
    private List<TextView> mListTextView;
    private String mConnectedDeviceName;
    private ProgressBar pb;
    private TextView tv;
    private SimpleDateFormat mSimpleDateFormat;
    private TextView mChannel1_1, mChannel1_2, mChannel1_3, mChannel2_1, mChannel2_2, mChannel2_3, mChannel3_1, mChannel3_2, mChannel3_3,
            mChannel4_1, mChannel4_2, mChannel4_3, mChannel5_1, mChannel5_2, mChannel5_3, mChannel6_1, mChannel6_2, mChannel6_3,
            mChannel7_1, mChannel7_2, mChannel7_3, mChannel8_1, mChannel8_2, mChannel8_3, mChannel9_1, mChannel9_2, mChannel9_3,
            mChannel10_1, mChannel10_2, mChannel10_3, mChannel11_1, mChannel11_2, mChannel11_3, mChannel12_1, mChannel12_2, mChannel12_3,
            mChannel13_1, mChannel13_2, mChannel13_3, mChannel14_1, mChannel14_2, mChannel14_3, mChannel15_1, mChannel15_2, mChannel15_3,
            mChannel16_1, mChannel16_2, mChannel16_3, mChannel17_1, mChannel17_2, mChannel17_3, mChannel18_1, mChannel18_2, mChannel18_3,
            mChannel19_1, mChannel19_2, mChannel19_3, mChannel20_1, mChannel20_2, mChannel20_3, mChannel21_1, mChannel21_2, mChannel21_3,
            mChannel22_1, mChannel22_2, mChannel22_3, mChannel23_1, mChannel23_2, mChannel23_3, mChannel24_1, mChannel24_2, mChannel24_3,
            mChannel25_1, mChannel25_2, mChannel25_3, mChannel26_1, mChannel26_2, mChannel26_3, mChannel27_1, mChannel27_2, mChannel27_3,
            mChannel28_1, mChannel28_2, mChannel28_3, mChannel29_1, mChannel29_2, mChannel29_3, mChannel30_1, mChannel30_2, mChannel30_3,
            mChannel31_1, mChannel31_2, mChannel31_3, mChannel32_1, mChannel32_2, mChannel32_3, mChannel33_1, mChannel33_2, mChannel33_3,
            mChannel34_1, mChannel34_2, mChannel34_3, mChannel35_1, mChannel35_2, mChannel35_3, mChannel36_1, mChannel36_2, mChannel36_3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mList = new ArrayList<>();//用来存放要存储的Bean对象
        mListTextView = new ArrayList<>();
        mCommonUtils = new CommonUtils(this);//操作数据库的工具类
        initData();//初始化控件
        //发送请求上一条数据
            mButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    byte[] b = {0x01,(byte)0xa3,0xE,0xE,0,0x8};
                    byte[] orderCRC =  intToByteArray(getCrc16(b));
                    byte[] end = {0x01,(byte)0xa3,0xE,0xE,0,0x8,orderCRC[2],orderCRC[3]};
                    sendMessage(end);
                    mButton.setEnabled(false);
                    mButton.setVisibility(View.GONE);
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mButton.setEnabled(true);
                            mButton.setVisibility(View.VISIBLE);
                        }
                    },2000);
                }
            });
        //获取本地蓝牙设备
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (mChatService == null) {
            //否则建立聊天会话
            setupChat();
        }

        //toolbar设置
        toolbar.setTitle("数据采集");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);//该条语句要放在setSupportActionBar(toolbar)后
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, mDrawer, toolbar, 0, 0);//显示左侧图标
        mDrawer.addDrawerListener(toggle);
        toggle.syncState();
        mNavigationView.setNavigationItemSelectedListener(this);
        //注册广播，监听蓝牙连接状况,与远程设备断开连接时发送的广播
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        this.registerReceiver(myReceiver, filter);
    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mChatService != null) {
            mChatService.stop();
        }
        this.unregisterReceiver(myReceiver);//将已经注册的广播解除
    }

    private long firstTime;//用来定义再按一次退出的变量

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        if (mDrawer.isDrawerOpen(mNavigationView)) {
            mDrawer.closeDrawer(GravityCompat.START);
        } else {
            if (firstTime + 2000 > System.currentTimeMillis()) {
                super.onBackPressed();
                System.exit(0);
            } else {
                Toast.makeText(this, "再按一次退出", Toast.LENGTH_SHORT).show();
            }
            firstTime = System.currentTimeMillis();
        }
    }



    @Override
        protected void onResume() {
            super.onResume();
        /*如果蓝牙没有被启用，那么我们确保
        * 在onResume()方法里mChatService被启用*/
            if(mChatService != null&&mBluetoothAdapter.isEnabled()){
                if(mChatService.getState() == BluetoothChatService.STATE_NONE){
                    mChatService.start();
                }
            }
        }

        private void setupChat() {
            //初始化BluetoothChatService进行蓝牙连接
            mChatService = new BluetoothChatService(this,mHandler);
        }
        /*从BluetoothChatService获得信息
           * */
        private Handler mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case Constants.MESSAGE_STATE_CHANGE:
                        switch (msg.arg1) {
                            case BluetoothChatService.STATE_CONNECTED:
                                Log.i("main--connected---",mChatService.getState()+"");
                                setToolbar("与"+mConnectedDeviceName+"连接成功");
                                break;
                            case BluetoothChatService.STATE_CONNECTING:
                                setToolbar("正在连接...");
                                Log.i("main--connecting---",mChatService.getState()+"");
                                break;
                            case BluetoothChatService.STATE_LISTEN:
                            case BluetoothChatService.STATE_NONE:
                                setToolbar("没有连接设备");
                                Log.i("main-----",mChatService.getState()+"");
                                break;
                        }
                        break;
                    case Constants.MESSAGE_READ:
                        pb.setVisibility(View.GONE);
                        byte[] readBuf = (byte[]) msg.obj;
                        byte[] result = new byte[msg.arg1-2];//除去校验位的数据
                        byte[] result_temp = new byte[msg.arg1];
                        System.arraycopy(readBuf,0,result_temp,0,msg.arg1);
                        byte[] crc16 = new byte[2];
                        temp = msg.arg1;
                        System.arraycopy(readBuf,msg.arg1-2,crc16,0,2);//获取字节数组的CRC校验码，最后两个字节
                        /*for (int i = 0; i < crc16.length; i++) {
                            Log.i("main--getCRC",Integer.toHexString(crc16[i]&0xff));
                        }*/
                        System.arraycopy(readBuf,0,result,0,msg.arg1-2);//获取除去CRC校验码之后的数据信息
                        byte[] myCRC =  intToByteArray(getCrc16(result));//生成CRC校验码,和获取的刚好相反
                      /* for (int i = 0; i < myCRC.length; i++) {
                            Log.i("main--myCRC",Integer.toHexString(myCRC[i]&0xff));
                        }*/
                        if(myCRC[3]==crc16[0]&&myCRC[2]==crc16[1]){
                            boolean equals = Arrays.equals(result, compare);//判断两个数组是否相等
                            if(!equals){
                                byteToFloat2(result);
                            }else {
                                Toast.makeText(MainActivity.this,"数据相同,请更新后采集",Toast.LENGTH_SHORT).show();
                            }
                        }else {
                            Toast.makeText(MainActivity.this,"校验未通过，重新请求",Toast.LENGTH_SHORT).show();
                        }
                        for (int i = 0; i < result_temp.length; i++) {
                            Log.i("main-output",i+":"+Integer.toHexString(result_temp[i]&0xff));
                        }

//                        Log.i("MainActivity-比较compare",compare.length+"");
  //                      Log.i("MainActivity-比较temp",result.length+"");
                          //Log.i("MainActivity--数据长度--",msg.arg1+"");
                      //  Log.i("MainActivity--字节数",msg.arg1+"");
                       // Log.i("MainActivity-前后比较",equals+"");


                    break;
                case Constants.MESSAGE_DEVICE_NAME:
                    mConnectedDeviceName = msg.getData().getString(Constants.DEVICE_NAME);
                        Toast.makeText(MainActivity.this,"连接到："+mConnectedDeviceName,Toast.LENGTH_SHORT).show();
                        pb.setVisibility(View.GONE);

                    break;
                case Constants.MESSAGE_TOAST:
                        Toast.makeText(MainActivity.this,msg.getData().getString(Constants.TOAST),Toast.LENGTH_SHORT).show();
                        pb.setVisibility(View.GONE);
                    break;
            }
        }
    };

    /**
     * @param str  要设置的状态
     */
        public void setToolbar(String str){
            toolbar.setSubtitle(str);
         }

    /**
     * @param s  要发送的数组
     */
        private void sendMessage(byte[] s) {
            if(mBluetoothAdapter.isEnabled()){
                //操作之前检查是否已经连接
                if(mChatService.getState() != BluetoothChatService.STATE_CONNECTED){
                    Toast.makeText(this,R.string.not_connected,Toast.LENGTH_SHORT).show();
                    return;
                }else {
                    //检查需要发送内容的长度
                    if(s.length > 0){
                        //获取内容并且告诉BluetoothChatService可以write了
                        temp = 0;
                        pb.setVisibility(View.VISIBLE);
                        mChatService.write(s);
                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                notice();
                            }
                        },2000);
                    }
                }
            }else {
                Toast.makeText(this,"蓝牙没有开启",Toast.LENGTH_SHORT).show();
            }

        }
    private void notice(){
        if(temp<=0){
            pb.setVisibility(View.GONE);
            Toast.makeText(this,"设备无响应",Toast.LENGTH_SHORT).show();
        }
    }
        /**
         * 自身设备的可见性，可见300秒
         */
    private void ensureDiscoverable(){
        if(mBluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE){
            //启动修改蓝牙可见性的Intent
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            //设置蓝牙可见性的时间，方法本身规定最多可见300秒
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION,300);
            startActivity(discoverableIntent);
        }
    }
        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            switch(requestCode){
                case REQUEST_CONNECT_DEVICE_SECURE:
                    //当DeviceListActivity返回一个连接的加密的设备
                    if(resultCode == Activity.RESULT_OK){
                        connectDevice(data,true);
                        pb.setVisibility(View.VISIBLE);
                    }
                    break;
                case REQUEST_CONNECT_DEVICE_INSECURE:
                    //当DeviceListActivity返回一个连接的没有加密的设备
                    if(resultCode == Activity.RESULT_OK){
                        connectDevice(data,false);
                        pb.setVisibility(View.VISIBLE);
                    }
                    break;
                case REQUEST_ENABLE_BT:
                    //返回的是要启动蓝牙时
                    if(resultCode == Activity.RESULT_OK){
                        setupChat();
                    }else {
                        //用户没有打开蓝牙，或者报错时
                        Toast.makeText(MainActivity.this,R.string.bt_not_enable_leaving,Toast.LENGTH_SHORT).show();
                       // MainActivity.this.finish();
                    }


            }
        }
        /**与其他设备建立连接
         * @param intent
         * @param secure
         */
        private void connectDevice(Intent intent,boolean secure) {
            //获取设备的MAC地址
            String address = intent.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
            //得到蓝牙设备
            BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
            mChatService.connect(device,secure);
        }
         @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
             switch (item.getItemId()){
                 /*启用蓝牙*/
                 case R.id.open_bluetooth:
                     if (!mBluetoothAdapter.isEnabled()) {
                         /*要请求启用蓝牙，请使用 ACTION_REQUEST_ENABLE */
                         Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                         /*请求开启成功后，定义一个REQUEST_ENABLE_BT返回值，被回调*/
                         startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
                     }else {
                         Toast.makeText(MainActivity.this,"蓝牙已经开启",Toast.LENGTH_SHORT).show();
                     }
                     break;
                 case R.id.secure_connect_scan:
                     if(mBluetoothAdapter.isEnabled()){
                         if(mChatService.getState() == BluetoothChatService.STATE_CONNECTED){
                             Toast.makeText(MainActivity.this,"请先断开已有连接",Toast.LENGTH_SHORT).show();
                         }else {

                             Intent serverIntent = new Intent(MainActivity.this, DeviceListActivity.class);
                             startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_SECURE);
                         }
                     }else {
                         Toast.makeText(MainActivity.this,"蓝牙没有开启",Toast.LENGTH_SHORT).show();
                     }

                     break;
                 case R.id.close_blue_service:
                     if(mBluetoothAdapter.isEnabled()){
                         if(mChatService.getState() == BluetoothChatService.STATE_NONE){
                             Toast.makeText(MainActivity.this,"已经断开连接了",Toast.LENGTH_SHORT).show();
                         }
                         if(mChatService!=null&&mChatService.getState()==BluetoothChatService.STATE_CONNECTED){
                             AlertDialog.Builder close = new AlertDialog.Builder(MainActivity.this);
                             close.setTitle("温馨提示");
                             close.setMessage("亲，确定要断开吗？");
                             close.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                 @Override
                                 public void onClick(DialogInterface dialogInterface, int i) {
                                     mChatService.stop();
                                        }
                                    });
                             close.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                 @Override
                                 public void onClick(DialogInterface dialogInterface, int i) {
                                     return;
                                       }
                                 });
                             close.create().show();
                                }
                         }else{
                             Toast.makeText(MainActivity.this,"蓝牙没有开启,不需要断开",Toast.LENGTH_SHORT).show();
                     }

                     break;
                 case R.id.request_data:
                   /*  byte[] b = {0x01,(byte)0xa3,0xE,0xE,0,0x6};
                     byte[] orderCRC =  intToByteArray(getCrc16(b));
                     byte[] end = {0x01,(byte)0xa3,0xE,0xE,0,0x6,orderCRC[2],orderCRC[3]};
                     sendMessage(end);
                     mDrawer.closeDrawer(GravityCompat.START);//关闭侧滑*/
                     break;
                 case R.id.history:
                     Intent intent = new Intent(this,QueryActivity.class);
                     startActivity(intent);
                     break;
                 case R.id.close_software:
                     AlertDialog.Builder out = new AlertDialog.Builder(MainActivity.this);
                     out.setTitle("温馨提示");
                     out.setMessage("亲，确定要离开吗？");
                     out.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                         @Override
                         public void onClick(DialogInterface dialogInterface, int i) {
                             mBluetoothAdapter.disable();
                             MainActivity.this.finish();
                         }
                     });
                     out.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                         @Override
                         public void onClick(DialogInterface dialogInterface, int i) {
                             return;
                         }
                     });
                     out.create().show();
                     break;
             }
            return true;
        }
        //字节数组转成整形
        public int bytesToInt(byte[] b) {
            if(b.length == 4){
                int i = (b[0] << 24) & 0xFF000000;
                i |= (b[1] << 16) & 0xFF0000;

                i |= (b[2] << 8) & 0xFF00;

                i |= b[3] & 0xFF;
                return i;
            }
            return 0;
        }
    //int转换成byte[]
    public  byte[] intToByteArray(int i) {
        byte[] result = new byte[4];
        //由高位到低位
        result[0] = (byte)((i >> 24) & 0xFF);
        result[1] = (byte)((i >> 16) & 0xFF);
        result[2] = (byte)((i >> 8) & 0xFF);
        result[3] = (byte)(i & 0xFF);
        return result;
    }
    //生成crc校验码
    public  int getCrc16(byte[] arr_buff) {
        int len = arr_buff.length;
        //预置 1 个 16 位的寄存器为十六进制FFFF, 称此寄存器为 CRC寄存器。
        int crc = 0xFFFF;
        int i, j;
        for (i = 0; i < len; i++) {
            //把第一个 8 位二进制数据 与 16 位的 CRC寄存器的低 8 位相异或, 把结果放于 CRC寄存器
            crc = ((crc & 0xFF00) | (crc & 0x00FF) ^ (arr_buff[i] & 0xFF));
            for (j = 0; j < 8; j++) {
                //把 CRC 寄存器的内容右移一位( 朝低位)用 0 填补最高位, 并检查右移后的移出位
                if ((crc & 0x0001) > 0) {
                    //如果移出位为 1, CRC寄存器与多项式A001进行异或
                    crc = crc >> 1;
                    crc = crc ^ 0xA001;
                } else
                    //如果移出位为 0,再次右移一位
                    crc = crc >> 1;
            }
        }
        return crc;

    }
    public void initData(){
            mListTextView.clear();
            mNavigationView = (NavigationView) findViewById(R.id.navigationView);
            pb = (ProgressBar)findViewById(R.id.waiting);
            toolbar = (Toolbar) findViewById(R.id.mToolBar);
            mButton = (Button) findViewById(R.id.mUpdate);
            mDrawer = (DrawerLayout) findViewById(R.id.mDrawer);
            mChannel1_1 = (TextView) findViewById(R.id.channel1_1);mChannel1_2 = (TextView) findViewById(R.id.channel1_2);mChannel1_3 = (TextView) findViewById(R.id.channel1_3);
            mChannel2_1 = (TextView) findViewById(R.id.channel2_1);mChannel2_2 = (TextView) findViewById(R.id.channel2_2);mChannel2_3 = (TextView) findViewById(R.id.channel2_3);
            mChannel3_1 = (TextView) findViewById(R.id.channel3_1);mChannel3_2 = (TextView) findViewById(R.id.channel3_2);mChannel3_3 = (TextView) findViewById(R.id.channel3_3);
            mChannel4_1 = (TextView) findViewById(R.id.channel4_1);mChannel4_2 = (TextView) findViewById(R.id.channel4_2);mChannel4_3 = (TextView) findViewById(R.id.channel4_3);
            mChannel5_1 = (TextView) findViewById(R.id.channel5_1);mChannel5_2 = (TextView) findViewById(R.id.channel5_2);mChannel5_3 = (TextView) findViewById(R.id.channel5_3);
            mChannel6_1 = (TextView) findViewById(R.id.channel6_1);mChannel6_2 = (TextView) findViewById(R.id.channel6_2);mChannel6_3 = (TextView) findViewById(R.id.channel6_3);
            mChannel7_1 = (TextView) findViewById(R.id.channel7_1);mChannel7_2 = (TextView) findViewById(R.id.channel7_2);mChannel7_3 = (TextView) findViewById(R.id.channel7_3);
            mChannel8_1 = (TextView) findViewById(R.id.channel8_1);mChannel8_2 = (TextView) findViewById(R.id.channel8_2);mChannel8_3 = (TextView) findViewById(R.id.channel8_3);
            mChannel9_1 = (TextView) findViewById(R.id.channel9_1);mChannel9_2 = (TextView) findViewById(R.id.channel9_2);mChannel9_3 = (TextView) findViewById(R.id.channel9_3);
            mChannel10_1 = (TextView) findViewById(R.id.channel10_1);mChannel10_2 = (TextView) findViewById(R.id.channel10_2);mChannel10_3 = (TextView) findViewById(R.id.channel10_3);
            mChannel11_1 = (TextView) findViewById(R.id.channel11_1);mChannel11_2 = (TextView) findViewById(R.id.channel11_2);mChannel11_3 = (TextView) findViewById(R.id.channel11_3);
            mChannel12_1 = (TextView) findViewById(R.id.channel12_1);mChannel12_2 = (TextView) findViewById(R.id.channel12_2);mChannel12_3 = (TextView) findViewById(R.id.channel12_3);
            mChannel13_1 = (TextView) findViewById(R.id.channel13_1);mChannel13_2 = (TextView) findViewById(R.id.channel13_2);mChannel13_3 = (TextView) findViewById(R.id.channel13_3);
            mChannel14_1 = (TextView) findViewById(R.id.channel14_1);mChannel14_2 = (TextView) findViewById(R.id.channel14_2);mChannel14_3 = (TextView) findViewById(R.id.channel14_3);
            mChannel15_1 = (TextView) findViewById(R.id.channel15_1);mChannel15_2 = (TextView) findViewById(R.id.channel15_2);mChannel15_3 = (TextView) findViewById(R.id.channel15_3);
            mChannel16_1 = (TextView) findViewById(R.id.channel16_1);mChannel16_2 = (TextView) findViewById(R.id.channel16_2);mChannel16_3 = (TextView) findViewById(R.id.channel16_3);
            mChannel17_1 = (TextView) findViewById(R.id.channel17_1);mChannel17_2 = (TextView) findViewById(R.id.channel17_2);mChannel17_3 = (TextView) findViewById(R.id.channel17_3);
            mChannel18_1 = (TextView) findViewById(R.id.channel18_1);mChannel18_2 = (TextView) findViewById(R.id.channel18_2);mChannel18_3 = (TextView) findViewById(R.id.channel18_3);
            mChannel19_1 = (TextView) findViewById(R.id.channel19_1);mChannel19_2 = (TextView) findViewById(R.id.channel19_2);mChannel19_3 = (TextView) findViewById(R.id.channel19_3);
            mChannel20_1 = (TextView) findViewById(R.id.channel20_1);mChannel20_2 = (TextView) findViewById(R.id.channel20_2);mChannel20_3 = (TextView) findViewById(R.id.channel20_3);
            mChannel21_1 = (TextView) findViewById(R.id.channel21_1);mChannel21_2 = (TextView) findViewById(R.id.channel21_2);mChannel21_3 = (TextView) findViewById(R.id.channel21_3);
            mChannel22_1 = (TextView) findViewById(R.id.channel22_1);mChannel22_2 = (TextView) findViewById(R.id.channel22_2);mChannel22_3 = (TextView) findViewById(R.id.channel22_3);
            mChannel23_1 = (TextView) findViewById(R.id.channel23_1);mChannel23_2 = (TextView) findViewById(R.id.channel23_2);mChannel23_3 = (TextView) findViewById(R.id.channel23_3);
            mChannel24_1 = (TextView) findViewById(R.id.channel24_1);mChannel24_2 = (TextView) findViewById(R.id.channel24_2);mChannel24_3 = (TextView) findViewById(R.id.channel24_3);
            mChannel25_1 = (TextView) findViewById(R.id.channel25_1);mChannel25_2 = (TextView) findViewById(R.id.channel25_2);mChannel25_3 = (TextView) findViewById(R.id.channel25_3);
            mChannel26_1 = (TextView) findViewById(R.id.channel26_1);mChannel26_2 = (TextView) findViewById(R.id.channel26_2);mChannel26_3 = (TextView) findViewById(R.id.channel26_3);
            mChannel27_1 = (TextView) findViewById(R.id.channel27_1);mChannel27_2 = (TextView) findViewById(R.id.channel27_2);mChannel27_3 = (TextView) findViewById(R.id.channel27_3);
            mChannel28_1 = (TextView) findViewById(R.id.channel28_1);mChannel28_2 = (TextView) findViewById(R.id.channel28_2);mChannel28_3 = (TextView) findViewById(R.id.channel28_3);
            mChannel29_1 = (TextView) findViewById(R.id.channel29_1);mChannel29_2 = (TextView) findViewById(R.id.channel29_2);mChannel29_3 = (TextView) findViewById(R.id.channel29_3);
            mChannel30_1 = (TextView) findViewById(R.id.channel30_1);mChannel30_2 = (TextView) findViewById(R.id.channel30_2);mChannel30_3 = (TextView) findViewById(R.id.channel30_3);
            mChannel31_1 = (TextView) findViewById(R.id.channel31_1);mChannel31_2 = (TextView) findViewById(R.id.channel31_2);mChannel31_3 = (TextView) findViewById(R.id.channel31_3);
            mChannel32_1 = (TextView) findViewById(R.id.channel32_1);mChannel32_2 = (TextView) findViewById(R.id.channel32_2);mChannel32_3 = (TextView) findViewById(R.id.channel32_3);
            mChannel33_1 = (TextView) findViewById(R.id.channel33_1);mChannel33_2 = (TextView) findViewById(R.id.channel33_2);mChannel33_3 = (TextView) findViewById(R.id.channel33_3);
            mChannel34_1 = (TextView) findViewById(R.id.channel34_1);mChannel34_2 = (TextView) findViewById(R.id.channel34_2);mChannel34_3 = (TextView) findViewById(R.id.channel34_3);
            mChannel35_1 = (TextView) findViewById(R.id.channel35_1);mChannel35_2 = (TextView) findViewById(R.id.channel35_2);mChannel35_3 = (TextView) findViewById(R.id.channel35_3);
            mChannel36_1 = (TextView) findViewById(R.id.channel36_1);mChannel36_2 = (TextView) findViewById(R.id.channel36_2);mChannel36_3 = (TextView) findViewById(R.id.channel36_3);
            mListTextView.add( mChannel1_1);mListTextView.add( mChannel1_2);mListTextView.add( mChannel1_3);mListTextView.add( mChannel2_1);mListTextView.add( mChannel2_2);mListTextView.add( mChannel2_3);
            mListTextView.add( mChannel3_1);mListTextView.add( mChannel3_2);mListTextView.add( mChannel3_3);mListTextView.add( mChannel4_1);mListTextView.add( mChannel4_2);mListTextView.add( mChannel4_3);
            mListTextView.add( mChannel5_1);mListTextView.add( mChannel5_2);mListTextView.add( mChannel5_3);mListTextView.add( mChannel6_1);mListTextView.add( mChannel6_2);mListTextView.add( mChannel6_3);
            mListTextView.add( mChannel7_1);mListTextView.add( mChannel7_2);mListTextView.add( mChannel7_3);mListTextView.add( mChannel8_1);mListTextView.add( mChannel8_2);mListTextView.add( mChannel8_3);
            mListTextView.add( mChannel9_1);mListTextView.add( mChannel9_2);mListTextView.add( mChannel9_3);mListTextView.add( mChannel10_1);mListTextView.add( mChannel10_2);mListTextView.add( mChannel10_3);
            mListTextView.add( mChannel11_1);mListTextView.add( mChannel11_2);mListTextView.add( mChannel11_3);mListTextView.add( mChannel12_1);mListTextView.add( mChannel12_2);mListTextView.add( mChannel12_3);
            mListTextView.add( mChannel13_1);mListTextView.add( mChannel13_2);mListTextView.add( mChannel13_3);mListTextView.add( mChannel14_1);mListTextView.add( mChannel14_2);mListTextView.add( mChannel14_3);
            mListTextView.add( mChannel15_1);mListTextView.add( mChannel15_2);mListTextView.add( mChannel15_3);mListTextView.add( mChannel16_1);mListTextView.add( mChannel16_2);mListTextView.add( mChannel16_3);
            mListTextView.add( mChannel17_1);mListTextView.add( mChannel17_2);mListTextView.add( mChannel17_3);mListTextView.add( mChannel18_1);mListTextView.add( mChannel18_2);mListTextView.add( mChannel18_3);
            mListTextView.add( mChannel19_1);mListTextView.add( mChannel19_2);mListTextView.add( mChannel19_3);mListTextView.add( mChannel20_1);mListTextView.add( mChannel20_2);mListTextView.add( mChannel20_3);
            mListTextView.add( mChannel21_1);mListTextView.add( mChannel21_2);mListTextView.add( mChannel21_3);mListTextView.add( mChannel22_1);mListTextView.add( mChannel22_2);mListTextView.add( mChannel22_3);
            mListTextView.add( mChannel23_1);mListTextView.add( mChannel23_2);mListTextView.add( mChannel23_3);mListTextView.add( mChannel24_1);mListTextView.add( mChannel24_2);mListTextView.add( mChannel24_3);
            mListTextView.add( mChannel25_1);mListTextView.add( mChannel25_2);mListTextView.add( mChannel25_3);mListTextView.add( mChannel26_1);mListTextView.add( mChannel26_2);mListTextView.add( mChannel26_3);
            mListTextView.add( mChannel27_1);mListTextView.add( mChannel27_2);mListTextView.add( mChannel27_3);mListTextView.add( mChannel28_1);mListTextView.add( mChannel28_2);mListTextView.add( mChannel28_3);
            mListTextView.add( mChannel29_1);mListTextView.add( mChannel29_2);mListTextView.add( mChannel29_3);mListTextView.add( mChannel30_1);mListTextView.add( mChannel30_2);mListTextView.add( mChannel30_3);
            mListTextView.add( mChannel31_1);mListTextView.add( mChannel31_2);mListTextView.add( mChannel31_3);mListTextView.add( mChannel32_1);mListTextView.add( mChannel32_2);mListTextView.add( mChannel32_3);
            mListTextView.add( mChannel33_1);mListTextView.add( mChannel33_2);mListTextView.add( mChannel33_3);mListTextView.add( mChannel34_1);mListTextView.add( mChannel34_2);mListTextView.add( mChannel34_3);
            mListTextView.add( mChannel35_1);mListTextView.add( mChannel35_2);mListTextView.add( mChannel35_3);mListTextView.add( mChannel36_1);mListTextView.add( mChannel36_2);mListTextView.add( mChannel36_3);

        }

    //监听广播
    private BroadcastReceiver myReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String str = intent.getAction();
            if(BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(str)){
                mChatService.stop();
            }
        }
    };

    /**将得到的字节数组解析成浮点数
     * @param b
     */
    public void byteToFloat2(byte[] b){
        if(b.length>0){
            compare = b.clone();//用来和下一次的数据比较
            float tem;
            int a = 0;
            int c =10;
            int d = 0;
            int h = 1;
            byte[] child;
            for (int i = 0; i <(b.length-10)/4 ; i++) { //前十个字节是不需要解析的，所以从第九位开始循环
                if(true){//如果从循环位加4大于字节数组的长度，则不能进行解析，
                    child = new byte[4];
                    System.arraycopy(b,c,child,0,4);
                    tem = Float.intBitsToFloat(bytesToInt(child));//将小的byte[]转换成float
                    tem=(float)(Math.round(tem*100))/100;//四舍五入
                    if(a<108){
                        mListTextView.get(a).setVisibility(View.VISIBLE);//让有数据的TextView显示
                        mListTextView.get(a).setText(tem+"");//以字符串的形式显示在TextView
                    }
                    a++;//还有数据，就让textview显示
                    d = a;//赋值，用来隐藏没有接收到数据的TextView
                }
                for (int j = d; j <mListTextView.size() ; j++) {
                    mListTextView.get(j).setVisibility(View.GONE);
                }
                c = c+4;//往后截取四个字节
            }
            if(a>=3){//a表示mListTextView目前含有的TextView数，一般是3的整数倍，因为每3个TextView表示一个通道的三个数据
                saveData2(a);
            }
        }
        else {
            Toast.makeText(this,"返回的字节数是0",Toast.LENGTH_SHORT).show();
        }

    }

    /**
     * @param temp 保存数据
     */
    public void saveData2(int temp){
        mList.clear();//每次需要保存数据时，将期清空，防止上次残留的数据
        int h = 0,m = 0,n = 0,z=0;//用来从mListTextView集合中取出相应TextView的角标
        Date date = new Date();//定义时间
        mSimpleDateFormat = new SimpleDateFormat("yy-MM-dd HH:mm:ss");
        String t = mSimpleDateFormat.format(date);
        for (int i = 0; i < (temp/3); i++) {//根据有多少TextView有数据来确定要生成多少个Bean，一般一个bean对应三个TextView
            Bean bean = new Bean();
            bean.setName("通道"+String.valueOf(i+1));//设置名字，数据库的查询是以名字来查询的
            z = m + n + h;
            bean.setData1(mListTextView.get(z).getText().toString());//设置数据1
            bean.setData2(mListTextView.get(z+1).getText().toString());//设置数据2
            bean.setData3(mListTextView.get(z+2).getText().toString());//设置数据3
            bean.setNow(date);//设置时间
            bean.setTimeDetail(t);//存放格式化的时间
            mList.add(bean);
            h++;//让他们自增，算出来的角标刚好吻合
            m++;
            n++;

        }
           boolean w = mCommonUtils.insertMultBean(mList);//批量插入数据

    }


}


