package ddwu.mobile.finalproject.ma01_20181801;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.List;

public class MyPlaceAdapter extends BaseAdapter {

    public static final String TAG = "MyPlaceAdapter";

    private LayoutInflater inflater;
    private Context context;
    private int layout;
    private List<MyPlace> list;


    public MyPlaceAdapter(Context context, int resource, List<MyPlace> list) {
        this.context = context;
        this.layout = resource;
        this.list = list;
        inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }


    @Override
    public int getCount() {
        return list.size();
    }


    @Override
    public MyPlace getItem(int position) {
        return list.get(position);
    }


    @Override
    public long getItemId(int position) {
        return list.get(position).get_id();
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        Log.d(TAG, "getView with position : " + position);
        View view = convertView;
        ViewHolder viewHolder = null;

        if (view == null) {
            view = inflater.inflate(layout, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.tvTitle = view.findViewById(R.id.mkTitle);
            viewHolder.tvAddr = view.findViewById(R.id.mkAddr);
            viewHolder.ivImage = view.findViewById(R.id.mkImage);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder)view.getTag();
        }

        MyPlace dto = list.get(position);

        viewHolder.tvTitle.setText(dto.getTitle());
        viewHolder.tvAddr.setText(dto.getAddr1() + " " + dto.getAddr2());

//        Glide 를 사용하여 웹이미지 로딩
        Glide.with(context)
                .load(dto.getFirstimage())
                .into(viewHolder.ivImage);      // viewHolder의 이미지 뷰에 저장

        return view;

    }


    public void setList(List<MyPlace> list) {
        this.list = list;
        notifyDataSetChanged();
    }


//    ※ findViewById() 호출 감소를 위해 필수로 사용할 것
    static class ViewHolder {
        public TextView tvTitle = null;
        public TextView tvAddr = null;
        public ImageView ivImage = null;
    }


}
