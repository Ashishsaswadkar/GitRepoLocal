package com.hungama.myplay.activity.ui.fragments;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.flurry.android.FlurryAgent;
import com.hungama.myplay.activity.R;
import com.hungama.myplay.activity.communication.CommunicationOperationListener;
import com.hungama.myplay.activity.communication.CommunicationManager.ErrorType;
import com.hungama.myplay.activity.data.DataManager;
import com.hungama.myplay.activity.data.configurations.ApplicationConfigurations;
import com.hungama.myplay.activity.data.dao.hungama.Comment;
import com.hungama.myplay.activity.data.dao.hungama.CommentsListingResponse;
import com.hungama.myplay.activity.data.dao.hungama.CommentsPostResponse;
import com.hungama.myplay.activity.data.dao.hungama.MediaContentType;
import com.hungama.myplay.activity.data.dao.hungama.MediaItem;
import com.hungama.myplay.activity.data.dao.hungama.MediaType;
import com.hungama.myplay.activity.data.dao.hungama.SocialNetwork;
import com.hungama.myplay.activity.gigya.FBFriend;
import com.hungama.myplay.activity.gigya.GigyaManager;
import com.hungama.myplay.activity.gigya.GoogleFriend;
import com.hungama.myplay.activity.gigya.TwitterLoginFragment;
import com.hungama.myplay.activity.gigya.GigyaManager.OnGigyaResponseListener;
import com.hungama.myplay.activity.gigya.TwitterLoginFragment.OnTwitterLoginListener;
import com.hungama.myplay.activity.operations.OperationDefinition;
import com.hungama.myplay.activity.operations.catchmedia.CMOperation;
import com.hungama.myplay.activity.operations.catchmedia.PartnerConsumerProxyCreateOperation;
import com.hungama.myplay.activity.operations.hungama.SocialCommentsListingOperation;
import com.hungama.myplay.activity.operations.hungama.SocialCommentsPostOperation;
import com.hungama.myplay.activity.ui.CommentsActivity;
import com.hungama.myplay.activity.ui.ProfileActivity;
import com.hungama.myplay.activity.util.Logger;
import com.hungama.myplay.activity.util.images.ImageCache;
import com.hungama.myplay.activity.util.images.ImageFetcher;
import com.squareup.picasso.Picasso;

