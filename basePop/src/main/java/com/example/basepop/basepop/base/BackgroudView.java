package com.example.basepop.basepop.base;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class BackgroudView extends View {  //弹窗背景 不可穿透

    private boolean isClickThrough;
    private onBack mOnBack;

    public BackgroudView(@NonNull Context context) {
        super(context);
    }

    public BackgroudView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public BackgroudView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }




    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

    }

    private float x, y;
    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if (isClickThrough){
            return false;
        }else {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    x = event.getX();
                    y = event.getY();
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    if (x==event.getX()&&y==event.getY()){
                        mOnBack.onback();
                    }
                    break;
            }
            return true;
        }


    }


    public void setClickThrough(boolean clickThrough) {
        isClickThrough = clickThrough;
    }


    public interface onBack{
        void onback();
    }
    public void setOnback(onBack onBack){
        mOnBack=onBack;
    }

}