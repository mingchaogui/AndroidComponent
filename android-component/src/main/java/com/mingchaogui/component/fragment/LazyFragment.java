package com.mingchaogui.component.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.View;


/**
 * <pre>
 * 若把初始化数据放到onInitData实现
 * 就是采用Lazy方式加载的Fragment
 * 若不需要Lazy加载则不实现
 * </pre>
 */
public abstract class LazyFragment extends Fragment {

    // 标志位，View已经创建完成，尚未装载数据
    private boolean mPreparedToInitData = false;
    // 标志位，已onStart且尚未onStop
    private boolean mIsStarted = false;
    // 标志位，当前Fragment的View是否已经创建并可见
    private boolean mViewActive = false;

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mPreparedToInitData = true;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        mPreparedToInitData = false;
    }

    @Override
    public void onStart() {
        super.onStart();

        mIsStarted = true;
        detectViewActiveState();
    }

    @Override
    public void onStop() {
        super.onStop();

        mIsStarted = false;
        detectViewActiveState();
    }

    /**
     * 如果是与ViewPager一起使用，调用的是setUserVisibleHint
     *
     * @param isVisibleToUser true if this fragment's UI is currently visible to the user (default),
     *                        false if it is not.
     */
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        detectViewActiveState();
    }

    /**
     * 如果是通过FragmentTransaction的show和hide的方法来控制显示，调用的是onHiddenChanged.
     * 若是初始就show的Fragment，为了触发该事件，需要先hide再show
     *
     * @param hidden True if the fragment is now hidden, false otherwise.
     */
    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);

        detectViewActiveState();
    }

    public boolean isViewActive() {
        return mViewActive;
    }

    /**
     * 检测View是否活跃，并回调相关方法
     */
    private void detectViewActiveState() {
        boolean viewActive = mIsStarted && getUserVisibleHint() && !isHidden();
        if (mViewActive == viewActive) {
            return;
        }
        mViewActive = viewActive;
        if (mViewActive) {
            tryInitData();
            onViewActive();
        } else {
            onViewInactive();
        }
    }

    private void tryInitData() {
        if (!mPreparedToInitData
                || !getUserVisibleHint()
                || isHidden()) {
            return;
        }
        mPreparedToInitData = false;
        onInitData();
    }

    /**
     * 初始化数据
     */
    protected void onInitData() {}

    /**
     * 当前Fragment的View处于活跃(即用户可见)的状态，可在此做一些操作，如开启自动刷新数据
     */
    protected void onViewActive() {}

    /**
     * 当前Fragment的View处于不活跃(即用户不可见)的状态，可在此做一些操作，如停止自动刷新数据
     */
    protected void onViewInactive() {}
}
