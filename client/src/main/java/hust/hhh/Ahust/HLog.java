package hust.hhh.Ahust;

import android.util.Log;

/**
 * Created by huang on 17-9-7.
 */

public class HLog {

    //规定每段显示的最大长度(系统的Log有大小限制,所以对于较长的Log,进行分段输出)
    private static int LOG_MAXLENGTH = 2000;

    //第一等级.用于打印一些重复的信息.
    private static String TAG = "HHH";

    public static void lineV() {
        line(1);
    }

    public static void lineD() {
        line(2);
    }

    public static void lineI() {
        line(3);
    }

    public static void lineW() {
        line(4);
    }

    public static void lineE() {
        line(5);
    }

    private static void line(int level) {
        String s = " ----  ----  ----  ----  ----  ----  ----  ----  ----  ----  ----  ----  " +
                "----  ----  ----  ----  ----  ----  ----  ----  ----  ----  ----  ----  ----  ---- ";
        switch (level) {
            case 1:
                Log.v(TAG, s);
                break;
            case 2:
                Log.d(TAG, s);
                break;
            case 3:
                Log.i(TAG, s);
                break;
            case 4:
                Log.w(TAG, s);
                break;
            case 5:
                Log.e(TAG, s);
                break;
        }


    }

    public static void v(String msg) {
        msg = getMethodNames(new Throwable().getStackTrace()) + msg;
        segmentPrint(1, "" + msg);
    }

    public static void d(String msg) {
        msg = getMethodNames(new Throwable().getStackTrace()) + msg;
        segmentPrint(2, msg);
    }

    public static void i(String msg) {
        msg = getMethodNames(new Throwable().getStackTrace()) + msg;
        segmentPrint(3, msg);
    }

    public static void w(String msg) {
        msg = getMethodNames(new Throwable().getStackTrace()) + msg;
        segmentPrint(4, msg);
    }

    public static void e(String msg) {
        msg = getMethodNames(new Throwable().getStackTrace()) + msg;
        segmentPrint(5, msg);
    }

    /**
     * 获取当前所在的文件名称,方法名以及行数
     */
    private static String getMethodNames(StackTraceElement[] sElements) {
        String className = sElements[1].getFileName();
        String methodName = sElements[1].getMethodName();
        int lineNumber = sElements[1].getLineNumber();
        StringBuilder sb = new StringBuilder();
        sb.append(" [ ")
                .append(className)
                .append(" -- ")
                .append(methodName)
                .append(" -- ")
                .append(lineNumber)
                .append(" ]    ");
        return sb.toString();
    }


    /**
     * 超长字符串分多次输出
     */
    private static void segmentPrint(int level, String msg) {
        int strLength = msg.length();
        int start = 0;
        int end = LOG_MAXLENGTH;
        //长度超标,第一条输出函数名,行数,其他的直接输出结果
        for (int i = 0; i < 100; i++) {
            //剩下的文本还是大于规定长度则继续重复截取并输出
            if (strLength > end) {
                print(level, msg.substring(start, end));
                start = end;
                end = end + LOG_MAXLENGTH;
            } else {
                print(level, msg.substring(start, strLength));
                break;
            }
        }
    }

    private static void print(int level, String msg) {
        switch (level) {
            case 1:
                Log.v(TAG, msg);
                break;
            case 2:
                Log.d(TAG, msg);
                break;
            case 3:
                Log.i(TAG, msg);
                break;
            case 4:
                Log.w(TAG, msg);
                break;
            case 5:
                Log.e(TAG, msg);
                break;
        }
    }
}
