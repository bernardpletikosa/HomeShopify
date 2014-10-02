package bp.jellena.shopify.ui.adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import bp.jellena.shopify.R;
import bp.jellena.shopify.data.db.Category;

/**
 * Created by bp on 28/09/14.
 */
public class CategoryGridAdapter extends ArrayAdapter<Category> {

    private Context context;
    private List<Category> data = new ArrayList<>();

    public CategoryGridAdapter(Context context, List<Category> data) {
        super(context, R.layout.category_view, data);
        this.context = context;
        this.data = data;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = ((Activity) context).getLayoutInflater();
        convertView = inflater.inflate(R.layout.category_view, parent, false);

        Category item = data.get(position);

        ((TextView) convertView.findViewById(R.id.category_name)).setText(item.name);

        ((GradientDrawable) convertView.findViewById(R.id.circleLayout).getBackground()).setColor(context.getResources().getColor(item.color));

        return convertView;
    }
}
