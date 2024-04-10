/*
 * Copyright (c) 2021. Drakeet Xu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.crow.base.ui.component.drawerlayout;

import static java.lang.Math.abs;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.crow.base.tools.extensions.TipExtKt;

/**
 * @author Drakeet Xu
 */
public class FullDraggableHelper {

  @NonNull
  private final Context context;
  @NonNull
  private final Callback callback;

  private float initialMotionX;
  private float initialMotionY;
  private float lastMotionX;
  private final int touchSlop;
  private final int swipeSlop;
  private final int distanceThreshold;
  private final int xVelocityThreshold;

  private int gravity = Gravity.NO_GRAVITY;
  private boolean isDraggingDrawer = false;
  private boolean shouldOpenDrawer = false;

  @Nullable
  private VelocityTracker velocityTracker = null;

  public FullDraggableHelper(@NonNull Context context, @NonNull Callback callback) {
    this.context = context;
    this.callback = callback;
    touchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
    swipeSlop = dipsToPixels(8);
    distanceThreshold = dipsToPixels(80);
    xVelocityThreshold = dipsToPixels(150);
  }

  public boolean onInterceptTouchEvent(MotionEvent event) {
    boolean intercepted = false;
    int action = event.getActionMasked();
    float x = event.getX();
    float y = event.getY();

//      Log.i(TipExtKt.TIPS_TAG, "initialMotionX is : " + initialMotionX + "\t lastMotionX is : " + lastMotionX + "\t x is : " + x + "\t x is : " + x);
    if (action == MotionEvent.ACTION_DOWN) {
      lastMotionX = initialMotionX = x;
      initialMotionY = y;
      return false;
    } else if (action == MotionEvent.ACTION_MOVE) {
      if (canNestedViewScroll(callback.getDrawerMainContainer(), false, (int) (x - lastMotionX), (int) x, (int) y)) {
        return false;
      }
      lastMotionX = x;
      float diffX = x - initialMotionX;
      intercepted = abs(diffX) > touchSlop
        && abs(diffX) > abs(y - initialMotionY)
        && isDrawerEnabled(diffX);
//    Log.i(TipExtKt.TIPS_TAG, "intercepted is : " + intercepted + "\t abs(diffX) is : " + abs(diffX) + "\t touchSlop is : " + touchSlop + "\t x is : " + x);
    }
    return intercepted;
  }

  @SuppressLint({ "RtlHardcoded" })
  public boolean onTouchEvent(MotionEvent event) {
    float x = event.getX();
    int action = event.getActionMasked();
    switch (action) {
      case MotionEvent.ACTION_MOVE: {
        float diffX = x - initialMotionX;
        if (isDrawerOpen() || !isDrawerEnabled(diffX)) {
          return false;
        }
        float absDiffX = abs(diffX);
        if (absDiffX > swipeSlop || isDraggingDrawer) {
          if (velocityTracker == null) {
            velocityTracker = VelocityTracker.obtain();
          }
          velocityTracker.addMovement(event);
          boolean lastDraggingDrawer = isDraggingDrawer;
          isDraggingDrawer = true;
          shouldOpenDrawer = absDiffX > distanceThreshold;

          // Not allowed to change direction in a process
          if (gravity == Gravity.NO_GRAVITY) {
            gravity = diffX > 0 ? Gravity.LEFT : Gravity.RIGHT;
          } else if ((gravity == Gravity.LEFT && diffX < 0) || (gravity == Gravity.RIGHT && diffX > 0)) {
            // Means that the motion first moves in one direction,
            // and then completely close the drawer in the reverse direction.
            // At this time, absDiffX should not be distributed anymore.
            // So for this case, we are returning false,
            // and set the initialMotionX to the direction changed point
            // to support quick dragging out with the original direction.
            initialMotionX = x;
            return false;
          }

          callback.offsetDrawer(gravity, absDiffX - swipeSlop);

          if (!lastDraggingDrawer) {
            callback.onDrawerDragging();
          }
        }
        return isDraggingDrawer;
      }
      case MotionEvent.ACTION_CANCEL:
      case MotionEvent.ACTION_UP: {
        if (isDraggingDrawer) {
          if (velocityTracker != null) {
            velocityTracker.computeCurrentVelocity(1000);
            float xVelocity = velocityTracker.getXVelocity();
            boolean fromLeft = (gravity == Gravity.LEFT);
            if (xVelocity > xVelocityThreshold) {
              shouldOpenDrawer = fromLeft;
            } else if (xVelocity < -xVelocityThreshold) {
              shouldOpenDrawer = !fromLeft;
            }
          }
          if (shouldOpenDrawer) {
            callback.smoothOpenDrawer(gravity);
          } else {
            callback.smoothCloseDrawer(gravity);
          }
        }
        shouldOpenDrawer = false;
        isDraggingDrawer = false;
        gravity = Gravity.NO_GRAVITY;
        if (velocityTracker != null) {
          velocityTracker.recycle();
          velocityTracker = null;
        }
      }
    }
    return true;
  }

  private boolean canNestedViewScroll(View view, boolean checkSelf, int dx, int x, int y) {
//    Boolean scroll = checkSelf && view.canScrollHorizontally(-dx);
//    Log.i(TipExtKt.TIPS_TAG, "Scroll is " + scroll);
    return checkSelf && view.canScrollHorizontally(-dx);
    /*if (view instanceof ViewGroup) {
      ViewGroup group = (ViewGroup) view;
      int scrollX = view.getScrollX();
      int scrollY = view.getScrollY();
      int count = group.getChildCount();
      for (int i = count - 1; i >= 0; i--) {
        View child = group.getChildAt(i);
        if (child.getVisibility() != View.VISIBLE) continue;
      Log.i(TipExtKt.TIPS_TAG, "scrollX is : " + scrollX + "child.getLeft() : " + child.getLeft());
        if (x + scrollX >= child.getLeft()
          && x + scrollX < child.getRight()
          && y + scrollY >= child.getTop()
          && y + scrollY < child.getBottom()
          && canNestedViewScroll(child, true, dx, x + scrollX - child.getLeft(), y + scrollY - child.getTop())) {
          return true;
        }
      }
    }
    return checkSelf && view.canScrollHorizontally(-dx);*/
  }

  @SuppressLint("RtlHardcoded")
  private boolean isDrawerOpen() {
    return callback.isDrawerOpen(Gravity.LEFT) || callback.isDrawerOpen(Gravity.RIGHT);
  }

  @SuppressLint("RtlHardcoded")
  private boolean isDrawerEnabled(float direction) {
    return direction > 0 && callback.hasEnabledDrawer(Gravity.LEFT)
      || direction < 0 && callback.hasEnabledDrawer(Gravity.RIGHT);
  }

  private int dipsToPixels(int dips) {
    float scale = context.getResources().getDisplayMetrics().density;
    return (int) (dips * scale + 0.5f);
  }

  public interface Callback {
    @NonNull
    View getDrawerMainContainer();
    boolean isDrawerOpen(int gravity);
    boolean hasEnabledDrawer(int gravity);
    void offsetDrawer(int gravity, float offset);
    void smoothOpenDrawer(int gravity);
    void smoothCloseDrawer(int gravity);
    void onDrawerDragging();
  }
}