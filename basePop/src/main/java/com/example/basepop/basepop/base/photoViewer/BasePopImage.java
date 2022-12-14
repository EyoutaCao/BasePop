package com.example.basepop.basepop.base.photoViewer;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.interpolator.view.animation.FastOutSlowInInterpolator;
import androidx.transition.ChangeBounds;
import androidx.transition.ChangeImageTransform;
import androidx.transition.ChangeTransform;
import androidx.transition.TransitionManager;
import androidx.transition.TransitionSet;

import com.bumptech.glide.Glide;
import com.example.basepop.R;
import com.example.basepop.basepop.base.Backgroud;
import com.example.basepop.basepop.base.BackgroudView;
import com.example.basepop.basepop.base.BasePop;
import com.example.basepop.basepop.base.utils.PxTool;
import com.example.basepop.basepop.base.utils.ViewUtils;

import java.util.Locale;

//中心弹框  中心弹出动画
public abstract class BasePopImage extends BasePop {
    protected int layout;
    protected BackgroudView mBaseView; //阴影背景
    protected ViewGroup mParent;
    protected View mContent;
    protected ImageView srcView;
    protected PhotoView mPhoto;
    private String url;
    private Rect rect;
    protected PhotoViewContainer mContainer;
    protected Activity activity;
    protected boolean isShow=false,isCreate=false,isShowBg=true;
    protected boolean isShowing=false,isDismissing=false;
    protected boolean dismissTouchOutside=true,dismissOnBack=true;
    //contentAnimate

    private final int animationDuration = 350;
    //shadowAnimate
    public ArgbEvaluator argbEvaluator = new ArgbEvaluator();
    private final int startColor = Color.TRANSPARENT;
    private final boolean isZeroDuration = false;
    private boolean isClickThrough=false;

    private LoadImage loadImage;

    protected int shadowBgColor = Color.rgb(32, 36, 46);
    private MyPopLis myPopLis;

    public BasePopImage(Activity activity){
        super(activity);
        this.activity =activity;
        setLayout(getImplLayoutId());
    }


    protected abstract int getImplLayoutId();

    public void setLayout(int layout){
        this.layout=layout;
    }

    @SuppressLint("ResourceType")
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
        if (!isShowBg){
            shadowBgColor= R.color.transparent;
        }
        //初始高度
        int maxWidth = getMaxWidth();
        mBase.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        mContent= LayoutInflater.from(activity).inflate(layout,mBase,false);
      //  mContent.setBackgroundColor(getResources().getColor(R.color.color2866FE));
        mContainer=new PhotoViewContainer(activity);
        setPhoto(mContent.findViewById(R.id.dialog_image_photo));
        if (mPhoto!=null){
            if (srcView !=null){
                mPhoto.setImageDrawable(srcView.getDrawable());
                //initParam();
            }
            if (loadImage!=null){
                loadImage.onLoad(mPhoto);
            }else {
                Glide.with(activity).load(url).into(mPhoto);
            }
            mPhoto.setOnViewTapListener((view, x, y) -> dismiss());
            mContainer.setContent((LinearLayout) mContent);
            mContainer.setOnDragChangeListener(new PhotoViewContainer.OnDragChangeListener() {
                @Override
                public void onRelease() {
                    dismiss();
                }

                @Override
                public void onDragChange(int dy, float scale, float fraction) {


                    mBaseView.setBackgroundColor((Integer) argbEvaluator.evaluate(fraction * .8f,shadowBgColor, Color.TRANSPARENT));
                }
            });
        }

