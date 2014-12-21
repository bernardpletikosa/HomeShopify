package bp.jellena.shopify.data;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by bp on 01/10/14.
 */
public class ShopifyConstants {

    public static final String TUTORIAL_HOME_FIRST_USE = "tutorial_home_first_use";
    public static final String TUTORIAL_SHOP_FIRST_USE = "tutorial_shop_first_use";
    public static final String TUTORIAL_PRODUCTS_FIRST_USE = "tutorial_products_first_use";

    public static final String PRODUCTS_BUNDLE_CATEGORY_ID = "bundle_category_id";

    private static final String SHARED_PREFERENCES = "shopify_shared_prefs";

    public static SharedPreferences getSharedPrefs(Context context) {
        return context.getSharedPreferences(ShopifyConstants.SHARED_PREFERENCES, Context.MODE_PRIVATE);
    }
}
