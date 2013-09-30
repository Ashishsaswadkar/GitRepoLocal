package com.hungama.myplay.activity.data;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.hungama.myplay.activity.data.DataManager.MoodIcon;
import com.hungama.myplay.activity.data.dao.campaigns.Campaign;
import com.hungama.myplay.activity.data.dao.campaigns.Placement;
import com.hungama.myplay.activity.data.dao.catchmedia.Playlist;
import com.hungama.myplay.activity.data.dao.hungama.Category;
import com.hungama.myplay.activity.data.dao.hungama.CategoryTypeObject;
import com.hungama.myplay.activity.data.dao.hungama.Genre;
import com.hungama.myplay.activity.data.dao.hungama.MediaCategoryType;
import com.hungama.myplay.activity.data.dao.hungama.MediaContentType;
import com.hungama.myplay.activity.data.dao.hungama.MediaItem;
import com.hungama.myplay.activity.data.dao.hungama.Mood;
import com.hungama.myplay.activity.data.dao.hungama.Plan;
import com.hungama.myplay.activity.data.dao.hungama.SubscriptionCheckResponse;
import com.hungama.myplay.activity.data.dao.hungama.SubscriptionResponse;
import com.hungama.myplay.activity.data.dao.hungama.Track;
import com.hungama.myplay.activity.data.events.CampaignPlayEvent;
import com.hungama.myplay.activity.data.events.Event;
import com.hungama.myplay.activity.data.events.PlayEvent;
import com.hungama.myplay.activity.data.persistance.Itemable;
import com.hungama.myplay.activity.playlist.PlaylistRequest;
import com.hungama.myplay.activity.util.FileUtils;
import com.hungama.myplay.activity.util.Logger;
import com.hungama.myplay.activity.util.images.DiskLruCache;

/**
 * Manages access to all available cached resources.
 */
public class CacheManager {
	
	private static final String TAG = "CacheManager";
	
	private static final String FILE_MEDIA_ITEMS = "media_items";
	private static final String FILE_EVENTS = "events";
	private static final String FILE_MOODS = "moods";
	private static final String FILE_PREFERENCES = "preferences";
	private static final String FILE_CURRENT_PLAN = "current_plan";
	private static final String FILE_PLANS = "plans";
	private static final String FILE_CAMPAIGN_LIST = "campaign_list";
	private static final String FILE_CAMPAIGN = "campaign";
	private static final String FILE_RADIO_PLACEMENT = "radio_placement";
	private static final String FILE_SPLASH_PLACEMENT = "splash_placement"; 
	private static final String FILE_ITEMABLE = "itemable";
	private static final String FILE_PLAYLIST = "playlist";
	private static final String FILE_TRACK = "track";
	private static final String FILE_PLAYLIST_REQUEST = "playlist_request";
	private static final String FILE_HUNGAMA_TRACK = "hungma_track";
	private static final String FILE_FEEDBACK_SUBJECTS = "feedback_subjects";
	
	
	static final String FOLDER_MOODS_IMAGES = "moods_images";
	static final int CACHE_SIZE_MOODS_IMAGES = 2 * 1024 * 1024; // 2M
	
	private Context mContext;
	private final String mInternalCachePath;
	
	private final File mMoodsImageCache;
	
	public CacheManager(Context context) {
		mContext = context;
		mInternalCachePath = mContext.getCacheDir().getAbsolutePath();
		
		mMoodsImageCache = mContext.getDir(FOLDER_MOODS_IMAGES, Context.MODE_PRIVATE);
	}
	
	
	// ======================================================
	// Media Content
	// ======================================================
	
	private final Object mMediaItemsMutext = new Object();
	
	/**
	 * Stores Media items in the application's internal storage.
	 * @param events
	 * @return true if success, false otherwise.
	 */
	public boolean storeMediaItems(MediaContentType mediaContentType, MediaCategoryType mediaCategoryType, List<MediaItem> mediaItems) {
		synchronized (mMediaItemsMutext) {
			if (mediaItems != null && mediaItems.size() > 0) {
				// serialize the media items to a json structure.
				Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
				String serializedMediaItems = gson.toJson(mediaItems);
				// generates the file location.
				String fileName = FILE_MEDIA_ITEMS + "_" + mediaContentType.toString().toLowerCase() + "_" + 
																		mediaCategoryType.toString().toLowerCase();
				File mediaItemsFile = new File(mInternalCachePath, fileName);
				// stores it.
				return writeSerializedToCacheFile(serializedMediaItems, mediaItemsFile);
			}
			return false;
		}
	}
	
