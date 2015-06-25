package clothes.puyan.tw.mushroom.datasource;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.parse.ParseObject;

import java.util.List;

import clothes.puyan.tw.mushroom.R;

/**
 * Created by Puyan on 6/25/15.
 */
public class NewsAdaptor extends ArrayAdapter<ParseObject> {
    public NewsAdaptor(Context context, int resource, List<ParseObject> objects) {
        super(context, resource, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LinearLayout newsView;
        ParseObject news=getItem(position);

        if(convertView==null){
            LayoutInflater inflater=(LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            newsView=(LinearLayout) inflater.inflate(R.layout.layout_news,parent,false);
        }else{
            newsView=(LinearLayout) convertView;
        }
        TextView tvTitle=(TextView) newsView.findViewById(R.id.tvType);

        switch ( news.getInt("Type") ){
            case 0:
                tvTitle.setText("新貨");
                newsView.setBackgroundColor(getContext().getResources().getColor(R.color.new_blue));
                break;
            default:
            case 1:
                tvTitle.setText("公告");
                newsView.setBackgroundColor(getContext().getResources().getColor(R.color.declare_green));
                break;
            case 2:
                tvTitle.setText("活動");
                newsView.setBackgroundColor(getContext().getResources().getColor(R.color.activty_gray));
                break;
            case 3:
                tvTitle.setText("公休");
                newsView.setBackgroundColor(getContext().getResources().getColor(R.color.holiday_red));
                break;
        }

        TextView tvContent=(TextView) newsView.findViewById(R.id.tvNewsContent);
        tvContent.setText(news.getString("Content"));

        return newsView;
    }
}
