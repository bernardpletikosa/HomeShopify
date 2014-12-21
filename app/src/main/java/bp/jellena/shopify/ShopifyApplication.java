package bp.jellena.shopify;

import com.activeandroid.ActiveAndroid;
import com.activeandroid.app.Application;

import bp.jellena.shopify.data.ShopifyCons;

public class ShopifyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        ActiveAndroid.initialize(this);

        ShopifyCons.init(getApplicationContext());
    }

    @Override
    public void onTerminate() {
        super.onTerminate();

        ActiveAndroid.dispose();
    }
}
