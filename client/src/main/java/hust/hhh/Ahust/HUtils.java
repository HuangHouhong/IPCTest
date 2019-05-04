package hust.hhh.Ahust;

import java.text.SimpleDateFormat;

/**
 * Created by huang on 17-9-15.
 */

public class HUtils {

    public static String formatData(long time){
        SimpleDateFormat dateFormat=new SimpleDateFormat(" yyyy - MM - dd ");
        return  dateFormat.format(time);
    }


    public static String formatTime(long time){
        SimpleDateFormat dateFormat=new SimpleDateFormat(" yyyy - MM - dd   hh:mm");
        return  dateFormat.format(time);
    }
}
