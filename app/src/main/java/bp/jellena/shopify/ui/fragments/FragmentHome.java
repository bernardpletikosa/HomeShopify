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
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import bp.jellena.shopify.R;
import bp.jellena.shopify.data.Fragments;
import bp.jellena.shopify.data.ShopifyCons;
import bp.jellena.shopify.data.db.Category;
import bp.jellena.shopify.events.ShopifyEvents;
import bp.jellena.shopify.ui.adapters.CategoryGridAdapter;
import butterknife.ButterKnife;
import butterknife.InjectView;
import de.greenrobot.event.EventBus;

public class FragmentHome extends Fragment {

    @InjectView(R.id.frag_home_category_grid) GridView mCategoryGrid;
    @InjectView(R.id.frag_home_tutorial) View mTutorialLayout;

    private List<Category> mCategories = new ArrayList<>();
    private CategoryGridAdapter mAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

        mAdapter = new CategoryGridAdapter(getActivity(), mCategories);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        ButterKnife.inject(this, view);

        mCategoryGrid.setAdapter(mAdapter);
        mCategoryGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (!(getActivity().getSupportFragmentManager().getFragments().get(0) instanceof FragmentProducts)) {
                    Bundle bundle = new Bundle();
                    bundle.putLong(ShopifyCons.PROD_BUNDLE_CATEGORY_ID, mCategories.get(position).getId());

                    int containerId = R.id.contentFrame;
                    if (getActivity().findViewById(R.id.detailsFrame) != null)
                        containerId = R.id.detailsFrame;

                    getActivity().getSupportFragmentManager().beginTransaction()
                            .replace(containerId, Fragment
                                    .instantiate(getActivity(), Fragments.PRODUCTS.getFragment(), bundle))
                            .commit();

                    EventBus.getDefault().post(new ShopifyEvents.CurrentCategory(mCategories.get(position)));
                }
            }
        });

        mCategoryGrid.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                EventBus.getDefault().post(new ShopifyEvents.EditCategoryEvent(mCategories.get(position)));
                return true;
            }
        });

        showTutorial();

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ((ActionBarActivity) getActivity()).getSupportActionBar()
                .setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.main_color)));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
        refreshCategoryList();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        getActivity().getMenuInflater().inflate(R.menu.home_menu, menu);
    }

    public void onEvent(ShopifyEvents.RefreshCategoryEvent rce) {
        refreshCategoryList();
    }

    private void refreshCategoryList() {
        mCategories.clear();
        List<Category> refreshedCategories = new Select().from(Category.class).execute();
        mCategories.addAll(refreshedCategories);

        Collections.sort(mCategories, new Comparator<Category>() {
            @Override
            public int compare(Category lhs, Category rhs) {
                return lhs.name.compareTo(rhs.name);
            }
        });

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mAdapter.notifyDataSetChanged();
            }
        });
    }

    private void showTutorial() {
        if (ShopifyCons.sp.getBoolean(ShopifyCons.TUT_HOME_FIRST_USE, true)) {
            ShopifyCons.sp.edit().putBoolean(ShopifyCons.TUT_HOME_FIRST_USE, false).apply();

            mTutorialLayout.setVisibility(View.VISIBLE);

            ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
            executor.schedule(new Runnable() {
                @Override
                public void run() {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mTutorialLayout.setVisibility(View.GONE);
                        }
                    });
                }
            }, 7, TimeUnit.SECONDS);
        }
    }
}
