package net.greybeardedgeek;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Point;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.List;

public class ConstrainedDragAndDropView extends LinearLayout {

    private int layoutId;
    private View dragHandle;
    private List<View> dropTargets = new ArrayList<View>();
    private boolean dragging = false;
    private int pointerId;

    public ConstrainedDragAndDropView(Context context) {
        super(context);
    }

    public ConstrainedDragAndDropView(Context context, AttributeSet attrs) {
        super(context, attrs);
        applyAttrs(context, attrs);
    }

    @SuppressLint("NewApi")
    public ConstrainedDragAndDropView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        applyAttrs(context, attrs);
    }

    public int getLayoutId() {
        return layoutId;
    }

    public void setLayoutId(int layoutId) {
        this.layoutId = layoutId;
    }

    public View getDragHandle() {
        return dragHandle;
    }

    public void setDragHandle(View dragHandle) {
        this.dragHandle = dragHandle;
        setupDragHandle();
    }

    public List<View> getDropTargets() {
        return dropTargets;
    }

    public void setDropTargets(List<View> dropTargets) {
        this.dropTargets = dropTargets;
    }

    public void addDropTarget(View target) {
        if (dropTargets == null) {
            dropTargets = new ArrayList<View>();
        }
        dropTargets.add(target);
    }

    private void applyAttrs(Context context, AttributeSet attrs) {
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.ConstrainedDragAndDropView, 0, 0);

        try {
            layoutId = a.getResourceId(R.styleable.ConstrainedDragAndDropView_layoutId, 0);

            if (layoutId > 0) {
                LayoutInflater.from(context).inflate(layoutId, this, true);
            }
        } finally {
            a.recycle();
        }
    }

    private void setupDragHandle() {
        this.setOnTouchListener(new DragAreaTouchListener());
    }

    private final class DragAreaTouchListener implements OnTouchListener {
        public boolean onTouch(View view, MotionEvent motionEvent) {

            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:

                    if(!dragging && isDragHandleTouch(motionEvent)) {
                        LinearLayout.LayoutParams lParams = (LinearLayout.LayoutParams) view.getLayoutParams();
                        pointerId = motionEvent.getPointerId(0);
                        updateDragPosition(motionEvent);
                        dragging = true;
                        Log.d("drag", "drag start");
                    }

                    break;

                case MotionEvent.ACTION_UP:
                    if (dragging && motionEvent.getPointerId(0) == pointerId) {
                        dragging = false;
                        Log.d("drag", "drag end");

                        int dropTargetIndex = findDropTargetIndex(motionEvent);
                        if(dropTargetIndex >= 0) {
                            Log.d("drag", "drop on target " + dropTargetIndex);
                        }
                    }
                    break;

               case MotionEvent.ACTION_MOVE:
                    if (dragging && motionEvent.getPointerId(0) == pointerId) {
                        updateDragPosition(motionEvent);

                        int dropTargetIndex = findDropTargetIndex(motionEvent);
                        if(dropTargetIndex >= 0) {
                            Log.d("drag", "hover on target " + dropTargetIndex);
                        }
                    }
                    break;

                default:
                    break;
            }

            return true;
        }
    }

    @SuppressLint("NewApi")
    private void updateDragPosition(MotionEvent motionEvent) {
        float candidateX = motionEvent.getX() - dragHandle.getWidth() / 2;
        if(candidateX > 0 && candidateX + dragHandle.getWidth() < this.getWidth()) {
            dragHandle.setX(candidateX);
        }

        /* if allowing vertical movement...
        float candidateY = motionEvent.getY() - dragHandle.getHeight() / 2;
        if(candidateY > 0 && candidateY + dragHandle.getHeight() < this.getHeight()) {
            dragHandle.setY(candidateY);
        }
        */
    }

    private boolean isDragHandleTouch(MotionEvent motionEvent) {
        Point point = new Point(
            new Float(motionEvent.getRawX()).intValue(),
            new Float(motionEvent.getRawY()).intValue()
        );

        return isPointInView(point, dragHandle);
    }

    int findDropTargetIndex(MotionEvent motionEvent) {
        int dropTargetIndex = -1;

        Point point = new Point(
                new Float(motionEvent.getRawX()).intValue(),
                new Float(motionEvent.getRawY()).intValue()
        );
        for(int i = 0; i < dropTargets.size(); i++) {
            if(isPointInView(point, dropTargets.get(i))) {
                dropTargetIndex = i;
                break;
            }
        }

        return dropTargetIndex;
    }

    /**
     * Determines whether a raw screen coordinate is within the bounds of the specified view
     * @param point - Point containing screen coordinates
     * @param view - View to test
     * @return true if the point is in the view, else false
     */
    private boolean isPointInView(Point point, View view) {

        int[] viewPosition = new int[2];
        view.getLocationOnScreen(viewPosition);

        int left = viewPosition[0];
        int right = left + view.getWidth();
        int top = viewPosition[1];
        int bottom = top + view.getHeight();

        return point.x >= left && point.x <= right && point.y >= top && point.y <= bottom;
    }
}
