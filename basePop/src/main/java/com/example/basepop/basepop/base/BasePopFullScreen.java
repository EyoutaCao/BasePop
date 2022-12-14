package com.example.basepop.basepop.base;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.res.Resources;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;

//中心弹框  中心弹出动画
public abstract class BasePopFullScreen extends BasePop{
    protected int layout;
    protected BackgroudView mBaseView; //阴影背景
    protected ViewGroup mParent;
    protected View mContent;
    protected Container mContainer;
    protected Activity activity;
    protected boolean isShow=false,isCreate=false;
    protected boolean isShowing=false,isDismissing=false;
    protected boolean dismissTouchOutside=true,dismissOnBack=true;
    //contentAnimate
    float startScale = 0;
    private int maxHeight=0;
    //private int needTop,screenHeight;
    private int animationDuration = 350;
    //shadowAnimate
    private boolean isClickThrough=false;
    private boolean isConScrollAble=false;
    private MyPopLis myPopLis;

    public BasePopFullScreen(Activity activity){
        super(activity);
        this.activity =activity;
        setLayout(getImplLayoutId());
    }


    protected abstract int getImplLayoutId();

    public void setLayout(int layout){
        this.layout=layout;
    }

    protected void onCreate(){  //加入弹窗

        isCreate=true;
        mBase=new Backgroud(activity);
        mBase.setClickThrough(isClickThrough);
        mBase.setOnback(()->{
            if (myPopLis!=null){
                myPopLis.onBack();
            }
            if (dismissOnBack){
                dismiss();
            }
        });
        maxHeight=getMaxHeight();
        //初始高度
        int maxWidth = getMaxWidth();
        mBase.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        mContent= LayoutInflater.from(activity).inflate(layout,mBase,false);
        mContainer=new Container(activity,isConScrollAble);

        FrameLayout.LayoutParams flp=new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        flp.gravity= Gravity.CENTER;
        mContainer.setLayoutParams(flp);
        mContainer.setMaxHeight(maxHeight);
        mContainer.setMaxWidth(maxWidth);
        mContainer.addView(mContent);
        mBaseView=new BackgroudView(activity);
        mBaseView.setOnback(()->{
            if (dismissTouchOutside){
                dismiss();
            }
        });
        FrameLayout.LayoutParams flp2=new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        mBaseView.setLayoutParams(flp2);
        mBase.addView(mBaseView);  //背景
        mBase.addView(mContainer);  //弹窗
        mParent =(FrameLayout) activity.getWindow().getDecorView();

        try {
            mParent.addView(mBase);
        }catch (Exception ignored){

        }

    }


    public void initAnimator() {
        mContainer.setAlpha(startScale);
    }

    public void animateShow() {

        if (myPopLis!=null){
            myPopLis.onShow();
        }

        mContainer.animate().alpha(1f)
                .setDuration(animationDuration).setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        isShow=true;
                        isShowing=false;
                    }
                })
                .setInterpolator(new OvershootInterpolator(1f))
//                .withLayer() 在部分6.0系统会引起crash
                .start();



    }

    public void animateDismiss() {

        if (myPopLis!=null){
            myPopLis.onDismiss();
        }
        mContainer.animate().alpha(0f).setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        isShow=false;
                        isDismissing=false;
                        try {
                            mParent.removeView(mBase);
                        }catch (Exception ignored){}
                    }
                }).setDuration(animationDuration)
                .setInterpolator(new FastOutSlowInInterpolator())
//                .withLayer() 在部分6.0系统会引起crash
                .start();

    }

    public <T extends View> T findViewById(int id){
        return mContent.findViewById(id);
    }


    //设置没有阴影的背景点击可穿透
    public BasePopFullScreen setClickThrough(boolean clickThrough) {
        isClickThrough = clickThrough;
        return this;
    }

    public BasePopFullScreen setMaxHeight(int max) {
        maxHeight=max;
        return this;
    }

    public BasePopFullScreen setDismissTouchOutside(boolean dismissTouchOutside) {
        this.dismissTouchOutside = dismissTouchOutside;
        return this;
    }

    public BasePopFullScreen setDismissOnBack(boolean dismissOnBack) {
        this.dismissOnBack = dismissOnBack;
        return this;
    }

    protected Resources getResources(){
        return mBase.getResources();
    }

    public BasePopFullScreen setConScrollAble(boolean conScrollAble) {
        isConScrollAble = conScrollAble;
        return this;
    }

    public boolean isShow(){
        return isShow;
    }

    public void beforeShow(){   //弹窗显示之前执行
        if (myPopLis!=null){
            myPopLis.beforeShow();
        }
        initAnimator();
    }

    public void beforeDismiss(){
        if (myPopLis!=null){
            myPopLis.beforeDismiss();
        }
    }

    public void show(){
        if (isShowing||isShow){
            if (isShow){
                dismiss();
            }
            return;
        }
        isShowing=true;
        if (!isCreate){
            onCreate();
        }else {
            try {
                mParent.addView(mBase);
                mBase.init();
            }catch (Exception ignored){}
        }

        beforeShow();
        animateShow();
    }
    public void dismiss(){
        if (isDismissing){
            return;
        }
        isDismissing=true;
        beforeDismiss();
        animateDismiss();
        onDismiss();
    }
    protected void onDismiss() {

    }

    public BasePopFullScreen setAnimationDuration(int animationDuration) {
        this.animationDuration = animationDuration;
        return this;
    }

    protected int getMaxHeight(){
        return 0;
    }
    public BasePopFullScreen setPopListener(MyPopLis myPopLis){
        this.myPopLis=myPopLis;
        return this;
    }

    public static class MyPopLis extends BasePop.MyPopLis {
    }

    protected int getMaxWidth(){return 0;}




}
