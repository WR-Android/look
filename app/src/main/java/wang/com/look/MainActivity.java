package wang.com.look;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    public static final int REQUESTSUCESS = 0;
    public static final int REQUESTNOYFOUND = 1;
    public static final int REQUESTEXCEPTION = 2;
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
                default:
                    break;
            }

        }
    };

    private static final String TAG = "MainActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        et_path = (EditText) findViewById(R.id.et_path);
        tv_result = (TextView) findViewById(R.id.tv_result);
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
