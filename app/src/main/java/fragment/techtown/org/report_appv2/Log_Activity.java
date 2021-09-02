package fragment.techtown.org.report_appv2;

import android.bluetooth.BluetoothGatt;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

public class Log_Activity extends AppCompatActivity {

    String DEVICE_NAME, DEVICE_MAC;
    int selectPOSITION;

    public TextView getTxtv_state() {
        return txtv_state;
    }


    static TextView txtv_state;

    public TextView getTxtv_rssi() {
        return txtv_rssi;
    }

    static TextView txtv_rssi;

    public boolean getIs_activity_on() {
        return Is_Log_activity_on;
    }

    static boolean Is_Log_activity_on = false;

    BluetoothModule bluetoothModule;

    private static class Log_holder{
        public static final Log_Activity INSTANCE = new Log_Activity();
    }

    public static Log_Activity getINSTANCE(){
        return Log_holder.INSTANCE;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log);
        bluetoothModule = BluetoothModule.getInstance();

        Bundle reciveOBJ = getIntent().getExtras();
        DEVICE_NAME = reciveOBJ.getString("name");
        DEVICE_MAC = reciveOBJ.getString("mac");
        selectPOSITION = reciveOBJ.getInt("position");

        txtv_state = (TextView)findViewById(R.id.log_txtv_state);
        txtv_rssi = (TextView)findViewById(R.id.log_txtv_rssi);

        TextView txtv_deviceName = (TextView)findViewById(R.id.log_txtv_deviceName);
        txtv_deviceName.setText(DEVICE_NAME);

        bluetoothModule.getThreadClass().start();

        Is_Log_activity_on = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        Is_Log_activity_on = true;
    }

    @Override
    protected void onStop() {
        super.onStop();
        Is_Log_activity_on = false;
    }

}
