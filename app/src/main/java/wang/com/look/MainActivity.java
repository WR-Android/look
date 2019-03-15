package wang.com.look;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    public static final int REQUESTSUCESS = 0;
    public static final int REQUESTNOYFOUND = 1;
    public static final int REQUESTEXCEPTION = 2;
    public static final int IMAGEIN = 3;
    public static final int IMAGEIN_CACHE = 4;

    private static final String TAG = "MainActivity";
    private ImageView iv;
    private EditText et_path;
    private TextView tv_result;

    private Handler handler = new Handler(){
        @Override
        //在主线程里执行
        public void handleMessage(Message msg) {
            switch (msg.what){
                case REQUESTSUCESS:
                    String content = (String) msg.obj;
                    tv_result.setText(content);
                    break;
                case REQUESTNOYFOUND:
                    Toast.makeText(getApplicationContext(), "请求资源不存在", Toast.LENGTH_SHORT).show();
                    break;
                case REQUESTEXCEPTION:
                    Toast.makeText(getApplicationContext(),"错误地址",Toast.LENGTH_SHORT).show();
                    break;
                case IMAGEIN:
                    Bitmap bitmap = (Bitmap) msg.obj;
                    iv.setImageBitmap(bitmap);
                    Toast.makeText(getApplicationContext(), "图片显示成功", Toast.LENGTH_SHORT).show();
                    break;
                case IMAGEIN_CACHE:
                    iv.setImageBitmap((Bitmap) msg.obj);
                    Toast.makeText(getApplicationContext(), "缓存图片显示成功", Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        et_path = (EditText) findViewById(R.id.et_path);
        tv_result = (TextView) findViewById(R.id.tv_result);
        iv = (ImageView) findViewById(R.id.iv);
    }

    public void click_image(View v){
        new Thread(){public void run(){
            try {
                //获得访问图片路径
                String path = et_path.getText().toString().trim();

                File file = new File(getCacheDir(), Base64.encodeToString(path.getBytes(),Base64.DEFAULT));
                if(file.exists() && file.length()>0){
                    //使用缓存图片
                    Bitmap cachebitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                    Message msg = Message.obtain();
                    msg.obj = cachebitmap;
                    msg.what = IMAGEIN_CACHE;
                    handler.sendMessage(msg);
                    Toast.makeText(getApplicationContext(), "显示缓存图片", Toast.LENGTH_SHORT).show();
                }else {
                    //第一次访问网络获取数据

                    //创建url对象指定我们要访问的网址（路径）
                    URL url = new URL(path);

                    //拿到http对象 用于发送或接收数据
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                    //设置发送get请求
                    conn.setRequestMethod("GET"); //get要求大写 默认就是get请求

                    //设置请求的超时时间
                    conn.setConnectTimeout(5000);

                    //获取服务器返回的状态码
                    int code = conn.getResponseCode();

                    if (code == 200) {

                        //获取图片数据 不管什么数据都是以流的形式返回
                        InputStream in = conn.getInputStream();

                        //缓存图片 Google提供了一个缓存目录

                        FileOutputStream fos = new FileOutputStream(file);
                        int len = -1;
                        byte[] buffer = new byte[1024];
                        while ((len = in.read(buffer)) != -1) {
                            fos.write(buffer, 0, len);
                        }
                        fos.close();
                        in.close();

                        //通过位图工厂获取bitmap
                        Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());

                        Message msg = Message.obtain(); //使用msg的静态方法可以减少对象的创建
                        msg.obj = bitmap;
                        msg.what = IMAGEIN;
                        handler.sendMessage(msg);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }}.start();
    }

    public void click(View v){

        //创建一个子线程
        new Thread(){public void run(){

            try {
                //获取源码地址
                String path = et_path.getText().toString().trim();

                //创建url对象指定我们要访问的网址（路径）
                URL url = new URL(path);

                //拿到http对象 用于发送或接收数据
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                //设置发送get请求
                conn.setRequestMethod("GET"); //get要求大写 默认就是get请求

                //设置请求的超时时间
                conn.setConnectTimeout(5000);

                //获取服务器返回的状态码
                int code = conn.getResponseCode();

                //获取头部信息，获取到重定向后的地址（为了解决返回码为302的问题）
                if(code == 302){
                    String location = conn.getHeaderField("Location");
                    Log.d(TAG, "location " + location);
                    //为URLConnection设置请求参数（请求方式，连接的超时时间等）
                    url  = new URL(location);
                    conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    conn.setConnectTimeout(5000);
                    code = conn.getResponseCode();
                }

                //如果code==200说明请求成功
                if(code==200){
                    Log.d(TAG, "run: 777");
                    //获取服务器返回的数据 是以流的形式返回的
                    //由于把流转换成字符串是一个非常常见的操作 所以我们抽出一个工具类（utils）
                    InputStream in = conn.getInputStream();
                    //使用我们定义的工具类 把in转换成String
                    String content = StreamTools.readStream(in);

                    //创建一个msg对象
                    Message msg = new Message();
                    msg.what = REQUESTSUCESS;
                    msg.obj = content;

                    //拿着我们创建的handler（助手） 告诉系统我要更新UI
                    handler.sendMessage(msg); //发了一条消息，消息里携带了把数据放到了msg里面handlerMessage方法就会执行

                }else{
                    //请求资源不存在 Toast是一个View 也不能在子线程更新UI
                    Message msg = new Message();
                    msg.what = REQUESTNOYFOUND;
                    handler.sendMessage(msg);
                }

            } catch (Exception e) {
                e.printStackTrace();
                Message msg = new Message();
                msg.what = REQUESTEXCEPTION; //错误地址
                handler.sendMessage(msg);

            }

        }}.start();

    }

}
