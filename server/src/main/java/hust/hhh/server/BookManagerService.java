package hust.hhh.server;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteCallbackList;
import android.os.RemoteException;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import hust.hhh.server.Ahust.HLog;

public class BookManagerService extends Service {
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        HLog.i("远程服务已经启动啦 ......");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new BookManager();
    }


    //CopyOnWriteArrayList是支持并发读写。解决了多个客户端来拿接服务器读写数据混乱的情况
    //注意，客户端与服务端必须不再一个进程中，否则客户端就会出现CopyOnWriteArrayList无法转化为ArrayList的错误
    private CopyOnWriteArrayList<Book> mBookList = new CopyOnWriteArrayList<>();

    //RemoteCallbackList是系统专门提供用于删除夸进程Listener的接口。他并不是一个List的实现类，他内部有一个Map专门保存AIDL的回调
    //注意，客户端发送的Listener与服务器中的Listener并不是一个对象（都不再一个进程中），RemoteCallbackList就是通过键值对的形式把两者联系在一起
    private RemoteCallbackList<IOnNewBookArrivedListener> mListenerList = new RemoteCallbackList<>();

    //创建一个类，继承在Aidl中定义的Interface。
    // 服务端的方法本省就运行在服务端的Binder线程池中，所以不需要开启子线程去执行异步操作，画蛇添足
    private class BookManager extends IBookManager.Stub {

        @Override
        public List<Book> getBookList() throws RemoteException {
            return mBookList;
        }

        @Override
        public void addBook(Book book) throws RemoteException {
            String desc = null;
            if (!mBookList.contains(book)) {
                mBookList.add(book);
                desc = book.bookId + " " + book.bookName + " 已经添加";
            } else {
                desc = book.bookId + " " + book.bookName + " 已经存在，不需要重复添加";
            }

            // 给每个监听器发送新书到达的消息
            //beginBroadcast与finishBroadcast必须配对使用，只要想RemoteCallbackList，就必须要先使用
            int len = mListenerList.beginBroadcast();
            for (int i = 0; i < len; i++) {
                HLog.i(" 监听器不为空，当前监听器序号为 ： " + i);

                //服务端调用客户端的监听器的方法，很显然，该监听器是运行在客户端的Binder线程池中
                IOnNewBookArrivedListener listener = mListenerList.getBroadcastItem(i);
                if (listener != null) {
                    listener.onNewBookArrived(desc);
                }
            }
            mListenerList.finishBroadcast();
        }

        @Override
        public void registerListener(IOnNewBookArrivedListener listener) throws RemoteException {
            mListenerList.register(listener);
        }

        @Override
        public void unRegisterListener(IOnNewBookArrivedListener listener) throws RemoteException {
            //实际上这里的listener对象在mListenerList并没有，RemoteCallbackList会根据键值对找到对应的listener
            boolean isUnregister = mListenerList.unregister(listener);
            if (isUnregister) {
                HLog.i("解除绑定Listener成功啦 ....");
            } else {
                HLog.i("解除绑定Listener失败 ？？？？？ ");
            }
        }
    }
}