	/**
	 * Receives list of {@link MediaItem}s stored in the application's internal storage.
	 * @param mediaContentType
	 * @param mediaCategoryType
	 * @return List of {@link MediaItem} if success, null otherwise.
	 */
	public List<MediaItem> getStoredMediaItems(MediaContentType mediaContentType, MediaCategoryType mediaCategoryType){
		synchronized (mMediaItemsMutext) {
			// generates the file location.
			String fileName = FILE_MEDIA_ITEMS + "_" + mediaContentType.toString().toLowerCase() + "_" + 
																	mediaCategoryType.toString().toLowerCase();
			File mediaItemsFile = new File(mInternalCachePath, fileName);

			String mediaItemsJson = readSerializedFromCacheFile(mediaItemsFile);
			if (!TextUtils.isEmpty(mediaItemsJson)) {
				// deserialize the json to the list of media items.
				Type listType = new TypeToken<ArrayList<MediaItem>>() {}.getType();
				Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
				List<MediaItem> items = null;
				// the magic.
				try {
					items = gson.fromJson(mediaItemsJson, listType);
					// populate the items with their region.
					for (MediaItem mediaItem : items) {
						mediaItem.setMediaContentType(mediaContentType);
					}
					return items;
				} catch (JsonSyntaxException exception) { exception.printStackTrace();
				} catch (JsonParseException exception) { exception.printStackTrace();}
			}
			return null;
		}
	}
	
	
	// ======================================================
	// Events. 
	// ======================================================
	
	private final Object mEventsMutext = new Object();
	
	/**
	 * Stores events in the application's internal storage.
	 * @param events
	 * @return true if success, false otherwise.
	 */
	public boolean storeEvents(List<Event> events) {
		synchronized (mEventsMutext) {
			Logger.v(TAG, "Storing events in internal storage.");
			if (events != null && events.size() > 0) {
				// serialize the media items to a json structure.
				Gson gson = new GsonBuilder().create();
				String serializedEvents = gson.toJson(events);
				
				// stores it.
				return writeSerializedToCacheFile(serializedEvents, new File(mInternalCachePath, FILE_EVENTS));
			} else {
				return writeSerializedToCacheFile("", new File(mInternalCachePath, FILE_EVENTS));
			}
		}
	}
	
	public List<Event> getStoredEvents(){
		synchronized (mEventsMutext) {
			Logger.v(TAG, "Getting events in internal storage.");
			// generates the file location.
			String serializedEvents = readSerializedFromCacheFile(new File(mInternalCachePath, FILE_EVENTS));
			// deserialize the json to the list of media items.
			Type listType = new TypeToken<ArrayList<Event>>() {}.getType();
			Gson gson = new GsonBuilder().registerTypeAdapter(listType, new EventElementAdapter()).create();
			List<Event> events = null;
			// the magic.
			try {
				events = gson.fromJson(serializedEvents, listType);
				return events;
			} catch (JsonSyntaxException exception) { exception.printStackTrace();
			} catch (JsonParseException exception) { exception.printStackTrace();}
			return null;
		}
	}
	
	public boolean storeEvent(Event event){
		synchronized (mEventsMutext) {
			Logger.v(TAG, "Storing event in internal storage.");
			List<Event> storedEvents = null;
			
			boolean result = false;
			
			if(event != null){
				storedEvents = getStoredEvents();
			}
			
			if(storedEvents != null){
				storedEvents.add(event);
				result = storeEvents(storedEvents);
			}else{
				storedEvents = new ArrayList<Event>();
				storedEvents.add(event);
				result = storeEvents(storedEvents);
			}
			
			return result;
		}
	}
	
	// ======================================================
	// Moods.
	// ======================================================
	
	private final Object mMoodsMutext = new Object();
	
	public boolean storeMoods(List<Mood> moods) {
		synchronized (mMoodsMutext) {
			Logger.v(TAG, "Storing moods in internal storage.");
			// serialize the moods to a json structure.
			Gson gson = new GsonBuilder().create();
			String serializedMoods = gson.toJson(moods);
			return writeSerializedToFile(serializedMoods, FILE_MOODS);
		}
	}
	
