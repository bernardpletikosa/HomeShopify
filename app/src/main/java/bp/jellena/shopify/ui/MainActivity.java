package bp.jellena.shopify.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.activeandroid.query.Select;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import bp.jellena.shopify.R;
import bp.jellena.shopify.data.Fragments;
import bp.jellena.shopify.data.ShopifyConstants;
import bp.jellena.shopify.data.db.Category;
import bp.jellena.shopify.data.db.Product;
import bp.jellena.shopify.data.model.NavigationDrawerItem;
import bp.jellena.shopify.events.ShopifyEvents;
import bp.jellena.shopify.helpers.DBObjectsHelper;
import bp.jellena.shopify.ui.fragments.FragmentSettings;
import bp.jellena.shopify.ui.fragments.FragmentHome;
import bp.jellena.shopify.ui.fragments.FragmentProducts;
import bp.jellena.shopify.ui.fragments.FragmentShop;
import bp.jellena.shopify.ui.navigationdrawer.NavigationDrawerView;
import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import butterknife.OnItemClick;
import de.greenrobot.event.EventBus;

/**
 * Created by Michal Bialas on 19/07/14.
 *
 * @author Michal Bialas
 */
public class MainActivity extends ActionBarActivity {

    private static final String STATE_SELECTED_POSITION = "selected_navigation_drawer_position";
    private static final String STATE_NEW_CATEGORY = "adding_new_category";
    private static final String STATE_NEW_CATEGORY_COLOR = "adding_new_category_color";
    private static final String STATE_CURRENT_CATEGORY_SHOWING = "current_category_showing";

    private static final String STATE_FRAG_SETTINGS = "state_frag_sett";
    private static final String STATE_FRAG_SHOP = "state_frag_shop";

    private static List<Integer> data = Arrays.asList(R.color.blue_light, R.color.purple_light,
            R.color.green_light, R.color.orange_light, R.color.red_light, R.color.blue_dark,
            R.color.purple_dark, R.color.green_dark, R.color.orange_dark, R.color.red_dark,
            R.color.material_purple, R.color.material_pink, R.color.material_blue);
    private int currentSelectedPosition = 0;

    @InjectView(R.id.navigationDrawerListViewWrapper)
    NavigationDrawerView mNavigationDrawerListViewWrapper;

    @InjectView(R.id.linearDrawer)
    LinearLayout mLinearDrawerLayout;

    @InjectView(R.id.drawerLayout)
    DrawerLayout mDrawerLayout;

    @InjectView(R.id.leftDrawerListView)
    ListView leftDrawerListView;

    @InjectView((R.id.new_item_layout))
    LinearLayout newItemLayout;

    @InjectView(R.id.new_category_color_layout)
    LinearLayout newCategoryColorLayout;

    @InjectView(R.id.new_item_name)
    EditText newItemNameET;

    private ActionBarDrawerToggle mDrawerToggle;

    private List<NavigationDrawerItem> navigationItems;

