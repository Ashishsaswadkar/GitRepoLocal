package com.hungama.myplay.activity.data.dao.hungama;

import java.io.Serializable;
import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.hungama.myplay.activity.ui.dialogs.ListDialog.ListDialogItem;
import com.hungama.myplay.activity.util.Utils;

/**
 * Categories given from Hungama server to filter {@link MediaItem}s.
 */
public class Category implements Serializable, CategoryTypeObject, ListDialogItem {
	
	@Expose
	@SerializedName("type")
	private final String type = CategoryTypeObject.TYPE_CATEGORY;
	@Expose
	@SerializedName("id")
	private final long id;
	@Expose
	@SerializedName("name")
	private final String name;
	@Expose
	@SerializedName("categoryTypeObjects")
	private final List<CategoryTypeObject> categoryTypeObjects;
	
	private boolean isRoot = false;
	private Category parentCategory = null;
	
	public Category(long id, String name, List<CategoryTypeObject> categoryTypeObjects) {
		this.id = id;
		this.name = name;
		this.categoryTypeObjects = categoryTypeObjects;
	}
	
	@Override
	public String getType() {
		return type;
	}

	@Override
	public long getId() {
		return id;
	}

	@Override
	public String getName() {
		return name;
	}
	
	public int getChildCount() {
		if (!Utils.isListEmpty(categoryTypeObjects)){
			return categoryTypeObjects.size();
		}
		return 0;
	}
	
	public List<CategoryTypeObject> getCategoryTypeObjects() {
		return categoryTypeObjects;
	}

	public CategoryTypeObject getChildAt(int position) {
		if (!Utils.isListEmpty(categoryTypeObjects)) {
			return categoryTypeObjects.get(position);
		}
		return null;
	}
	
	public boolean isRoot() {
		return isRoot;
	}
	
	public Category getParentCategory() {
		return parentCategory;
	}

	public void setIsRoot(boolean isBarren) {
		this.isRoot = isBarren;
	}
	
	public void setParentCategory(Category parentCategory) {
		this.parentCategory = parentCategory;
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof Category))
			return false;
		
		Category category = (Category) o;
		if (!(this.id == category.getId())){
			return false;
		}
		
		if (!(this.name.equalsIgnoreCase(category.getName()))){
			return false;
		}
		
		return true;
	}
}