	public List<Mood> getStoredMoods() {
		synchronized (mMoodsMutext) {
			Logger.v(TAG, "Getting moods from internal storage.");
			// generates the file location.
			String serializedMoods = readSerializedFromFile(FILE_MOODS);
			// deserialize the json to the list of media items.
			Type listType = new TypeToken<ArrayList<Mood>>() {}.getType();
			Gson gson = new GsonBuilder().create();
			List<Mood> moods = null;
			// the magic.
			try {
				moods = gson.fromJson(serializedMoods, listType);
				return moods;
			} catch (JsonSyntaxException exception) { exception.printStackTrace();
			} catch (JsonParseException exception) { exception.printStackTrace();}
			return null;
		}
	}
	
	public Drawable getMoodIcon(Mood mood, DataManager.MoodIcon moodIcon) throws IOException {
		DiskLruCache diskLruCache = DiskLruCache.open(mMoodsImageCache, 1, 1, CACHE_SIZE_MOODS_IMAGES);
		// gets the right image URL, will be used as the key of the image.
		String imageUrl = moodIcon == MoodIcon.SMALL ? mood.getSmallImageUrl() : mood.getBigImageUrl();
		// creates new instance of a bitmap drawable.
		return BitmapDrawable.createFromPath(diskLruCache.createFilePath(imageUrl));
	}
	
	
	// ======================================================
	// Preferences - categories.
	// ======================================================
	
	private final Object mPreferencesMutext = new Object();
	
	public boolean storePreferences(List<CategoryTypeObject> categories) {
		synchronized (mPreferencesMutext) {
			Logger.v(TAG, "Storing preferences in internal storage.");
			// serialize the moods to a json structure.
			Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
			String serializedMoods = gson.toJson(categories);
			return writeSerializedToFile(serializedMoods, FILE_PREFERENCES);
		}
	}
	
	public List<CategoryTypeObject> getStoredPreferences(){
		synchronized (mPreferencesMutext) {
			Logger.v(TAG, "Getting preferences from internal storage.");
			// generates the file location.
			String serializedCategories = readSerializedFromFile(FILE_PREFERENCES);
			// deserialize the json to the list of categories.
			Type listType = new TypeToken<ArrayList<CategoryTypeObject>>() {}.getType();
			Gson gson = new GsonBuilder().registerTypeAdapter(listType, new CategoryTypeObjectElementAdapter()).create();
			List<CategoryTypeObject> categoryTypeObject = null;
			// the magic.
			try {
				categoryTypeObject = gson.fromJson(serializedCategories, listType);
				return categoryTypeObject;
			} catch (JsonSyntaxException exception) { exception.printStackTrace();
			} catch (JsonParseException exception) { exception.printStackTrace();}
			return null;
		}
	}
	
	
	// ======================================================
	// Current Subscription Plan
	// ======================================================
		
	private final Object mSubscriptionMutext = new Object();
	
	/**
	 * Stores Subscription Plan in the application's internal storage.
	 * @param events
	 * @return true if success, false otherwise.
	 */
	public boolean storeSubscriptionCurrentPlan(SubscriptionCheckResponse subscriptionCheckResponse) {
		synchronized (mSubscriptionMutext) {
			if (subscriptionCheckResponse.getPlan() != null) {
				// serialize the media items to a json structure.
				Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
				String serializedCurrentPlan = gson.toJson(subscriptionCheckResponse);
				// generates the file location.
				String fileName = FILE_CURRENT_PLAN;
				File currentPlanFile = new File(mInternalCachePath, fileName);
				// stores it.
				return writeSerializedToCacheFile(serializedCurrentPlan, currentPlanFile);
			}
			return false;
		}
	}
	
	/**
	 * @return current subscribed {@link Plan} if success, null otherwise.
	 */
	public SubscriptionCheckResponse getStoredCurrentPlan(){
		synchronized (mSubscriptionMutext) {
			// generates the file location.
			String fileName = FILE_CURRENT_PLAN;
			File currentPlanFile = new File(mInternalCachePath, fileName);

			String currentPlanJson = readSerializedFromCacheFile(currentPlanFile);
			if (!TextUtils.isEmpty(currentPlanJson)) {
				Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
				SubscriptionCheckResponse subscribedPlan = null;
				// the magic.
				try {
					subscribedPlan = gson.fromJson(currentPlanJson, SubscriptionCheckResponse.class);						
					return subscribedPlan;
				} catch (JsonSyntaxException exception) { exception.printStackTrace();
				} catch (JsonParseException exception) { exception.printStackTrace();}
			}
			return null;
		}
	}
	
