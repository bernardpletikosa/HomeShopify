package bp.jellena.shopify.ui.adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import bp.jellena.shopify.R;
import bp.jellena.shopify.data.db.Product;
import bp.jellena.shopify.data.db.ProductState;

public class ProductListAdapter extends ArrayAdapter<Product> {

    private final int mColor;
    private Context mContext;
    private List<Product> mProducts;

    public ProductListAdapter(Context context, List<Product> data, int color) {
        super(context, R.layout.products_list_row, data);
        this.mProducts = data;
        this.mContext = context;
        this.mColor = color;
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

        setColor(convertView);

        return convertView;
    }

    private void setColor(View view) {
        GradientDrawable mDrawable = (GradientDrawable) view.findViewById(R.id.item_row_status_view)
                .getBackground();
        mDrawable.setColor(getContext().getResources().getColor(mColor));
        view.findViewById(R.id.item_row_status_view).setBackground(mDrawable);
    }
}
