package net.greybeardedgeek;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Point;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;

public class ConstrainedDragAndDropView extends LinearLayout {

    private int layoutId;
    private View dragHandle;
    private List<View> dropTargets = new ArrayList<View>();
    private boolean dragging = false;
    private int pointerId;

    private int selectedDropTargetIndex = -1;
    private int lastSelectedDropTargetIndex = -1;

    private boolean allowHorizontalDrag = true;
    private boolean allowVerticalDrag = true;

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

    public boolean isAllowHorizontalDrag() {
        return allowHorizontalDrag;
    }

    public void setAllowHorizontalDrag(boolean allowHorizontalDrag) {
        this.allowHorizontalDrag = allowHorizontalDrag;
    }

    public boolean isAllowVerticalDrag() {
        return allowVerticalDrag;
    }

    public void setAllowVerticalDrag(boolean allowVerticalDrag) {
        this.allowVerticalDrag = allowVerticalDrag;
    }

    private void applyAttrs(Context context, AttributeSet attrs) {
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.ConstrainedDragAndDropView, 0, 0);


        try {
            /*
            layoutId = a.getResourceId(R.styleable.ConstrainedDragAndDropView_layoutId, 0);

            if (layoutId > 0) {
                LayoutInflater.from(context).inflate(layoutId, this, true);
            }
            */
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

                        updateDragPosition(motionEvent);
                        int dropTargetIndex = findDropTargetIndexUnderDragHandle();
                        if(dropTargetIndex >= 0) {
                            Log.d("drag", "drop on target " + dropTargetIndex);
                            selectDropTarget(dropTargetIndex);
                            snapDragHandleToDropTarget(dropTargetIndex);
                        } else {
                            deselectDropTarget();
                            snapDragHandleToDropTarget(lastSelectedDropTargetIndex);
                        }
                    }
                    break;

               case MotionEvent.ACTION_MOVE:
                    if (dragging && motionEvent.getPointerId(0) == pointerId) {
                        updateDragPosition(motionEvent);
                        int dropTargetIndex = findDropTargetIndexUnderDragHandle();
                        if(dropTargetIndex >= 0) {
                            Log.d("drag", "hover on target " + dropTargetIndex);
                            selectDropTarget(dropTargetIndex);
                        } else {
                            deselectDropTarget();
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

        if(allowHorizontalDrag) {
            float candidateX = motionEvent.getX() - dragHandle.getWidth() / 2;
            if(candidateX > 0 && candidateX + dragHandle.getWidth() < this.getWidth()) {
                dragHandle.setX(candidateX);
            }
        }

        if(allowVerticalDrag) {
            float candidateY = motionEvent.getY() - dragHandle.getHeight() / 2;
            if(candidateY > 0 && candidateY + dragHandle.getHeight() < this.getHeight()) {
                dragHandle.setY(candidateY);
            }
        }
    }

    @SuppressLint("NewApi")
    private void snapDragHandleToDropTarget(int dropTargetIndex) {

        View dropTarget = dropTargets.get(dropTargetIndex);
        float xCenter = dropTarget.getX() + dropTarget.getWidth() / 2;
        float yCenter = dropTarget.getY() + dropTarget.getHeight() / 2;

        float xOffset = dragHandle.getWidth() / 2;
        float yOffset = dragHandle.getHeight() / 2;

        float x = xCenter - xOffset;
        float y = yCenter - yOffset;

        dragHandle.setX(x);
        dragHandle.setY(y);
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

    int findDropTargetIndexUnderDragHandle() {
        int dropTargetIndex = -1;
        for(int i = 0; i < dropTargets.size(); i++) {
            if(isCollision(dragHandle, dropTargets.get(i))) {
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

    @SuppressLint("NewApi")
    private boolean isCollision(View view1, View view2) {
        boolean collision = false;


        do {
            if(view1.getY() + view1.getHeight() < view2.getY()) {
                break;
            }

            if(view1.getY() > view2.getY() + view2.getHeight()) {
                break;
            }

            if(view1.getX() > view2.getX() + view2.getWidth()) {
                break;
            }

            if(view1.getX() + view1.getWidth() < view2.getX()) {
                break;
            }

            collision = true;

        } while(false);

        return collision;
    }

    private void selectDropTarget(int index) {
        deselectDropTarget();
        selectedDropTargetIndex = index;
        dropTargets.get(selectedDropTargetIndex).setSelected(true);
    }

    private void deselectDropTarget() {
        if(selectedDropTargetIndex > -1) {
            dropTargets.get(selectedDropTargetIndex).setSelected(false);
            lastSelectedDropTargetIndex = selectedDropTargetIndex;
            selectedDropTargetIndex = -1;
        }
    }
}
