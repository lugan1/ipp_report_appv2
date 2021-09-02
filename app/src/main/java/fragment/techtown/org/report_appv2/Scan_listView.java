package fragment.techtown.org.report_appv2;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;


public class Scan_listView extends LinearLayout {
    TextView textView1;
    TextView textView2;
    TextView textView3;
    TextView textView4;

    public Scan_listView(Context context) {
        super(context);
        init(context);
    }

    public Scan_listView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public void init(Context context){
        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.main_activity_list,this,true);

        textView1 = (TextView)findViewById(R.id.device_name);
        textView2 = (TextView)findViewById(R.id.device_address);
        textView3 = (TextView)findViewById(R.id.device_rssi);
        textView4 = (TextView)findViewById(R.id.main_txtv_state);

    }

    public void setName(String name){
        textView1.setText(name);
    }

    public void setAddress(String address){
        textView2.setText(address);
    }

    public void setRssi(String rssi) {
        textView3.setText(rssi);
    }

    public void setTextVTag(Object obj) {
        textView4.setTag(obj);
        //Log.v("cnnt","tag : "+String.valueOf(textView4.getTag()));
    }

}
