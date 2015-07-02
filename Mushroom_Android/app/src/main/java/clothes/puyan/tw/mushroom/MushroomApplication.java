package clothes.puyan.tw.mushroom;

import android.app.Application;

import com.parse.Parse;
import com.parse.ParseInstallation;

/**
 * Created by Puyan on 6/29/15.
 */
public class MushroomApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // Enable Local Datastore.
        Parse.initialize(this, "", "");
        
        ParseInstallation.getCurrentInstallation().saveInBackground();

        //
    }
}
