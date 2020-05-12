package weather.wu.com.more;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.orhanobut.logger.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import weather.wu.com.weather.R;

/**
 * Created by Administrator on 2017/2/28.
 */
public class BlueToolthActivity extends Activity {

    //定义组件
    TextView mStatusLabel;
    Button mConnectBtn, mSendBtn, mQuitBtn;
    TextView txReceived;
    //device var
    private ProgressDialog progressDialog;
    private BluetoothAdapter mBluetoothAdapter = null;
    private BluetoothSocket btSocket = null;
    private OutputStream outStream = null;
    private InputStream inStream = null;
    //这条是蓝牙串口通用的UUID，不要更改
    private static final UUID MY_UUID =
            UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static String address = "20:15:11:09:47:91"; // <==要连接的目标蓝牙设备MAC地址
    private ReceiveThread rThread=null;  //数据接收线程
    //接收到的字符串
    String ReceiveData="";
    MyHandler handler;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetoolth);
        //首先调用初始化函数
        Init();
        InitBluetooth();
        handler=new MyHandler();
        //连接按钮
        mConnectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //判断蓝牙是否打开
                if(!mBluetoothAdapter.isEnabled())
                {
                    mBluetoothAdapter.enable();
                }
                mBluetoothAdapter.startDiscovery();
                showProgressDialog("提示", "正在连接......");
                //创建连接
                new ConnectTask().execute(address);
            }
        });
        mQuitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(btSocket!=null)
                {
                    try {
                        btSocket.close();//关闭socket
                        btSocket=null;
                        if(rThread!=null)
                        {
                            rThread.join();
                        }
                       /* if(timer!=null){
                            timer.cancel();
                        }*/
                        mStatusLabel.setText("当前连接已断开");
//						etReceived.setText("");
                    } catch (IOException e) {

                        e.printStackTrace();
                    } catch (InterruptedException e) {

                        e.printStackTrace();
                    }
                }
            }
        });

        mSendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                new SendInfoTask().execute("1");
				/*
					      byte[] msgBuffer = etSend.getText().toString().getBytes();

					      System.out.println("to be send:"+etSend.getText().toString());

					      try {

					      System.out.println("size be send:"+msgBuffer.length);
				          outStream.write(msgBuffer);

					       } catch (IOException e) {
					           Log.e("error", "ON RESUME: Exception during write.", e);
					           //return "发送失败";
				       }
					      System.out.println("发送成功");
					      */

            }
        });
    }

    /**
     * 显示加载框
     * @param title
     * @param message
     */
    public void showProgressDialog(String title, String message) {
        if (progressDialog == null) {
            progressDialog = ProgressDialog.show(BlueToolthActivity.this, title,
                    message, true, false);
        } else if (progressDialog.isShowing()) {
            progressDialog.setTitle(title);
            progressDialog.setMessage(message);
        }
        progressDialog.show();

    }

    /**
     * 隐藏加载框
     */
    public void hideProgressDialog() {

        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }

    }

    /**
     * 初始化控件
     */
    public void Init()
    {
        mStatusLabel =(TextView)this.findViewById(R.id.textView1);
        mConnectBtn =(Button)this.findViewById(R.id.blue_btn_conn);
        mSendBtn =(Button)this.findViewById(R.id.blue_btn_send);
        mQuitBtn =(Button)this.findViewById(R.id.blue_btn_disconn);
        mQuitBtn.setClickable(false);
        mSendBtn.setClickable(false);
      //  etSend=(EditText)this.findViewById(R.id.editText1);
        txReceived=(TextView)this.findViewById(R.id.blue_rec_temp);
    }

    /**
     * 初始化蓝牙适配器
     */
    public void InitBluetooth()
    {
        //得到一个蓝牙适配器
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(mBluetoothAdapter == null)
        {
            Toast.makeText(this, "你的手机不支持蓝牙", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
    }
/*    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }*/

    /**
     * 异步连接蓝牙
     */
    class ConnectTask extends AsyncTask<String,String,String>
    {
        @Override
        protected String doInBackground(String... params) {
            BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(params[0]);
            try {
                btSocket = device.createRfcommSocketToServiceRecord(MY_UUID);//等同于传入Ip
                btSocket.connect();//连接
                Logger.e( "连接没有建立");
            } catch (IOException e) {
                try {
                    btSocket.close();
                    return "连接设备失败";
                } catch (IOException e2) {
                    Logger .e("无法关闭socket,连接失败", e2);
                    return "连接关闭失败";
                }
            }
            //取消搜索
            mBluetoothAdapter.cancelDiscovery();
            try {
                outStream = btSocket.getOutputStream();
                // inStream = btSocket.getInputStream();
            } catch (IOException e) {
                Logger.e("流创建失败", e);
                return "Socket 流创建失败";
            }
            return "设备连接成功";
        }
        //这个方法是在主线程中运行的，所以可以更新界面
        @Override
        protected void onPostExecute(String result) {
            //连接成功则启动监听
            rThread=new ReceiveThread();
            rThread.start();
            mStatusLabel.setText(result);//设置状态
            hideProgressDialog();//隐藏对话框
            if("设备连接成功".equals(result)){
                mQuitBtn.setClickable(true);
                mSendBtn.setClickable(true);
            }
    /*        Message message = Message.obtain();
            message.what =2;
           handler.sendMessageDelayed(message,2000);*/
       /*  if("蓝牙连接正常,Socket 创建成功".equals(result)) {

         }*/

               /*  if(timer.){
                   Timer timer = new Timer();
                   timer.schedule(task, 2000, 5000);
               }else{
                   timer.schedule(task, 2000, 5000);
               }

            }*/

            super.onPostExecute(result);
        }
    }

    /**
     * 发送数据到蓝牙设备的异步任务
     */
    class SendInfoTask extends AsyncTask<String,String,String>
    {
        @Override
        protected String doInBackground(String... arg0) {
            if(btSocket==null)
            {
                return "还没有创建连接";
            }
            /*if(arg0[0].length()>0)//不是空白串
            {*/
                //String target=arg0[0];
                try {
                    byte[] msgBuffer = arg0[0].getBytes();
                    //  将msgBuffer中的数据写到outStream对象中
                    outStream.write(msgBuffer);
                } catch (IOException e) {
                    Logger.e("error", "ON RESUME: Exception during write.", e);
                    return "获取失败";
                }
            return "获取成功";
        }
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            mStatusLabel.setText(result);
            //将发送框清空
            //  etSend.setText("");
        }
    }
    //从蓝牙接收信息的线程
    class ReceiveThread extends Thread
    {
        String buffer="";
        @Override
        public void run() {
            while(btSocket!=null )
            {
                //定义一个存储空间buff，定义长度为1024
                byte[] buff=new byte[1024];
                try {
                    //从socket中获取数据
                    inStream = btSocket.getInputStream();
                   // System.out.println("waitting for instream");
                    inStream.read(buff); //读取数据存储在buff数组中
//                        System.out.println("buff receive :"+buff.length);
                    // ReceiveData = new String(buff,0,inStream.available());
                    //ReceiveData = new String(buff,"ASCII");
                    processBuffer(buff,1024);
                    //System.out.println("receive content:"+ReceiveData);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        /**
         * 格式化接收到的字节数组
         * @param buff
         * @param size
         */
        private void processBuffer(byte[] buff,int size)
        {
            int length=0;
            //计算buff的长度
            for(int i=0;i<size;i++)
            {
//				if(buff[i]>'\0')
                //未到最后一位，计算长度
                if(buff[i]>'\0')
                {
                    length++;
                }
                else
                {
                    break;
                }
            }

//			System.out.println("receive fragment size:"+length);

            byte[] newbuff=new byte[length];  //newbuff字节数组，用于存放真正接收到的数据

            for(int j=0;j<length;j++)
            {
                newbuff[j]=buff[j];
            }

            //ReceiveData=""++"C";
			ReceiveData=ReceiveData+new String(newbuff);
          //  byte[] newbuff1 = newbuff.

            /*String temp = "";
            temp = temp+new String(newbuff);
            Logger.e("Data",temp);*/
//			System.out.println("result :"+ReceiveData);
//			Message msg=new Message();  //by ywq

            Message msg=Message.obtain();
           // msg.obj = temp;
            msg.what=1;
            handler.sendMessageDelayed(msg,1000);  //发送消息:系统会自动调用handleMessage( )方法来处理消息
        }
    }

/*
    private final Timer timer = new Timer();

    //任务
    private TimerTask task = new TimerTask() {
        public void run() {
            Message msg = Message.obtain();
            msg.what = 2;
            handler.sendMessage(msg);
        }
    };*/


    //更新界面的Handler类
    class MyHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what){
                case 1:
                    txReceived.setText(ReceiveData);
                    break;
               /* case 2:
                    new SendInfoTask().execute("1");
                    break;*/
            }
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            if(rThread!=null)
            {
                if(btSocket!=null){
                    btSocket.close();
                    btSocket=null;
                }

                rThread.join();
            }
           /* if(timer!=null){
                timer.cancel();
            }*/
           /* if(timer!=null){
                timer.cancel();
            }*/
            this.finish();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

}
