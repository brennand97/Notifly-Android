/*
 * Copyright (c) 2016 Brennan Douglas
 */

package com.notiflyapp.ui.activities.devices;

import android.animation.Animator;
import android.graphics.Rect;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.AbsListView;
import android.widget.ListView;

/**
 * Created by Brennan on 5/2/2016.
 */
class DeviceSwipeTouchListener implements View.OnTouchListener {

    // Cached ViewConfiguration and system-wide constant values
    private int mSlop;
    private int mMinFlingVelocity;
    private int mMaxFlingVelocity;
    private long mAnimationTime;

    //Fixed Properties
    private ListView mListView;
    private SwipeCallback mSwipeCallback;

    //Transient Properties
    private VelocityTracker mVelocityTracker;
    private View mView, mMoveView;
    private float mDownX, mDownY, mMoveDownX;
    private float mViewWidth = 1;
    private int mDownPosition;
    private boolean mSwiping, mPaused, mMoved;

    public interface SwipeCallback {

        /**
         * This is the method that retrieves the specific view that will be swiped from the clicked
         * ListView element.
         * @param view ListView View clicked
         * @return  Element to be swiped/moved
         */
        View getView(View view);

        /**
         * Called on completed swipe right
         * @param view ListView View clicked
         */
        void onSwipe(View view);

        /**
         * Called to determine limit of swipe to the right, exposing left underside of view,
         * @param view ListView View clicked
         * @return  The limit of the swipe to the right
         */
        float getWidth(View view);

        void initializeBottomLayer(View view);

        void deinitializeBottomLayer(View view);
    }

    public DeviceSwipeTouchListener(ListView listView, SwipeCallback swipeCallback){

        mListView = listView;
        mSwipeCallback = swipeCallback;

        ViewConfiguration vc = ViewConfiguration.get(listView.getContext());
        mSlop = vc.getScaledTouchSlop();
        mMinFlingVelocity = vc.getScaledMinimumFlingVelocity();
        mMaxFlingVelocity = vc.getScaledMaximumFlingVelocity();
        mAnimationTime = listView.getContext().getResources().getInteger(
                android.R.integer.config_shortAnimTime);

    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:

                if (mPaused) {
                    return false;
                }

                Rect rect = new Rect();
                int childCount = mListView.getChildCount();
                int[] listViewCoords = new int[2];
                mListView.getLocationOnScreen(listViewCoords);
                int x = (int) event.getRawX() - listViewCoords[0];
                int y = (int) event.getRawY() - listViewCoords[1];
                View child;
                for (int i = 0; i < childCount; i++) {
                    child = mListView.getChildAt(i);
                    child.getHitRect(rect);
                    if (rect.contains(x, y)) {
                        View mMoveViewTemp = mSwipeCallback.getView(child);
                        if(mView != null) {
                            if(mListView.getPositionForView(child) != mListView.getPositionForView(mView)) {
                                closeView(mMoveView, mView);
                            }
                        }
                        mMoveView = mMoveViewTemp;
                        mView = child;
                    }
                }

                if (mView != null) {
                    mDownX = event.getRawX();
                    mDownY = event.getRawY();
                    mMoveDownX = mMoveView.getTranslationX();
                    mMoved = false;
                    mDownPosition = mListView.getPositionForView(mView);
                    mSwipeCallback.initializeBottomLayer(mView);

                    mVelocityTracker = VelocityTracker.obtain();
                    mVelocityTracker.addMovement(event);
                }
                v.onTouchEvent(event);

                return true;
            case MotionEvent.ACTION_UP:

                if (mVelocityTracker == null) {
                    break;
                }

                float deltaX2 = event.getRawX() - mDownX;
                float deltaY = event.getRawY() - mDownY;

                mVelocityTracker.addMovement(event);
                mVelocityTracker.computeCurrentVelocity(1000);

                float velocityX = Math.abs(mVelocityTracker.getXVelocity());
                float velocityY = Math.abs(mVelocityTracker.getYVelocity());

                boolean swipe = false;
                boolean moveRight = false;
                if (mMinFlingVelocity <= velocityX && velocityX <= mMaxFlingVelocity && velocityY < velocityX) {
                    swipe = true;
                    moveRight = mVelocityTracker.getXVelocity() > 0;
                } else if (Math.abs(deltaX2) > mViewWidth / 2 && deltaX2 > deltaY) {
                    swipe = true;
                    moveRight = deltaX2 > 0;
                }

                if(swipe && moveRight) {
                    mSwipeCallback.initializeBottomLayer(mView);
                    mMoveView.animate()
                            .translationX(mSwipeCallback.getWidth(mView))
                            .setDuration(mAnimationTime)
                            .setListener(null);
                } else {
                    // cancel
                    closeView(mMoveView, mView);
                }

                mVelocityTracker = null;
                mDownX = 0;
                mDownPosition = ListView.INVALID_POSITION;
                mSwiping = false;
                mMoved = false;

                break;
            case MotionEvent.ACTION_MOVE:

                if (mVelocityTracker == null || mPaused) {
                    break;
                }

                mVelocityTracker.addMovement(event);
                float deltaX = event.getRawX() - mDownX;
                if (Math.abs(deltaX) > mSlop) {
                    mSwiping = true;
                    mListView.requestDisallowInterceptTouchEvent(true);

                    // Cancel ListView's touch (un-highlighting the item)
                    MotionEvent cancelEvent = MotionEvent.obtain(event);
                    cancelEvent.setAction(MotionEvent.ACTION_CANCEL |
                            (event.getActionIndex()
                                    << MotionEvent.ACTION_POINTER_INDEX_SHIFT));
                    mListView.onTouchEvent(cancelEvent);
                }

                if (mSwiping && (mMoveDownX + deltaX > 0) && (mMoveDownX + deltaX < mSwipeCallback.getWidth(mView))) {
                    mMoveView.setTranslationX(deltaX + mMoveDownX);
                    return true;
                } else if(mSwiping && (mMoveDownX + deltaX > 0)) {
                    mMoveView.setTranslationX(mSwipeCallback.getWidth(mView));
                } else if(mSwiping && (mMoveDownX + deltaX < mSwipeCallback.getWidth(mView))) {
                    mMoveView.setTranslationX(0);
                }

                break;
        }

        return false;

    }

    public void setEnabled(boolean enabled) {
        mPaused = !enabled;
    }

    public AbsListView.OnScrollListener makeScrollListener() {
        return new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int scrollState) {
                setEnabled(scrollState != AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL);
                closeView(mMoveView, mView);
            }

            @Override
            public void onScroll(AbsListView absListView, int i, int i1, int i2) {
            }
        };
    }

    public void closeView(View move, final View view) {
        if(move != null) {
            move.animate()
                    .translationX(0)
                    .setDuration(mAnimationTime)
                    .setListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animator animation) {
                            if(view != null) {
                                mSwipeCallback.deinitializeBottomLayer(view);
                            }
                        }

                        @Override
                        public void onAnimationCancel(Animator animation) {

                        }

                        @Override
                        public void onAnimationRepeat(Animator animation) {

                        }
                    });
        }
    }

}
