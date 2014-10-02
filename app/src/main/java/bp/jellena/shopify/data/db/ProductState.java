package bp.jellena.shopify.data.db;

/**
 * Created by bp on 28/09/14.
 */
public enum ProductState {

    EMPTY(0), BETWEEN(1), FULL(2);

    private final int id;

    ProductState(int id) {
        this.id = id;
    }

    public static ProductState getStateById(int id) {
        switch (id) {
            case 0:
                return EMPTY;
            case 1:
                return BETWEEN;
            case 2:
                return FULL;
            default:
                return EMPTY;
        }
    }

    public int getId() {
        return id;
    }
}
