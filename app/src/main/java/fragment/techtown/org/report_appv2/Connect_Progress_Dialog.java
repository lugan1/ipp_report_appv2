package fragment.techtown.org.report_appv2;

import android.app.Dialog;
import android.content.Context;
import android.view.Window;
import android.widget.TextView;

public class Connect_Progress_Dialog extends Dialog {
    TextView txtv_connecting_state;

    public Connect_Progress_Dialog(Context context) {
        super(context);

        requestWindowFeature(Window.FEATURE_NO_TITLE); // 지저분한(?) 다이얼 로그 제목을 날림
        setContentView(R.layout.connect_progress_dialog); // 다이얼로그에 박을 레이아웃
        txtv_connecting_state = (TextView)findViewById(R.id.txtv_connecting_state);
        txtv_connecting_state.setTag("connecting_state");
    }

    public void connecting_state_setText(String text){
        txtv_connecting_state.setText(text);
    }
}
