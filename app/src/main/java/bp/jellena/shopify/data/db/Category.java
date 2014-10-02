package bp.jellena.shopify.data.db;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

/**
 * Created by bp on 28/09/14.
 */
@Table(name = "Categories")
public class Category extends Model {

    @Column(name = "Name")
    public String name;

    @Column(name = "Color")
    public int color;

    public Category(){
        super();
    }
}
