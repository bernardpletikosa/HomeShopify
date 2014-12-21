package bp.jellena.shopify.ui.adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.activeandroid.Model;
import com.activeandroid.query.Select;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import bp.jellena.shopify.R;
import bp.jellena.shopify.data.db.Category;
import bp.jellena.shopify.data.db.Product;
import bp.jellena.shopify.data.db.ProductState;

public class ShopListAdapter extends ArrayAdapter<Product> {

    private Context context;

    private List<Product> data;
    private Map<Long, Integer> categoryColors = new HashMap<>();

    public ShopListAdapter(Context context, List<Product> data) {
        super(context, R.layout.products_list_row, data);
        this.data = data;
        this.context = context;

        List<Category> categories = new Select().from(Category.class).execute();
        for (Category category : categories) {
            categoryColors.put(category.getId(), category.color);
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = ((Activity) context).getLayoutInflater();
        convertView = inflater.inflate(R.layout.products_list_row, parent, false);

        Product product = data.get(position);

        ((TextView) convertView.findViewById(R.id.item_row_name_TV)).setText(product.name);

        switch (ProductState.getStateById(product.state)) {
            case EMPTY:
                convertView.findViewById(R.id.item_row_status_view).setBackgroundResource(R
                        .drawable.item_circle_empty);
                break;
            case BETWEEN:
                convertView.findViewById(R.id.item_row_status_view).setBackgroundResource(R.drawable.item_circle_between);
                break;
            case FULL:
                convertView.findViewById(R.id.item_row_status_view).setBackgroundResource(R
                        .drawable.item_circle_full);
                break;
        }

        setColor(convertView, categoryColors.get(product.categoryId));

        return convertView;
    }

    private void setColor(View view, int color) {
        GradientDrawable mDrawable = (GradientDrawable) view.findViewById(R.id.item_row_status_view)
                .getBackground();
        mDrawable.setColor(getContext().getResources().getColor(color));
        view.findViewById(R.id.item_row_status_view).setBackground(mDrawable);
    }
}
