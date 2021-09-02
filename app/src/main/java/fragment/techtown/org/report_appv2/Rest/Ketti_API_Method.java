package fragment.techtown.org.report_appv2.Rest;

import android.util.Log;

import org.json.JSONObject;

import fragment.techtown.org.report_appv2.Rest.OBJ.Login_POST;
import fragment.techtown.org.report_appv2.Rest.OBJ.Sign_Up_POST;
import fragment.techtown.org.report_appv2.Rest.OBJ.Login_RESPONSE;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class Ketti_API_Method {
    Retrofit retrofit;
    ApiService apiService;
    JSONObject jsonObject;


    public Ketti_API_Method() {

    }

    public void init(){
        retrofit = new Retrofit.Builder().baseUrl(ApiService.API_URL).
                //addConverterFactory(ScalarsConverterFactory.create()).
                addConverterFactory(GsonConverterFactory.create()).build();
        //request를 전달할 url을 넣어서 생성

        apiService = retrofit.create(ApiService.class);
        //get, post와 같은 서비스를 이용하기 위해서 인터페이스 생성

    }

    public void SignUp(Sign_Up_POST sign_up_post, Callback<Login_RESPONSE> callback) {
        init();

        Call<Login_RESPONSE> BodyObj = apiService.SignUp(
                sign_up_post.getUser_id(),
                sign_up_post.getPassword(),
                sign_up_post.getName(),
                sign_up_post.getGender(),
                sign_up_post.getBirth(),
                Integer.parseInt(sign_up_post.getHeight()),
                Integer.parseInt(sign_up_post.getWeight()),
                sign_up_post.getPhone(),
                "user");

        BodyObj.enqueue(callback);
    }

    public void Login(Login_POST login_post, Callback<Login_RESPONSE> callback) {
        init();

        Call<Login_RESPONSE> BodyObj = apiService.Login(
            login_post.getId(),
            login_post.getPw());

        BodyObj.enqueue(callback);
    }



    /*public String API_errorTest(Response<Login_Response> response){
        String errorMessage = "";
        if(response.body() != null){
            Login_Response login_response = response.body();
            if(login_response.getResult() == false){
                Log.v("cnnt","로그인 실패");
                Log.v("cnnt","error Code : "+login_response.getError());
                Log.v("cnnt","message : "+login_response.getMessage());

                switch (login_response.getError()){
                    case 3 :
                        errorMessage = "아이디가 틀렸습니다.";
                        break;
                    case 257 :
                        errorMessage = "비밀번호가 틀렸습니다.";
                        break;
                    case 6 :
                        errorMessage = "비밀번호 틀림 횟수 초과. 계정이 잠겨졌습니다.";
                        break;
                    default :
                        errorMessage = "알수없는 오류. error code : "+login_response.getError()+" message : "+login_response.getMessage();
                        break;
                }
            }
            else if(login_response.getResult() == true){
                errorMessage = "success";
            }
        }
        return  errorMessage;
    }*/
}
