package fragment.techtown.org.report_appv2;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.List;

import fragment.techtown.org.report_appv2.Util.File_Util;
import fragment.techtown.org.report_appv2.Util.HexUtil;

public class BluetoothModule extends AppCompatActivity {
    //BluetoothGatt 객체로 Connect 해주고, writeCharacter 해주는 클래스
    private static BluetoothGatt bluetoothGatt;
    BluetoothManager bluetoothManager;

    Connect_Progress_Dialog connect_progress_dialog;

    String mac_address,value,now_time;

    HexUtil hexUtil = new HexUtil();

    private TextView main_textvState;
    private TextView main_textStateBuffer;
    private TextView log_textvState;

    private BluetoothConnectImpl btConnectCallback;
    private BluetoothWriteImpl btWriteCallback;

    private Context context;

    BluetoothGattCharacteristic notify_characteristic = null;
    BluetoothGattDescriptor notify_config_desc = null;

    Log_Activity log_activity;
    MainActivity mainActivity;

    Handler handler = new Handler();




    public TextView getMain_textvState() {
        return main_textvState;
    }

    public TextView getMain_textStateBuffer() {
        return main_textStateBuffer;
    }


    public Context getContext() {
        return context;
    }

    BluetoothModule() {

    }
    //생성자

    private static class BluetoothModuleHolder {
        private static final BluetoothModule instance = new BluetoothModule();
    }
    //블루투스 모듈 객체(변수) 생성자


    public static BluetoothModule getInstance() {
        return BluetoothModuleHolder.instance;
    }
    //블루투스 모듈 객체(변수) 반환

    public boolean isConnected() {
        return (bluetoothGatt != null && bluetoothGatt.connect());
    }
    //연결됏는지 확인
    //위 조건이 트루인지 펄스인지

    public BluetoothGatt getGatt() {
        return this.bluetoothGatt;
    }
    //블루투스 가트객체 얻어옴

    public void setGatt(BluetoothGatt bluetoothGatt) {
        this.bluetoothGatt = bluetoothGatt;
    }

    public void disconnect() {
        if (isConnected()) {
            bluetoothGatt.disconnect();
        }
    }
    //연결 끊음
    //만약 연결됐으면 BT가트 객체.disconnect()

    public BluetoothManager getBluetoothManager() {
        return bluetoothManager;
    }

    public void setTime(String time) {
        this.now_time = time;
    }

    public String getMac_address() {
        return mac_address;
    }

    public void setMac_address(String mac_address) {
        this.mac_address = mac_address;
    }


    public void ReadChar(int serviceIndex, int characteristicIndex){
        BluetoothGattCharacteristic characteristic = getGatt().getServices().get(serviceIndex).getCharacteristics().get(characteristicIndex);
        getGatt().readCharacteristic(characteristic);

        //getGatt로 현재 gatt 객체 읽고, 캐릭터 가져옴
        //현재 Gatt객체로 readCharacteristic(characteristic);
    }

    public void Notify_Enable(int serviceIndex, int characteristicIndex, boolean enable){
        BluetoothGattCharacteristic characteristic = getGatt().getServices().get(serviceIndex).getCharacteristics().get(characteristicIndex);

        //List<BluetoothGattDescriptor> bluetoothGattDescriptors =  characteristic.getDescriptors();

        getGatt().setCharacteristicNotification(characteristic,enable);

    }

    public void Notify_Enable_Descriptor_Write(int serviceIndex, int characteristicIndex, boolean enable){
        //Log.v("BLELOG","Call notify_enable_descriptor_write");
        BluetoothGattCharacteristic characteristic = getGatt().getServices().get(serviceIndex).getCharacteristics().get(characteristicIndex);

        List<BluetoothGattDescriptor> bluetoothGattDescriptors =  characteristic.getDescriptors();

        BluetoothGattDescriptor descriptors = bluetoothGattDescriptors.get(0);

        if(enable == true){
            descriptors.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        }else if(enable == false){
            descriptors.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
        }
        getGatt().writeDescriptor(descriptors);
    }

