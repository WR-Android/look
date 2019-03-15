package wang.com.look;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
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

    private static final String TAG = "MainActivity";
    private ImageView iv;
    private EditText et_path;
    private TextView tv_result;

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
                    final Bitmap cachebitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            iv.setImageBitmap(cachebitmap);
                            Toast.makeText(getApplicationContext(), "缓存图片显示成功", Toast.LENGTH_SHORT).show();
                        }
                    });
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
                        final Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                iv.setImageBitmap(bitmap);
                                Toast.makeText(getApplicationContext(), "缓存图片显示成功", Toast.LENGTH_SHORT).show();

                            }
                        });

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
                    final String content = StreamTools.readStream(in);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            tv_result.setText(content);
                        }
                    });
                }else{
                    //请求资源不存在
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), "请求资源不存在", Toast.LENGTH_SHORT).show();
                        }
                    });
                }

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(),"错误地址",Toast.LENGTH_SHORT).show();
                    }
                });

            }

        }}.start();

    }

}
