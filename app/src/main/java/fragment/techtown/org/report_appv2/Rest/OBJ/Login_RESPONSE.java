package fragment.techtown.org.report_appv2.Rest.OBJ;

public class Login_RESPONSE {
    public String getCode() {
        return code;
    }
    String code;

    public String getMessage() {
        return message;
    }

    String message;

    public String string(){
        String string="code : "+this.code+"  message : "+this.message;

        return string;
    }
}