public class CommentsFragment extends MainFragment implements OnClickListener, 
															CommunicationOperationListener, 
															OnGigyaResponseListener, 
															OnTwitterLoginListener{
	
	private static final String TAG = "CommentsFragment";
	public static final String FRAGMENT_COMMENTS = "fragment_comments";
	
	private static final String COMMA = ",";
	private static final String FACEBOOK_PROVIDER = "facebook";
	private static final String TWITTER_PROVIDER = "twitter";
	
	private static final int SUCCESS = 1;
	private static final int NO_COMMENTS_FOUND = 2;
	
	
	private static final String VALUE = "value";
	
	private DataManager mDataManager;
	private GigyaManager mGigyaManager;
	private ApplicationConfigurations mApplicationConfigurations;
	
	private View rootView;
	private MediaItem mMediaItem;
	private int numOfComments;
	
	private boolean mIsFacebookLoggedIn = false;
	private boolean mIsTwitterLoggedIn = false;
	
	private ImageButton facebookImageButton;
	private ImageButton twitterImageButton;
	private Button postButton;
	private EditText commentBox;
	private Button commentsNum;
	
	private ListView mCommentsList;
//	private ImageFetcher mImageFetcher = null;
	
	private TextWatcher mTextWatcher;
	
	// Data members
	private TwitterLoginFragment mTwitterLoginFragment;
		
	private Context mContext;
	FragmentManager mFragmentManager;
	
	// ======================================================
	// Fragment lifecycle methods
	// ======================================================
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mGigyaManager = new GigyaManager(getActivity());
		mGigyaManager.setOnGigyaResponseListener(this);
		showLoadingDialog(R.string.application_dialog_loading_content);
		
		mDataManager = DataManager.getInstance(getActivity().getApplicationContext());
		mApplicationConfigurations = mDataManager.getApplicationConfigurations();
		
		// gets the media item from parent.
		Bundle data = getArguments();
		mMediaItem = (MediaItem) data.getSerializable(CommentsActivity.EXTRA_DATA_MEDIA_ITEM);
		numOfComments = 0;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {		
		rootView = inflater.inflate(R.layout.fragment_comments, container, false);			
		return rootView;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		mContext = getActivity();
		mFragmentManager = getActivity().getSupportFragmentManager();
	}
	
	@Override
	public void onStart() {
		super.onStart();
		mGigyaManager.socializeGetUserInfo();
		showLoadingDialog(R.string.application_dialog_loading_content);
		FlurryAgent.onStartSession(getActivity(), getString(R.string.flurry_app_key)); FlurryAgent.onPageView();
	}
	
    @Override
    public void onResume() {
        super.onResume();
//        if (mImageFetcher != null) {
//        	mImageFetcher.setExitTasksEarly(false);
//        }
        
    }

    @Override
    public void onPause() {
        super.onPause();
//        if (mImageFetcher != null) {
//        	mImageFetcher.setExitTasksEarly(true);
//            mImageFetcher.flushCache();
//        }
    }
	
	@Override
	public void onStop() {
		super.onStop();
		FlurryAgent.onEndSession(getActivity());
	}
	
    @Override
    public void onDestroy() {
        super.onDestroy();
//        if (mImageFetcher != null) {
//        	mImageFetcher.closeCache();
//        	mImageFetcher = null;
//        }
    }
	
	
	// ======================================================
	// Helper Methods
	// ======================================================

	private void initializeComponents() {
		// get all the comments
		if (mMediaItem.getMediaContentType() == MediaContentType.VIDEO) {
			mDataManager.getComments(mMediaItem.getId(), MediaType.VIDEO, 0, 100, this);
		} else {
			mDataManager.getComments(mMediaItem.getId(), mMediaItem.getMediaType(), 0, 100, this);
		}		
		
		if (mMediaItem != null ) {
			//set Components
			Button loginButton = (Button) rootView.findViewById(R.id.login_signup_button_login); 
			loginButton.setOnClickListener(this);
			
			facebookImageButton = (ImageButton) rootView.findViewById(R.id.comments_image_facebook);
			twitterImageButton = (ImageButton) rootView.findViewById(R.id.comments_image_twitter);			
			facebookImageButton.setOnClickListener(this);
			twitterImageButton.setOnClickListener(this);
			
			postButton = (Button) rootView.findViewById(R.id.post_button); 
			postButton.setEnabled(false);
			postButton.setOnClickListener(this);
			
			
			//set the number of comments for the item.
			commentsNum = (Button) rootView.findViewById(R.id.button_media_details_comment);
			commentsNum.setText(String.valueOf(numOfComments));
			
			//set the title
			TextView title = (TextView) rootView.findViewById(R.id.main_title_bar_text);
			title.setText(mMediaItem.getTitle());
			
			mCommentsList = (ListView) rootView.findViewById(R.id.listview_comments);
			
			// check if there is a social login (facebook/twitter)
//			 mIsFacebookLoggedIn = true; //TODO: ############# need gigya method from Dudu #########################
//			 mIsTwitterLoggedIn = false;	//TODO: ############# need gigya method from Dudu #########################
			
			if (!mIsFacebookLoggedIn && !mIsTwitterLoggedIn) {
				//show "Need to login Panel"
				LinearLayout notLoggedInPanel = (LinearLayout) rootView.findViewById(R.id.need_to_login_panel);
				notLoggedInPanel.setVisibility(View.VISIBLE);
				
				//hide "logged in panel"
				LinearLayout LoggedInPanel = (LinearLayout) rootView.findViewById(R.id.logged_in_panel);
				LoggedInPanel.setVisibility(View.GONE);

				RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) mCommentsList.getLayoutParams();
				layoutParams.addRule(RelativeLayout.ABOVE, R.id.need_to_login_panel);
				mCommentsList.setLayoutParams(layoutParams);
			} else  { 
				//hide "Need to login Panel"
				LinearLayout notLoggedInPanel = (LinearLayout) rootView.findViewById(R.id.need_to_login_panel);
				notLoggedInPanel.setVisibility(View.GONE);
				
				//show "logged in panel"
				LinearLayout LoggedInPanel = (LinearLayout) rootView.findViewById(R.id.logged_in_panel);
				LoggedInPanel.setVisibility(View.VISIBLE);

				RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) mCommentsList.getLayoutParams();
				layoutParams.addRule(RelativeLayout.ABOVE, R.id.logged_in_panel);
				mCommentsList.setLayoutParams(layoutParams);
				
				//set EditText key listener for post button
				commentBox = (EditText) rootView.findViewById(R.id.comment_edit_text);
				mTextWatcher = new TextWatcher() {
					private boolean isEnabled = true;
					@Override
					public void onTextChanged(CharSequence s, int start, int before, int count) {
						if (commentBox.length() > 0) {							
							setPostButtonEnabled(true);
						} else {
							setPostButtonEnabled(false);
						}						
					}
					
					@Override
					public void beforeTextChanged(CharSequence s, int start, int count,
							int after) {
						// TODO Auto-generated method stub
						
					}
					
					@Override
					public void afterTextChanged(Editable s) {
						// TODO Auto-generated method stub
						
					}
				};
				commentBox.addTextChangedListener(mTextWatcher);
				
				//set facebook and twitter 
				if (mIsFacebookLoggedIn) {
					//setFacebookImageButtonLoggedIn();
					setFacebookImageButtonSelected();
				} else {
					setFacebookImageButtonNotLoggedIn();
				}
				
				if (mIsTwitterLoggedIn) {
					//setTwitterImageButtonLoggedIn();
					setTwitterImageButtonSelected();
				} else {
					setTwitterImageButtonNotLoggedIn();
				}
			
				
			}
			
		}
	}

	public void showSocialLoginDialog() {
		//set up custom dialog
        final Dialog dialog = new Dialog(getActivity());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_social_login);
        
        TextView title = (TextView) dialog.findViewById(R.id.long_click_custom_dialog_title_text);
        title.setText(getResources().getString(R.string.comments_login_dialog_title));
        
        ImageButton closeButton = (ImageButton) dialog.findViewById(R.id.long_click_custom_dialog_title_image);
        closeButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				dialog.dismiss();				
			}
		});
        
        dialog.setCancelable(true);
        dialog.show();
        
        LinearLayout facebookLogin = (LinearLayout) dialog.findViewById(R.id.long_click_custom_dialog_play_now_row);
        LinearLayout twitterLogin = (LinearLayout) dialog.findViewById(R.id.long_click_custom_dialog_add_to_queue_row);
        
        
        facebookLogin.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				mGigyaManager.facebookLogin();
				dialog.dismiss();
			}
		});                    
        
        twitterLogin.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				mGigyaManager.twitterLogin();
				dialog.dismiss();			
			}
		});
        
	}
	
	/**
	* set the provider to send in postComment
	* @return provider
	*/
	private String setProvider() {
		String provider = "";
		if (facebookImageButton.isSelected()) {
			provider = FACEBOOK_PROVIDER;
		}
		if (twitterImageButton.isSelected()) {
			if (provider.length() > 0) {							
				provider = provider + COMMA + TWITTER_PROVIDER;
			} else {
				provider = provider + TWITTER_PROVIDER;
			}					
		}
		return provider;
	}
	
	public void toggleFragmentTitle() {
		if (getActivity() instanceof CommentsActivity) {
			((CommentsActivity) getActivity()).toggleActivityTitle();
		}
//		RelativeLayout titleBar = (RelativeLayout) rootView.findViewById(R.id.main_title_bar_comments);
//		if (titleBar.getVisibility() == View.VISIBLE) {
//			titleBar.setVisibility(View.GONE);
//		} else {
//			titleBar.setVisibility(View.VISIBLE);
//		}
		
	}
	
	// ======================================================
	// set facebook and twitter buttons
	// ======================================================
	
	private void setFacebookImageButtonNotLoggedIn() {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
			facebookImageButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.icon_facebook_unselected));
        } else {
        	facebookImageButton.setBackground(getResources().getDrawable(R.drawable.icon_facebook_unselected));
        }
		facebookImageButton.setEnabled(true);
	}
	
	private void setFacebookImageButtonLoggedIn() {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
			facebookImageButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.icon_invite_facebook));
        } else {
        	facebookImageButton.setBackground(getResources().getDrawable(R.drawable.icon_invite_facebook));
        }
		facebookImageButton.setEnabled(true);
	}
	
	private void setFacebookImageButtonSelected() {
		if (getActivity() != null) {
			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
				facebookImageButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.icon_facebook_selected));
	        } else {
	        	facebookImageButton.setBackground(getResources().getDrawable(R.drawable.icon_facebook_selected));
	        }
		}
		facebookImageButton.setSelected(true);
		facebookImageButton.setEnabled(true);
	}
	
	private void setTwitterImageButtonNotLoggedIn() {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
			twitterImageButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.icon_twitter_unselected));
        } else {
        	twitterImageButton.setBackground(getResources().getDrawable(R.drawable.icon_twitter_unselected));
        }
		twitterImageButton.setEnabled(true);
	}
	
	private void setTwitterImageButtonLoggedIn() {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
			twitterImageButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.icon_invite_twitter));
        } else {
        	twitterImageButton.setBackground(getResources().getDrawable(R.drawable.icon_invite_twitter));
        }
		twitterImageButton.setEnabled(true);
	}
	
	private void setTwitterImageButtonSelected() {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
			twitterImageButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.icon_twitter_selected));
        } else {
        	twitterImageButton.setBackground(getResources().getDrawable(R.drawable.icon_twitter_selected));
        }
		twitterImageButton.setSelected(true);
		twitterImageButton.setEnabled(true);
	}
	
	private void setPostButtonEnabled(boolean isEnabled) {
		if (isEnabled) {
			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
				postButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.background_button_blue));
	        } else {
	        	postButton.setBackground(getResources().getDrawable(R.drawable.background_button_blue));
	        }
			postButton.setEnabled(true);
		} else {
			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
				postButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.background_button_blue_disabled));
	        } else {
	        	postButton.setBackground(getResources().getDrawable(R.drawable.background_button_blue_disabled));
	        }
			postButton.setEnabled(false);
		}
		
	}
	
	
	// ======================================================
	// Communication Operation Methods
	// ======================================================
	
	@Override
	public void onStart(int operationId) {
		switch (operationId) {
		case OperationDefinition.Hungama.OperationId.SOCIAL_COMMENT_GET:
		case OperationDefinition.Hungama.OperationId.SOCIAL_COMMENT_POST:
			showLoadingDialog(R.string.application_dialog_loading_content);
			break;
		}
		
	}

	@Override
	public void onSuccess(int operationId, Map<String, Object> responseObjects) {
		switch (operationId) {
		case OperationDefinition.Hungama.OperationId.SOCIAL_COMMENT_GET: {
			CommentsListingResponse mCommentsListingResponse = (CommentsListingResponse) responseObjects.get(SocialCommentsListingOperation.RESULT_KEY_COMMENTS_LISTING);
			if (mCommentsListingResponse != null) {
				switch (mCommentsListingResponse.getCode()) {
				case SUCCESS:
					numOfComments = mCommentsListingResponse.getTotalCount();
					commentsNum.setText(String.valueOf(numOfComments));
					
					if(mContext != null){
						CommentsAdapter mCommentsAdapter = new CommentsAdapter(mContext,mCommentsListingResponse.getMyData());
						mCommentsList.setAdapter(mCommentsAdapter);
					}
					
					break;
					
				case NO_COMMENTS_FOUND:
					numOfComments = 0;
					commentsNum.setText(String.valueOf(numOfComments));
					List<Comment> emptyList = new ArrayList<Comment>();
					if (mContext != null) {
						CommentsAdapter mCommentsAdapterNoComments = new CommentsAdapter(mContext, emptyList);
						mCommentsList.setAdapter(mCommentsAdapterNoComments);
					}					
					break;

				default:
					break;
				}
				
			}
			break;
		}
		
		case OperationDefinition.Hungama.OperationId.SOCIAL_COMMENT_POST: {
			CommentsPostResponse mCommentsPostResponse = (CommentsPostResponse) responseObjects.get(SocialCommentsPostOperation.RESULT_KEY_COMMENTS_POST);
			if (mCommentsPostResponse != null && mCommentsPostResponse.getCode() == SUCCESS) {
//				Toast toast = Toast.makeText(getActivity(), getResources().getString(R.string.comments_post_confirmation, String.valueOf(mCommentsPostResponse.getPointsEarned())), Toast.LENGTH_LONG);
//				toast.setGravity(Gravity.CENTER, 0, 0);
//				toast.show();
				commentBox.setText("");
				//refresh list and numOfComments button
				numOfComments++;
				initializeComponents();
//				mDataManager.getComments(mMediaItem.getId(), mMediaItem.getMediaType(), 1, 100, this);
			} else {
				Toast toast = Toast.makeText(getActivity(), getResources().getString(R.string.comments_post_error), Toast.LENGTH_LONG);
				toast.setGravity(Gravity.CENTER, 0, 0);
				toast.show();
			}
			break;
		}
		
		case (OperationDefinition.CatchMedia.OperationId.PARTNER_CONSUMER_PROXY_CREATE):
						
				String activationCode = (String) responseObjects.get(ApplicationConfigurations.ACTIVATION_CODE);
				String partnerUserId = (String) responseObjects.get(ApplicationConfigurations.PARTNER_USER_ID);
				boolean isRealUser = (Boolean) responseObjects.get(ApplicationConfigurations.IS_REAL_USER);
				Map<String, Object> signupFieldsMap = (Map<String, Object>) 
						responseObjects.get(PartnerConsumerProxyCreateOperation.RESPONSE_KEY_OBJECT_SIGNUP_FIELDS);
				
				/*
				 * iterates thru the original signup fields, 
				 * looking for the registered phone number, if exists,
				 * stores it in the application configuration 
				 * as part of the user's credentials.
				 */
				Map<String, Object> fieldMap = (Map<String, Object>) signupFieldsMap.get("phone_number");
				String value = "";
				if (fieldMap != null) {
					value = (String) fieldMap.get(VALUE);
				}
				mApplicationConfigurations.setUserLoginPhoneNumber(value);
		
				// stores partner user id to connect with Hungama REST API.
				mApplicationConfigurations.setPartnerUserId(partnerUserId);
				mApplicationConfigurations.setIsRealUser(isRealUser);
				// let's party!
				mDataManager.createDeviceActivationLogin(activationCode, this);
			
		break;

		case (OperationDefinition.CatchMedia.OperationId.DEVICE_ACTIVATION_LOGIN_CREATE):

			Map<String, Object> responseMap = (Map<String, Object>) responseObjects.get(CMOperation.RESPONSE_KEY_GENERAL_OBJECT);
			// stores the session and other crucial properties.
			String sessionID = (String) responseMap.get(ApplicationConfigurations.SESSION_ID);
			int householdID = ((Long) responseMap.get(ApplicationConfigurations.HOUSEHOLD_ID)).intValue();
			int consumerID = ((Long) responseMap.get(ApplicationConfigurations.CONSUMER_ID)).intValue();
			String passkey = (String) responseMap.get(ApplicationConfigurations.PASSKEY);
	
			mApplicationConfigurations.setSessionID(sessionID);
			mApplicationConfigurations.setHouseholdID(householdID);
			mApplicationConfigurations.setConsumerID(consumerID);
			mApplicationConfigurations.setPasskey(passkey);

			if(mTwitterLoginFragment != null){
				mTwitterLoginFragment.finish();	
//				mIsTwitterLoggedIn = true;
//				setTwitterImageButtonLoggedIn();
//				initializeComponents();
				mGigyaManager.socializeGetUserInfo();
			}
			
			
				
			break;

		default:
			break;
		}
		
		hideLoadingDialog();
	}

	@Override
	public void onFailure(int operationId, ErrorType errorType,
			String errorMessage) {
		switch (operationId) {
		case OperationDefinition.Hungama.OperationId.SOCIAL_COMMENT_GET: {
			Logger.i(TAG, "Failed loading comments");
		}	
			break;
			
		case OperationDefinition.Hungama.OperationId.SOCIAL_COMMENT_POST: {
			Logger.i(TAG, "Failed posting comment");
		}	
			break;				
		
		case OperationDefinition.CatchMedia.OperationId.PARTNER_CONSUMER_PROXY_CREATE: {
			Toast toast = Toast.makeText(getActivity(), errorMessage, Toast.LENGTH_LONG);
			toast.setGravity(Gravity.CENTER, 0, 0);
			toast.show();
			
			mGigyaManager.cancelGigyaProviderLogin();
			
		}	
			break;			
			
		default:
			break;
		}
		
		hideLoadingDialog();
	}

	
	// ======================================================
	// On Click Method
	// ======================================================

	@Override
	public void onClick(View view) {
		int viewId = view.getId();
		
		switch (viewId) {
		case R.id.login_signup_button_login:
			showSocialLoginDialog();
			break;
			
		case R.id.comments_image_facebook:
			if (!mIsFacebookLoggedIn) {
				mGigyaManager.facebookLogin();
			} else 
				if (view.isSelected()) {
						view.setSelected(false);
						setFacebookImageButtonLoggedIn();				
					} else {
						view.setSelected(true);
						setFacebookImageButtonSelected();				
					}
				
			break;
			
		case R.id.comments_image_twitter:
			if (!mIsTwitterLoggedIn) {
				mGigyaManager.twitterLogin();
			} else if (view.isSelected()) {
						view.setSelected(false);
						setTwitterImageButtonLoggedIn();
					} else {
						view.setSelected(true);
						setTwitterImageButtonSelected();
					}
			
			break;
			
		case R.id.post_button:
			if (facebookImageButton.isSelected() || twitterImageButton.isSelected()) {				
				String provider = setProvider();
				String encodedTextToPost = null;
				try {
					encodedTextToPost = URLEncoder.encode(commentBox.getText().toString(), "UTF-8");
				} catch (UnsupportedEncodingException e) {
					Logger.e(TAG, "Failed to encode url");
					e.printStackTrace();
				}
				if (encodedTextToPost != null) {					
					if (mMediaItem.getMediaContentType() == MediaContentType.VIDEO) {
						mDataManager.postComment(mMediaItem.getId(), MediaType.VIDEO, provider, encodedTextToPost, this);
					} else {
						mDataManager.postComment(mMediaItem.getId(), mMediaItem.getMediaType(), provider, encodedTextToPost, this);
					}						
				}
			} else {
				Toast toast = Toast.makeText(getActivity(), getResources().getString(R.string.comments_post_select_provider), Toast.LENGTH_LONG);
				toast.setGravity(Gravity.CENTER, 0, 0);
				toast.show(); 
			}
			
			break;
		
		case R.id.comment_user_image:
			View parent = (View) view.getParent();
			Comment commentLine = (Comment) parent.getTag(R.id.view_tag_object);
			Bundle arguments = new Bundle();
			arguments.putString(ProfileActivity.DATA_EXTRA_USER_ID,  String.valueOf(commentLine.getUserId()));
			Intent profileActivity = new Intent(getActivity().getApplicationContext(), ProfileActivity.class);
			profileActivity.putExtras(arguments);
			startActivity(profileActivity);
			break;
			
		default:
			break;
		}
		
	}

	
	// ======================================================
	// Gigya Response Listener Methods
	// ======================================================
	
	@Override
	public void onGigyaLoginListener(SocialNetwork provider, Map<String, Object> signupFields, long setId) {
		
		if (provider == SocialNetwork.TWITTER) {
			// Twitter
			toggleFragmentTitle();
			FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
			fragmentTransaction.setCustomAnimations(R.anim.slide_left_enter,
					R.anim.slide_left_exit,
	                R.anim.slide_right_enter,
	                R.anim.slide_right_exit);
			
			TwitterLoginFragment fragment = new TwitterLoginFragment(signupFields, setId);
			fragmentTransaction.replace(R.id.comments_fragmant_container, fragment, TwitterLoginFragment.FRAGMENT_TWITTER_LOGIN);
			fragmentTransaction.addToBackStack(TwitterLoginFragment.class.toString());
			fragmentTransaction.commit();
			
			// Listen to result from TwitterLoinFragment
			fragment.setOnTwitterLoginListener(this);
			setTwitterImageButtonLoggedIn();
		} else {
			// FaceBook, Google
			// Call PCP 
			mDataManager.createPartnerConsumerProxy(signupFields, setId, this, false);
			mGigyaManager.socializeGetUserInfo();
		}
		
	}
	
	@Override
	public void onSocializeGetFriendsInfoListener(List<FBFriend> fbFriendsList) {}

	@Override
	public void onSocializeGetContactsListener(List<GoogleFriend> googleFriendsList) {}
	
	@Override
	public void onSocializeGetUserInfoListener() {		
		mIsFacebookLoggedIn = mGigyaManager.isFBConnected();
		mIsTwitterLoggedIn = mGigyaManager.isTwitterConnected();
		initializeComponents();
	}
	
	@Override
	public void onGigyaLogoutListener() {}
	
	
	// ======================================================
	// Twitter login callback
	// ======================================================
	
	@Override
	public void onTwitterLoginListener(TwitterLoginFragment fragment,
			Map<String, Object> signupFields, long setId) {
		// Call PCP 
		// It's include the email and password that user insert in TwitterLoginFragment
		mDataManager.createPartnerConsumerProxy(signupFields, setId, this, false);
		mTwitterLoginFragment = fragment;
		
	}

	@Override
	public void onCancelLoginListener() {
		mGigyaManager.removeConnetion(SocialNetwork.TWITTER);
		
	}
	
	
	// ======================================================
	// Comments Adapter
	// ======================================================
	
	private static class ViewHolder {
		TextView commentUserName;
		TextView commentDate;
		TextView commentText;
		ImageView commentUserImage;
	}
	
	private class CommentsAdapter extends BaseAdapter {
		
		private List<Comment> mComments;
		private LayoutInflater mInflater;	
		
		private Context mContext;
		
		public CommentsAdapter(Context context, List<Comment> comments) {
			
			mContext = context;
			
			mComments = comments;
			mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			
			// initializes the image loader.
			int imageSize = 110;
			if (getActivity() != null) {
				imageSize = getResources().getDimensionPixelSize(R.dimen.comment_result_line_image_size);
			}
			
			// creates the cache.
//			ImageCache.ImageCacheParams cacheParams =
//	                new ImageCache.ImageCacheParams(getActivity(), DataManager.FOLDER_THUMBNAILS_CACHE);
//	        cacheParams.setMemCacheSizePercent(getActivity(), 0.10f);
//			
//			mImageFetcher = new ImageFetcher(getActivity(), imageSize);
//			mImageFetcher.setLoadingImage(R.drawable.background_home_tile_album_default);
//			mImageFetcher.addImageCache(getChildFragmentManager(), cacheParams);
//	        mImageFetcher.setImageFadeIn(true);
		}			
		
		@Override
		public int getCount() {
			return mComments.size();
		}

		@Override
		public Object getItem(int position) {
			return mComments.get(position);
		}

		@Override
		public long getItemId(int position) {
			return mComments.get(position).getUserId();
		}
		
		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
		
			ViewHolder viewHolder;
			// create view if not exist.
			if (convertView == null) {
				convertView = mInflater.inflate(R.layout.list_item_comment, parent, false);
				
				viewHolder = new ViewHolder();
				
				viewHolder.commentUserName = (TextView) convertView.findViewById(R.id.comment_user_name);
				viewHolder.commentDate = (TextView) convertView.findViewById(R.id.comment_date);
				viewHolder.commentUserImage = (ImageView) convertView.findViewById(R.id.comment_user_image);
				
				convertView.setTag(R.id.view_tag_view_holder, viewHolder);
			} else {
				viewHolder = (ViewHolder) convertView.getTag(R.id.view_tag_view_holder);
			}			
			
			// populate the view from the Comments list.
			Comment comment = mComments.get(position);
			// stores the object in the view.
			convertView.setTag(R.id.view_tag_object, comment);
			
			viewHolder.commentUserName.setText(comment.getUserName());
			viewHolder.commentDate.setText("at " + comment.getTime());
			TextView commentText = (TextView) convertView.findViewById(R.id.comment_text);
			commentText.setText(comment.getComment());
			
			// gets the image and its size.
//	        viewHolder.commentUserImage = (ImageView) convertView.findViewById(R.id.comment_user_image);
	        viewHolder.commentUserImage.setOnClickListener(CommentsFragment.this);
//	        viewHolder.commentUserImage.setOnClickListener(new OnClickListener() {
//				
//				@Override
//				public void onClick(View v) {
////					Bundle arguments = new Bundle();
////					arguments.putLong(ProfileActivity.DATA_EXTRA_USER_ID,  comment.getUserId());
////					Intent profileActivity = new Intent(getActivity().getApplicationContext(), ProfileActivity.class);
////					profileActivity.putExtras(arguments);
////					startActivity(profileActivity);
//				}
//			});
	        Picasso.with(mContext).cancelRequest(viewHolder.commentUserImage);
	        if (mContext != null && !TextUtils.isEmpty(comment.getPhotoUrl())) {
//	        	mImageFetcher.loadImage(comment.getPhotoUrl(), viewHolder.commentUserImage);
	        	Picasso.with(mContext)
	        			.load(comment.getPhotoUrl())
	        			.placeholder(R.drawable.background_home_tile_album_default)
	        			.into(viewHolder.commentUserImage);
	        } 
//	        else {
//	        	if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
//	        		viewHolder.commentUserImage.setBackgroundDrawable(getResources().getDrawable(R.drawable.background_media_details_playlist_inside_thumb));
//	        	} else {
//	        		viewHolder.commentUserImage.setBackground(getResources().getDrawable(R.drawable.background_media_details_playlist_inside_thumb));
//	        	}
//	        }
			
			return convertView;
		}
		
	}

	@Override
	public void onFacebookInvite() {}

	@Override
	public void onTwitterInvite() {}
	
	
}
