package clothes.puyan.tw.mushroom;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import clothes.puyan.tw.mushroom.datasource.BookingManager;
import clothes.puyan.tw.mushroom.datasource.NewsAdaptor;


public class ClosetClassActivity extends AppCompatActivity {
    static public String CLASS_NAME_EXTRA="CLASS_NAME_EXTRA";
    static public String PARSE_CLASS_NAME_EXTRA="PARSE_CLASS_NAME_EXTRA";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_closetclass);
        setTitle(getIntent().getStringExtra(CLASS_NAME_EXTRA));

        ParseQuery queryNews=ParseQuery.getQuery(getIntent().getStringExtra(PARSE_CLASS_NAME_EXTRA));
        queryNews.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> objects, ParseException e) {
                if (e == null) {
                    LayoutInflater inflater=(LayoutInflater)getSystemService(LAYOUT_INFLATER_SERVICE);
                    LinearLayout llClothes=(LinearLayout) findViewById(R.id.llClothes);

                    for(final ParseObject cloth : objects){
                        RelativeLayout rlClothView=(RelativeLayout) inflater.inflate(R.layout.layout_cloth,llClothes,false);
                        llClothes.addView(rlClothView);

                        TextView tvPrice=(TextView)rlClothView.findViewById(R.id.tvClothPrice);
                        tvPrice.setText("$"+cloth.getInt("price"));

                        rlClothView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                View checkView=v.findViewById(R.id.imgViewSelect);
                                if(!BookingManager.getInstance().isContainItem(cloth)){
                                    checkView.setVisibility(View.VISIBLE);
                                    v.setBackgroundColor(Color.parseColor("#888888"));
                                    BookingManager.getInstance().addItem(cloth);
                                }else{
                                    checkView.setVisibility(View.INVISIBLE);
                                    v.setBackgroundColor(Color.parseColor("#444444"));
                                    BookingManager.getInstance().deleteItem(cloth);
                                }
                            }
                        });

                        final ImageView imgViewCloth=(ImageView) rlClothView.findViewById(R.id.imgViewCloth);
                        new AsyncTask<String, Void, Bitmap>() {
                            @Override
                            protected Bitmap doInBackground(String... params) {
                                String url = params[0];
                                return getBitmapFromURL(url);
                            }

                            @Override
                            protected void onPostExecute(Bitmap result) {
                                imgViewCloth.setImageBitmap(result);
                                super.onPostExecute(result);
                            }
                        }.execute(cloth.getString("imgUrl"));
                    }

                } else {

                }
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_closet_class, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_my_closet) {
            if(BookingManager.getInstance().getBookingArray()==null||
               BookingManager.getInstance().getBookingArray().length==0){
                AlertDialog.Builder builder = new AlertDialog.Builder(ClosetClassActivity.this);
                builder.setMessage("衣櫃裡還沒有衣服喔");
                builder.setPositiveButton("好",null);
                builder.create().show();

                return true;
            }
            Intent intent = new Intent(ClosetClassActivity.this, MyClosetActivity.class);
            startActivity(intent);
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private static Bitmap getBitmapFromURL(String imageUrl)
    {
        try
        {
            URL url = new URL(imageUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap bitmap = BitmapFactory.decodeStream(input);
            return bitmap;
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return null;
        }
    }
}
