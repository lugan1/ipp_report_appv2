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

    // 트윈에니메이션 -Rotate 회전 xml의 코드를 로드해서 적용하기
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
        ///////////////////////BLE 사용 가능한지 체크 ///////////////////////////////////////////////////////////
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "BLE 기능을 지원하지 않습니다.", Toast.LENGTH_SHORT).show();
        }

        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "블루투스 기능을 지원하지 않습니다.", Toast.LENGTH_SHORT).show();
        }
        /////////////////////////////////////////////////////////////////////////////////////////////////////////


        /////////////////////////// BLE 검색장치 체크////////////////////////////////////////////////////////////
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
                                    //로그인 성공
                                    Intent onLogActivity = new Intent(getApplicationContext(),Log_Activity.class);
                                    onLogActivity.putExtra("position",selectPosition);
                                    onLogActivity.putExtra("name",DEVICE_NAME);
                                    onLogActivity.putExtra("mac",DEVICE_MAC);
                                    startActivityForResult(onLogActivity,REQUEST_LOG_ACTIVITY);
                                }
                                else if(response.body().getCode().equals("0001")){
                                    Toast.makeText(getApplicationContext(),"error code 1 : 비밀번호가 틀립니다.",Toast.LENGTH_SHORT).show();
                                    Log.v("cnnt","response : "+response.body().string());
                                }
                                else if(response.body().getCode().equals("0013")){
                                    Toast.makeText(getApplicationContext(),"error code 13 : 아이디가 존재하지 않습니다.",Toast.LENGTH_SHORT).show();
                                }
                                else if(response.body().getCode().equals("0015")){
                                    Toast.makeText(getApplicationContext(),"error code 15 : 텍스트 입력창이 비었습니다.",Toast.LENGTH_SHORT).show();
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
        alert.setPositiveButton("확인", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();     //닫기
            }
        });
        alert.setMessage("장치를 연결하고 시도해주세요.");
        alert.show();

    }





    ////////////////////////////////////////////////위험권한 체크///////////////////////////////////////////////////////////////////////
    public void pemissionCheck() {
        int permissionCOARS = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);

        int permissionFINE = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);

        if (permissionCOARS != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION)) {
                Toast.makeText(this, "COARS 권한 설명 필요함", Toast.LENGTH_LONG).show();
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
            }
        }

        if (permissionFINE != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                Toast.makeText(this, "FINE 권한 설명 필요함", Toast.LENGTH_LONG).show();
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
                Toast.makeText(this, "블루투스 기능을 활성화한 후  다시 실행해주세요", Toast.LENGTH_SHORT).show();
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
        builder.setTitle("가입 성공");
        builder.setMessage("회원가입에 성공했습니다.");
        builder.setPositiveButton("확인",
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
            //mobiCARE-EMG 만 얻도록 필터링
            scanFilterList.add(scanFilter.build());

            ScanSettings.Builder scanSettings = new ScanSettings.Builder();
            scanSettings.setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES);

            scanSettings.setScanMode(ScanSettings.SCAN_MODE_BALANCED);
            //전력을 고려해서 중간 균형으로 스캔함.
            scanSettings.setMatchMode(ScanSettings.MATCH_MODE_AGGRESSIVE);
            //공격적 모드에서 hw는 신호 강도가 약하고 조준 / 일치 횟수가 적을 때조차도 더 빨리 일치를 결정합니다.

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
                //동일한 설정의 BLE 스캔이 이미 앱에서 시작되었으므로 스캔을 시작하지 못합니다.
               Toast.makeText(getApplicationContext(),"동일한 설정의 BLE 스캔이 이미 앱에서 시작되었으므로 스캔을 시작하지 못합니다.",Toast.LENGTH_LONG).show();
            }
            if(errorCode == ScanCallback.SCAN_FAILED_APPLICATION_REGISTRATION_FAILED){
                //앱을 등록할 수 없으므로 검색을 시작하지 못했습니다.
                Toast.makeText(getApplicationContext(),"앱을 등록할 수 없으므로 검색을 시작하지 못했습니다.",Toast.LENGTH_LONG).show();
            }
            if(errorCode == ScanCallback.SCAN_FAILED_INTERNAL_ERROR){
                //내부 오류로 인해 스캔을 시작하지 못함
                Toast.makeText(getApplicationContext(),"내부 오류로 인해 스캔을 시작하지 못함",Toast.LENGTH_LONG).show();
            }
            if(errorCode == ScanCallback.SCAN_FAILED_FEATURE_UNSUPPORTED){
                //이 기능은 지원되지 않으므로 전원 최적화 검색을 시작할 수 없습니다.
                Toast.makeText(getApplicationContext(),"이 기능은 지원되지 않으므로 전원 최적화 검색을 시작할 수 없습니다.",Toast.LENGTH_LONG).show();
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



    /////////////////////리스트뷰 디바이스 아이템 클릭 리스너 구현//////////////////////////////////////////////////////
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
            //선택된 리스트뷰의 position에 해당하는 텍스트뷰를 갖고옴

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



    /////////////////////////어답터 구현////////////////////////////////////////////////////////////////////////
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
                            //중복검사 제외
                            //Log.v("cnnt","중복");
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
