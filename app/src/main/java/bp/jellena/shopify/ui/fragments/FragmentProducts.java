package bp.jellena.shopify.ui.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import com.activeandroid.query.Select;
import com.nhaarman.listviewanimations.appearance.simple.SwingLeftInAnimationAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import bp.jellena.shopify.R;
import bp.jellena.shopify.data.ShopifyConstants;
import bp.jellena.shopify.data.db.Category;
import bp.jellena.shopify.data.db.Product;
import bp.jellena.shopify.events.ShopifyEvents;
import bp.jellena.shopify.helpers.DBObjectsHelper;
import bp.jellena.shopify.ui.adapters.ProductListAdapter;
import bp.jellena.shopify.ui.views.ProductAddView;
import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;

public class FragmentProducts extends Fragment {

    @InjectView(R.id.frag_prod_list_view) GridView itemsListView;
    @InjectView(R.id.frag_prod_add_item_view) ProductAddView addItemView;
    @InjectView(R.id.frag_prod_add_prod_btn) View addNewProdBtn;
    @InjectView(R.id.frag_prod_tutorial) View tutorialLayout;

    private long mCategoryId;
    private int mColor;
    private List<Product> mProducts = new ArrayList<>();
    private ProductListAdapter itemsListAdapter;
    private Product editingProduct;
    private ProductAddView.AddProductListener productListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mCategoryId = getArguments().getLong(ShopifyConstants.PRODUCTS_BUNDLE_CATEGORY_ID, 0);

        Category cat = new Select().from(Category.class).where("Id = ?",
                mCategoryId).executeSingle();
        mColor = cat.color;

        itemsListAdapter = new ProductListAdapter(getActivity(), mProducts, mColor);

        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_products, container, false);
        ButterKnife.inject(this, view);

        addNewProdBtn.setBackgroundColor(getResources().getColor(mColor));

        showProductsTutorial();

        SwingLeftInAnimationAdapter animAdapter = new
                SwingLeftInAnimationAdapter(itemsListAdapter);
        animAdapter.setAbsListView(itemsListView);

        itemsListView.setAdapter(animAdapter);
        itemsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                scheduleAddProductRefresh(1);

                Product product = mProducts.get(position);
                product.state = ++product.state % 3;
                product.save();
                itemsListAdapter.notifyDataSetChanged();
            }
        });

        itemsListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                editingProduct = mProducts.get(position);

                addNewProdBtn.setVisibility(View.GONE);
                addItemView.initView(getProductListener(), editingProduct.name, mColor);
                scheduleAddProductRefresh(10);

                return true;
            }
        });

        refreshProducts();

        return view;
    }

    @OnClick(R.id.frag_prod_add_prod_btn)
    public void onAddNewProdClicked() {
        addNewProdBtn.setVisibility(View.GONE);

        addItemView.initView(getProductListener(), mColor);

        scheduleAddProductRefresh(10);
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

        if (handler != null) {
            handler.removeCallbacks(addViewRunnable);
        }
        addViewRunnable = null;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }

    public void onEvent(ShopifyEvents.RefreshProductsEvent rpe) {
        refreshProducts();
    }

    private void refreshProducts() {
        if (mCategoryId <= 0)
            return;

        if (mProducts != null)
            mProducts.clear();

        List<Product> updates = new Select().from(Product.class).where("CategoryId = ?",
                mCategoryId).execute();

        if (updates != null && !updates.isEmpty()) {
            mProducts.addAll(updates);

            Collections.sort(mProducts, new Comparator<Product>() {
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

    private Handler handler;
    private Runnable addViewRunnable;

    private void scheduleAddProductRefresh(int time) {
        if (addViewRunnable == null)
            addViewRunnable = new Runnable() {
                public void run() {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            addNewProdBtn.setVisibility(View.VISIBLE);
                            addItemView.setViewVisibility(false);
                        }
                    });
                }
            };

        if (handler == null)
            handler = new Handler();
        else
            handler.removeCallbacks(addViewRunnable);

        handler.postDelayed(addViewRunnable, time * 1000);
    }

    private ProductAddView.AddProductListener getProductListener() {
        if (productListener == null)
            productListener = new ProductAddView.AddProductListener() {
                @Override
                public void onAddEvent(String name) {
                    DBObjectsHelper.createProduct(getActivity(), editingProduct, name, mCategoryId);
                    scheduleAddProductRefresh(10);
                }

                @Override
                public void onDeleteEvent() {
                    DBObjectsHelper.removeItem(editingProduct);
                    Toast.makeText(getActivity(), "Product " + editingProduct.name + " deleted.", Toast.LENGTH_LONG).show();
                    scheduleAddProductRefresh(1);
                }

                @Override
                public void onTypeEvent() {
                    scheduleAddProductRefresh(7);
                }
            };

        return productListener;
    }

    private void showProductsTutorial() {
        if (ShopifyConstants.getSharedPrefs(getActivity()).getBoolean(ShopifyConstants.TUTORIAL_PRODUCTS_FIRST_USE, true)) {
            ShopifyConstants.getSharedPrefs(getActivity()).edit().putBoolean(ShopifyConstants.TUTORIAL_PRODUCTS_FIRST_USE, false).apply();

            tutorialLayout.setVisibility(View.VISIBLE);

            ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
            executor.schedule(new Runnable() {
                @Override
                public void run() {
                    tutorialLayout.setVisibility(View.GONE);
                }
            }, 7, TimeUnit.SECONDS);
        }
    }
}