    /**
     * 맥주소를 받아서 connect
     */
    public void gattConnect(String macAddress, BluetoothConnectImpl btConnectCallback, final Context context, final TextView textvState) {
        if(macAddress != null){
            setMac_address(macAddress);
        }

        log_activity = Log_Activity.getINSTANCE();
        mainActivity = MainActivity.getINSTANCE();

        this.btConnectCallback = btConnectCallback;

        this.context = context;

        main_textStateBuffer = getMain_textvState();
        //지금 현재 클릭한 리스트뷰 말고, 이전에 클릭한 리스트뷰아이템의 텍스트뷰를 버퍼에 저장.

        this.main_textvState = textvState;


        bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();

        final BluetoothDevice device = bluetoothAdapter.getRemoteDevice(macAddress);


        //이미 연결되어 있는 GATT가 있다면 연결을 끊어야됨. 다중연결 방지
        if((bluetoothGatt != null && (bluetoothManager.getConnectionState(bluetoothGatt.getDevice(),BluetoothProfile.GATT)==BluetoothProfile.STATE_CONNECTED))){
            //블루투스 가트객체가 null이 아니고, 이미 연결되어 있다면
            Log.v("cnnt",bluetoothGatt.getDevice().getName()+" 이미 연결되어있음");
            bluetoothGatt.disconnect();
            //연결 끊어버림. (과제에서는 기기 1개만 선택(연결)되도록 시나리오가 짜여짐)
            if(bluetoothManager.getConnectionState(bluetoothGatt.getDevice(),BluetoothProfile.GATT) == BluetoothProfile.STATE_DISCONNECTED){
                Log.v("cnnt",bluetoothGatt.getDevice().getName()+" 연결끊김");
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        getMain_textStateBuffer().setText("연결 끊김");
                        Toast.makeText(context,"장치선택을 위해서, 이전에 연결했던 장치는 해제됩니다.",Toast.LENGTH_LONG).show();
                    }
                });
            }
        }
        try{
            if(mainActivity.getIs_Main_activity_on()){
                Log.v("cnnt","main acti on == true");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        connect_progress_dialog = new Connect_Progress_Dialog(getContext());
                        connect_progress_dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
                        connect_progress_dialog.connecting_state_setText("연결중 입니다...");
                        connect_progress_dialog.show();
                    }
                });
            }
            else if(log_activity.getIs_activity_on()){
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        log_textvState = log_activity.getTxtv_state();
                        log_textvState.setText("연결중...");
                    }
                });
            }

            bluetoothGatt = device.connectGatt(context, false, gattCallback);

        }catch (final IllegalArgumentException e){
            if(mainActivity.getIs_Main_activity_on()){
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(connect_progress_dialog!=null && connect_progress_dialog.isShowing()){
                            connect_progress_dialog.dismiss();
                            Toast.makeText(context,e.getMessage()+" 연결 실패",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
            else if(log_activity.getIs_activity_on()){
                log_textvState = log_activity.getTxtv_state();
                log_textvState.setText("연결 실패");
            }
        }
    }

    //gatt연결 메소드
    //macAdress , btConnectCallback 필요

    //btManager 객체 생성 (getSystemService BLUETOOTH_SEVICE)
    //bt아답터 객체 생성
    //블루투스 디바이스 객체 = 블루투스 아답터.getRemoteDevice(MAC주소);
    //bt가트 = device.connectGatt(컨텍스트, true, gatt콜백);


    //getRemoteDevice(MAC주소)
        //지정된 Bluetooth 하드웨어 주소에 대한 BluetoothDevice 개체를 가져옴

    //connectGatt()
        //이 장치에서 호스팅하는 GATT 서버에 연결합니다.
        //
        //호출자는 GATT 클라이언트 역할을 합니다. 콜백은 연결 상태 및 추가 GATT 클라이언트 작업과 같은 호출자에게 결과를 제공하는 데 사용됩니다.
        //
        //메서드는 BluetoothGatt 인스턴스를 반환합니다.
        //
        //BluetoothGatt를 사용하여 GATT 클라이언트 작업을 수행할 수 있습니다.


    /**
     * 프로토콜 보내기 write 를 하고 ble장치로부터 값을 받으면 onCharacteristicChanged() 메소드가 호출 된다
     */
    public void sendProtocol(String protocol, BluetoothWriteImpl btWriteCallback) {
        if (isConnected()) {
            //Log.v("BLELOG","sendProtocol() : 프로토콜을 보내는 메소드");
            this.btWriteCallback = btWriteCallback;
            protocol = "<" + protocol.toUpperCase() + ">";
            //writeGattCharacteristic.setValue(protocol);
            //bluetoothGatt.writeCharacteristic(writeGattCharacteristic);
        }
    }
    //프로토콜 보내는 메소드
    //만약 연결되었으면
    //전송 콜백에 파라메터 전송 콜백값 넣음
    //프로토콜 = "< 프로토콜대문자 >"
    //전송하면 writeCharicteristic 호출


    /**
     * 블루투스 콜백
     */
    private BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        final Handler handler = new Handler(Looper.getMainLooper());

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, final int rssi, int status) {
            super.onReadRemoteRssi(gatt, rssi, status);
            handler.post(new Runnable() {
                @Override
                public void run() {
                    log_activity.getTxtv_rssi().setText("rssi "+String.valueOf(rssi));
                }
            });
        }

        /**
         * 연결상태가 변화 할때 마다 (연결, 끊김) 호출
         */
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            //Log.v("BLELOG","gattCallback => onConnectionStateChange() : 클라이언트가 원격에 연결/연결해제된 시기를 나타내는 콜백");

            if (newState == BluetoothProfile.STATE_CONNECTED) {
                //Log.v("BLELOG","newState == BluetoothProfile.STATE_CONNECTED 새로운 연결이 발견됨. discoverService()호출 : 원격 장치에서 제공하는 서비스뿐만 아니라 해당 특성 및 설명자도 검색");
                if(mainActivity.getIs_Main_activity_on()){
                    Log.v("cnnt","main acti on == true");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if(connect_progress_dialog!=null && connect_progress_dialog.isShowing()){
                                connect_progress_dialog.connecting_state_setText("서비스 탐색 중...");
                            }
                        }
                    });
                }
               else if(log_activity.getIs_activity_on()){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            log_textvState = log_activity.getTxtv_state();
                            log_textvState.setText("서비스 탐색 중...");
                        }
                    });
                }