	/**
	 * @return current subscribed {@link Plan} if success, null otherwise.
	 */
	public boolean deleteStoredCurrentPlan(){
		synchronized (mSubscriptionMutext) {
			// generates the file location.
			String fileName = FILE_CURRENT_PLAN;
			File currentPlanFile = new File(mInternalCachePath, fileName);
			return deleteCurrentPlanFile(currentPlanFile);
		}
	}
	
	// ======================================================
	// Subscription Plans For Upgrade
	// ======================================================
			
	private final Object mSubscriptionPlansMutext = new Object();
	
	/**
	 * Stores Subscription Plans For Upgrade in the application's internal storage.
	 * @param List of {@link Plan}s
	 * @return true if success, false otherwise.
	 */
	public boolean storeSubscriptionPlans(List<Plan> plans) {
		synchronized (mSubscriptionPlansMutext) {
			if (plans != null) {
				// serialize the media items to a json structure.
				Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
				String serializedPlans = gson.toJson(plans);
				// generates the file location.
				String fileName = FILE_PLANS;
				File currentPlanFile = new File(mInternalCachePath, fileName);
				// stores it.
				return writeSerializedToCacheFile(serializedPlans, currentPlanFile);
			}
			return false;
		}
	}
	
	/**
	 * @return {@link Plan}s  if success, null otherwise.
	 */
	public List<Plan> getStoredPlans(){
		synchronized (mSubscriptionPlansMutext) {
			// generates the file location.
			String fileName = FILE_PLANS;
			File currentPlanFile = new File(mInternalCachePath, fileName);

			String currentPlanJson = readSerializedFromCacheFile(currentPlanFile);
			if (!TextUtils.isEmpty(currentPlanJson)) {
				Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
				List<Plan> subscriptionPlans = null;
				Type listType = new TypeToken<List<Plan>>() {}.getType();//TODO: check
				// the magic.
				try {
					subscriptionPlans = gson.fromJson(currentPlanJson, listType);						
					return subscriptionPlans;
				} catch (JsonSyntaxException exception) { exception.printStackTrace();
				} catch (JsonParseException exception) { exception.printStackTrace();}
			}
			return null;
		}
	}
		
		
	// ======================================================
	// Private helper methods. 
	// ======================================================
	
	private boolean writeSerializedToCacheFile(String serializedItems, File destination) {
		FileOutputStream fileOutputStream = null;
		try {
			fileOutputStream = new FileOutputStream(destination);
			fileOutputStream.write(serializedItems.getBytes());
			
			fileOutputStream.close();
			fileOutputStream = null;
			return true;
		} catch (FileNotFoundException e) { e.printStackTrace(); return false;
		} catch (IOException e) { e.printStackTrace(); return false;
		} finally {
			if (fileOutputStream != null) {
				try {
					fileOutputStream.close();
				} catch (IOException e) { e.printStackTrace(); }
				fileOutputStream = null;
				return false;
			}
		}
	}
	
	private String readSerializedFromCacheFile(File source) {
		if (source.exists()) {
			BufferedReader inputBufferedReader = null;
			StringBuilder responseBuilder = new StringBuilder();
			try {
				inputBufferedReader = new BufferedReader(
						new InputStreamReader(new FileInputStream(source)));
				for (String line = null; (line = inputBufferedReader.readLine()) != null;) {
					responseBuilder.append(line);
				}
				
				inputBufferedReader.close();
				inputBufferedReader = null;
				
				return responseBuilder.toString();
				
			} catch (FileNotFoundException exception) { exception.printStackTrace();
			} catch (IOException exception) { exception.printStackTrace(); 
			} finally {
				if (inputBufferedReader != null) {
					try {
						inputBufferedReader.close();
						inputBufferedReader = null;
						
					} catch (IOException e) { e.printStackTrace(); }
				}
			} 
		}
		return null;
	}
	
	private boolean deleteCurrentPlanFile(File currentPlanFile) {
		if (currentPlanFile.exists()) {
			return currentPlanFile.delete();
		}
		return false;
	}
	
