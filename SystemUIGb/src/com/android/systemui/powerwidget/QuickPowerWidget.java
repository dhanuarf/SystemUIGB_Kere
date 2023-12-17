package com.android.systemui.powerwidget;

import android.widget.*;
import android.view.*;
import android.content.*;
import android.util.*;
import com.android.systemui.R;
import android.graphics.drawable.*;
import android.graphics.*;
import com.android.systemui.statusbar.*;
public class QuickPowerWidget extends PowerWidget
{

	boolean debug = false;
	String tag = "QuickPowerWidget";
	
	private View mDumpView;
	
	public ExpandedPanelView mExpPanelView;
	
	private PowerWidgetContainer mButtonContainer ;
	private ViewGroup.LayoutParams QUICK_TILE_LAYOUT_PARAMS ;
	private PowerButtonView mMoreButtonView;
	private ImageView mMoreButtonIc;
	private View.OnClickListener mMoreButtonIcClickListener;
	private View.OnLongClickListener mMoreButtonIcLongClickListener;
	
	
	private boolean mStateOpen = false;
	public QuickPowerWidget(Context c, AttributeSet as)
	{
		super(c, as);

	}

	@Override
	protected void recreateButtonLayout()
	{
		// TODO: Implement this method
		super.recreateButtonLayout();

		removeAllViews();
		// create FrameLayout to hold the buttons with quicksettings style
		mButtonContainer = new PowerWidgetContainer(getContext());
		mButtonContainer.setNumColumns(6);

		int h = getContext().getResources().getDimensionPixelSize(R.dimen.qpw_tile_height);
		QUICK_TILE_LAYOUT_PARAMS = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,	h);
		
		// buttons
		int index = 1;
        for (String button : mButtonNames)
		{
            PowerButton pb = mButtons.get(button);
            if (pb != null)
			{
                PowerButtonView buttonView = (PowerButtonView)mInflater.inflate(R.layout.qs_item, null, false);
				//		if(buttonView!=null)buttonView.setContent(R.layout.power_widget_button, mInflater);
                pb.setupButton(buttonView);
				pb.setTextVisible(false);
				if (index <= 5)
				{
					mButtonContainer.addView(buttonView, QUICK_TILE_LAYOUT_PARAMS);

				}
				else
				{
					// stop the loop because there 
					// are enough buttons
					break;
				}
				index++;
            }
        }
		/*
		// dumpView
		int buttonNum = mButtons.size();
		if(buttonNum <5){
			int dumpViewNum = 5-buttonNum;
			
			for(int i =0; i<dumpViewNum; i++){
				PowerButtonView dumpView = new PowerButtonView(getContext(),null);
				mButtonContainer.addView(dumpView, QUICK_TILE_LAYOUT_PARAMS);
			}
		}
		*/
		
		// morebutton
		//setupMoreButton();

		addView(mButtonContainer, PARENT_LAYOUT_PARAMS);
		if(mExpPanelView!=null)mExpPanelView.updateQPW();
		if(debug)Log.d(tag, "expPanelV: "+ mExpPanelView);
	}
	private void setupMoreButton()
	{
		mMoreButtonView = (PowerButtonView)mInflater.inflate(R.layout.qs_item, null, false);
		mMoreButtonIc = (ImageView)mMoreButtonView.findViewById(R.id.ic_qs_state);

		// hide text
		mMoreButtonView.findViewById(R.id.qs_name).setVisibility(View.GONE);

		mMoreButtonIc.setImageResource(R.drawable.ic_more);
		
		mButtonContainer.addView(mMoreButtonView, QUICK_TILE_LAYOUT_PARAMS);

	}
	public void setQsState(boolean openState)
	{
			if (openState)
			{
//				mMoreButtonIc.setImageResource(R.drawable.ic_more_up);
				View[] v = getButtonView();
				for (int i=0; i < getButtonCount()-1 ; i++)
				{
					v[i].setVisibility(GONE);
				}
				requestDisallowClick(true);
			}
			else{
//				mMoreButtonIc.setImageResource(R.drawable.ic_more);
				View[] v = getButtonView();
				
				// button count minus one to put more button
				for (int i=0; i < getButtonCount()-1 ; i++)
				{
					v[i].setVisibility(VISIBLE);
				}
				requestDisallowClick(false);
			}
			mStateOpen = openState;
		}
	public boolean getQsState()
	{
		return mStateOpen;
	}
	public View[] getButtonView()
	{
		
		View[] v = new View[getButtonCount()];
		for (int i=0; i < getButtonCount() ; i++)
		{
			v[i] = mButtonContainer.getChildAt(i);
		}
		return v;
	}
	public int getButtonCount(){
		return mButtonContainer.getChildCount();
	}
	public void setMoreButtonClickListener(View.OnClickListener ocl)
	{
		if (mMoreButtonView != null)
			mMoreButtonView.setOnClickListener(ocl);
	}
	public void setMoreButtonLongClickListener(View.OnLongClickListener ocl)
	{
		if (mMoreButtonView != null)
			mMoreButtonView.setOnLongClickListener(ocl);
	}
	public void setExpandedPanel(ExpandedPanelView epv){
		mExpPanelView = epv;
	}
	public void requestDisallowClick(boolean disable){
		for (PowerButton pb : mButtons.values())
		{
            pb.disableClick(disable);
        }
	}
	public void setTintColor(int color)
	{
        // color them
        for (PowerButton pb : mButtons.values())
		{
            pb.setTintColor(color);
        }
		//mMoreButtonIc.setColorFilter(color);
		//mMoreButtonView.setColor(color);
    }

}
