package fragment.techtown.org.report_appv2.Util;

import android.os.SystemClock;
import android.view.View;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Time_Utility {

    public Time time = new Time();

    public Time getTime(){
        return time;
    }

    public class Time{
        long systemTime;
        String gmtCode;
        String requestDateTime;

        public long getSystemTime() {
            return systemTime;
        }

        public String getGmtCode() {
            return gmtCode;
        }

        public String getRequestDateTime() {
            return requestDateTime;
        }

        public Time() {
            systemTime = System.currentTimeMillis();
            Date date = new Date(systemTime);
            date.getTime();
            //min api 24 ==> 26으로 올림
            //ZoneOffset offset  = ZonedDateTime.now().getOffset();
            //gmtCode = "GMT"+String.valueOf(offset);
            //gmtCode = gmtCode.replace(":","");

            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH_mm_ss");
            requestDateTime = simpleDateFormat.format(date);
        }
    }


}
