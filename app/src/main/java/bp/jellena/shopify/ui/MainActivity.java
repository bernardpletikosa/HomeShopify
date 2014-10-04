package bp.jellena.shopify.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
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
    private Menu mMenu;
    private long currentCategoryIdShowing;
    private Category editingCategory;
    private Product editingProduct;

    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);

        prefs = getSharedPreferences(ShopifyConstants.SHARED_PREFERENCES, Context.MODE_PRIVATE);

        getSupportActionBar().setIcon(R.drawable.ic_launcher);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().add(R.id.contentFrame,
                    Fragment.instantiate(MainActivity.this, Fragments.HOME.getFragment())).commit();
            showHomeTutorial();
        } else {
            currentSelectedPosition = savedInstanceState.getInt(STATE_SELECTED_POSITION);
        }

        navigationItems = new ArrayList<>();
        navigationItems.add(new NavigationDrawerItem(getString(R.string.fragment_home), true));
        navigationItems.add(new NavigationDrawerItem(getString(R.string.fragment_shop), true));
        navigationItems.add(new NavigationDrawerItem(getString(R.string.fragment_settings),
                R.drawable.ic_navigation_drawer, false));

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
        editingProduct = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
    }

    private void initHome() {
        getSupportFragmentManager().beginTransaction().replace(R.id.contentFrame,
                Fragment.instantiate(MainActivity.this, Fragments.HOME.getFragment())).commit();

        selectItem(currentSelectedPosition);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_SELECTED_POSITION, currentSelectedPosition);

        outState.putBoolean(STATE_NEW_CATEGORY, addingItem);

        outState.putInt(STATE_NEW_CATEGORY_COLOR, newItemSelectedColor);

        outState.putLong(STATE_CURRENT_CATEGORY_SHOWING, currentCategoryIdShowing);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();

        if (savedInstanceState == null)
            return;

        addingItem = savedInstanceState.getBoolean(STATE_NEW_CATEGORY);
        newItemSelectedColor = savedInstanceState.getInt(STATE_NEW_CATEGORY_COLOR);
        currentCategoryIdShowing = savedInstanceState.getLong(STATE_CURRENT_CATEGORY_SHOWING);
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
        if (getSupportFragmentManager().getFragments().get(0) instanceof FragmentShop)
            getMenuInflater().inflate(R.menu.shop_menu, menu);
        else if (getSupportFragmentManager().getFragments().get(0) instanceof FragmentSettings)
            getMenuInflater().inflate(R.menu.settings_menu, menu);
        else
            getMenuInflater().inflate(R.menu.main, menu);

        this.mMenu = menu;
        if (addingItem)
            initiateNewItemLayout(menu.getItem(1));

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item))
            return true;

        switch (item.getItemId()) {
            case R.id.menu_action_add:
                addingItem = newItemLayout.getVisibility() != View.VISIBLE;
                if (addingItem) {
                    newItemNameET.requestFocus();
                } else {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(newItemNameET.getWindowToken(), 0);
                }

                initiateNewItemLayout(item);
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

        String name = newItemNameET.getText().toString();
        newItemNameET.setText("");
        newItemLayout.getChildAt(0).setBackgroundResource(R.color.purple_dark);

        if (getSupportFragmentManager().getFragments().get(0) instanceof FragmentHome)
            DBObjectsHelper.createCategory(getApplicationContext(), editingCategory, name, newItemSelectedColor);
        else
            DBObjectsHelper.createProduct(getApplicationContext(), editingProduct, name, currentCategoryIdShowing);

        newItemSelectedColor = -1;
    }

    @OnClick(R.id.new_item_delete_btn)
    public void removeItem() {
        if (DBObjectsHelper.removeItem(editingCategory))
            Toast.makeText(this, "Category " + editingCategory.name + " deleted.", Toast.LENGTH_SHORT).show();
        if (DBObjectsHelper.removeItem(editingProduct))
            Toast.makeText(this, "Item " + editingProduct.name + " deleted.", Toast.LENGTH_SHORT).show();
    }

    private void initiateNewItemLayout(MenuItem menuItem) {
        menuItem.setIcon(addingItem ? R.drawable.ic_action_cancel : R.drawable.ic_action_new);
        newItemLayout.setVisibility(addingItem ? View.VISIBLE : View.GONE);
        if ((getSupportFragmentManager().getFragments().get(0) instanceof FragmentHome))
            newCategoryColorLayout.setVisibility(addingItem ? View.VISIBLE : View.GONE);
        else
            newCategoryColorLayout.setVisibility(View.GONE);
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

        initiateNewItemLayout(mMenu.getItem(1));
    }

    public void onEvent(ShopifyEvents.EditProductEvent epe) {
        this.editingProduct = epe.getProduct();

        addingItem = true;
        newItemNameET.setText(epe.getProduct().name);
        initiateNewItemLayout(mMenu.getItem(1));
    }

    public void onEvent(ShopifyEvents.CurrentCategory cc) {
        this.currentCategoryIdShowing = cc.getCurrentCatategory().getId();
        reinitiateAddLayout();

        showProductsTutorial();

        getSupportActionBar().setTitle(cc.getCurrentCatategory().name);
        newItemLayout.getChildAt(0).setBackgroundResource(R.color.purple_dark);
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
                    showHomeTutorial();

                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.contentFrame, Fragment
                                    .instantiate(MainActivity.this, Fragments.HOME.getFragment()))
                            .commit();
                }
                break;
            case 1:
                if (!(getSupportFragmentManager().getFragments().get(0) instanceof FragmentShop)) {
                    showShopTutorial();
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.contentFrame, Fragment
                                    .instantiate(MainActivity.this, Fragments.SHOP.getFragment()))
                            .commit();
                }
                break;
            case 2:
                if (!(getSupportFragmentManager().getFragments().get(0) instanceof FragmentSettings)) {
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.contentFrame, Fragment
                                    .instantiate(MainActivity.this, Fragments.SETTINGS.getFragment()))
                            .commit();
                }
                break;
        }

        reinitiateAddLayout();
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getFragments().get(0) instanceof FragmentProducts ||
                getSupportFragmentManager().getFragments().get(0) instanceof FragmentShop ||
                getSupportFragmentManager().getFragments().get(0) instanceof FragmentShop) {

            reinitiateAddLayout();
            initHome();
        } else {
            super.onBackPressed();
        }
    }

    private void reinitiateAddLayout() {
        editingCategory = null;
        editingProduct = null;
        addingItem = false;
        newItemSelectedColor = -1;

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(newItemNameET.getWindowToken(), 0);

        if (mMenu != null && mMenu.size() > 1)
            initiateNewItemLayout(mMenu.getItem(1));
    }

    private void showHomeTutorial() {
        if (prefs.getBoolean(ShopifyConstants.TUTORIAL_HOME_FIRST_USE, true)) {
            ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
            executor.schedule(new Runnable() {
                @Override
                public void run() {
                    prefs.edit().putBoolean(ShopifyConstants.TUTORIAL_HOME_FIRST_USE, false).apply();
                    EventBus.getDefault().post(new ShopifyEvents.TutorialHome(true));

                    ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
                    executor.schedule(new Runnable() {
                        @Override
                        public void run() {
                            EventBus.getDefault().post(new ShopifyEvents.TutorialHome(false));
                        }
                    }, 7, TimeUnit.SECONDS);
                }
            }, 1, TimeUnit.SECONDS);
        }
    }

    private void showShopTutorial() {
        if (prefs.getBoolean(ShopifyConstants.TUTORIAL_SHOP_FIRST_USE, true)) {
            ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
            executor.schedule(new Runnable() {
                @Override
                public void run() {
                    if (new Select().from(Product.class).execute().isEmpty()) {
                        EventBus.getDefault().post(new ShopifyEvents.TutorialShop());
                    }
                    prefs.edit().putBoolean(ShopifyConstants.TUTORIAL_SHOP_FIRST_USE, false).apply();
                }
            }, 1, TimeUnit.SECONDS);
        }
    }

    private void showProductsTutorial() {
        if (prefs.getBoolean(ShopifyConstants.TUTORIAL_PRODUCTS_FIRST_USE, true)) {
            ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
            executor.schedule(new Runnable() {
                @Override
                public void run() {
                    prefs.edit().putBoolean(ShopifyConstants.TUTORIAL_PRODUCTS_FIRST_USE, false).apply();
                    EventBus.getDefault().post(new ShopifyEvents.TutorialProducts(true));

                    ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
                    executor.schedule(new Runnable() {
                        @Override
                        public void run() {
                            EventBus.getDefault().post(new ShopifyEvents.TutorialProducts(false));
                        }
                    }, 7, TimeUnit.SECONDS);
                }
            }, 1, TimeUnit.SECONDS);
        }
    }
}
