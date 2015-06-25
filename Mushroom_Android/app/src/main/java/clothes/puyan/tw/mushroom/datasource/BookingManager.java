package clothes.puyan.tw.mushroom.datasource;

import com.parse.ParseObject;

import java.util.ArrayList;

/**
 * Created by Puyan on 6/25/15.
 */
public class BookingManager {
    private static BookingManager sharedInstance = new BookingManager();
    public static BookingManager getInstance() {
        return sharedInstance;
    }

    private ArrayList<ParseObject> arrayBookingItem=new ArrayList<>();

    private BookingManager() {
    }

    public boolean isContainItem(ParseObject item){
        for(ParseObject bookingItem : arrayBookingItem){
            if(bookingItem.getObjectId().equalsIgnoreCase(item.getObjectId())) return true;
        }
        return false;
    }

    public boolean addItem(ParseObject item){
        if(!isContainItem(item)){
            arrayBookingItem.add(item);
            return true;
        }
        return false;
    }

    public boolean deleteItem(ParseObject item){
        for(ParseObject bookingItem : arrayBookingItem){
            if(bookingItem.getObjectId().equalsIgnoreCase(item.getObjectId())){
                arrayBookingItem.remove(bookingItem);
                return true;
            }
        }
        return false;
    }

    public ParseObject[] getBookingArray(){
        if(arrayBookingItem.size()<=0) return null;

        ParseObject[] result=new ParseObject[arrayBookingItem.size()];

        for(int i=0;i<arrayBookingItem.size();i++){
            result[i]=arrayBookingItem.get(i);
        }

        return result;
    }

    public void clearBooking(){
        arrayBookingItem.clear();
    }
}
