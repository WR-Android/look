package wang.com.look;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class StreamTools {
    //把一个inputStream 转换成一个String
    public static String readStream(InputStream in){

        //定义一个内存输出流
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int len = -1;
        byte[] buffer = new byte[1024]; //1 kb
        try {
            while((len=in.read(buffer))!=-1){

                baos.write(buffer,0,len);
            }
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        String content = new String(baos.toByteArray());
        return content;
    }
}
