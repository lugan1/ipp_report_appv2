package fragment.techtown.org.report_appv2;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.RotateAnimation;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.Vector;

import fragment.techtown.org.report_appv2.Rest.Ketti_API_Method;
import fragment.techtown.org.report_appv2.Rest.OBJ.Login_POST;
import fragment.techtown.org.report_appv2.Rest.OBJ.Login_RESPONSE;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    static final UUID uuid = UUID.fromString("");

    Button btn_create;
    Button btn_login;
    ImageButton btn_scan_start;
    EditText edit_id, edit_password;
    ListView scan_divice_listView;

    TextView txtvState;


    public boolean getIs_Main_activity_on() {
        return Is_Main_activity_on;
    }

    static boolean Is_Main_activity_on = false;
    private static final int FINISH = 300;
    private static final int CERATEPOST = 301;

    Login_POST login_post = new Login_POST();


    List<BluetoothDevice> mLeDevices = new ArrayList<BluetoothDevice>();

    List<Integer>mLeRssi = new ArrayList<Integer>();


    Handler mHandler;
    BluetoothLeScanner mBLEScanner;

    BluetoothAdapter mBluetoothAdapter;
    int value;

    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_CREATE_ACTIVITY = 2;
    private static final int REQUEST_LOG_ACTIVITY = 3;

    public static String DEVICE_NAME, DEVICE_MAC ;

    static LeDeviceListAdapter mLeDeviceListAdapter;

    private static int selectPosition;

    // ????????????????????? -Rotate ?????? xml??? ????????? ???????????? ????????????
    Animation anim;

    ProgressDialog mProgressDialog;

    BluetoothModule bluetoothModule = BluetoothModule.getInstance();







    private static class Main_holder{
        public static final MainActivity INSTANCE = new MainActivity();
    }

    public static MainActivity getINSTANCE(){
        return Main_holder.INSTANCE;
    }

    @SuppressLint("ResourceType")
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Is_Main_activity_on = true;
        ///////////////////////BLE ?????? ???????????? ?????? ///////////////////////////////////////////////////////////
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "BLE ????????? ???????????? ????????????.", Toast.LENGTH_SHORT).show();
        }

        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "???????????? ????????? ???????????? ????????????.", Toast.LENGTH_SHORT).show();
        }
        /////////////////////////////////////////////////////////////////////////////////////////////////////////


        /////////////////////////// BLE ???????????? ??????////////////////////////////////////////////////////////////
        mBLEScanner = mBluetoothAdapter.getBluetoothLeScanner();
        // Checks if Bluetooth LE Scanner is available.
        if (mBLEScanner == null) {
            Toast.makeText(this, "Can not find BLE Scanner", Toast.LENGTH_SHORT).show();
        }
        /////////////////////////////////////////////////////////////////////////////////////////////////////////

        btn_login = (Button)findViewById(R.id.main_btn_login);
        btn_create = (Button)findViewById(R.id.main_btn_create);
        btn_scan_start = (ImageButton)findViewById(R.id.btn_scan_start);

        edit_id = (EditText)findViewById(R.id.main_editText_lgid);
        edit_password = (EditText)findViewById(R.id.main_editText_lgpass);

        scan_divice_listView = (ListView)findViewById(R.id.main_scan_listview);

        mLeDeviceListAdapter = new LeDeviceListAdapter();
        scan_divice_listView.setAdapter(mLeDeviceListAdapter);
        scan_divice_listView.setOnItemClickListener(deviceClickListener);

        mHandler = new Handler();

        pemissionCheck();

        mLeDeviceListAdapter.clear();
        anim = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.rotate_anim);
        anim.setFillAfter(true);
        mBleStartScan();
        //mBLEScanner.startScan(mScanCallback);

        btn_scan_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mBleStartScan() == true){
                    btn_scan_start.clearAnimation();
                    mBLEScanner.stopScan(mScanCallback);
                    mBleStartScan();
                }
            }
        });

        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int state = bluetoothManager.getConnectionState(mLeDevices.get(selectPosition),BluetoothProfile.GATT);
                Log.v("cnnt","state : "+state);
                if(state == BluetoothProfile.STATE_DISCONNECTED){
                    mBLEScanner.stopScan(mScanCallback);
                    btn_scan_start.clearAnimation();
                    not_connected_device_Dialog();
                }
                else{
                    Ketti_API_Method ketti_api_method = new Ketti_API_Method();
                    login_post.setId(edit_id.getText().toString());
                    login_post.setPw(edit_password.getText().toString());
                    ketti_api_method.Login(login_post, new Callback<Login_RESPONSE>() {
                        @Override
                        public void onResponse(Call<Login_RESPONSE> call, Response<Login_RESPONSE> response) {
                            if(response.body() != null){
                                if(response.body().getCode().equals("0000")){
                                    //????????? ??????
                                    Intent onLogActivity = new Intent(getApplicationContext(),Log_Activity.class);
                                    onLogActivity.putExtra("position",selectPosition);
                                    onLogActivity.putExtra("name",DEVICE_NAME);
                                    onLogActivity.putExtra("mac",DEVICE_MAC);
                                    startActivityForResult(onLogActivity,REQUEST_LOG_ACTIVITY);
                                }
                                else if(response.body().getCode().equals("0001")){
                                    Toast.makeText(getApplicationContext(),"error code 1 : ??????????????? ????????????.",Toast.LENGTH_SHORT).show();
                                    Log.v("cnnt","response : "+response.body().string());
                                }
                                else if(response.body().getCode().equals("0013")){
                                    Toast.makeText(getApplicationContext(),"error code 13 : ???????????? ???????????? ????????????.",Toast.LENGTH_SHORT).show();
                                }
                                else if(response.body().getCode().equals("0015")){
                                    Toast.makeText(getApplicationContext(),"error code 15 : ????????? ???????????? ???????????????.",Toast.LENGTH_SHORT).show();
                                }
                                else{
                                    Toast.makeText(getApplicationContext(),"error code "+response.body().getCode()+" : "+response.body().getMessage(),Toast.LENGTH_SHORT).show();
                                }
                                //Toast.makeText(getApplicationContext(),"response.body() != null",Toast.LENGTH_SHORT).show();
                                //Toast.makeText(getApplicationContext(),response.body().string(),Toast.LENGTH_SHORT).show();
                            }
                            else if(response.body() == null){
                                try {
                                    Toast.makeText(getApplicationContext(),""+response.errorBody().string(),Toast.LENGTH_LONG).show();
                                    Log.v("cnnt","result == null");
                                    Log.v("cnnt",response.errorBody().string());
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }

                        @Override
                        public void onFailure(Call<Login_RESPONSE> call, Throwable t) {

                        }
                    });
                }
            }
        });

        btn_create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent onCreateActivity = new Intent(getApplicationContext(),CreateAcc_Activity.class);
                startActivityForResult(onCreateActivity, REQUEST_CREATE_ACTIVITY);
            }
        });
    }
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////







    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    protected void onStop() {
        super.onStop();
        Is_Main_activity_on = false;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mBLEScanner.stopScan(mScanCallback);
                btn_scan_start.clearAnimation();
            }
        });
        Log.v("cnnt","on stop");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        Is_Main_activity_on = false;
        // Log.v("ccnt","frag onDestroy()");
    }

    @Override
    protected void onResume() {
        super.onResume();

        Is_Main_activity_on = true;
    }
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////








    public void not_connected_device_Dialog(){
        AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
        alert.setPositiveButton("??????", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();     //??????
            }
        });
        alert.setMessage("????????? ???????????? ??????????????????.");
        alert.show();

    }





    ////////////////////////////////////////////////???????????? ??????///////////////////////////////////////////////////////////////////////
    public void pemissionCheck() {
        int permissionCOARS = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);

        int permissionFINE = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);

        if (permissionCOARS != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION)) {
                Toast.makeText(this, "COARS ?????? ?????? ?????????", Toast.LENGTH_LONG).show();
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
            }
        }

        if (permissionFINE != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                Toast.makeText(this, "FINE ?????? ?????? ?????????", Toast.LENGTH_LONG).show();
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 2);
            }
        }
    }
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // User chose not to enable Bluetooth.
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_CANCELED){
                Toast.makeText(this, "???????????? ????????? ???????????? ???  ?????? ??????????????????", Toast.LENGTH_SHORT).show();
            }
        }
        if(requestCode == REQUEST_CREATE_ACTIVITY) {
            if(requestCode == FINISH){

            }
            else if (resultCode == CERATEPOST){
                if (! this.isFinishing()) {
                    Create_Succes_Dialog_show();
                }
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    void Create_Succes_Dialog_show()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("?????? ??????");
        builder.setMessage("??????????????? ??????????????????.");
        builder.setPositiveButton("??????",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
        builder.show();
    }

    public boolean mBleStartScan(){
        if(!mBluetoothAdapter.isEnabled()){
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
        else if (mBluetoothAdapter.isEnabled()){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    btn_scan_start.startAnimation(anim);
                }
            });

            List<ScanFilter> scanFilterList = new Vector<>();
            ScanFilter.Builder scanFilter = new ScanFilter.Builder();
            scanFilter.setDeviceName("mobiCARE-EMG");
            //mobiCARE-EMG ??? ????????? ?????????
            scanFilterList.add(scanFilter.build());

            ScanSettings.Builder scanSettings = new ScanSettings.Builder();
            scanSettings.setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES);

            scanSettings.setScanMode(ScanSettings.SCAN_MODE_BALANCED);
            //????????? ???????????? ?????? ???????????? ?????????.
            scanSettings.setMatchMode(ScanSettings.MATCH_MODE_AGGRESSIVE);
            //????????? ???????????? hw??? ?????? ????????? ????????? ?????? / ?????? ????????? ?????? ???????????? ??? ?????? ????????? ???????????????.

            mBLEScanner.startScan(null,scanSettings.build(),mScanCallback);
        }
        return true;
    }






    // Device scan callback.
    public ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            //Log.v("cnnt","callbackType : "+callbackType);
            processResult(result);
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            //Log.v("BLELOG","ScanCallback");
            for (ScanResult result : results) {
                processResult(result);
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            //Log.v("BLELOG","onScanFaild error code :"+errorCode);

            if(errorCode == ScanCallback.SCAN_FAILED_ALREADY_STARTED){
                //????????? ????????? BLE ????????? ?????? ????????? ????????????????????? ????????? ???????????? ????????????.
               Toast.makeText(getApplicationContext(),"????????? ????????? BLE ????????? ?????? ????????? ????????????????????? ????????? ???????????? ????????????.",Toast.LENGTH_LONG).show();
            }
            if(errorCode == ScanCallback.SCAN_FAILED_APPLICATION_REGISTRATION_FAILED){
                //?????? ????????? ??? ???????????? ????????? ???????????? ???????????????.
                Toast.makeText(getApplicationContext(),"?????? ????????? ??? ???????????? ????????? ???????????? ???????????????.",Toast.LENGTH_LONG).show();
            }
            if(errorCode == ScanCallback.SCAN_FAILED_INTERNAL_ERROR){
                //?????? ????????? ?????? ????????? ???????????? ??????
                Toast.makeText(getApplicationContext(),"?????? ????????? ?????? ????????? ???????????? ??????",Toast.LENGTH_LONG).show();
            }
            if(errorCode == ScanCallback.SCAN_FAILED_FEATURE_UNSUPPORTED){
                //??? ????????? ???????????? ???????????? ?????? ????????? ????????? ????????? ??? ????????????.
                Toast.makeText(getApplicationContext(),"??? ????????? ???????????? ???????????? ?????? ????????? ????????? ????????? ??? ????????????.",Toast.LENGTH_LONG).show();
            }
        }
        private void processResult(final ScanResult result) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mLeDeviceListAdapter.addDevice(result);
                    mLeDeviceListAdapter.notifyDataSetChanged();
                }
            });
        }
    };
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////



    /////////////////////???????????? ???????????? ????????? ?????? ????????? ??????//////////////////////////////////////////////////////
    private AdapterView.OnItemClickListener deviceClickListener = new AdapterView.OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
            DEVICE_NAME = mLeDevices.get(position).getName();
            DEVICE_MAC = mLeDevices.get(position).getAddress();
            selectPosition = position;
            mBLEScanner.stopScan(mScanCallback);
            btn_scan_start.clearAnimation();
            if(view.findViewWithTag(position)!=null){
                txtvState = view.findViewWithTag(position);
            }
            //????????? ??????????????? position??? ???????????? ??????????????? ?????????

            bluetoothModule.gattConnect(DEVICE_MAC, new BluetoothModule.BluetoothConnectImpl() {
                @Override
                public void onSuccessConnect(BluetoothDevice device) {

                }

                @Override
                public void onFailed() {

                }
            },MainActivity.this,txtvState);

        }
    };
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////



    /////////////////////////????????? ??????////////////////////////////////////////////////////////////////////////
    private class LeDeviceListAdapter extends BaseAdapter {

        public LeDeviceListAdapter() {
            super();
            mLeDevices = new ArrayList<BluetoothDevice>();
            mLeRssi = new ArrayList<Integer>();
        }

        @Override
        public void notifyDataSetChanged() {
            super.notifyDataSetChanged();
        }

        public void addDevice(ScanResult result) {
            if(result != null){
                if(result.getDevice().getName() != null){
                    if(!mLeDevices.contains(result.getDevice())){
                        mLeDevices.add(result.getDevice());
                        mLeRssi.add(result.getRssi());
                    }
                    else if (mLeDevices.contains(result.getDevice())) {
                            //???????????? ??????
                            //Log.v("cnnt","??????");
                            mLeRssi.set(mLeDevices.indexOf(result.getDevice()),result.getRssi());
                        }
                }
            }
        }

        public BluetoothDevice getDevice(int position) {
            return mLeDevices.get(position);
        }

        public void clear() {
            mLeDevices.clear();
        }

        @Override
        public int getCount() {
            return mLeDevices.size();
        }

        @Override
        public Object getItem(int i) {
            return mLeDevices.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            //ViewHolder viewHolder = new ViewHolder();

            Scan_listView singerItemView = new Scan_listView(getApplicationContext());

            try{
                BluetoothDevice device = mLeDevices.get(i);
                int DeviceRssi = mLeRssi.get(i);

                final String deviceName = device.getName();

                Log.v("cnnt","DEVICE_NAME : "+deviceName);

                if (deviceName != null && deviceName.length() > 0){
                    singerItemView.setName(deviceName);
                }else{
                    singerItemView.setName("unknown device");
                }
                singerItemView.setTextVTag(i);

                singerItemView.setAddress(device.getAddress());
                singerItemView.setRssi(String.valueOf(DeviceRssi));
            }catch(IndexOutOfBoundsException e){
                mLeDevices.clear();
                mLeDeviceListAdapter.clear();
                mLeDeviceListAdapter.notifyDataSetChanged();
            }
            return singerItemView;
        }
    }



}
