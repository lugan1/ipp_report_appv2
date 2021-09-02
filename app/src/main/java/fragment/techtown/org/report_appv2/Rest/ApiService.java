package fragment.techtown.org.report_appv2.Rest;


import fragment.techtown.org.report_appv2.Rest.OBJ.Login_RESPONSE;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface ApiService {
    // API Gateway Stage URL
    public static final String API_URL = "http://api.ifitness.pe.kr:4662/";

    @FormUrlEncoded
    @POST("SignUp")
    Call<Login_RESPONSE> SignUp(
            @Field("user_id") String user_id,
            @Field("password") String password,
            @Field("name") String name,
            @Field("gender") String gender,
            @Field("birth") String birth,
            @Field("height") int height,
            @Field("weight") int weight,
            @Field("phone") String phone,
            @Field("type") String user);
    //post로 String 보낼때는 Query 가 아닌 Field로 바꿔야함. , 그리고 @FromUrlEncoded 추가해야됨.

    @FormUrlEncoded
    @POST("Login")
    Call<Login_RESPONSE> Login(
            @Field("user_id") String user_id,
            @Field("password") String password);




/*    @FormUrlEncoded
    @POST("comments")
    Call<ResponseBody>getPostCommentsStr(@Field("postId") String postId);
    //post로 String 보낼때는 Query 가 아닌 Field로 바꿔야함. , 그리고 @FromUrlEncoded 추가해야됨.

    @Headers( "Content-Type: application/json; charset=utf8"  )
    @POST("Organization/SelectOrganization")
    Call<Select_Organization_Response>SelectOrganization(@Body String body);


    @Headers( "Content-Type: application/json; charset=utf8"  )
    @POST("Organization/SelectOrganizationByUser")
    Call<ResponseBody>SelectOrganizationByUser(@Body String body);


    @Headers( "Content-Type: application/json; charset=utf8"  )
    @POST("Manager/CreateAccount")
    Call<Create_Account_Response>CreateAccount(@Body String body);


   @Headers( "Content-Type: application/json; charset=utf8"  )
    @POST("Account/Login")
    Call<Login_Response>Login(@Body String body);


    @Headers( "Content-Type: application/json; charset=utf8"  )
    @POST("Account/Logout")
    Call<Logout_Response>Logout(@Body String body);


    @Headers( "Content-Type: application/json; charset=utf8"  )
    @POST("Account/SessionRenew")
    Call<Session_Renew_Response>Session_Renew(@Body String body);*/


}

