/**
 * 
 */
package com.hungama.myplay.activity.ui.widgets;

import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.hungama.myplay.activity.R;

/**
 * Custom layout to hold the media item's thumbnail and it's play button in a square shape.
 * when measuring it, the layout will try to get the maximum width and height it cans.
 */
public class StreamMediaItemView extends RelativeLayout {
	
	private static final String TAG = "StreamMediaItemView";
	
	private static final int DEFAULT_MEASURE_SIZE = 100;
	
	private static final int VIEW_ID_BUTTON_ID = 10001;
	
	private int mPlayButtonSize;
	
	private ImageView backgroundImage;
	private SocialMyStreamMediaItemListener mSocialMyStreamMediaItemListener;
	
	public StreamMediaItemView(Context context) {
		super(context);
	}
	
	public StreamMediaItemView(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		init();
	}

	public StreamMediaItemView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		
		init();
	}
	
	private void init() {
		
		Resources resources = getResources();
		mPlayButtonSize = resources.getDimensionPixelSize(R.dimen.social_mystream_item_media_item_play_button_size);
		int buttonBottomMargin = resources.getDimensionPixelSize(R.dimen.social_mystream_item_media_item_play_button_margin_bottom);
		int buttonRigthMargin = resources.getDimensionPixelSize(R.dimen.social_mystream_item_media_item_play_button_margin_right);

		
		// creates the background image.
		backgroundImage = new ImageView(getContext());
		RelativeLayout.LayoutParams backgroundImageParams = 
					new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, 
												    RelativeLayout.LayoutParams.MATCH_PARENT);
		
		backgroundImage.setImageResource(R.drawable.background_home_tile_album_default);
		
		addView(backgroundImage, backgroundImageParams);

		// creates the play button.
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(mPlayButtonSize, mPlayButtonSize);
		params.bottomMargin = buttonBottomMargin;
		params.rightMargin = buttonRigthMargin;
		params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		
		ImageButton playButton = new ImageButton(getContext());
		playButton.setId(VIEW_ID_BUTTON_ID);
		playButton.setImageResource(0);
		playButton.setImageResource(R.drawable.icon_home_music_tile_play);
		
		addView(playButton, params);
		
		// adds listeners.
		playButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View view) {
				if (mSocialMyStreamMediaItemListener != null) {
					mSocialMyStreamMediaItemListener.onPlayButtonClicked(StreamMediaItemView.this);
				}
			}
		});
		playButton.setOnLongClickListener(new OnLongClickListener() {
			
			@Override
			public boolean onLongClick(View view) {
				if (mSocialMyStreamMediaItemListener != null) {
					mSocialMyStreamMediaItemListener.onPlayButtonLongClicked(StreamMediaItemView.this);
				}
				return true;
			}
		});
		
		backgroundImage.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View view) {
				if (mSocialMyStreamMediaItemListener != null) {
					mSocialMyStreamMediaItemListener.onItemClicked(StreamMediaItemView.this);
				}
			}
		});
		backgroundImage.setOnLongClickListener(new OnLongClickListener() {
			
			@Override
			public boolean onLongClick(View view) {
				if (mSocialMyStreamMediaItemListener != null) {
					mSocialMyStreamMediaItemListener.onItemLongClicked(StreamMediaItemView.this);
				}
				return true;
			}
		});
		
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		
		int count = getChildCount();
		for (int i = 0; i < count; i++) {
			View child = getChildAt(i);
			child.measure(MeasureSpec.makeMeasureSpec(mPlayButtonSize, MeasureSpec.EXACTLY), 
						  MeasureSpec.makeMeasureSpec(mPlayButtonSize, MeasureSpec.EXACTLY));
		}
		
		/*
		 * Assigns the width and the height of the layout as equal,
		 * based on the biggest value given to them.
		 */
		int width = calculateMeasure(widthMeasureSpec);
		int height = calculateMeasure(heightMeasureSpec);
		
		width = Math.min(width, height);
		setMeasuredDimension(width, width);
	}
	
	/*
	 * Calculates the view's dimension based on the given spec.
	 * @param measureSpec value from the {@code onMeasure()} method.
	 * @return the size of the given spec this view requests to be.
	 */
	private int calculateMeasure(int measureSpec) {
		int specMode = MeasureSpec.getMode(measureSpec);
		int specSize = MeasureSpec.getSize(measureSpec);
		
		if (specMode == MeasureSpec.AT_MOST || 
					specMode == MeasureSpec.EXACTLY) {
			/*
			 * The view wants to be as its parent asks or 
			 * the maximum size it gives it.
			 */
			return specSize;
		}
		
		// retrieving a default size.
		return DEFAULT_MEASURE_SIZE;
	}
	
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		
		// sets the background image.
		View backgroundImage = getChildAt(0);
		backgroundImage.layout(0, 0, getMeasuredWidth(), getMeasuredHeight());
		
		// sets the play button.
		int left = getMeasuredWidth() - mPlayButtonSize;
		int top = getMeasuredHeight() - mPlayButtonSize;
		
		View palyButton = getChildAt(1);
		
		palyButton.layout(left, top, getMeasuredWidth(), getMeasuredHeight());
		
	}
	
	
	// ======================================================
	// Public interface. 
	// ======================================================
	
	public interface SocialMyStreamMediaItemListener {
		
		public void onPlayButtonClicked(View mediaItemView);
		
		public void onItemClicked(View mediaItemView);
		
		public void onPlayButtonLongClicked(View mediaItemView);
		
		public void onItemLongClicked(View mediaItemView);
	}
	
	public void setSocialMyStreamMediaItemListener(SocialMyStreamMediaItemListener listener) {
		mSocialMyStreamMediaItemListener = listener;
	}
	
	public ImageView getBackgroundImage() {
		return (ImageView) getChildAt(0);
	}
	
	public void setImageResource(int resId) {
		if (backgroundImage != null) {
			backgroundImage.setImageResource(resId);
		}
	}
	
	/**
	 * By default the button is visible.
	 * True - show the button, False otherwise.
	 *  
	 * @param visibility
	 */
	public void setPlayButtonVisibilty(boolean visibility) {
		ImageButton playButton = (ImageButton) findViewById(VIEW_ID_BUTTON_ID);
		if (visibility) {
			playButton.setVisibility(View.VISIBLE);
		} else {
			playButton.setVisibility(View.INVISIBLE);
		}
	}
}