    private boolean addingItem = false;
    private int newItemSelectedColor = -1;
    private long currentCategoryIdShowing;
    private Category editingCategory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().add(R.id.contentFrame,
                    Fragment.instantiate(MainActivity.this, Fragments.HOME.getFragment())).commit();
        } else {
            currentSelectedPosition = savedInstanceState.getInt(STATE_SELECTED_POSITION);
        }

        navigationItems = new ArrayList<>();
        navigationItems.add(new NavigationDrawerItem(getString(R.string.fragment_home), true));
        navigationItems.add(new NavigationDrawerItem(getString(R.string.fragment_shop), true));
        navigationItems.add(new NavigationDrawerItem(getString(R.string.fragment_settings),
                R.drawable.ic_action_settings, false));

        mNavigationDrawerListViewWrapper.replaceWith(navigationItems);

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                R.drawable.ic_navigation_drawer, R.string.navigation_drawer_open,
                R.string.navigation_drawer_close) {
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                supportInvalidateOptionsMenu();
            }

            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                getSupportActionBar().setTitle(getTitle());
                supportInvalidateOptionsMenu();
            }
        };

        mDrawerLayout.setDrawerListener(mDrawerToggle);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        for (final Integer color : data) {
            View view = getLayoutInflater().inflate(R.layout.category_color_view, null, false);
            ((GradientDrawable) view.getBackground()).setColor(getResources().getColor(color));
            newCategoryColorLayout.addView(view);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    newItemSelectedColor = color;
                    newItemLayout.getChildAt(0).setBackgroundColor(getResources().getColor(color));
                }
            });
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
        editingCategory = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_SELECTED_POSITION, currentSelectedPosition);

        outState.putBoolean(STATE_NEW_CATEGORY, addingItem);

        outState.putInt(STATE_NEW_CATEGORY_COLOR, newItemSelectedColor);

        outState.putLong(STATE_CURRENT_CATEGORY_SHOWING, currentCategoryIdShowing);

        outState.putBoolean(STATE_FRAG_SHOP, getSupportFragmentManager().getFragments().get(0) instanceof FragmentShop);
        outState.putBoolean(STATE_FRAG_SETTINGS, getSupportFragmentManager().getFragments().get(0) instanceof FragmentSettings);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();

        if (savedInstanceState == null)
            return;

        if (savedInstanceState.getBoolean(STATE_FRAG_SHOP)) {
            initShopFrag();
            return;
        }
        if (savedInstanceState.getBoolean(STATE_FRAG_SETTINGS)) {
            initSettingsFrag();
            return;
        }

        initHome();

        addingItem = savedInstanceState.getBoolean(STATE_NEW_CATEGORY);
        toogleNewItemLayout();

        currentCategoryIdShowing = savedInstanceState.getLong(STATE_CURRENT_CATEGORY_SHOWING);
        if (currentCategoryIdShowing > 0) {
            initCategory();
        } else {
            if (findViewById(R.id.detailsFrame) != null) {
                ((FrameLayout) findViewById(R.id.detailsFrame)).removeAllViews();
            }
        }

        newItemSelectedColor = savedInstanceState.getInt(STATE_NEW_CATEGORY_COLOR);
        if (newItemSelectedColor > 0)
            newItemLayout.getChildAt(0).setBackgroundColor(getResources().getColor(newItemSelectedColor));
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item))
            return true;

        switch (item.getItemId()) {
            case R.id.menu_action_add:
                addingItem = newItemLayout.getVisibility() != View.VISIBLE;
                toogleNewItemLayout();
                return true;
            case R.id.menu_action_refresh:
                EventBus.getDefault().post(new ShopifyEvents.RefreshShopProducts());
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @OnClick(R.id.new_item_add_btn)
    public void addItem() {
        if (newItemNameET.getText().toString() == null || newItemNameET.getText().toString().isEmpty()) {
            newItemNameET.setError("You must put some text here :)");
            return;
        }

        if ((getSupportFragmentManager().getFragments().get(0) instanceof FragmentHome) && newItemSelectedColor < 0) {
            Animation shake = AnimationUtils.loadAnimation(MainActivity.this, R.anim.shake);
            newCategoryColorLayout.startAnimation(shake);
            return;
        }

        if (getSupportFragmentManager().getFragments().get(0) instanceof FragmentHome)
            DBObjectsHelper.createCategory(getApplicationContext(), editingCategory, newItemNameET.getText().toString(), newItemSelectedColor);

        newItemSelectedColor = -1;
        newItemNameET.setText("");
        newItemLayout.getChildAt(0).setBackgroundResource(R.color.purple_dark);
    }

    @OnClick(R.id.new_item_delete_btn)
    public void removeItem() {
        if (DBObjectsHelper.removeItem(editingCategory))
            Toast.makeText(this, "Category " + editingCategory.name + " deleted.", Toast.LENGTH_SHORT).show();
    }

    @OnItemClick(R.id.leftDrawerListView)
    public void OnItemClick(int position, long id) {
        if (mDrawerLayout.isDrawerOpen(mLinearDrawerLayout)) {
            mDrawerLayout.closeDrawer(mLinearDrawerLayout);
            onNavigationDrawerItemSelected(position);

            selectItem(position);
        }
    }

    public void onEvent(ShopifyEvents.EditCategoryEvent ece) {
        this.editingCategory = ece.getCategory();

        addingItem = true;
        newItemSelectedColor = ece.getCategory().color;
        if (newItemSelectedColor > 0)
            newItemLayout.getChildAt(0).setBackgroundColor(getResources().getColor(newItemSelectedColor));

        newItemNameET.setText(ece.getCategory().name);

        newItemLayout.setVisibility(addingItem ? View.VISIBLE : View.GONE);
    }

    public void onEvent(ShopifyEvents.CurrentCategory cc) {
        this.currentCategoryIdShowing = cc.getCurrentCatategory().getId();
        reinitiateAddLayout();

        getSupportActionBar().setTitle(cc.getCurrentCatategory().name);
        newItemLayout.getChildAt(0).setBackgroundResource(R.color.purple_dark);
    }

    private void toogleNewItemLayout() {
        if (addingItem) {
            newItemNameET.requestFocus();
        } else {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(newItemNameET.getWindowToken(), 0);
        }

        newItemLayout.setVisibility(addingItem ? View.VISIBLE : View.GONE);
    }

    private void selectItem(int position) {
        if (leftDrawerListView != null) {
            leftDrawerListView.setItemChecked(position, true);

            navigationItems.get(currentSelectedPosition).setSelected(false);
            navigationItems.get(position).setSelected(true);

            currentSelectedPosition = position;
            getSupportActionBar().setTitle(navigationItems.get(currentSelectedPosition).getItemName());
        }

        if (mLinearDrawerLayout != null)
            mDrawerLayout.closeDrawer(mLinearDrawerLayout);
    }

    private void onNavigationDrawerItemSelected(int position) {
        switch (position) {
            case 0:
                if (!(getSupportFragmentManager().getFragments().get(0) instanceof FragmentHome)) {
                    Fragment homeFrag = getSupportFragmentManager().findFragmentByTag(Fragments.HOME.getFragment());
                    if (homeFrag == null)
                        homeFrag = Fragment.instantiate(MainActivity.this, Fragments.HOME.getFragment());

                    getSupportFragmentManager().beginTransaction().replace(R.id.contentFrame, homeFrag).commit();
                }
                break;
            case 1:
                if (!(getSupportFragmentManager().getFragments().get(0) instanceof FragmentShop)) {
                    initShopFrag();
                }
                break;
            case 2:
                if (!(getSupportFragmentManager().getFragments().get(0) instanceof FragmentSettings)) {
                    initSettingsFrag();
                }
                break;
        }

        reinitiateAddLayout();
    }

    private void initShopFrag() {
        Fragment shopFrag = getSupportFragmentManager().findFragmentByTag(Fragments.SHOP.getFragment());
        if (shopFrag == null)
            shopFrag = Fragment.instantiate(MainActivity.this, Fragments.SHOP.getFragment());

        getSupportFragmentManager().beginTransaction().replace(R.id.contentFrame, shopFrag).commit();
    }

    private void initSettingsFrag() {
        Fragment settFrag = getSupportFragmentManager().findFragmentByTag(Fragments.SETTINGS.getFragment());
        if (settFrag == null)
            settFrag = Fragment.instantiate(MainActivity.this, Fragments.SETTINGS.getFragment());

        getSupportFragmentManager().beginTransaction().replace(R.id.contentFrame, settFrag).commit();
    }

    private boolean doubleBackToExitPressedOnce = false;

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getFragments().get(0) instanceof FragmentProducts ||
                getSupportFragmentManager().getFragments().get(0) instanceof FragmentShop ||
                getSupportFragmentManager().getFragments().get(0) instanceof FragmentSettings) {

            currentCategoryIdShowing = -1;
            initHome();
        } else if (addingItem) {
            reinitiateAddLayout();
        } else {
            if (doubleBackToExitPressedOnce) {
                super.onBackPressed();
                return;
            }

            this.doubleBackToExitPressedOnce = true;
            Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    doubleBackToExitPressedOnce = false;
                }
            }, 2000);
        }
    }

    private void initCategory() {
        Bundle bundle = new Bundle();
        bundle.putLong(ShopifyConstants.PRODUCTS_BUNDLE_CATEGORY_ID, currentCategoryIdShowing);

        int containerId = R.id.contentFrame;
        if (findViewById(R.id.detailsFrame) != null)
            containerId = R.id.detailsFrame;

        getSupportFragmentManager().beginTransaction()
                .replace(containerId, Fragment
                        .instantiate(this, Fragments.PRODUCTS.getFragment(), bundle)).commit();
    }

    private void initHome() {
        reinitiateAddLayout();

        Fragment homeFrag = getSupportFragmentManager().findFragmentByTag(Fragments.HOME.getFragment());
        if (homeFrag == null)
            homeFrag = Fragment.instantiate(MainActivity.this, Fragments.HOME.getFragment());

        getSupportFragmentManager().beginTransaction().replace(R.id.contentFrame, homeFrag).commit();

        selectItem(currentSelectedPosition);
    }

    private void reinitiateAddLayout() {
        editingCategory = null;
        addingItem = false;
        newItemSelectedColor = -1;

        toogleNewItemLayout();
    }
}
