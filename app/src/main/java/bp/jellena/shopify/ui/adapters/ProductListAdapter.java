package bp.jellena.shopify.ui.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import bp.jellena.shopify.R;
import bp.jellena.shopify.data.db.Product;
import bp.jellena.shopify.data.db.ProductState;

/**
 * Created by bp on 28/09/14.
 */
public class ProductListAdapter extends ArrayAdapter<Product> {

    private Context context;

    private List<Product> data;

    public ProductListAdapter(Context context, List<Product> data) {
        super(context, R.layout.products_list_row, data);
        this.data = data;
        this.context = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = ((Activity) context).getLayoutInflater();
        convertView = inflater.inflate(R.layout.products_list_row, parent, false);

        Product product = data.get(position);

        ((TextView) convertView.findViewById(R.id.item_row_name_TV)).setText(product.name);

        switch (ProductState.getStateById(product.state)) {
            case EMPTY:
                convertView.findViewById(R.id.item_row_status_view).setBackgroundResource(R.drawable.item_circle_empty);
                break;
            case BETWEEN:
                convertView.findViewById(R.id.item_row_status_view).setBackgroundResource(R.drawable.item_circle_between);
                break;
            case FULL:
                convertView.findViewById(R.id.item_row_status_view).setBackgroundResource(R.drawable.item_circle_full);
                break;
        }

        return convertView;
    }
}
