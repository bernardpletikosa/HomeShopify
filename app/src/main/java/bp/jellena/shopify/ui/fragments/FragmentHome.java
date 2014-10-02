package bp.jellena.shopify.ui.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
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
import bp.jellena.shopify.data.Fragments;
import bp.jellena.shopify.data.db.Category;
import bp.jellena.shopify.events.ShopifyEvents;
import bp.jellena.shopify.ui.adapters.CategoryGridAdapter;
import butterknife.ButterKnife;
import butterknife.InjectView;
import de.greenrobot.event.EventBus;

/**
 * Created by bp on 01/10/14.
 */
public class FragmentHome extends Fragment {

    public static final String BUNDLE_CATEGORY_ID = "bundle_category_id";

    @InjectView(R.id.frag_home_category_grid)
    GridView categoryGrid;

    private List<Category> categories = new ArrayList<>();
    private CategoryGridAdapter customGridAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        customGridAdapter = new CategoryGridAdapter(getActivity(), categories);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        ButterKnife.inject(this, view);

        categoryGrid.setAdapter(customGridAdapter);
        categoryGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (!(getActivity().getSupportFragmentManager().getFragments().get(0) instanceof FragmentProducts)) {
                    Bundle bundle = new Bundle();
                    bundle.putLong(BUNDLE_CATEGORY_ID, categories.get(position).getId());

                    getActivity().getSupportFragmentManager().beginTransaction()
                            .replace(R.id.contentFrame, Fragment
                                    .instantiate(getActivity(), Fragments.PRODUCTS.getFragment(), bundle))
                            .commit();

                    EventBus.getDefault().post(new ShopifyEvents.CurrentCategory(categories.get(position)));
                }
            }
        });

        categoryGrid.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                EventBus.getDefault().post(new ShopifyEvents.EditCategoryEvent(categories.get(position)));
                return true;
            }
        });

        return view;
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

    public void onEvent(final ShopifyEvents.TutorialHome th) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                getActivity().findViewById(R.id.frag_home_tutorial).setVisibility(th.isStart() ? View.VISIBLE : View.GONE);
            }
        });
    }

    public void onEvent(ShopifyEvents.RefreshCategoryEvent rce) {
        refreshCategoryList();
    }

    private void refreshCategoryList() {
        categories.clear();
        List<Category> refreshedCategories = new Select().from(Category.class).execute();
        categories.addAll(refreshedCategories);

        Collections.sort(categories, new Comparator<Category>() {
            @Override
            public int compare(Category lhs, Category rhs) {
                return lhs.name.compareTo(rhs.name);
            }
        });

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                customGridAdapter.notifyDataSetChanged();
            }
        });
    }
}
