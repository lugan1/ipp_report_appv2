package fragment.techtown.org.report_appv2;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import java.io.IOException;

import fragment.techtown.org.report_appv2.Rest.Ketti_API_Method;
import fragment.techtown.org.report_appv2.Rest.OBJ.Sign_Up_POST;
import fragment.techtown.org.report_appv2.Rest.OBJ.Login_RESPONSE;
import fragment.techtown.org.report_appv2.Util.Time_Utility;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CreateAcc_Activity extends AppCompatActivity {
    Button btn_createPOST, btn_cancel;

    Sign_Up_POST sign_up_post;
    Login_RESPONSE login_response;


    RadioGroup rdioGroup_gender;

    String gender;

    private static final int FINISH = 300;
    private static final int CERATEPOST = 301;

    EditText edit_id, edit_pw, edit_name, edit_phone, edit_birth, edit_height, edit_weight;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_createacc);

        sign_up_post = new Sign_Up_POST();
        login_response = new Login_RESPONSE();


        btn_createPOST = (Button)findViewById(R.id.create_btn_accept);
        btn_cancel = (Button)findViewById(R.id.create_btn_cancel);

        rdioGroup_gender = (RadioGroup)findViewById(R.id.create_radioGroup_gender);

        edit_id = (EditText)findViewById(R.id.create_edit_id);
        edit_pw = (EditText)findViewById(R.id.create_edit_password);
        edit_name = (EditText)findViewById(R.id.create_edit_name);
        edit_birth = (EditText)findViewById(R.id.create_edit_birth);
        edit_height = (EditText)findViewById(R.id.create_edit_height);
        edit_weight = (EditText)findViewById(R.id.create_edit_weight);
        edit_phone = (EditText)findViewById(R.id.create_edit_phone);


        btn_createPOST.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Create_POST_DIALOG();
            }
        });

        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(FINISH);
                finish();
            }
        });

    }

    public void Create_POST_DIALOG()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("전송 확인");
        builder.setMessage("전송(POST) 하시겠습니까?");
        builder.setPositiveButton("예",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Time_Utility utility = new Time_Utility();
                        Time_Utility.Time time = utility.getTime();
                        witchRadioButton();
                        getTexteditString();

                        Ketti_API_Method ketti_api_method = new Ketti_API_Method();
                        ketti_api_method.SignUp(sign_up_post, new Callback<Login_RESPONSE>() {
                            @Override
                            public void onResponse(Call<Login_RESPONSE> call, Response<Login_RESPONSE> response) {

                            }

                            @Override
                            public void onFailure(Call<Login_RESPONSE> call, Throwable t) {

                            }
                        });
                    }
                });
        builder.setNegativeButton("아니오",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
        builder.show();
    }


    public void witchRadioButton(){
        int radio_gender_id = rdioGroup_gender.getCheckedRadioButtonId();
        switch (radio_gender_id){
            case R.id.radioButton_man :
                gender = "male";
                sign_up_post.setGender(gender);
                break;

            case R.id.radiobutton_women :
                gender = "female";
                sign_up_post.setGender(gender);
                break;
        }
    }

    public void getTexteditString(){
        sign_up_post.setUser_id(edit_id.getText().toString());
        sign_up_post.setPassword(edit_pw.getText().toString());
        sign_up_post.setName(edit_name.getText().toString());
        sign_up_post.setBirth(edit_birth.getText().toString());
        sign_up_post.setWeight(edit_weight.getText().toString());
        sign_up_post.setHeight(edit_height.getText().toString());
        sign_up_post.setPhone(edit_phone.getText().toString());
    }

}
