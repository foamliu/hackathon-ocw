package org.hackathon_ocw.androidclient;

import android.widget.VideoView;
import android.content.Context;
import android.util.AttributeSet;

/**
 * Created by dianyang on 2016/3/17.
 */
public class CustomVideoView extends VideoView {

    private int measuredWidth = 0;
    private int measuredHeight = 0;

    public CustomVideoView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public CustomVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomVideoView(Context context) {
        super(context);
    }

    public void setNewDimension(int width, int height) {
        this.measuredHeight = height;
        this.measuredWidth = width;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // TODO Auto-generated method stub

        int width = getDefaultSize(0, widthMeasureSpec);
        int height = getDefaultSize(0, heightMeasureSpec);
        setMeasuredDimension(width, height);
    }
}
