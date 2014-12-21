package bp.jellena.shopify.events;

import bp.jellena.shopify.data.db.Category;
import bp.jellena.shopify.data.db.Product;

/**
 * Created on 01/10/14
 *
 * @author bp
 */
public class ShopifyEvents {

    public static class RefreshCategoryEvent {
        public RefreshCategoryEvent() {
        }
    }

    public static class EditCategoryEvent {
        private Category category;

        public EditCategoryEvent(Category category) {
            this.category = category;
        }

        public Category getCategory() {
            return category;
        }
    }

    public static class RefreshProductsEvent {
        public RefreshProductsEvent() {
        }
    }

    public static class CurrentCategory {
        private final Category category;

        public CurrentCategory(Category category) {
            this.category = category;
        }

        public Category getCurrentCatategory() {
            return category;
        }
    }

    public static class RefreshShopProducts {
        public RefreshShopProducts() {
        }
    }
}
