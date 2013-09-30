package com.hungama.myplay.activity.operations.hungama;

import java.util.ArrayList;
import java.util.List;

import com.hungama.myplay.activity.data.dao.hungama.Category;
import com.hungama.myplay.activity.data.dao.hungama.Discover;
import com.hungama.myplay.activity.data.dao.hungama.DiscoverSearchResultIndexer;
import com.hungama.myplay.activity.data.dao.hungama.Era;
import com.hungama.myplay.activity.data.dao.hungama.Genre;
import com.hungama.myplay.activity.data.dao.hungama.Mood;
import com.hungama.myplay.activity.data.dao.hungama.Tempo;
import com.hungama.myplay.activity.util.Utils;

public abstract class DiscoverOperation extends HungamaOperation {

	protected static final String KEY_ID = "discovery_id";
	protected static final String KEY_NAME = "discovery_name";
	protected static final String KEY_MOOD = "mood";
	protected static final String KEY_TAG = "tag";
	protected static final String KEY_GENRE = "genre";
	protected static final String KEY_CATEGORY = "category";
	protected static final String KEY_FROM_ERA = "from_era";
	protected static final String KEY_TO_ERA = "to_era";
	protected static final String KEY_TEMPO = "tempo";
	
	protected static final String KEY_START_INDEX = "startIndex";
	protected static final String KEY_LENGTH = "length";
	protected static final String KEY_MAX = "max";
	
	protected String buildURLParametersFromDiscoverObject(Discover discover) {
		
		Mood mood = discover.getMood();
		List<Genre> genres = discover.getGenres();
		List<Category> categories = discover.getCategories();
		List<Tempo> tempos = discover.getTempos();
		Era era = discover.getEra();
		
		StringBuilder serverURL = new StringBuilder();
		
		// mood or tag.
		if (mood != null && mood.getId() > 0) {
			serverURL.append(KEY_MOOD).append(EQUALS).append(Integer.toString(mood.getId())).append(AMPERSAND);
			serverURL.append(KEY_TAG).append(EQUALS).append(AMPERSAND);
		} else {
			if (mood != null){
				serverURL.append(KEY_MOOD).append(EQUALS).append(AMPERSAND);
				serverURL.append(KEY_TAG).append(EQUALS).append(mood.getName()).append(AMPERSAND);
			} else {
				// puts clear fields.
				serverURL.append(KEY_MOOD).append(EQUALS).append(AMPERSAND);
				serverURL.append(KEY_TAG).append(EQUALS).append(AMPERSAND);
			}
		}
		
		// genres.
		serverURL.append(KEY_GENRE).append(EQUALS);
		if (!Utils.isListEmpty(genres)) {
			int lastIndex = genres.size() - 1;
			for (int i = 0; i <= lastIndex; i++){
				serverURL.append(genres.get(i).getName());
				// avoids adding comma after the last item.
				if (i < lastIndex) {
					serverURL.append(COMMA);
				}
			}
		}
		serverURL.append(AMPERSAND);
		
		// categories.
		serverURL.append(KEY_CATEGORY).append(EQUALS);
		if (!Utils.isListEmpty(categories)) {
			int lastIndex = categories.size() - 1;
			for (int i = 0; i <= lastIndex; i++){
				serverURL.append(categories.get(i).getName());
				// avoids adding comma after the last item.
				if (i < lastIndex) {
					serverURL.append(COMMA);
				}
			}
		}
		
		/*
		 * if there are only genres without their parent categories
		 * we need to find what are their parents and adds them too.
		 * 
		 * to perform that we do this bypass to avoid preferences sync issues,
		 * due to the fact that genres / sub categories don't have references to their parent.
		 */
		
		// get the category of this genre.
		if (!Utils.isListEmpty(genres)) {
			
			List<Category> syntheticAddedCategories = new ArrayList<Category>();
			
			int lastIndex = genres.size() - 1;
			for (int i = 0; i <= lastIndex; i++) {
				
				/*
				 * checks if the parent category for the genre is not in the request parameters,
				 * if not, adds it to the category param list values.
				 * we validate it was added only once, by using a synthetic added category list.
				 */
				Category parentCategory = genres.get(i).getParentCategory();
				if (parentCategory != null && !(categories.contains(parentCategory))
										   && !(syntheticAddedCategories.contains(parentCategory))) {
					
					/*
					 * Adds to the category param list value for the first time
					 * if wev'e already populated the list values for the category param.
					 */
					if (i == 0 && !Utils.isListEmpty(categories)) {
						serverURL.append(COMMA);
					}
					
					// adds to the already added list.
					syntheticAddedCategories.add(parentCategory);
					
					serverURL.append(parentCategory.getName());
					// avoids adding comma after the last item.
					if (i < lastIndex) {
						serverURL.append(COMMA);
					}
				}
			}
		}
		
		
		serverURL.append(AMPERSAND);
		
		// Era
		String fromEra = null;
		String toEra = null;
		if (era != null) {
			serverURL.append(KEY_FROM_ERA).append(EQUALS).append(era.getFrom()).append(AMPERSAND);
			serverURL.append(KEY_TO_ERA).append(EQUALS).append(era.getTo()).append(AMPERSAND);
		} else {
			serverURL.append(KEY_FROM_ERA).append(EQUALS).append(Era.getDefaultFrom()).append(AMPERSAND);
			serverURL.append(KEY_TO_ERA).append(EQUALS).append(Era.getDefaultTo()).append(AMPERSAND);
		}
		
		// tempos.
		serverURL.append(KEY_TEMPO).append(EQUALS);
		if (!Utils.isListEmpty(tempos)) {
			int lastIndex = tempos.size() - 1;
			for (int i = 0; i <= lastIndex; i++){
				serverURL.append(tempos.get(i).toString().toLowerCase());
				// avoids adding comma after the last item.
				if (i < lastIndex) {
					serverURL.append(COMMA);
				}
			}
		} else {
			serverURL.append(Tempo.AUTO.toString().toLowerCase());
		}
		serverURL.append(AMPERSAND);
		
		String requestUrl = serverURL.toString();
		requestUrl = requestUrl.replace(" ", "%20");
		
		return requestUrl;
	}
	
	protected String buildURLParametersFromDiscoverSearchResultIndexer(DiscoverSearchResultIndexer discoverSearchResultIndexer) {
		
		int startIndex;
		int length;
		
		if (discoverSearchResultIndexer != null){
			startIndex = discoverSearchResultIndexer.getStartIndex();
			length = discoverSearchResultIndexer.getLength();
		} else {
			startIndex = DiscoverSearchResultIndexer.DEFAULT_START_INDEX;
			length = DiscoverSearchResultIndexer.DEFAULT_LENGTH;
		}

		StringBuilder serverURL = new StringBuilder();
		
		serverURL.append(KEY_START_INDEX).append(EQUALS).append(startIndex).append(AMPERSAND);
		serverURL.append(KEY_LENGTH).append(EQUALS).append(length);
		
		String requestUrl = serverURL.toString();
		requestUrl = requestUrl.replace(" ", "%20");
		
		return requestUrl;
	}

	
}
