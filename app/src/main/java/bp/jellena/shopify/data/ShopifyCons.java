package bp.jellena.shopify.data;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Arrays;
import java.util.List;

import bp.jellena.shopify.R;

public class ShopifyCons {

    public static List<Integer> colors = Arrays.asList(
            R.color.red_300, R.color.red_600, R.color.red_900,
            R.color.pink_300, R.color.pink_600, R.color.pink_900,
            R.color.purple_300, R.color.purple_600, R.color.purple_900,
            R.color.dark_purple_300, R.color.dark_purple_600, R.color.dark_purple_900,
            R.color.indigo_300, R.color.indigo_600, R.color.indigo_900,
            R.color.blue_300, R.color.blue_600, R.color.blue_900,
            R.color.teal_300, R.color.teal_600, R.color.teal_900,
            R.color.green_300, R.color.green_600, R.color.green_900,
            R.color.lime_300, R.color.lime_600, R.color.lime_900,
            R.color.yellow_300, R.color.yellow_600, R.color.yellow_900,
            R.color.orange_300, R.color.orange_600, R.color.orange_900,
            R.color.brown_300, R.color.brown_600, R.color.brown_900,
            R.color.grey_300, R.color.grey_600, R.color.grey_900
    );

    public static final String STATE_SELECTED_POSITION = "selected_navigation_drawer_position";
    public static final String STATE_NEW_CATEGORY = "adding_new_category";
    public static final String STATE_NEW_CATEGORY_COLOR = "adding_new_category_color";
    public static final String STATE_CURRENT_CATEGORY_SHOWING = "current_category_showing";
    public static final String STATE_FRAG_SETTINGS = "state_frag_sett";
    public static final String STATE_FRAG_SHOP = "state_frag_shop";

    public static final String TUT_HOME_FIRST_USE = "tutorial_home_first_use";
    public static final String TUT_SHOP_FIRST_USE = "tutorial_shop_first_use";
    public static final String TUT_PRODUCTS_FIRST_USE = "tutorial_products_first_use";

    public static final String PROD_BUNDLE_CATEGORY_ID = "bundle_category_id";

    private static final String SHARED_PREFERENCES = "shopify_shared_prefs";

    public static SharedPreferences sp;

    public static void init(Context context) {
        sp = context.getSharedPreferences(ShopifyCons.SHARED_PREFERENCES,
                Context.MODE_PRIVATE);
    }

}
