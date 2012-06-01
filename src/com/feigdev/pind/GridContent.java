package com.feigdev.pind;

import java.util.ArrayList;

public class GridContent {
	private ArrayList<SmallPinItem> pi;
	private String next_page_url;
	
	public GridContent(){
		pi = new ArrayList<SmallPinItem>();
	}
	
	public int size(){
		return pi.size();
	}
	
	public void add(SmallPinItem spi){
		if ("".equals(spi.getThumb_url()) || "".equals(spi.getThumb_loc())){
			return;
		}
		pi.add(spi);
	}
	
	public void remove(SmallPinItem spi){
		if (!pi.remove(spi)){
			for (SmallPinItem item : pi){
				if (item.getId().equals(spi.getId())){
					pi.remove(item);
				}
			}
		}
	}
	
	public ArrayList<SmallPinItem> getSpiList(){
		return pi;
	}
	
	public SmallPinItem get(int index){
		return pi.get(index);
	}

	public String getNext_page_url() {
		return next_page_url;
	}

	public void setNext_page_url(String next_page_url) {
		this.next_page_url = next_page_url;
	}
	
	public class SmallPinItem {
		private String id;
		private int row;
		private String thumb_loc;
		private String thumb_url;
		
		public SmallPinItem(){
			id = "";
			thumb_loc = "";
			thumb_url = "";
		}
		
		public SmallPinItem(String id, int rownum, String thumb_loc, String thumb_url){
			this.id = id;
			this.thumb_loc = thumb_loc;
			this.thumb_url = thumb_url;
			this.row = rownum;
		}
		
		public String getId() {
			return id;
		}
		public void setId(String id) {
			this.id = id;
		}
		
		public int getRow() {
			return row;
		}

		public void setRow(int row) {
			this.row = row;
		}

		public String getThumb_loc() {
			return thumb_loc;
		}

		public void setThumb_loc(String thumb_loc) {
			this.thumb_loc = thumb_loc;
		}

		public String getThumb_url() {
			return thumb_url;
		}

		public void setThumb_url(String thumb_url) {
			this.thumb_url = thumb_url;
		}
	}

	public boolean contains(SmallPinItem smallPinItem) {
		for (SmallPinItem spi : pi){
			if (spi.getId().equals(smallPinItem.getId())){
				return true;
			}
		}
		return false;
	}
	

	public boolean contains(String item_id) {
		for (SmallPinItem spi : pi){
			if (spi.getId().equals(item_id)){
				return true;
			}
		}
		return false;
	}
}
