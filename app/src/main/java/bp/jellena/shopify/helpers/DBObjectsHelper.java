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

public class DBObjectsHelper {

    public static void createProduct(Context context, Product editingProduct, String name, long categoryId) {
        Product product;

        if (editingProduct != null) {
            product = new Select().from(Product.class).where("Id = ?", editingProduct.getId()).executeSingle();
            Toast.makeText(context, "Updated product " + product.name, Toast.LENGTH_SHORT).show();
        } else {
            product = new Select().from(Product.class).where("categoryId = ? AND name = ?", categoryId, name).executeSingle();
            if (product == null) {
                product = new Product();
            } else {
                Toast.makeText(context, "Product " + product.name + " already exist.", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        product.name = name;
        product.categoryId = categoryId;
        product.state = ProductState.EMPTY.getId();
        product.save();

        EventBus.getDefault().post(new ShopifyEvents.RefreshProductsEvent());
    }

    public static void createCategory(Context context, Category editingCategory, String name, int color) {
        Category category;

        if (editingCategory != null) {
            category = new Select().from(Category.class).where("Id = ?", editingCategory.getId()).executeSingle();
            Toast.makeText(context, "Updated category " + category.name, Toast.LENGTH_SHORT).show();
        } else {
            category = new Select().from(Category.class).where("name = ?", name).executeSingle();
            if (category == null) {
                category = new Category();
            } else {
                Toast.makeText(context, "Category " + category.name + " already exist.", Toast.LENGTH_LONG).show();
                return;
            }
        }

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
