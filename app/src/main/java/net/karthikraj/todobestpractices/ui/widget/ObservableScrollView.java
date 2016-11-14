package net.karthikraj.todobestpractices.ui.widget;

/**
 * Created by karthik on 10/11/16.
 */

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ScrollView;

/**
 * An extension to {@link ScrollView} which exposes a scroll listener.
 */
public class ObservableScrollView extends ScrollView {

    private OnScrollListener listener;

    public ObservableScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setListener(OnScrollListener listener) {
        this.listener = listener;
    }

    @Override
    protected void onScrollChanged(int currentScrollX,
                                   int currentScrollY,
                                   int oldScrollX,
                                   int oldScrollY) {
        super.onScrollChanged(currentScrollX, currentScrollY, oldScrollX, oldScrollY);
        if (listener != null) {
            listener.onScrolled(currentScrollY);
        }
    }

    public interface OnScrollListener {
        void onScrolled(int scrollY);
    }
}
