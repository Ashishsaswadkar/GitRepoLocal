package com.hungama.myplay.activity.data.dao.hungama.social;

import com.google.gson.annotations.SerializedName;

public class Profile {
	
	public static final String KEY_USER_ID = "user_id";
	public static final String KEY_USER_POINTS = "user_points";
	public static final String KEY_CURRENT_LEVEL = "current_level";
	public static final String KEY_MAX_LEVEL = "max_level";
	public static final String KEY_MY_COLLECTIONS = "my_collection";
	public static final String KEY_USER_NAME = "user_name";
	public static final String KEY_USER_IMAGE_URL = "user_image";
	public static final String KEY_USER_BADGES = "badges";
	public static final String KEY_USER_LEADERBOARDS = "leader_board";
	public static final String KEY_USER_DISVOVERIES = "my_discovery";
	public static final String KEY_USER_FAVORITE_ALBUMS = "fav_albums";
	public static final String KEY_USER_FAVORITE_PLAYLISTS = "fav_playlist";
	public static final String KEY_USER_FAVORITE_SONGS = "fav_songs";
	public static final String KEY_USER_FAVORITE_VIDEOS = "fav_videos";
	
	@SerializedName(KEY_USER_ID)
	public final long id;
	@SerializedName(KEY_USER_POINTS)
	public final long points;
	@SerializedName(KEY_CURRENT_LEVEL)
	public final long currentLevel;
	@SerializedName(KEY_MAX_LEVEL)
	public final long maxLevel;
	@SerializedName(KEY_MY_COLLECTIONS)
	public final long collections;
	@SerializedName(KEY_USER_NAME)
	public final String name;
	@SerializedName(KEY_USER_IMAGE_URL)
	public final String imageUrl;
	@SerializedName(KEY_USER_BADGES)
	public final UserBadges userBadges;
	@SerializedName(KEY_USER_LEADERBOARDS)
	public final UserLeaderBoardUsers userLeaderBoardUsers;
	@SerializedName(KEY_USER_DISVOVERIES)
	public final UserDisoveries userDisoveries;
	@SerializedName(KEY_USER_FAVORITE_ALBUMS)
	public final UserFavoriteAlbums userFavoriteAlbums;
	@SerializedName(KEY_USER_FAVORITE_PLAYLISTS)
	public final UserFavoritePlaylists userFavoritePlaylists;
	@SerializedName(KEY_USER_FAVORITE_SONGS)
	public final UserFavoriteSongs userFavoriteSongs;
	@SerializedName(KEY_USER_FAVORITE_VIDEOS)
	public final UserFavoriteVideos userFavoriteVideos;
	
	public Profile(long id, long points, long currentLevel, long maxLevel, long collections, 
			String name, String imageUrl,
			UserBadges userBadges, UserLeaderBoardUsers userLeaderBoardUsers,
			UserDisoveries userDisoveries,
			UserFavoriteAlbums userFavoriteAlbums,
			UserFavoritePlaylists userFavoritePlaylists,
			UserFavoriteSongs userFavoriteSongs,
			UserFavoriteVideos userFavoriteVideos) {
		
		this.id = id;
		this.points = points;
		this.currentLevel = currentLevel;
		this.maxLevel = maxLevel;
		this.collections = collections;
		this.name = name;
		this.imageUrl = imageUrl;
		this.userBadges = userBadges;
		this.userLeaderBoardUsers = userLeaderBoardUsers;
		this.userDisoveries = userDisoveries;
		this.userFavoriteAlbums = userFavoriteAlbums;
		this.userFavoritePlaylists = userFavoritePlaylists;
		this.userFavoriteSongs = userFavoriteSongs;
		this.userFavoriteVideos = userFavoriteVideos;
	}

}