	private boolean writeSerializedToFile(String serializedItems, String fileName) {
		FileOutputStream fileOutputStream = null;
		try {
			fileOutputStream = mContext.openFileOutput(fileName, Context.MODE_PRIVATE);
			fileOutputStream.write(serializedItems.getBytes());
			
			fileOutputStream.close();
			fileOutputStream = null;
			
			return true;
			
		} catch (FileNotFoundException e) { e.printStackTrace(); return false;
		} catch (IOException e) { e.printStackTrace(); return false;
		} finally {
			if (fileOutputStream != null) {
				try {
					fileOutputStream.close();
					fileOutputStream = null;
					
				} catch (IOException e) { e.printStackTrace(); }
				fileOutputStream = null;
				return false;
			}
		}
	}
	
	private String readSerializedFromFile(String fileName) {
		BufferedReader inputBufferedReader = null;
		StringBuilder responseBuilder = new StringBuilder();
		try {
			inputBufferedReader = new BufferedReader(
					new InputStreamReader(mContext.openFileInput(fileName)));
			for (String line = null; (line = inputBufferedReader.readLine()) != null;) {
				responseBuilder.append(line);
			}
			
			inputBufferedReader.close();
			inputBufferedReader = null;
			
			return responseBuilder.toString();
			
		} catch (FileNotFoundException exception) { exception.printStackTrace();
		} catch (IOException exception) { exception.printStackTrace(); 
		} finally {
			if (inputBufferedReader != null) {
				try {
					inputBufferedReader.close();
					inputBufferedReader = null;
					
				} catch (IOException e) { e.printStackTrace(); }
				inputBufferedReader = null;
			}
		} 
		return null;
	}
	
	/**
	 * Adapter for Gsonning different concrete implementations of the type {@link Event}.
	 */
	private class EventElementAdapter implements JsonDeserializer<List<Event>> {
		
		private static final String UNIQUE_FIELD_PLAY_EVENT = "playingSourceType";
		private static final String EVENT_PATH = "com.hungama.myplay.activity.data.events.";
		
		@Override
		public List<Event> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
			JsonArray jsonArray = json.getAsJsonArray();
			JsonObject jsonObject = null;
			
			List<Event> events = new ArrayList<Event>();
			
			for (JsonElement jsonElement : jsonArray) {
				jsonObject = jsonElement.getAsJsonObject();
				try {
					if(jsonObject.has(UNIQUE_FIELD_PLAY_EVENT)) {
						events.add((Event) context.deserialize(jsonObject, Class.forName(EVENT_PATH + PlayEvent.class.getSimpleName())));
					} else {
						events.add((Event) context.deserialize(jsonObject, Class.forName(EVENT_PATH + CampaignPlayEvent.class.getSimpleName())));
					}
		        } catch (ClassNotFoundException cnfe) {
		            throw new JsonParseException("Unknown element type: " + PlayEvent.class, cnfe);
		        }
			}
			
