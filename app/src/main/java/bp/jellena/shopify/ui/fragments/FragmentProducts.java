package bp.jellena.shopify.ui.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.activeandroid.query.Select;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import bp.jellena.shopify.R;
import bp.jellena.shopify.data.db.Category;
import bp.jellena.shopify.data.db.Product;
import bp.jellena.shopify.events.ShopifyEvents;
import bp.jellena.shopify.ui.adapters.ProductListAdapter;
import butterknife.ButterKnife;
import butterknife.InjectView;
import de.greenrobot.event.EventBus;

/**
 * Created by bp on 01/10/14.
 */
public class FragmentProducts extends Fragment {

    @InjectView(R.id.frag_prod_list_view)
    ListView itemsListView;

    private Category currentCategory = null;
    private List<Product> products = new ArrayList<>();
    private ProductListAdapter itemsListAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle = this.getArguments();
        long categoryId = bundle.getLong(FragmentHome.BUNDLE_CATEGORY_ID, 0);

        if (categoryId <= 0)
            Toast.makeText(getActivity(), "There was an error. Category ID doesn't exist", Toast.LENGTH_LONG).show();

        if (currentCategory == null)
            currentCategory = new Select().from(Category.class).where("Id = ?", categoryId).executeSingle();

        itemsListAdapter = new ProductListAdapter(getActivity(), products);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_products, container, false);
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

    public void onEvent(ShopifyEvents.RefreshProductsEvent rpe) {
        refreshItemList();
    }

    public void onEvent(final ShopifyEvents.TutorialProducts tp) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                getActivity().findViewById(R.id.frag_prod_tutorial).setVisibility(tp.isStart() ? View.VISIBLE : View.GONE);
            }
        });
    }

    private void refreshItemList() {
        products.clear();
        List<Product> updates = new Select().from(Product.class).where("categoryId = ?", currentCategory.getId()).execute();
        products.addAll(updates);

        Collections.sort(products, new Comparator<Product>() {
            @Override
            public int compare(Product lhs, Product rhs) {
                return lhs.name.compareTo(rhs.name);
            }
        });

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                itemsListAdapter.notifyDataSetChanged();
            }
        });
    }
}
