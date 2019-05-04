package hust.hhh.client;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Parcelable;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import hust.hhh.Ahust.HLog;
import hust.hhh.server.Book;
import hust.hhh.server.IBookManager;
import hust.hhh.server.IOnNewBookArrivedListener;


public class MainActivity extends AppCompatActivity {


    private Button mAdd;
    private Button mGetAll;
    private LinearLayout mContainer;
    private EditText mInputBookName;
    private EditText mInputBookId;
    private boolean isConnected;
    private IBookManager mBookManager;

    //注册一个监听器，接受服务端发送来的信息.一定要继承stub，而不是IOnNewBookArrivedListener，否则会报错（NullPointException）
    private IOnNewBookArrivedListener mListener = new IOnNewBookArrivedListener.Stub() {

        @Override
        public void onNewBookArrived(String desc) throws RemoteException {
            HLog.i( " 监听器接受到服务器的信息 ： " + desc);
            Toast.makeText(MainActivity.this, desc, Toast.LENGTH_SHORT).show();
        }
    };


    //绑定服务端可能存在耗时操作，而当前ServiceConnection均是运行在UI线程，可能会ANR
    private ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            HLog.i("绑定服务器成功");
            mBookManager = IBookManager.Stub.asInterface(iBinder);
            isConnected = true;

            try {
                //绑定成功后设置死亡代理
                iBinder.linkToDeath(mDeathRecipient,0);

                //注册一个监听器
                mBookManager.registerListener(mListener);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            HLog.i("绑定服务器失败啦 ...... ");
            isConnected = false;
            Toast.makeText(MainActivity.this, "连接服务器失败啦 .....",
                    Toast.LENGTH_SHORT).show();
        }
    };

    //创建一个DeathRecipient对象，解决Binder断裂导致的问题
    private IBinder.DeathRecipient mDeathRecipient = new IBinder.DeathRecipient() {

        //如果Binder意外死亡，那么该方法就会受到Binder对象死亡通知。该方法是运行在Binder thread中
        @Override
        public void binderDied() {
            if (mBookManager!=null){
                mBookManager.asBinder().unlinkToDeath(mDeathRecipient,0);
                mBookManager = null;

                //重新绑定远程服务
                bindServer();
            }
        }
    };



    //创建一个Messenger对象，用于将客户端的其他线程切换到主线程
    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    updateUI(msg);
                    break;
                default:
                    Toast.makeText(MainActivity.this, "没有符合条件的Message对象",
                            Toast.LENGTH_SHORT).show();
            }
        }
    };


    /**
     * 更新UI显示。
     * 即显示出所有的书籍id和名称
     */
    private void updateUI(Message msg) {
        Bundle bundle = msg.getData();
        ArrayList<Book> bookList = bundle.getParcelableArrayList("bookList");
        mContainer.removeAllViews();
        for (Book book : bookList) {
            TextView tv = new TextView(this);
            tv.setText(book.bookId + " : " + book.bookName);
            mContainer.addView(tv);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();

        //绑定服务
        bindServer();

        //添加书籍
        mAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {

                    int id = Integer.parseInt(mInputBookId.getText().toString());
                    String name = mInputBookName.getText().toString();

                    //向服务端添加书籍
                    mBookManager.addBook(new Book(id, name));

                    //清空输入框
                    mInputBookId.getText().clear();
                    mInputBookName.getText().clear();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        });

        //获取书籍列表
        mGetAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {

                    //从服务端获取所有的书籍列表
                    List<Book> bookList = mBookManager.getBookList();

                    //发送到UI线程，更新UI
                    Message msg = Message.obtain();
                    msg.what = 1;
                    Bundle bundle = new Bundle();
                    bundle.putParcelableArrayList("bookList", (ArrayList<? extends Parcelable>) bookList);
                    msg.setData(bundle);
                    mHandler.sendMessage(msg);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void bindServer() {
        Intent intent = new Intent();
        intent.setAction("android.intent.action.IPCSERVER");
        intent.setPackage("hust.hhh.server");
        bindService(intent, conn, Context.BIND_AUTO_CREATE);
    }

    private void initView() {
        setContentView(R.layout.activity_main);
        mInputBookName = (EditText) findViewById(R.id.input_name);
        mInputBookId = (EditText) findViewById(R.id.input_id);
        mAdd = (Button) findViewById(R.id.bt_add);
        mGetAll = (Button) findViewById(R.id.bt_get_all);
        mContainer = (LinearLayout) findViewById(R.id.container_result);
    }


    @Override
    protected void onDestroy() {
        if (isConnected) {

            //反注册监听器
            try {
                mBookManager.unRegisterListener(mListener);
            } catch (RemoteException e) {
                e.printStackTrace();
            }

            //取消绑定
            unbindService(conn);
        }
        super.onDestroy();
    }
}