			return events;
		}

	}
	
	/**
	 * Adapter for Gsonning different concrete implementations of the type {@link CategoryTypeObject}.
	 */
	private class CategoryTypeObjectElementAdapter implements JsonDeserializer<List<CategoryTypeObject>> {
		
		private static final String UNIQUE_FIELD_CATEGORY_TYPE_OBJECT = "type";
		private static final String CATEGORY_TYPE_OBJECT_PATH = "com.hungama.myplay.activity.data.dao.hungama.";

		private static final String MEMBER_ID = "id";
		private static final String MEMBER_NAME = "name";
		private static final String MEMBER_CATEGORY_OBJECT_TYPES = "categoryTypeObjects";
		
		@Override
		public List<CategoryTypeObject> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
			JsonArray jsonArray = json.getAsJsonArray();
			JsonObject jsonObject = null;
			
			String type = null;
			
			List<CategoryTypeObject> categoryTypeObjects = new ArrayList<CategoryTypeObject>();
			
			// iterates thru the root categories.
			for (JsonElement jsonElement : jsonArray) {
				jsonObject = jsonElement.getAsJsonObject();
				type = jsonObject.get(UNIQUE_FIELD_CATEGORY_TYPE_OBJECT).getAsString();
				
				try {
					if(type.equalsIgnoreCase((CategoryTypeObject.TYPE_CATEGORY.toString().toLowerCase()))) {
						// checks if we need to parse the inner category types of the given category type.
						if (jsonObject.get(MEMBER_CATEGORY_OBJECT_TYPES) != null) {
							
							/*
							 * Due to the problem the both Category and Genre are from the same type,
							 * and Category can contain list of both categories and genres, we must pares it
							 * manually :(
							 */
							
							/*
							 * First creates the Category object,
							 * then populates its children by a reference to the list.  
							 */
							long typeId = jsonObject.get(MEMBER_ID).getAsLong();
							String typeName = jsonObject.get(MEMBER_NAME).getAsString();
							
							/*
							 * this list is first initializes and sets as the children in the category,
							 * will be populate when parsing the category's children. 
							 */
							List<CategoryTypeObject> subCategoryTypes = new ArrayList<CategoryTypeObject>();
							
							Category parentCategory = new Category(typeId, typeName, subCategoryTypes);
							
							// gets the list of the sub category types.
							JsonArray subCategoryTypesObjects = jsonObject.getAsJsonArray(MEMBER_CATEGORY_OBJECT_TYPES);
							
							JsonObject subObjectType;
							for (int i = 0; i < subCategoryTypesObjects.size(); i++) {
								subObjectType = (JsonObject) subCategoryTypesObjects.get(i);
								type = subObjectType.get(UNIQUE_FIELD_CATEGORY_TYPE_OBJECT).getAsString();
								
								if(type.equalsIgnoreCase((CategoryTypeObject.TYPE_CATEGORY.toString().toLowerCase()))) {
									long subCategoryTypeId = subObjectType.get(MEMBER_ID).getAsLong();
									String subCategoryTypeName = subObjectType.get(MEMBER_NAME).getAsString();
									
									Category subCategory = new Category(subCategoryTypeId, subCategoryTypeName, null);
									subCategory.setParentCategory(parentCategory);
									
									subCategoryTypes.add(subCategory);
									
								} else {
									// creates the genre.
									Genre childGenre = context.deserialize(subObjectType, 
											Class.forName(CATEGORY_TYPE_OBJECT_PATH + Genre.class.getSimpleName()));
									// sets a reference to its parent.
									childGenre.setParentCategory(parentCategory);
									
									subCategoryTypes.add(childGenre);
									
								}
							}
							
							categoryTypeObjects.add(parentCategory);
							
						} else {
							categoryTypeObjects.add((CategoryTypeObject) context.deserialize(jsonObject, 
									Class.forName(CATEGORY_TYPE_OBJECT_PATH + Category.class.getSimpleName())));
						}
						
					} else {
						categoryTypeObjects.add((CategoryTypeObject) context.deserialize(jsonObject, 
								Class.forName(CATEGORY_TYPE_OBJECT_PATH + Genre.class.getSimpleName())));
					}
		        } catch (ClassNotFoundException cnfe) {
		            throw new JsonParseException("Unknown element type", cnfe);
		        }
			}
			
			return categoryTypeObjects;
		}
		
	}
	
	
	// ======================================================
	// Campaigns.
	// ======================================================
	
	private final Object mCampaignListMutext = new Object();
	
	public boolean storeCampaignList(List<String> list) {
		synchronized (mCampaignListMutext) {
			Logger.v(TAG, "Storing Campaign List id's in internal storage.");
			
			Gson gson = new GsonBuilder().create();
			String serializedCampaignList = gson.toJson(list);
			return writeSerializedToFile(serializedCampaignList, FILE_CAMPAIGN_LIST);
		}
	}
	
	public List<String> getStoredCampaignList() {
		synchronized (mCampaignListMutext) {
			Logger.v(TAG, "Getting Campaign List id's from internal storage.");
			// generates the file location.
			String serializedCampaignList = readSerializedFromFile(FILE_CAMPAIGN_LIST);
			// deserialize the json to the list of media items.
			Type listType = new TypeToken<ArrayList<String>>() {}.getType();
			Gson gson = new GsonBuilder().create();
			List<String> list = null;
			// the magic.
			try {
				list = gson.fromJson(serializedCampaignList, listType);
				return list;
			} catch (JsonSyntaxException exception) { exception.printStackTrace();
			} catch (JsonParseException exception) { exception.printStackTrace();}
			return null;
		}
	}
	
	private final Object mCampaignMutext = new Object();
	
	public boolean storeCampaign(List<Campaign> list) {
		synchronized (mCampaignMutext) {
			Logger.v(TAG, "Storing Campaigns in internal storage.");
			
			Gson gson = new GsonBuilder().create();
			String serializedCampaign = gson.toJson(list);
			return writeSerializedToFile(serializedCampaign, FILE_CAMPAIGN);
		}
	}
	
	public List<Campaign> getStoredCampaign() {
		synchronized (mCampaignListMutext) {
			Logger.v(TAG, "Getting Campaign  from internal storage.");
			// generates the file location.
			String serializedCampaign = readSerializedFromFile(FILE_CAMPAIGN);
			// deserialize the json to the list of media items.
			Type listType = new TypeToken<ArrayList<Campaign>>() {}.getType();
			Gson gson = new GsonBuilder().create();
			List<Campaign> list = null;
			// the magic.
			try {
				list = gson.fromJson(serializedCampaign, listType);
				return list;
			} catch (JsonSyntaxException exception) { exception.printStackTrace();
			} catch (JsonParseException exception) { exception.printStackTrace();}
			return null;
		}
	}
	
	private final Object mRadioPlacementMutext = new Object();
	
	public boolean storeRadioPlacement(List<Placement> list) {
		synchronized (mRadioPlacementMutext) {
			Logger.v(TAG, "Storing Radio Placements in internal storage.");
			
			Gson gson = new GsonBuilder().create();
			String serializedCampaign = gson.toJson(list);
			return writeSerializedToFile(serializedCampaign, FILE_RADIO_PLACEMENT);
		}
	}
	
	public List<Placement> getStoredRadioPlacement() {
		synchronized (mRadioPlacementMutext) {
			Logger.v(TAG, "Getting Radio Placements from internal storage.");
			// generates the file location.
			String serializedPlacement = readSerializedFromFile(FILE_RADIO_PLACEMENT);
			// deserialize the json to the list of media items.
			Type listType = new TypeToken<ArrayList<Placement>>() {}.getType();
			Gson gson = new GsonBuilder().create();
			List<Placement> list = null;
			// the magic.
			try {
				list = gson.fromJson(serializedPlacement, listType);
				return list;
			} catch (JsonSyntaxException exception) { exception.printStackTrace();
			} catch (JsonParseException exception) { exception.printStackTrace();}
			return null;
		}
	}
	
	private final Object mSplashPlacementMutext = new Object();
	
	public boolean storeSplashPlacement(List<Placement> list) {
		synchronized (mSplashPlacementMutext) {
			Logger.v(TAG, "Storing Splash Placements in internal storage.");
			
			Gson gson = new GsonBuilder().create();
			String serializedCampaign = gson.toJson(list);
			return writeSerializedToFile(serializedCampaign, FILE_SPLASH_PLACEMENT);
		}
	}
	
	public List<Placement> getStoredSplashPlacement() {
		synchronized (mSplashPlacementMutext) {
			Logger.v(TAG, "Getting Splash Placements from internal storage.");
			// generates the file location.
			String serializedPlacement = readSerializedFromFile(FILE_SPLASH_PLACEMENT);
			// deserialize the json to the list of media items.
			Type listType = new TypeToken<ArrayList<Placement>>() {}.getType();
			Gson gson = new GsonBuilder().create();
			List<Placement> list = null;
			// the magic.
			try {
				list = gson.fromJson(serializedPlacement, listType);
				return list;
			} catch (JsonSyntaxException exception) { exception.printStackTrace();
			} catch (JsonParseException exception) { exception.printStackTrace();}
			return null;
		}
	}
	
	
	// ======================================================
	// Feedback subjects.
	// ======================================================
	
	private final Object mFeedbackSubjectMutext = new Object();
	
	public boolean storeFeedbackSubjects(List<String> subjects) {
		synchronized (mFeedbackSubjectMutext) {
			Logger.v(TAG, "Storing feedback's subjects in internal storage.");
			Gson gson = new GsonBuilder().create();
			String serializedSubjects = gson.toJson(subjects);
			return writeSerializedToFile(serializedSubjects, FILE_FEEDBACK_SUBJECTS);
		}
	}
	
	public List<String> getStoredFeedbackSubjects() {
		synchronized (mFeedbackSubjectMutext) {
			Logger.v(TAG, "Getting feedback's subjects from internal storage.");
			String serializedSubjects = readSerializedFromFile(FILE_FEEDBACK_SUBJECTS);
			// deserialize the JSON to the list of subjects (AKA. list of stupid strings).
			Type listType = new TypeToken<ArrayList<String>>() {}.getType();
			Gson gson = new GsonBuilder().create();
			List<String> subjects = null;
			// the magic...
			try {
				subjects = gson.fromJson(serializedSubjects, listType);
				return subjects;
			} catch (JsonSyntaxException exception) { exception.printStackTrace();
			} catch (JsonParseException exception) { exception.printStackTrace();}
			return null;
		}
	}
	
	
	// ======================================================
	// Itemables (Playlist).
	// ======================================================
	
	private final Object mItemableListMutext = new Object();
	
	public boolean storePlaylists(Map<Long, Playlist> list) {
		synchronized (mItemableListMutext) {
			Logger.v(TAG, "Storing Itemables List in internal storage.");
			
			String fileName = FILE_PLAYLIST;
		
			Gson gson = new GsonBuilder().create();
			String serializedItemableList = gson.toJson(list);
			return writeSerializedToFile(serializedItemableList, fileName);
		}
	}
	
	public Map<Long, Playlist> getStoredPlaylists(){
		
		synchronized (mItemableListMutext) {
			Logger.v(TAG, "Getting Itemables from internal storage.");
			
			String serializedItemables = readSerializedFromFile(FILE_PLAYLIST);

			Type listType = new TypeToken<Map<Long, Playlist>>() {}.getType();
			
			Gson gson = new GsonBuilder().create();
			
			Map<Long, Playlist> list = null;
			
			// the magic.
			try {
				list = gson.fromJson(serializedItemables, listType);
				return list;
			} catch (JsonSyntaxException exception) { exception.printStackTrace();
			} catch (JsonParseException exception) { exception.printStackTrace();}
			return null;
		}
	}
	
	
	// ======================================================
	// Playlist Request.
	// ======================================================
	
	private final Object mPlaylistRequestMutext = new Object();
	
	public boolean storeRequestList(List<PlaylistRequest> list) {
		synchronized (mPlaylistRequestMutext) {
			Logger.v(TAG, "Storing Playlist Request List in internal storage.");
			
			String fileName = FILE_PLAYLIST_REQUEST;
			
			Gson gson = new GsonBuilder().create();
			String serializedItemableList = gson.toJson(list);
			return writeSerializedToFile(serializedItemableList, fileName);
		}
	}
	
	public List<PlaylistRequest> getStoredRequestList() {
		synchronized (mCampaignListMutext) {
			Logger.v(TAG, "Getting Playlist Request List from internal storage.");
			// generates the file location.
			String serializedCampaign = readSerializedFromFile(FILE_PLAYLIST_REQUEST);
			// deserialize the json to the list of media items.
			Type listType = new TypeToken<ArrayList<PlaylistRequest>>() {}.getType();
			Gson gson = new GsonBuilder().create();
			List<PlaylistRequest> list = null;
			// the magic.
			try {
				list = gson.fromJson(serializedCampaign, listType);
				return list;
			} catch (JsonSyntaxException exception) { exception.printStackTrace();
			} catch (JsonParseException exception) { exception.printStackTrace();}
			return null;
		}
	}
	
	
	// ======================================================
	// Track (Hungama)
	// ======================================================
	
	private final Object mTrackListMutext = new Object();
	
	public boolean storeTrackList(Map<Long,Track> list) {
		synchronized (mTrackListMutext) {
			Logger.v(TAG, "Storing Tracks List in internal storage.");
			
			//Log.i(TAG, "Set Tracks: " + list.keySet().toString());
			
			String fileName = FILE_HUNGAMA_TRACK;
						
			Gson gson = new GsonBuilder().create();
			String serializedItemableList = gson.toJson(list);
			return writeSerializedToFile(serializedItemableList, fileName);
		}
	}
	
	public Map<Long,Track> getStoredTracks(){
		
		synchronized (mItemableListMutext) {
			Logger.v(TAG, "Getting Tracks from internal storage.");
			
			String serializedItemables = readSerializedFromFile(FILE_HUNGAMA_TRACK);
			
			Type listType = new TypeToken<Map<Long,Track>>() {}.getType();
			
			Gson gson = new GsonBuilder().create();
			
			Map<Long,Track> list = null;
			
			// the magic.
			try {
				list = gson.fromJson(serializedItemables, listType);
				
				if(list != null){
					//Log.i(TAG, "Get Tracks :" + list.keySet().toString());
				}
				
				return list;
			} catch (JsonSyntaxException exception) { exception.printStackTrace();
			} catch (JsonParseException exception) { exception.printStackTrace();}
			return null;
		}
	}
	
}
