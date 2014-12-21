package bp.jellena.shopify.data.db;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

@Table(name = "Products")
public class Product extends Model {

    @Column(name = "Name") public String name;
    @Column(name = "State") public int state;
    @Column(name = "CategoryId") public long categoryId;

    public Product() {
        super();
    }
}
