package com.android.systemui.powerwidget;

import android.widget.*;
import android.util.*;
import android.content.*;
import android.provider.*;
import android.view.*;
import android.content.res.*;

public class PowerWidgetContainer extends FrameLayout
{
	private int mNumColumns;
	private int mCellGap;
	public PowerWidgetContainer(Context c){
		this(c, null);
	}
	public PowerWidgetContainer(Context c, AttributeSet as){
		super(c, as);
		mCellGap = 2;
        mNumColumns = 3; 
		
		updateResources();
	}

    void updateResources() {
		requestLayout();
    }
	
	public void setNumColumns(int columns){
		mNumColumns = columns;
		updateResources();
	}
	public void setCellGap(int gap){
		mCellGap = gap;
		updateResources();
	}
	public int getNumColumns(){
		return mNumColumns;
	}
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // Calculate the cell width dynamically
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        int availableWidth = (int) (width - getPaddingLeft() - getPaddingRight() -
			(mNumColumns-1) * mCellGap); // nggo jaga2, maune (mNumColumns - 1)
        float cellWidth = (float) Math.ceil(((float) availableWidth) / mNumColumns);

        // Update each of the children's widths accordingly to the cell width
        int N = getChildCount();
        int cellHeight = 0;
        int cursor = 0;
        for (int i = 0; i < N; ++i) {
            // Update the child's width
            PowerButtonView v = (PowerButtonView) getChildAt(i);
            if (v.getVisibility() != View.GONE) {
                ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
                float colSpan = v.getColumnSpan();
                lp.width = (int) ((colSpan * cellWidth) + (colSpan - 1) * mCellGap);

                // Measure the child
                int newWidthSpec = MeasureSpec.makeMeasureSpec(lp.width, MeasureSpec.EXACTLY);
                int newHeightSpec = MeasureSpec.makeMeasureSpec(lp.height, MeasureSpec.EXACTLY);
                v.measure(newWidthSpec, newHeightSpec);

                // Save the cell height
                if (cellHeight <= 0) {
                    cellHeight = v.getMeasuredHeight();
                }
                cursor += colSpan;
            }
        }

        // Set the measured dimensions.  We always fill the tray width, but wrap to the height of
        // all the tiles.
        int numRows = (int) Math.ceil((float) cursor / mNumColumns);
        int newHeight = (int) ((numRows * cellHeight) + ((numRows - 1) * mCellGap)) +
			getPaddingTop() + getPaddingBottom();
        setMeasuredDimension(width, newHeight);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        int N = getChildCount();
        int x = getPaddingLeft();
        int y = getPaddingTop();
        int cursor = 0;
		
        for (int i = 0; i < N; ++i) {
            PowerButtonView v = (PowerButtonView) getChildAt(i);
            ViewGroup.LayoutParams lp = v.getLayoutParams();
            if (v.getVisibility() != GONE) {
                int col = cursor % mNumColumns;
                float colSpan = v.getColumnSpan();
                int row = cursor / mNumColumns;
				
                // Push the item to the next row if it can't fit on this one
                if ((col + colSpan) > mNumColumns) {
                    x = getPaddingLeft();
                    y += lp.height + mCellGap;
                    row++;
                }

                // Layout the container
                v.layout(x, y, x + lp.width, y + lp.height);
				
                // Offset the position by the cell gap or reset the position and cursor when we
                // reach the end of the row
                cursor += v.getColumnSpan();
                if (cursor < (((row + 1) * mNumColumns))) {
                    x += lp.width + mCellGap;
                } else {
                    x = getPaddingLeft();
                    y += lp.height + mCellGap;
                }
            }
			
        }
		
    }
}
