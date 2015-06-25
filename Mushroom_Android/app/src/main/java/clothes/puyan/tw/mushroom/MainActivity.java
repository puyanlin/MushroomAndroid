package clothes.puyan.tw.mushroom;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.parse.*;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class MainActivity extends Activity {
    final String TAG="MainActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Enable Local Datastore.
        Parse.initialize(this, "", "");
        ParseInstallation.getCurrentInstallation().saveInBackground();


        LinearLayout llBtnFindMe=(LinearLayout) findViewById(R.id.llbtn_findMe);
        llBtnFindMe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.e(TAG, "clicked");

                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("香菇日韓服飾");

                LayoutInflater inflater = getLayoutInflater();
                ScrollView contactView = (ScrollView) inflater.inflate(R.layout.layout_contact, null, false);

                contactView.findViewById(R.id.lineRow).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String url = "line://ti/p/l6dsTkb1Bf";
                        Intent i = new Intent(Intent.ACTION_VIEW);
                        i.setData(Uri.parse(url));
                        startActivity(i);
                    }
                });
                contactView.findViewById(R.id.fbRow).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String url = "https://www.facebook.com/gogo.mushroom";
                        Intent i = new Intent(Intent.ACTION_VIEW);
                        i.setData(Uri.parse(url));
                        startActivity(i);
                    }
                });

                contactView.findViewById(R.id.phoneRow).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String number = "tel:0916242077";
                        Intent callIntent = new Intent(Intent.ACTION_CALL, Uri.parse(number));
                        startActivity(callIntent);
                    }
                });

                contactView.findViewById(R.id.addrRow).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String map = "http://maps.google.co.in/maps?q=" + "新北市三重區長壽西街54號";
                        Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(map));
                        startActivity(i);
                    }
                });

                contactView.findViewById(R.id.myMailRow).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent data = new Intent(Intent.ACTION_SENDTO);
                        data.setData(Uri.parse("mailto:puyanlinmailbox@gmail.com"));
                        startActivity(data);
                    }
                });

                builder.setView(contactView);
                builder.create().show();
            }
        });

        ParseQuery queryCloset=ParseQuery.getQuery("Closet");
        queryCloset.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> objects, ParseException e) {
                if (e == null) {
                    LinearLayout llClosetContainer=(LinearLayout) findViewById(R.id.llClosetContainer);

                    LayoutInflater inflater=(LayoutInflater)getSystemService(LAYOUT_INFLATER_SERVICE);
                    for(ParseObject closet:objects){
                        View closetView=inflater.inflate(R.layout.layout_closetrow,llClosetContainer,false);
                        TextView tvName=(TextView)closetView.findViewById(R.id.tvClosetName);
                        tvName.setText(closet.getString("Name"));

                        TextView tvDetail=(TextView) closetView.findViewById(R.id.tvClosetDetail);

                        boolean onSale=closet.getBoolean("onSale");
                        boolean isNew=closet.getBoolean("New");
                        if(onSale) tvDetail.setText("特價");
                        if(isNew) tvDetail.setText("新");

                        llClosetContainer.addView(closetView);

                        final ImageView closetImage=(ImageView) closetView.findViewById(R.id.imgViewClosetCover);
                        new AsyncTask<String, Void, Bitmap>() {
                            @Override
                            protected Bitmap doInBackground(String... params) {
                                String url = params[0];
                                return getBitmapFromURL(url);
                            }

                            @Override
                            protected void onPostExecute(Bitmap result) {
                                closetImage.setImageBitmap(result);
                                super.onPostExecute(result);
                            }
                        }.execute(closet.getString("CoverURL"));
                    }

                } else {

                }
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        final View progressGetOpenClose=findViewById(R.id.progressOpenClose);
        final View llInfo=findViewById(R.id.llOpenCloseInfo);
        final View rlOpenCloseView=findViewById(R.id.rlOpenClose);
        final View rlSale=findViewById(R.id.rlSales);

        rlOpenCloseView.setBackgroundColor(getResources().getColor(R.color.standard_yellow));
        progressGetOpenClose.setVisibility(View.VISIBLE);
        llInfo.setVisibility(View.INVISIBLE);
        rlSale.setVisibility(View.GONE);

        ParseConfig.getInBackground(new ConfigCallback() {
            @Override
            public void done(ParseConfig parseConfig, ParseException e) {
                boolean open = parseConfig.getBoolean("open");
                rlOpenCloseView.setBackgroundColor(getResources().getColor(open ? R.color.open_green : R.color.close_red));
                progressGetOpenClose.setVisibility(View.INVISIBLE);
                llInfo.setVisibility(View.VISIBLE);

                TextView tvOepnClose = (TextView) findViewById(R.id.tvOpenClose);
                tvOepnClose.setText(open ? "今天有開" : "今天沒開");
                String detailInfo;
                if (open) {
                    detailInfo = "營業時間 " + parseConfig.getString("openTime") + " ~ " + parseConfig.getString("closeTime");
                } else {
                    detailInfo = parseConfig.getString("closeReason");
                }
                TextView tvDetail = (TextView) findViewById(R.id.tvDescriptionOpenClose);
                tvDetail.setText(detailInfo);

                boolean onSale = parseConfig.getBoolean("onSale");
                rlSale.setVisibility(onSale ? View.VISIBLE : View.GONE);
                if (onSale) {
                    String saleInfo = parseConfig.getString("saleContent").replace("\\n", "\n");
                    //Log.e(TAG, saleInfo);
                    TextView tvSaleInfo = (TextView) findViewById(R.id.tvSalesInfo);
                    tvSaleInfo.setText(saleInfo);
                    tvSaleInfo.setTextSize(parseConfig.getInt("saleTextSize"));

                    final ImageView imgBg=(ImageView) findViewById(R.id.imgViewSalebg);

                    if(!parseConfig.getString("saleImage").equalsIgnoreCase("")) {
                        new AsyncTask<String, Void, Bitmap>() {
                            @Override
                            protected Bitmap doInBackground(String... params) {
                                String url = params[0];
                                return getBitmapFromURL(url);
                            }

                            @Override
                            protected void onPostExecute(Bitmap result) {
                                imgBg.setImageBitmap(result);
                                super.onPostExecute(result);
                            }
                        }.execute(parseConfig.getString("saleImage"));
                    }

                }
            }
        });


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
