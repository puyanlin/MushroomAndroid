package clothes.puyan.tw.mushroom;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;

import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.SaveCallback;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;

import clothes.puyan.tw.mushroom.datasource.BookingManager;


public class MyClosetActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_closet);
        reloadData();

        findViewById(R.id.btnBooking).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MyClosetActivity.this);
                builder.setTitle("跟香菇說");
                final LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
                View bookingView=inflater.inflate(R.layout.layout_booking_dialog, null, false);
                builder.setView(bookingView);
                builder.setCancelable(true);

                final AlertDialog dialog=builder.create();
                dialog.setCanceledOnTouchOutside(false);

                final EditText etName=(EditText) bookingView.findViewById(R.id.etName);
                final EditText etPhone=(EditText) bookingView.findViewById(R.id.etPhone);
                final Switch swithRememberMe=(Switch) bookingView.findViewById(R.id.switchRemember);
                final ProgressBar progress=(ProgressBar) bookingView.findViewById(R.id.progressBooking);
                Button btnSubmit=(Button) bookingView.findViewById(R.id.btnSubmit);

                final SharedPreferences pref=getSharedPreferences("mushroom", 0);
                boolean remember=pref.getBoolean("remember",true);

                swithRememberMe.setChecked(remember);
                if(remember){
                    etName.setText(pref.getString("name",""));
                    etPhone.setText(pref.getString("phone",""));

                }

                btnSubmit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (etName.getText().toString().equalsIgnoreCase("")) {
                            etName.setError("記得填名字喔");
                            return;
                        }
                        if (etPhone.getText().toString().equalsIgnoreCase("")) {
                            etPhone.setError("記得填電話喔");
                            return;
                        }

                        pref.edit().putBoolean("remember", swithRememberMe.isChecked()).commit();

                        if (swithRememberMe.isChecked()) {
                            pref.edit().putString("name", etName.getText().toString()).commit();
                            pref.edit().putString("phone", etPhone.getText().toString()).commit();
                        } else {
                            pref.edit().putString("name", "").commit();
                            pref.edit().putString("phone", "").commit();
                        }

                        final ParseObject bookingData = new ParseObject("BookingList");
                        bookingData.put("customer", etName.getText().toString());
                        bookingData.put("phone", etPhone.getText().toString());

                        String[] items = new String[BookingManager.getInstance().getBookingArray().length];
                        int totalPrice=0;
                        for (int i = 0; i < BookingManager.getInstance().getBookingArray().length; i++) {
                            items[i] = BookingManager.getInstance().getBookingArray()[i].getString("mushroomId");
                            totalPrice+= BookingManager.getInstance().getBookingArray()[i].getInt("price");
                        }
                        bookingData.put("items", Arrays.asList(items));
                        bookingData.put("itemsData", Arrays.asList(BookingManager.getInstance().getBookingArray()));
                        bookingData.put("totalPrice", Integer.valueOf(totalPrice));
                        bookingData.put("handled", Boolean.valueOf(false));

                        progress.setVisibility(View.VISIBLE);

                        bookingData.saveInBackground(new SaveCallback() {
                            @Override
                            public void done(ParseException e) {
                                if(e==null) dialog.dismiss();

                                AlertDialog.Builder builder = new AlertDialog.Builder(MyClosetActivity.this);
                                builder.setTitle("預訂完成");

                                String msg="預定單號 "+bookingData.getObjectId()+" ，香菇會盡快與您聯絡，謝謝";

                                builder.setMessage(msg);
                                builder.setPositiveButton("好", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        BookingManager.getInstance().clearBooking();
                                        finish();
                                    }
                                });
                                builder.create().show();
                            }
                        });

                    }
                });


                dialog.show();


            }
        });

    }

    private void reloadData(){
        final LinearLayout llClosetContainer = (LinearLayout) findViewById(R.id.llMyClosetContainer);
        llClosetContainer.removeAllViews();
        final LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);

        for (final ParseObject cloth : BookingManager.getInstance().getBookingArray()) {
            final View closetView = inflater.inflate(R.layout.layout_closetrow, llClosetContainer, false);
            TextView tvName = (TextView) closetView.findViewById(R.id.tvClosetName);
            final String closetName=cloth.getString("DisplayName");
            tvName.setText(closetName);

            TextView tvDetail = (TextView) closetView.findViewById(R.id.tvClosetDetail);
            tvDetail.setText("$"+cloth.getInt("price"));

            llClosetContainer.addView(closetView);

            closetView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(MyClosetActivity.this);

                    View detailView = inflater.inflate(R.layout.layout_bookingdetail, null, false);
                    final ImageView imgItem = (ImageView) detailView.findViewById(R.id.imageViewDetail);

                    new AsyncTask<String, Void, Bitmap>() {
                        @Override
                        protected Bitmap doInBackground(String... params) {
                            String url = params[0];
                            return getBitmapFromURL(url);
                        }

                        @Override
                        protected void onPostExecute(Bitmap result) {
                            imgItem.setImageBitmap(result);
                            super.onPostExecute(result);
                        }
                    }.execute(cloth.getString("imgUrl"));

                    builder.setView(detailView);
                    builder.setPositiveButton("好", null);
                    builder.setNegativeButton("刪掉這件", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            BookingManager.getInstance().deleteItem(cloth);
                            dialog.dismiss();

                            if (BookingManager.getInstance().getBookingArray() == null ||
                                    BookingManager.getInstance().getBookingArray().length == 0) {
                                llClosetContainer.removeAllViews();
                                AlertDialog.Builder builder = new AlertDialog.Builder(MyClosetActivity.this);
                                builder.setMessage("衣櫃裡沒有衣服囉，挑幾件吧！");
                                builder.setPositiveButton("好", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        finish();
                                    }
                                });
                                builder.create().show();

                            } else {
                                reloadData();
                            }
                        }
                    });
                    builder.create().show();
                }
            });

            final ImageView closetImage = (ImageView) closetView.findViewById(R.id.imgViewClosetCover);
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
            }.execute(cloth.getString("imgUrl"));
        }
    }

    private static Bitmap getBitmapFromURL(String imageUrl) {
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
