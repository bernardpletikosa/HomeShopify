package bp.jellena.shopify.ui.adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.activeandroid.query.Select;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import bp.jellena.shopify.R;
import bp.jellena.shopify.data.db.Category;
import bp.jellena.shopify.data.db.Product;
import bp.jellena.shopify.data.db.ProductState;

public class ShopListAdapter extends ArrayAdapter<Product> {

    private Context mContext;
    private List<Product> mProducts;
    private Map<Long, Integer> mCategoryColors = new HashMap<>();

    public ShopListAdapter(Context context, List<Product> data) {
        super(context, R.layout.products_list_row, data);
        this.mProducts = data;
        this.mContext = context;

        List<Category> categories = new Select().from(Category.class).execute();
        for (Category category : categories) {
            mCategoryColors.put(category.getId(), category.color);
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
        convertView = inflater.inflate(R.layout.products_list_row, parent, false);

        Product product = mProducts.get(position);

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

        setColor(convertView, mCategoryColors.get(product.categoryId));

        return convertView;
    }

    private void setColor(View view, int color) {
        GradientDrawable mDrawable = (GradientDrawable) view.findViewById(R.id.item_row_status_view)
                .getBackground();
        mDrawable.setColor(getContext().getResources().getColor(color));
        view.findViewById(R.id.item_row_status_view).setBackground(mDrawable);
    }
}