        FrameLayout.LayoutParams flp=new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        flp.gravity= Gravity.CENTER;
        mContainer.setLayoutParams(flp);
      /*  mContainer.setMaxHeight(maxHeight);
        mContainer.setMaxWidth(maxWidth);*/
        mContainer.setClipChildren(false);
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
        if (srcView != null) {
            int[] locations = ViewUtils.getLocation(srcView);
            if(isLayoutRtl(activity)){
                int left = -(PxTool.getWindowWidthAndHeight(activity)[0] - locations[0] - srcView.getWidth());
                rect = new Rect(left, locations[1], left + srcView.getWidth(), locations[1] + srcView.getHeight());
            }else {
                rect = new Rect(locations[0], locations[1], locations[0] + srcView.getWidth(), locations[1] + srcView.getHeight());
            }
        }
        initParam();
    }

    public void animateShow() {

        if (myPopLis!=null){
            myPopLis.onShow();
        }
        mPhoto.post(() -> {
            TransitionManager.beginDelayedTransition((ViewGroup) mPhoto.getParent(), new TransitionSet()
                    .setDuration(animationDuration)
                    .addTransition(new ChangeBounds())
                    .addTransition(new ChangeTransform())
                    .addTransition(new ChangeImageTransform())
                    .setInterpolator(new FastOutSlowInInterpolator()));
            mPhoto.setScaleY(1);
            mPhoto.setScaleX(1);
            mPhoto.setTranslationY(0);
            mPhoto.setTranslationX(0);

        });
        ValueAnimator animator = ValueAnimator.ofObject(argbEvaluator, startColor,shadowBgColor );
        animator.addUpdateListener(animation -> {
            if (isShowBg){
                mBaseView.setBackgroundColor((Integer) animation.getAnimatedValue());
            }
        });
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                isShow=true;
                isShowing=false;
            }
        });
        animator.setInterpolator(new FastOutSlowInInterpolator());
        animator.setDuration(isZeroDuration?0:animationDuration).start();

    }

    public void animateDismiss() {

        if (myPopLis!=null){
            myPopLis.onDismiss();
        }
        mPhoto.post(() -> {
            TransitionManager.beginDelayedTransition((ViewGroup) mContent.getParent(), new TransitionSet()
                    .setDuration(animationDuration)
                    .addTransition(new ChangeBounds())
                    .addTransition(new ChangeTransform())
                    .addTransition(new ChangeImageTransform())
                    .setInterpolator(new FastOutSlowInInterpolator()));
            FrameLayout.LayoutParams flp=new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            flp.gravity= Gravity.CENTER;
            mContainer.setLayoutParams(flp);
            mContent.setScaleX(1);
            mContent.setScaleY(1);
            initAnimator();
        });

        final int start = ((ColorDrawable) mBaseView.getBackground()).getColor();
        ValueAnimator animator = ValueAnimator.ofObject(argbEvaluator, start, startColor);
        animator.addUpdateListener(animation -> {
            if (isShowBg){
                mBaseView.setBackgroundColor((Integer) animation.getAnimatedValue());
            }
        });
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                isShow=false;
                isDismissing=false;
                try {
                    mParent.removeView(mBase);
                }catch (Exception ignored){}

            }
        });
        animator.setInterpolator(new FastOutSlowInInterpolator());
        animator.setDuration(isZeroDuration?0:animationDuration).start();
    }

    public <T extends View> T findViewById(int id){
        return mContent.findViewById(id);
    }

    public static boolean isLayoutRtl(Context context) {
        Locale primaryLocale;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            primaryLocale = context.getResources().getConfiguration().getLocales().get(0);
        } else {
            primaryLocale = context.getResources().getConfiguration().locale;
        }
        return TextUtils.getLayoutDirectionFromLocale(primaryLocale) == View.LAYOUT_DIRECTION_RTL;
    }

    //设置没有阴影的背景点击可穿透
    public BasePopImage setClickThrough(boolean clickThrough) {
        isClickThrough = clickThrough;
        return this;
    }


    public BasePopImage setShowBg(boolean isShowBg) {
        this.isShowBg=isShowBg;
        return this;
    }
    public BasePopImage setDismissTouchOutside(boolean dismissTouchOutside) {
        this.dismissTouchOutside = dismissTouchOutside;
        return this;
    }

    public BasePopImage setDismissOnBack(boolean dismissOnBack) {
        this.dismissOnBack = dismissOnBack;
        return this;
    }

    public void setPhoto(PhotoView mPhoto) {
        this.mPhoto = mPhoto;
    }

    //设置图片路径 (网络图片)
    public BasePopImage setUrl(String url) {
        this.url = url;
        return this;
    }

    private void initParam(){
        mPhoto.attacher.reset();
        mPhoto.setScaleX((float) (rect.width())/PxTool.dpToPx(PxTool.W));
        mPhoto.setScaleY((float) (rect.width())/PxTool.dpToPx(PxTool.W));
        mPhoto.setTranslationX(-(float)((PxTool.dpToPx(PxTool.W)-rect.width())/2f-rect.left));
        mPhoto.setTranslationY(-(float)((mParent.getMeasuredHeight()-rect.height())/2f-rect.top));
    }


    protected Resources getResources(){
        return mBase.getResources();
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

    public BasePopImage setSrcView(ImageView srcView) {
        this.srcView = srcView;
        return this;
    }

    protected int getMaxHeight(){
        return 0;
    }
    public BasePopImage setPopListener(MyPopLis myPopLis){
        this.myPopLis=myPopLis;
        return this;
    }

    public interface LoadImage{
        void onLoad(ImageView view);
    }
    //可自定义加载大图方式
    public BasePopImage setLoadImage(LoadImage loadImage) {
        this.loadImage = loadImage;
        return this;
    }

    protected int getMaxWidth(){return 0;}


    public static class MyPopLis extends BasePop.MyPopLis {
        protected void beforeShow(){};
        protected void beforeDismiss(){};
        protected void onShow(){};
        protected void onDismiss(){};
        protected void onBack(){};
    }


}
