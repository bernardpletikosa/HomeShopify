package bp.jellena.shopify.data;

import android.support.v4.app.Fragment;

import bp.jellena.shopify.ui.fragments.FragmentAbout;
import bp.jellena.shopify.ui.fragments.FragmentHome;
import bp.jellena.shopify.ui.fragments.FragmentProducts;
import bp.jellena.shopify.ui.fragments.FragmentShop;

/**
 * Created by Michal Bialas on 19/07/14.
 */
public enum Fragments {

    HOME(FragmentHome.class), SHOP(FragmentShop.class), PRODUCTS(FragmentProducts.class), ABOUT(FragmentAbout.class);

    final Class<? extends Fragment> fragment;

    private Fragments(Class<? extends Fragment> fragment) {
        this.fragment = fragment;
    }

    public String getFragment() {
        return fragment.getName();
    }
}
