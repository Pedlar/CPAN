package org.notlocalhost.cpan.view;

import android.content.Context;
import android.util.AttributeSet;
import android.webkit.WebView;

/**
 * Created by pedlar on 11/2/14.
 */
public class ObservableWebView extends WebView {
    private OnScrollListener mOnScrollListener;

    public ObservableWebView(Context context) {
        super(context);
    }

    public ObservableWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ObservableWebView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public ObservableWebView(Context context, AttributeSet attrs, int defStyleAttr, boolean privateBrowsing) {
        super(context, attrs, defStyleAttr, privateBrowsing);
    }

    @Override
    public void onScrollChanged(int h, int v, int oldH, int oldV) {
        if(mOnScrollListener != null) {
            mOnScrollListener.onScroll(v, h);
        }
    }

    public void setOnScrollListener(OnScrollListener onScrollListener) {
        mOnScrollListener = onScrollListener;
    }

    public OnScrollListener getOnScrollListener() {
        return mOnScrollListener;
    }

    public interface OnScrollListener {
        public void onScroll(int v, int h);
    }
}
