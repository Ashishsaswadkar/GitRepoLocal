package com.hungama.myplay.activity.ui.fragments;

import com.hungama.myplay.activity.R;
import com.hungama.myplay.activity.data.dao.hungama.MediaTrackDetails;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class PlayerInfoFragment extends Fragment implements OnClickListener {
	
	private static final String TAG = "PlayerInfoFragment";
	
	public static final String FRAGMENT_ARGUMENT_MEDIA_TRACK_DETAILS = "fragment_argument_media_track_details";
	
	public interface OnInfoItemSelectedListener {
		
		public void onInfoItemSelected(String infoItemText);
	}
	
	public void setOnInfoItemSelectedListener(OnInfoItemSelectedListener listener) {
		mOnInfoItemSelectedListener = listener;
	}
	
	private OnInfoItemSelectedListener mOnInfoItemSelectedListener;
	private MediaTrackDetails mMediaTrackDetails = null;
	
	private TextView mTextAlbum;
	private TextView mTextLanguageCategory;
	private TextView mTextMood;
	private TextView mTextGenre;
	private TextView mTextMusic;
	private TextView mTextSingers;
	private TextView mTextCast;
	private TextView mTextLyrics;
	
	private RelativeLayout infoPage;
	private Button infoPageButton;
	private Button shareButton;
	private boolean infoWasClicked = false;
	private LinearLayout infoAlbum;
	private LinearLayout infoLanguageCategory;
	private LinearLayout infoMood;
	private LinearLayout infoGenre;
	private LinearLayout infoMusic;
	private LinearLayout infoSingers;
	private LinearLayout infoCast;
	private LinearLayout infoLyrics;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Bundle data = getArguments();
		if (data != null) {
			mMediaTrackDetails = (MediaTrackDetails) data.getSerializable(FRAGMENT_ARGUMENT_MEDIA_TRACK_DETAILS);
		}
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		
		View rootView = inflater.inflate(R.layout.fragment_player_info, container, false);
		
		// sets a temporarily solution when the data is not available.
		// TODO: Sets an error message for not available information.
		if (mMediaTrackDetails != null){
			initializeUserControls(rootView);
			populateUserControls(rootView);
		} else {
			rootView.setVisibility(View.INVISIBLE);
		}
		
		return rootView;
	}
	
	private void initializeUserControls(View rootView) {
		
//		mTextAlbum = (TextView) rootView.findViewById(R.id.textview_row_1_right);
//		mTextLanguageCategory = (TextView) rootView.findViewById(R.id.textview_row_2_right);
//		mTextMood = (TextView) rootView.findViewById(R.id.textview_row_3_right);
//		mTextGenre = (TextView) rootView.findViewById(R.id.textview_row_4_right);
//		mTextMusic = (TextView) rootView.findViewById(R.id.textview_row_5_right);
//		mTextSingers = (TextView) rootView.findViewById(R.id.textview_row_6_right);
//		mTextCast = (TextView) rootView.findViewById(R.id.textview_row_7_right);
//		mTextLyrics = (TextView) rootView.findViewById(R.id.textview_row_8_right);
		

		infoAlbum = (LinearLayout) rootView.findViewById(R.id.textview_row_1_right);
		infoAlbum.setOnClickListener(this);
		infoLanguageCategory = (LinearLayout) rootView.findViewById(R.id.textview_row_2_right);
		infoLanguageCategory.setOnClickListener(this);
		infoMood = (LinearLayout) rootView.findViewById(R.id.textview_row_3_right);
		infoMood.setOnClickListener(this);
		infoGenre = (LinearLayout) rootView.findViewById(R.id.textview_row_4_right);
		infoGenre.setOnClickListener(this);
		infoMusic = (LinearLayout) rootView.findViewById(R.id.textview_row_5_right);
		infoMusic.setOnClickListener(this);
		infoSingers = (LinearLayout) rootView.findViewById(R.id.textview_row_6_right);
		infoSingers.setOnClickListener(this);
		infoCast = (LinearLayout) rootView.findViewById(R.id.textview_row_7_right);
		infoCast.setOnClickListener(this);
		infoLyrics = (LinearLayout) rootView.findViewById(R.id.textview_row_8_right);
		infoLyrics.setOnClickListener(this);
	}

	private void populateUserControls(View rootView) {
		View seperator;
		if (!mMediaTrackDetails.getAlbumName().equalsIgnoreCase("")  &&
				!mMediaTrackDetails.getReleaseYear().equalsIgnoreCase("")) {
			String albumAndYear = mMediaTrackDetails.getAlbumName() + " (" + mMediaTrackDetails.getReleaseYear() + ")";
			setTextForTextViewButton (albumAndYear, infoAlbum);
//			infoAlbum.setText();
		} else {
			hideTableRow(infoAlbum);
			seperator = (View) rootView.findViewById(R.id.seperator_1);
			seperator.setVisibility(View.GONE);
		}
		
		if (!mMediaTrackDetails.getLanguage().equalsIgnoreCase("")) {
			String language = mMediaTrackDetails.getLanguage();
			setTextForTextViewButton (language, infoLanguageCategory);
//			infoLanguageCategory.setText(mMediaTrackDetails.getLanguage());
		} else {
			hideTableRow(infoLanguageCategory);
			seperator = (View) rootView.findViewById(R.id.seperator_2);
			seperator.setVisibility(View.GONE);
		}
		
		if (!mMediaTrackDetails.getMood().equalsIgnoreCase("")) {
			String mood = mMediaTrackDetails.getMood();
			setTextForTextViewButton (mood, infoMood);
//			infoMood.setText(mMediaTrackDetails.getMood());
		} else {
			hideTableRow(infoMood);
			seperator = (View) rootView.findViewById(R.id.seperator_3);
			seperator.setVisibility(View.GONE);
		}
		
		if (!mMediaTrackDetails.getGenre().equalsIgnoreCase("")) {
			String genre = mMediaTrackDetails.getGenre();
			setTextForTextViewButton (genre, infoGenre);
//			infoGenre.setText(mMediaTrackDetails.getGenre());
		} else {
			hideTableRow(infoGenre);
			seperator = (View) rootView.findViewById(R.id.seperator_4);
			seperator.setVisibility(View.GONE);
		}
		
		if (!mMediaTrackDetails.getMusicDirector().equalsIgnoreCase("")) {
			String musicDirector = mMediaTrackDetails.getMusicDirector();
			setTextForTextViewButton (musicDirector, infoMusic);
//			infoMusic.setText(mMediaTrackDetails.getMusicDirector());
		} else {
			hideTableRow(infoMusic);
			seperator = (View) rootView.findViewById(R.id.seperator_5);
			seperator.setVisibility(View.GONE);
		}		
		
		if (!mMediaTrackDetails.getSingers().equalsIgnoreCase("")) {
			String singers = mMediaTrackDetails.getSingers();
			setTextForTextViewButton (singers, infoSingers);				
		} else {
			hideTableRow(infoSingers);
			seperator = (View) rootView.findViewById(R.id.seperator_6);
			seperator.setVisibility(View.GONE);
		}
		
		if (!mMediaTrackDetails.getCast().equalsIgnoreCase("")) {
			String cast = mMediaTrackDetails.getCast();
			setTextForTextViewButton (cast, infoCast);
//			infoCast.setText(mMediaTrackDetails.getCast());
		} else {
			hideTableRow(infoCast);
			seperator = (View) rootView.findViewById(R.id.seperator_7);
			seperator.setVisibility(View.GONE);
		}
		
		if (!mMediaTrackDetails.getLyricist().equalsIgnoreCase("")) {
			String lyricist = mMediaTrackDetails.getLyricist();
			setTextForTextViewButton (lyricist, infoLyrics);
//			infoLyrics.setText(mMediaTrackDetails.getLyricist());
		} else {
			hideTableRow(infoLyrics);
			seperator = (View) rootView.findViewById(R.id.seperator_8);
			seperator.setVisibility(View.GONE);
		}		
	}
	
	private void hideTableRow(View view) {
		TableRow tableRow = (TableRow) view.getParent();
		tableRow.setVisibility(View.GONE);		
	}
	
	private void setTextForTextViewButton (String text, LinearLayout row) {
		boolean isOneWord = true;
		TextView keywordButton = null;
		if (text.contains(",")) {
			String[] parts = text.split(",");
			int i = 0;
			for (final String keyword : parts) {
				boolean lastPosition =  i == parts.length-1 ? true : false;
				if (lastPosition) {
					keywordButton = createTextViewButtonInfo(keyword, isOneWord);
				} else {
					keywordButton = createTextViewButtonInfo(keyword, !isOneWord);
				}
				row.addView(keywordButton);
				i++;
			}
		} else {
			keywordButton = createTextViewButtonInfo(text, isOneWord);
			row.addView(keywordButton);
		}				
	}
	
	private TextView createTextViewButtonInfo(final String keyword ,boolean isOneWord) {
		TextView keywordButton = new TextView(getActivity());
		keywordButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (mOnInfoItemSelectedListener != null) {
					mOnInfoItemSelectedListener.onInfoItemSelected(keyword);
				}
//				openMainSearchFragment(keyword);
				
			}
		});
		if (isOneWord) {
			keywordButton.setText(keyword);
		} else {
			keywordButton.setText(keyword + ",");
		}
		keywordButton.setTextAppearance(getActivity(), R.style.videoPlayeInfoRowText);
		keywordButton.setTypeface(null,Typeface.BOLD);
		keywordButton.setSingleLine(false);
		return keywordButton;
	}	
	
	@Override
	public void onClick(View view) {
		int viewId = view.getId();
		if (viewId == R.id.textview_row_1_right || 
				viewId == R.id.textview_row_2_right || 
				viewId == R.id.textview_row_3_right || 
				viewId == R.id.textview_row_4_right ||
				viewId == R.id.textview_row_5_right ||
				viewId == R.id.textview_row_6_right ||
				viewId == R.id.textview_row_7_right ||
				viewId == R.id.textview_row_8_right) {
			
			if (mOnInfoItemSelectedListener != null) {
				mOnInfoItemSelectedListener.onInfoItemSelected(((TextView) view).getText().toString());
			}
		}
	}

}
