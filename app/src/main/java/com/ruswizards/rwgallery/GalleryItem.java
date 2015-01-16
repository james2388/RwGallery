package com.ruswizards.rwgallery;

/**
 * Copyright (C) 2014 Rus Wizards
 * <p/>
 * Created: 16.01.2015
 * Vladimir Farafonov
 */
public class GalleryItem {
	public enum ItemType{
		WEB_ITEM, LOCAL_ITEM, DIRECTORY
	}

	private String title_;
	private String source_;
	private ItemType itemType_;

	public GalleryItem(String title, String source, ItemType itemType){
		title_ = title;
		source_ = source;
		itemType_ = itemType;
	}

	public String getTitle() {
		return title_;
	}

	public void setTitle(String title_) {
		this.title_ = title_;
	}

	public String getSource() {
		return source_;
	}

	public void setSource(String source_) {
		this.source_ = source_;
	}

	public ItemType getItemType() {
		return itemType_;
	}

	public void setItemType(ItemType itemType_) {
		this.itemType_ = itemType_;
	}
}
