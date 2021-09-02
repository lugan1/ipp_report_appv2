package fragment.techtown.org.report_appv2.Util;

import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class File_Util {
    /**
     * 디렉토리 생성
     * @return dir
     */
    public File makeDirectory(String dir_path){
        File dir = new File(dir_path);
        if (!dir.exists())
        {
            dir.mkdirs();
            //Log.i( "BLELOG" , "!dir.exists" );
        }
        return dir;
    }

    /**
     * 파일 생성
     * @param dir
     * @return file
     */
    public File makeFile(File dir , String file_path){
        File file = null;
        boolean isSuccess = false;
        if(dir.isDirectory()){
            file = new File(file_path);
            if(file!=null&&!file.exists()){
                //Log.i( "BLELOG" , "!file.exists" );
                try {
                    isSuccess = file.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally{
                    //Log.i("BLELOG", "파일생성 여부 = " + isSuccess);
                }
            }
        }
        return file;
    }

    /**
     * (dir/file) 절대 경로 얻어오기
     * @param file
     * @return String
     */
    public String getAbsolutePath(File file){
        return ""+file.getAbsolutePath();
    }

    /**
     * (dir/file) 삭제 하기
     * @param file
     */
    public boolean deleteFile(File file){
        boolean result;
        if(file!=null&&file.exists()){
            file.delete();
            result = true;
        }else{
            result = false;
        }
        return result;
    }

    /**
     * 파일여부 체크 하기
     * @param file
     * @return
     */
    public boolean isFile(File file){
        boolean result;
        if(file!=null&&file.exists()&&file.isFile()){
            result=true;
        }else{
            result=false;
        }
        return result;
    }

    /**
     * 디렉토리 여부 체크 하기
     * @param dir
     * @return
     */
    public boolean isDirectory(File dir){
        boolean result;
        if(dir!=null&&dir.isDirectory()){
            result=true;
        }else{
            result=false;
        }
        return result;
    }

    /**
     * 파일 존재 여부 확인 하기
     * @param file
     * @return
     */
    public boolean isFileExist(File file){
        boolean result;
        if(file!=null&&file.exists()){
            result=true;
        }else{
            result=false;
        }
        return result;
    }

    /**
     * 파일 이름 바꾸기
     * @param file
     */
    public boolean reNameFile(File file , File new_name){
        boolean result;
        if(file!=null&&file.exists()&&file.renameTo(new_name)){
            result=true;
        }else{
            result=false;
        }
        return result;
    }

    /**
     * 디렉토리에 안에 내용을 보여 준다.
     * @return
     */
    public String[] getList(File dir){
        if(dir!=null&&dir.exists())
            return dir.list();
        return null;
    }

    /**
     * 파일에 내용 쓰기
     * @param file
     * @param file_content
     * @return
     */
    //byte[] file_content
    public boolean writeFile(File file , String file_content){
        file_content = "\n"+file_content;
        boolean result;
        //FileOutputStream fos;
        FileWriter fileWriter;
        BufferedWriter bufferedWriter;
        if(file!=null&&file.exists()&&file_content!=null){
            try {
                //fos = new FileOutputStream(file);
                fileWriter = new FileWriter(file,true);
                OutputStreamWriter opwrite = new OutputStreamWriter(new FileOutputStream(file,true),"euc-kr");
                //ms949
                bufferedWriter = new BufferedWriter(opwrite);
                try {
                    //fileWriter.write(file_content);
                    bufferedWriter.write(file_content);
                    bufferedWriter.flush();
                    bufferedWriter.close();
                    //fileWriter.flush();
                    opwrite.close();
                    //fileWriter.close();
                    //fos.write(file_content);
                    //fos.flush();
                    //fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            result = true;
        }else{
            result = false;
        }
        return result;
    }

    /**
     * 파일 읽어 오기
     * @param file
     */
    public void readFile(File file) throws IOException {
        if(file!=null&&file.exists()){
            String path = file.getPath();
            BufferedReader br = null;
            try {
                br = new BufferedReader(new FileReader(path));
                String readStr = "";
                String str = null;
                while(((str = br.readLine()) != null)){
                    readStr += str +"\n";
                    Log.v("FWLOG","readStr : "+readStr);
                }
                br.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

        }


        /*FileReader fr = null ;
        BufferedReader bufrd = null ;

         char ch ;
         String data;
        try {
            // open file.
            fr = new FileReader(file) ;
            bufrd = new BufferedReader(fr) ;

            // read 1 char from file.
            data = bufrd.readLine();
            Log.v("FWLOG","file path : "+file.getPath() + "    file name : "+file.getName());
            Log.v("FWLOG","readFile : "+data);

            // close file.
            bufrd.close() ;
            fr.close() ;
        } catch (Exception e) {
            e.printStackTrace() ;
        }*/

/*        int readcount=0;
        if(file!=null&&file.exists()){
            try {
                Log.v("FWLOG","readFile () : "+file.getPath());

                FileInputStream fis = new FileInputStream(file);

                readcount = (int)file.length();
                byte[] buffer = new byte[readcount];
                fis.read(buffer);
                for(int i=0 ; i<file.length();i++){
                    Log.d("FWLOG", "data : "+buffer[i]);
                }
                fis.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }*/
    }

    /**
     * 파일 복사
     * @param file
     * @param save_file
     * @return
     */
    public boolean copyFile(File file , String save_file){
        boolean result;
        if(file!=null&&file.exists()){
            try {
                FileInputStream fis = new FileInputStream(file);
                FileOutputStream newfos = new FileOutputStream(save_file);
                int readcount=0;
                byte[] buffer = new byte[1024];
                while((readcount = fis.read(buffer,0,1024))!= -1){
                    newfos.write(buffer,0,readcount);
                }
                newfos.close();
                fis.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            result = true;
        }else{
            result = false;
        }
        return result;
    }
    
}
