package bp.jellena.shopify.ui.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.activeandroid.query.Select;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import bp.jellena.shopify.R;
import bp.jellena.shopify.data.db.Product;
import bp.jellena.shopify.events.ShopifyEvents;
import bp.jellena.shopify.ui.adapters.ProductListAdapter;
import butterknife.ButterKnife;
import butterknife.InjectView;
import de.greenrobot.event.EventBus;

/**
 * Created by bp on 01/10/2014.
 */
public class FragmentShop extends Fragment {

    @InjectView(R.id.frag_prod_list_view)
    ListView itemsListView;

    private List<Product> products = new ArrayList<>();
    private ProductListAdapter itemsListAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        itemsListAdapter = new ProductListAdapter(getActivity(), products);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_shop, container, false);
        ButterKnife.inject(this, view);

        itemsListView.setAdapter(itemsListAdapter);
        itemsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Product product = products.get(position);
                product.state = ++product.state % 3;
                product.save();
                itemsListAdapter.notifyDataSetChanged();
            }
        });

        itemsListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                EventBus.getDefault().post(new ShopifyEvents.EditProductEvent(products.get(position)));
                return true;
            }
        });

        refreshItemList();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }

    public void onEvent(ShopifyEvents.RefreshShopProducts rsp) {
        refreshItemList();
    }

    public void onEvent(ShopifyEvents.TutorialShop ts) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                getActivity().findViewById(R.id.frag_shop_tutorial).setVisibility(View.VISIBLE);
            }
        });
    }

    private void refreshItemList() {
        products.clear();
        List<Product> updates = new Select().from(Product.class).execute();
        products.addAll(updates);

        Collections.sort(products, new Comparator<Product>() {
            @Override
            public int compare(Product lhs, Product rhs) {
                return lhs.state - rhs.state;
            }
        });

        itemsListAdapter.notifyDataSetChanged();
    }

}
