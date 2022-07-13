package datasets;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class DateTimeContervTest {

	public static void main(String[] argv) {
        Locale.setDefault(new Locale("en", "US", "UNI"));
        SimpleDateFormat sourceDateFormat = new SimpleDateFormat("E MMM dd HH:mm:ss yyyy");
        System.out.println(sourceDateFormat.format(new Date()));
        try {
            Date sourceDate = sourceDateFormat.parse("Thu Jan 1 19:30:00 1970");
            long time = sourceDate.getTime();
            System.out.println(time);
        } catch (Exception e) {
            e.printStackTrace();
        }
	}
}