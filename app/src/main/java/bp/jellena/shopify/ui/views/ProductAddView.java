package bp.jellena.shopify.ui.views;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;

import bp.jellena.shopify.R;

public class ProductAddView extends LinearLayout {

    private Context context;

    public interface AddProductListener {
        public void onAddEvent(String name);

        public void onDeleteEvent();

        public void onTypeEvent();
    }

    public ProductAddView(Context context) {
        super(context);
        LayoutInflater.from(context).inflate(R.layout.add_item_custom_view, this);
    }

    public ProductAddView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        LayoutInflater.from(context).inflate(R.layout.add_item_custom_view, this);
    }

    public ProductAddView(Context context, AttributeSet attrs, int defStyle) {
        this(context, attrs);
    }

    public void initView(final AddProductListener addProductListener, int color) {
        setViewVisibility(true);
        setBackgroundColor(getResources().getColor(color));

        final EditText name = (EditText) findViewById(R.id.add_item_name);
        name.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                addProductListener.onTypeEvent();
            }
        });

        findViewById(R.id.add_item_create_btn).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (name.getText().toString() == null || name.getText().toString().isEmpty()) {
                    name.setError("You must put some text here :)");
                } else {
                    addProductListener.onAddEvent(name.getText().toString());

                    name.setText("");
                }
            }
        });

        findViewById(R.id.add_item_delete_btn).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!name.getText().toString().isEmpty())
                    addProductListener.onDeleteEvent();
            }
        });
    }

    public void initView(AddProductListener addProductListener, String name, int color) {
        initView(addProductListener, color);

        ((EditText) findViewById(R.id.add_item_name)).setText(name);
    }

    public void setViewVisibility(boolean isVisible) {
        EditText nameET = (EditText) findViewById(R.id.add_item_name);

        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);

        if (isVisible) {
            setVisibility(VISIBLE);
            nameET.requestFocus();
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED,0);
        } else {
            setVisibility(GONE);
            imm.hideSoftInputFromWindow(nameET.getWindowToken(), 0);
        }
    }
}
