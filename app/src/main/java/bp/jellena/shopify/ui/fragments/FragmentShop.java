package bp.jellena.shopify.ui.fragments;

import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
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
import bp.jellena.shopify.data.ShopifyCons;
import bp.jellena.shopify.data.db.Product;
import bp.jellena.shopify.events.ShopifyEvents;
import bp.jellena.shopify.ui.adapters.ShopListAdapter;
import butterknife.ButterKnife;
import butterknife.InjectView;
import de.greenrobot.event.EventBus;

public class FragmentShop extends Fragment {

    @InjectView(R.id.frag_shop_list_view) GridView mListView;
    @InjectView(R.id.frag_shop_tutorial) View mTutorialLayout;

    private List<Product> mProducts = new ArrayList<>();
    private ShopListAdapter mAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

        mAdapter = new ShopListAdapter(getActivity(), mProducts);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_shop, container, false);
        ButterKnife.inject(this, view);

        showShopTutorial();

        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Product product = mProducts.get(position);
                product.state = ++product.state % 3;
                product.save();
                mAdapter.notifyDataSetChanged();
            }
        });

        refreshProducts();

        return view;
    }
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ((ActionBarActivity) getActivity()).getSupportActionBar()
                .setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.main_color)));
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
        mProducts.clear();

        List<Product> updates = new Select().from(Product.class).execute();
        if (updates != null && !updates.isEmpty()) {
            mProducts.addAll(updates);

            Collections.sort(mProducts, new Comparator<Product>() {
                @Override
                public int compare(Product lhs, Product rhs) {
                    return lhs.state - rhs.state;
                }
            });

            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mAdapter.notifyDataSetChanged();
                }
            });
        }
    }

    private void showShopTutorial() {
        if (ShopifyCons.sp.getBoolean(ShopifyCons.TUT_SHOP_FIRST_USE, true)) {
            if (new Select().from(Product.class).execute().isEmpty()) {
                mTutorialLayout.setVisibility(View.VISIBLE);
            }
            ShopifyCons.sp.edit().putBoolean(ShopifyCons.TUT_SHOP_FIRST_USE, false).apply();
        }
    }
}
