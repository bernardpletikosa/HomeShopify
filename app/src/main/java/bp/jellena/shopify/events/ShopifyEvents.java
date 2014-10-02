package bp.jellena.shopify.events;

import bp.jellena.shopify.data.db.Category;
import bp.jellena.shopify.data.db.Product;

/**
 * Created by bp on 01/10/14.
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

    public static class EditProductEvent {
        private Product product;

        public EditProductEvent(Product product) {
            this.product = product;
        }

        public Product getProduct() {
            return product;
        }
    }

    public static class TutorialHome {
        private final boolean start;

        public TutorialHome(boolean start) {
            this.start = start;
        }

        public boolean isStart() {
            return start;
        }
    }

    public static class TutorialProducts {
        private final boolean start;

        public TutorialProducts(boolean start) {
            this.start = start;
        }

        public boolean isStart() {
            return start;
        }
    }

    public static class TutorialShop {
        public TutorialShop() {
        }
    }

    public static class RefreshShopProducts {
        public RefreshShopProducts() {
        }
    }
}
