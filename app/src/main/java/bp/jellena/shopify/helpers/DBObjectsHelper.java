package bp.jellena.shopify.helpers;

import android.content.Context;
import android.widget.Toast;

import com.activeandroid.query.Delete;
import com.activeandroid.query.Select;

import bp.jellena.shopify.data.db.Category;
import bp.jellena.shopify.data.db.Product;
import bp.jellena.shopify.data.db.ProductState;
import bp.jellena.shopify.events.ShopifyEvents;
import de.greenrobot.event.EventBus;

/**
 * Created by bp -- 02/10/14.
 */
public class DBObjectsHelper {

    public static void createProduct(Context context, Product editingProduct, String name, long categoryId) {
        Product product = null;

        if (editingProduct != null)
            product = new Select().from(Product.class).where("Id = ?", editingProduct.getId()).executeSingle();

        if (product == null)
            product = new Product();
        else
            Toast.makeText(context, "Updated " + product.name, Toast.LENGTH_SHORT).show();

        product.name = name;
        product.categoryId = categoryId;
        product.state = ProductState.EMPTY.getId();
        product.save();

        EventBus.getDefault().post(new ShopifyEvents.RefreshProductsEvent());
    }

    public static void createCategory(Context context, Category editingCategory, String name, int color) {
        Category category = null;

        if (editingCategory != null)
            category = new Select().from(Category.class).where("Id = ?", editingCategory.getId()).executeSingle();

        if (category == null)
            category = new Category();
        else
            Toast.makeText(context, "Updated " + category.name, Toast.LENGTH_SHORT).show();

        category.name = name;
        category.color = color;
        category.save();

        EventBus.getDefault().post(new ShopifyEvents.RefreshCategoryEvent());
    }

    public static boolean removeItem(Category editingCategory) {
        if (editingCategory == null)
            return false;

        new Delete().from(Product.class).where("CategoryId = ?", editingCategory.getId()).execute();

        editingCategory.delete();
        EventBus.getDefault().post(new ShopifyEvents.RefreshCategoryEvent());

        return true;
    }

    public static boolean removeItem(Product editingProduct) {
        if (editingProduct == null)
            return false;

        editingProduct.delete();
        EventBus.getDefault().post(new ShopifyEvents.RefreshProductsEvent());

        return true;
    }
}
