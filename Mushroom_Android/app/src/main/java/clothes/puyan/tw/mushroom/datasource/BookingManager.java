package clothes.puyan.tw.mushroom.datasource;

/**
 * Created by Puyan on 6/25/15.
 */
public class BookingManager {
    private static BookingManager sharedInstance = new BookingManager();

    public static BookingManager getInstance() {
        return sharedInstance;
    }

    private BookingManager() {
    }
}
