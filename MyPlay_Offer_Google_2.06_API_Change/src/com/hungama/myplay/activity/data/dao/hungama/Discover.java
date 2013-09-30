package com.hungama.myplay.activity.data.dao.hungama;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import android.text.TextUtils;

import com.hungama.myplay.activity.ui.dialogs.ListDialog.ListDialogItem;
import com.hungama.myplay.activity.util.Utils;

public class Discover implements ListDialogItem, Serializable {
	
	private int id;
	private String name;
	private Mood mood;
	private List<Genre> genres;
	private List<Category> categories;
	private List<Tempo> tempos;
	private Era era;
	
	public Discover(int id, String name, Mood mood, List<Genre> genres, 
					List<Category> categories, List<Tempo> tempos, Era era) {
		this.id = id;
		this.name = name;
		this.mood = mood;
		this.genres = genres;
		this.categories = categories;
		this.tempos = tempos;
		this.era = era;
	}
	
	// getters:
	
	@Override
	public long getId() {
		return id;
	}
	
	@Override
	public String getName() {
		return name;
	}
	
	public Mood getMood() {
		return mood;
	}
	
	public List<Genre> getGenres() {
		return genres;
	}

	public List<Category> getCategories() {
		return categories;
	}
	
	public List<Tempo> getTempos() {
		return tempos;
	}
	
	public Era getEra() {
		return era;
	}
	
	// setters:

	public void setName(String name) {
		this.name = name;
	}

	public void setMood(Mood mood) {
		this.mood = mood;
	}

	public void setGenres(List<Genre> genres) {
		if (genres != null) {
			this.genres = new ArrayList<Genre>(genres);
		} else {
			this.genres = null;
		}
		 
	}

	public void setCategories(List<Category> categories) {
		if (categories != null) {
			this.categories = new ArrayList<Category>(categories);
		} else {
			this.categories = null;
		}
	}

	public void setTempos(List<Tempo> tempos) {
		if (tempos != null) {
			this.tempos = new ArrayList<Tempo>(tempos);
		} else {
			this.tempos = null;
		}
	}

	public void setEra(Era era) {
		this.era = era;
	}
	
	// util.
	
	public static Discover createNewDiscover() {
		List<Tempo> tempos = new ArrayList<Tempo>();
		tempos.add(Tempo.AUTO);
		Era era = new Era(Era.getDefaultFrom(), Era.getDefaultTo());
		
		return new Discover(0, null, null, null, null, tempos, era);
	}
	
	@Override
	public boolean equals(Object o) {
		if (! (o instanceof Discover))
			return false;
		
		Discover discover = (Discover) o;
		
		// validation of id.
		if (this.getId() != discover.getId())
			return false;
		
		// validation of name;
		if ((TextUtils.isEmpty(this.name) && !TextUtils.isEmpty(discover.getName()) || 
				(!TextUtils.isEmpty(this.name) && TextUtils.isEmpty(discover.getName())))){
			return false;
		}
		
		if (this.name != null && discover.getName() != null && 
						!(this.name.equals(discover.getName()))) {
			return false;
		}
		
		// Mood.
		if ((this.mood != null && discover.getMood() == null) || 
				(this.mood == null && discover.getMood() != null)) {
			return false;
		}
		
		if (this.mood != null && discover.getMood() != null && 
								!(mood.equals(discover.getMood()))) {
			return false;
		}
		
		// Categories.
		if ((Utils.isListEmpty(this.categories) && !Utils.isListEmpty(discover.getCategories())) &&
				(!Utils.isListEmpty(this.categories) && Utils.isListEmpty(discover.getCategories()))) {
			return false;
		}
		
		if ((!Utils.isListEmpty(this.categories) && !Utils.isListEmpty(discover.getCategories())) &&
													this.categories.equals(discover.getCategories())){
			return false;
		}
		
		// Tempos.
		if ((Utils.isListEmpty(this.tempos) && !Utils.isListEmpty(discover.getTempos())) &&
				(!Utils.isListEmpty(this.tempos) && Utils.isListEmpty(discover.getTempos()))) {
			return false;
		}
		
		if ((!Utils.isListEmpty(this.tempos) && !Utils.isListEmpty(discover.getTempos())) &&
													this.tempos.equals(discover.getTempos())){
			return false;
		}
		
		// Era.
		if ((this.era != null && discover.getEra() == null) || 
				(this.era == null && discover.getEra() != null)) {
			return false;
		}
		
		if (this.era != null && discover.getEra() != null && !
								(era.equals(discover.getEra()))) {
			return false;
		}
		
		return true;
	}
	
	
}