/*
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(connectView_textv_state != null){
                            connectView_textv_state.setText("연결 됨.\n서비스 탐색 중...");
                        }

                        if(logFragment_textv_state != null){
                            logFragment_textv_state.setText("서비스 탐색 중... ");

                        }
                    }
                });
*/

                bluetoothGatt.discoverServices(); // onServicesDiscovered() 호출 (서비스 연결 위해 꼭 필요)
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                if(mainActivity.getIs_Main_activity_on()){
                    Log.v("cnnt","main acti on == true");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if(connect_progress_dialog!=null && connect_progress_dialog.isShowing()){
                                connect_progress_dialog.dismiss();
                            }
                            getMain_textvState().setText("연결 끊김");
                            getMain_textvState().setVisibility(View.VISIBLE);
                        }
                    });
                }
                else if(log_activity.getIs_activity_on()){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            log_textvState = log_activity.getTxtv_state();
                            log_textvState.setText("연결 끊김");
                        }
                    });
                }

            }
        }
        /* GATT 클라이언트가 원격에 연결/연결 해제된 시기를 나타내는 콜백
         * GATT 서버.
         *gatt : GATT 클라이언트
         *status : 연결 또는 연결 해제 작업의 상태
         *newStatus : 새 연결상태 */



        //원격 장치에서 제공하는 서비스뿐만 아니라 해당 특성 및 설명자도 검색합니다.
        //이것은 비동기식 작업입니다.
        // 서비스 검색이 완료되면 BluetoothGattCallback.onServicesDiscovered callback이 트리거됩니다.
        // 검색에 성공하면 getServices 기능을 사용하여 원격 서비스를 검색할 수 있습니다.
        //반환:
        //true, 원격 서비스 검색이 시작된 경우


        /**
         * 서비스 연결 후 ( Notification 설정 ) cf) setCharacteristicNotification 까지만 해도  Notification이 되지만 이 메소드의 콜백을 받지 못한다
         * (setCharacteristicNotification이 비동기로 완료되기 전에 통신을 한다면 에러가 난다) -> writeDescriptor가 완료 된 순간부터 통신이 가능하다
         */
        @Override
        public void onServicesDiscovered(final BluetoothGatt gatt, int status) {
/*            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(connectView_textv_state != null){
                        connectView_textv_state.setText("연결 됨.\n서비스 탐색완료. \nGatt 작업 중..");
                        btn_service.setEnabled(true);
                    }

                    if(logFragment_textv_state != null){
                        logFragment_textv_state.setText("Gatt 작업 중.. ");
                    }
                }
            });*/

            if (status == BluetoothGatt.GATT_SUCCESS) {
                setGatt(gatt);
                Log.v("cnnt","연결 성공");
                if(mainActivity.getIs_Main_activity_on()){
                    Log.v("cnnt","main acti on == true");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if(connect_progress_dialog!=null && connect_progress_dialog.isShowing()){
                                connect_progress_dialog.dismiss();
                            }
                            getMain_textvState().setText("연결 됨");
                            Log.v("cnnt", getMain_textvState().getText().toString());
                            getMain_textvState().setVisibility(View.VISIBLE);
                        }
                    });
                }else if(log_activity.getIs_activity_on()){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            log_textvState = log_activity.getTxtv_state();
                            log_textvState.setText("연결 됨");
                        }
                    });
                }
            }

        }
        /* 원격 서비스, 특성 및 설명자 목록이 표시될 때 콜백 호출
         * 원격 장치의 경우 업데이트되었으며, 새로운 서비스가 검색되었습니다.
         * 여기서 BLE장치가 가진 특성 취득
         *gatt : GATT클라이언트 호출
         *status : 새 장치가 탐색된 경우 */

        @Override
        public void onCharacteristicRead(final BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            Log.v("cnnt","onCharacteristicRead()");


        }
        /* 특성 읽기 작업의 결과를 보고하는 콜백
         *gatt : GATT클라이언트 호출
         *charicteristic : 연결된 원격장치에서 읽은 특성
         *status : 읽기 작업이 완료된 경우 */

        @Override
        public void onDescriptorWrite(final BluetoothGatt gatt, final BluetoothGattDescriptor descriptor, final int status) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    //Log.v("BLELOG","onDescriptorWrite() : 설명자 쓰기 작업의 결과를 나타내는 콜백  : ");

                    btConnectCallback.onSuccessConnect(gatt.getDevice()); // 통신 준비 완료
                }
            });
        }
        /* 설명자 전송 작업의 결과를 나타내는 콜백
         *gatt : GATT클라이언트 호출
         *descriptor : 연결된 원격 장치에 쓰였던 설명자.
         *status : 다음과 같은경우(BluetoothGatt.GATT_SUCESS)의 쓰기작업 결과 */

        /**
         * 가장 중요한 메소드, ble 기기의 값을 받아옴. BLE장치가 데이터를 송신해서 그것을 앱이 받으면 콜백함.
         */
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic) {

        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorRead(gatt, descriptor, status);
            Log.v("cnnt","onDescriptorRead");
        }
        /*원격 특성 알림의 결과로 콜백이 트리거됨
         *gatt : 특성이 연관된 GATT 클라이언트
         *characteristic : 원격지의 결과로 업데이트된 특성 */
    };

    public synchronized File Write_EMG_data(String data, int ch) throws IOException {
        File_Util file_util = new File_Util();
        String filename = "";
        String path = "/sdcard/mobicare_EMG/"+now_time+"/";

        switch (ch){
            case 1:
                filename = "ch1.txt";
                break;
            case 2:
                filename = "ch2.txt";
                break;
            case 3:
                filename = "ch3.txt";
                break;
            case 4:
                filename = "ch4.txt";
                break;
        }

        //폴더 생성
        File dir = file_util.makeDirectory(path);
        //파일 생성
        File file = file_util.makeFile(dir, (path+filename));

        //파일 쓰기
        file_util.writeFile(file , data);

        //미디어 스캐닝
        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE , Uri.parse("file://"+file));
        context.sendBroadcast(intent);

        return file;
    }

    public interface BluetoothConnectImpl {
        void onSuccessConnect(BluetoothDevice device);

        void onFailed();
    }

    public interface BluetoothWriteImpl {
        void onSuccessWrite(int status, String data) throws IOException;

        void onFailed(Exception e);
    }

    public ThreadClass getThreadClass(){
        ThreadClass threadClass = new ThreadClass(getGatt());
        return threadClass;
    }

    public class ThreadClass extends Thread{
        BluetoothGatt bluetoothGatt;
        public ThreadClass(BluetoothGatt bluetoothGatt) {
            this.bluetoothGatt = bluetoothGatt;
        }

        @Override
        public void run() {
            super.run();
            while (!(bluetoothManager.getConnectionState(bluetoothGatt.getDevice(),BluetoothProfile.GATT) == BluetoothProfile.STATE_DISCONNECTED)){
                //블루투스 연결이 끊기면 스레드 중지
                if(log_activity.getIs_activity_on() == false){
                    //로그 액티비티가 띄어져 있지 않으면 스레드 중지
                    break;
                }
                bluetoothGatt.readRemoteRssi();
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            if(log_activity.getIs_activity_on() == true){
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        log_activity.getTxtv_rssi().setText("rssi -0");
                    }
                });
            }
        }
    }


}