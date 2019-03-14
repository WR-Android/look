package wang.com.look;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    private EditText et_path;
    private TextView tv_result;
    private static final String TAG = "MainActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        et_path = (EditText) findViewById(R.id.et_path);
        tv_result = (TextView) findViewById(R.id.tv_result);
    }

    public void click(View v){
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
            //如果code==200说明请求成功
            if(code==200){
                //获取服务器返回的数据 是以流的形式返回的
                //由于把流转换成字符串是一个非常常见的操作 所以我们抽出一个工具类（utils）
                InputStream in = conn.getInputStream();
                //使用我们定义的工具类 把in转换成String
                String content = StreamTools.readStream(in);

                //把流里面的数据展示到TextView上
                tv_result.setText(content);
            }

        } catch (Exception e) {
            Log.i(TAG, "click: 1111");
            e.printStackTrace();
        }
    }

}
