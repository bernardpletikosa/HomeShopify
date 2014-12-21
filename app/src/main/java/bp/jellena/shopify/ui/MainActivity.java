package bp.jellena.shopify.ui;

import android.content.Context;
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

import java.util.ArrayList;
import java.util.List;

import bp.jellena.shopify.R;
import bp.jellena.shopify.data.Fragments;
import bp.jellena.shopify.data.ShopifyCons;
import bp.jellena.shopify.data.db.Category;
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

public class MainActivity extends ActionBarActivity {

    @InjectView(R.id.navigationDrawerListViewWrapper) NavigationDrawerView mNavDrawer;
    @InjectView(R.id.linearDrawer) LinearLayout mLinearDrawerLayout;
    @InjectView(R.id.drawerLayout) DrawerLayout mDrawerLayout;
    @InjectView(R.id.leftDrawerListView) ListView mDrawerListView;
    @InjectView((R.id.new_item_layout)) LinearLayout mNewItemLayout;
    @InjectView(R.id.new_category_color_layout) LinearLayout mNewCategoryColorLayout;
    @InjectView(R.id.new_item_name) EditText mNewItemName;

    private ActionBarDrawerToggle mDrawerToggle;
    private List<NavigationDrawerItem> mNavItems;

    private boolean mAddingItem = false;
    private int mItemSelectedColor = -1;
    private long mCurrentCatIdShowing;
    private int mCurrentSelected = 0;
    private Category mEditingCategory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().add(R.id.contentFrame,
                    Fragment.instantiate(MainActivity.this, Fragments.HOME.getFragment())).commit();
        } else {
            mCurrentSelected = savedInstanceState.getInt(ShopifyCons.STATE_SELECTED_POSITION);
        }

        mNavItems = new ArrayList<>();
        mNavItems.add(new NavigationDrawerItem(getString(R.string.fragment_home), true));
        mNavItems.add(new NavigationDrawerItem(getString(R.string.fragment_shop), true));
        mNavItems.add(new NavigationDrawerItem(getString(R.string.fragment_settings),
                R.drawable.ic_action_settings, false));

        mNavDrawer.replaceWith(mNavItems);

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

        for (final Integer color : ShopifyCons.colors) {
            View view = getLayoutInflater().inflate(R.layout.category_color_view, null, false);
            ((GradientDrawable) view.getBackground()).setColor(getResources().getColor(color));
            mNewCategoryColorLayout.addView(view);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mItemSelectedColor = color;
                    mNewItemLayout.getChildAt(0).setBackgroundColor(getResources().getColor(color));
                }
            });
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
        mEditingCategory = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(ShopifyCons.STATE_SELECTED_POSITION, mCurrentSelected);

        outState.putBoolean(ShopifyCons.STATE_NEW_CATEGORY, mAddingItem);

        outState.putInt(ShopifyCons.STATE_NEW_CATEGORY_COLOR, mItemSelectedColor);

        outState.putLong(ShopifyCons.STATE_CURRENT_CATEGORY_SHOWING, mCurrentCatIdShowing);

        outState.putBoolean(ShopifyCons.STATE_FRAG_SHOP, getSupportFragmentManager().getFragments().get(0) instanceof FragmentShop);
        outState.putBoolean(ShopifyCons.STATE_FRAG_SETTINGS, getSupportFragmentManager().getFragments().get(0) instanceof FragmentSettings);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();

        if (savedInstanceState == null)
            return;

        if (savedInstanceState.getBoolean(ShopifyCons.STATE_FRAG_SHOP)) {
            initShopFrag();
            return;
        }
        if (savedInstanceState.getBoolean(ShopifyCons.STATE_FRAG_SETTINGS)) {
            initSettingsFrag();
            return;
        }

        initHome();

        mAddingItem = savedInstanceState.getBoolean(ShopifyCons.STATE_NEW_CATEGORY);
        toogleNewItemLayout();

        mCurrentCatIdShowing = savedInstanceState.getLong(ShopifyCons.STATE_CURRENT_CATEGORY_SHOWING);
        if (mCurrentCatIdShowing > 0) {
            initCategory();
        } else {
            if (findViewById(R.id.detailsFrame) != null) {
                ((FrameLayout) findViewById(R.id.detailsFrame)).removeAllViews();
            }
        }

        mItemSelectedColor = savedInstanceState.getInt(ShopifyCons.STATE_NEW_CATEGORY_COLOR);
        if (mItemSelectedColor > 0)
            mNewItemLayout.getChildAt(0).setBackgroundColor(getResources().getColor(mItemSelectedColor));
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
                mAddingItem = mNewItemLayout.getVisibility() != View.VISIBLE;
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
        if (mNewItemName.getText().toString() == null || mNewItemName.getText().toString().isEmpty()) {
            mNewItemName.setError("You must put some text here :)");
            return;
        }

        if ((getSupportFragmentManager().getFragments().get(0) instanceof FragmentHome) && mItemSelectedColor < 0) {
            Animation shake = AnimationUtils.loadAnimation(MainActivity.this, R.anim.shake);
            mNewCategoryColorLayout.startAnimation(shake);
            return;
        }

        if (getSupportFragmentManager().getFragments().get(0) instanceof FragmentHome) {
            DBObjectsHelper.createCategory(getApplicationContext(), mEditingCategory, mNewItemName.getText().toString(), mItemSelectedColor);
            mEditingCategory = null;
        }

        mItemSelectedColor = -1;
        mNewItemName.setText("");
        mNewItemLayout.getChildAt(0).setBackgroundResource(R.color.main_color);
    }

    @OnClick(R.id.new_item_delete_btn)
    public void removeItem() {
        if (DBObjectsHelper.removeItem(mEditingCategory)) {
            Toast.makeText(this, "Category " + mEditingCategory.name + " deleted.",
                    Toast.LENGTH_SHORT).show();
            mEditingCategory = null;
        }
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
        this.mEditingCategory = ece.getCategory();

        mAddingItem = true;
        mItemSelectedColor = ece.getCategory().color;
        if (mItemSelectedColor > 0)
            mNewItemLayout.getChildAt(0).setBackgroundColor(getResources().getColor(mItemSelectedColor));

        mNewItemName.setText(ece.getCategory().name);

        mNewItemLayout.setVisibility(mAddingItem ? View.VISIBLE : View.GONE);
    }

    public void onEvent(ShopifyEvents.CurrentCategory cc) {
        this.mCurrentCatIdShowing = cc.getCurrentCatategory().getId();
        reinitiateAddLayout();

        getSupportActionBar().setTitle(cc.getCurrentCatategory().name);
        mNewItemLayout.getChildAt(0).setBackgroundResource(R.color.main_color);
    }

    private void toogleNewItemLayout() {
        if (mAddingItem) {
            mNewItemName.requestFocus();
        } else {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(mNewItemName.getWindowToken(), 0);
        }

        mNewItemLayout.setVisibility(mAddingItem ? View.VISIBLE : View.GONE);
    }

    private void selectItem(int position) {
        if (mDrawerListView != null) {
            mDrawerListView.setItemChecked(position, true);

            mNavItems.get(mCurrentSelected).setSelected(false);
            mNavItems.get(position).setSelected(true);

            mCurrentSelected = position;
            getSupportActionBar().setTitle(mNavItems.get(mCurrentSelected).getItemName());
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
        selectItem(1);
    }

    private void initSettingsFrag() {
        Fragment settFrag = getSupportFragmentManager().findFragmentByTag(Fragments.SETTINGS.getFragment());
        if (settFrag == null)
            settFrag = Fragment.instantiate(MainActivity.this, Fragments.SETTINGS.getFragment());

        getSupportFragmentManager().beginTransaction().replace(R.id.contentFrame, settFrag).commit();
        selectItem(2);
    }

    private boolean backPressed = false;

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getFragments().get(0) instanceof FragmentProducts ||
                getSupportFragmentManager().getFragments().get(0) instanceof FragmentShop ||
                getSupportFragmentManager().getFragments().get(0) instanceof FragmentSettings) {

            mCurrentCatIdShowing = -1;
            initHome();
        } else if (mAddingItem) {
            reinitiateAddLayout();
        } else {
            if (backPressed) {
                super.onBackPressed();
                return;
            }

            this.backPressed = true;
            Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    backPressed = false;
                }
            }, 2000);
        }
    }

    private void initCategory() {
        Bundle bundle = new Bundle();
        bundle.putLong(ShopifyCons.PROD_BUNDLE_CATEGORY_ID, mCurrentCatIdShowing);

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

        selectItem(0);
    }

    private void reinitiateAddLayout() {
        mEditingCategory = null;
        mAddingItem = false;
        mItemSelectedColor = -1;

        toogleNewItemLayout();
    }
}
