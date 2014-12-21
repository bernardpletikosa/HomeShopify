package bp.jellena.shopify.ui.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import com.activeandroid.query.Select;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import bp.jellena.shopify.R;
import bp.jellena.shopify.data.ShopifyConstants;
import bp.jellena.shopify.data.db.Product;
import bp.jellena.shopify.events.ShopifyEvents;
import bp.jellena.shopify.ui.adapters.ProductListAdapter;
import bp.jellena.shopify.ui.adapters.ShopListAdapter;
import butterknife.ButterKnife;
import butterknife.InjectView;
import de.greenrobot.event.EventBus;

public class FragmentShop extends Fragment {

    @InjectView(R.id.frag_shop_list_view)
    GridView itemsListView;

    @InjectView(R.id.frag_shop_tutorial)
    View tutorialLayout;

    private List<Product> products = new ArrayList<>();
    private ShopListAdapter itemsListAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

        itemsListAdapter = new ShopListAdapter(getActivity(), products);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_shop, container, false);
        ButterKnife.inject(this, view);

        showShopTutorial();

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

        refreshProducts();

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

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        getActivity().getMenuInflater().inflate(R.menu.shop_menu, menu);
    }

    public void onEvent(ShopifyEvents.RefreshShopProducts rsp) {
        refreshProducts();
    }

    private void refreshProducts() {
        products.clear();

        List<Product> updates = new Select().from(Product.class).execute();
        if (updates != null && !updates.isEmpty()) {
            products.addAll(updates);

            Collections.sort(products, new Comparator<Product>() {
                @Override
                public int compare(Product lhs, Product rhs) {
                    return lhs.state - rhs.state;
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

    private void showShopTutorial() {
        if (ShopifyConstants.getSharedPrefs(getActivity()).getBoolean(ShopifyConstants.TUTORIAL_SHOP_FIRST_USE, true)) {
            if (new Select().from(Product.class).execute().isEmpty()) {
                tutorialLayout.setVisibility(View.VISIBLE);
            }
            ShopifyConstants.getSharedPrefs(getActivity()).edit().putBoolean(ShopifyConstants.TUTORIAL_SHOP_FIRST_USE, false).apply();
        }
    }
}
