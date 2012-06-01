package com.feigdev.pind;

import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import android.graphics.Point;

public class PinItem {
	private JSONObject pinItem;
	private int rowNum;
	private User user;
	private ImageObj images;
	private Counts counts;
	private Sizes sizes;
	private ArrayList<Comment> comments;
	private Board board;
	
	public PinItem(){
		user = new User(null);
		images = new ImageObj();
		counts = new Counts();
		sizes = new Sizes();
		comments = new ArrayList<Comment>();
		board = new Board(null);
		try {
			setPinItem(null);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	public boolean hasImages(){
		if (null == getThumb() || "".equals(getThumb()) 
				|| null == getImage() || "".equals(getImage()) 
				|| null == getImages().getThumb_loc() || "".equals(getImages().getThumb_loc())
				|| null == getImages().getMob_loc() || "".equals(getImages().getMob_loc())){
			return false;
		}
		return true;
	}
	
	public String getThumb(){
		try {
			return pinItem.getJSONObject(PindJsonParser.IMAGES).optString(PindJsonParser.THUMBNAIL);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "";
	}
	
	public String getImage(){
		try {
			return pinItem.getJSONObject(PindJsonParser.IMAGES).optString(PindJsonParser.MOBILE);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "";
	}
	
	public Comment newComment(JSONObject obj){
		return new Comment(obj);
	}
	
	public User newUser(JSONObject obj){
		return new User(obj);
	}
	
	public Board newBoard(JSONObject obj){
		return new Board(obj);
	}
	
	public class ImageObj {
		private String thumb_loc = null;
		private String mob_loc = null;
		
		public ImageObj(){
			thumb_loc = "";
			mob_loc = "";
		}
		
		public ImageObj(String thumb,String mob){
			thumb_loc = thumb;
			mob_loc = mob;
		}
		public String getThumb_loc() {
			return thumb_loc;
		}
		public void setThumb_loc(String thumb_loc) {
			this.thumb_loc = thumb_loc;
		}
		public String getMob_loc() {
			return mob_loc;
		}
		public void setMob_loc(String mob_loc) {
			this.mob_loc = mob_loc;
		}
	}
	
	public class Counts{
		public int getRepins() {
			try {
				return pinItem.getJSONObject(PindJsonParser.COUNTS).optInt(PindJsonParser.REPINS);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return 0;
		}
		public int getComments() {
			try {
				return pinItem.getJSONObject(PindJsonParser.COUNTS).optInt(PindJsonParser.COMMENTS);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return 0;
		}
		public int getLikes() {
			try {
				return pinItem.getJSONObject(PindJsonParser.COUNTS).optInt(PindJsonParser.LIKES);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return 0;
		}
	}
	
	public class Sizes{
		public Point getMobile() {
			try {
				return new Point(pinItem.getJSONObject(PindJsonParser.SIZES).getJSONObject(PindJsonParser.MOBILE).optInt(PindJsonParser.WIDTH), pinItem.getJSONObject(PindJsonParser.SIZES).getJSONObject(PindJsonParser.MOBILE).optInt(PindJsonParser.HEIGHT));
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return new Point (80,80);
		}
		public Point getBoard() {
			try {
				return new Point(192, pinItem.getJSONObject(PindJsonParser.SIZES).getJSONObject(PindJsonParser.BOARD).optInt(PindJsonParser.HEIGHT));
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return new Point(192,192);
		}
		
	}
	
	public class Comment{
		private JSONObject comment;
		private User user;
		
		public Comment(JSONObject obj){
			comment = obj;
			try {
				user = newUser(comment.getJSONObject(PindJsonParser.USER));
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		public String getText() {
			return comment.optString(PindJsonParser.TEXT);
		}
		public User getUser() {
			return user;
		}
		public void setUser(User user) {
			this.user = user;
		}
	}
	
	

	public String getDomain() {
		return pinItem.optString(PindJsonParser.DOMAIN);
	}

	public String getDescription() {
		return pinItem.optString(PindJsonParser.DESCRIPTION);
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public ImageObj getImages() {
		return images;
	}

	public Counts getCounts() {
		return counts;
	}

	public String getId() {
		return pinItem.optString(PindJsonParser.ID);
	}
	
	public Sizes getSizes() {
		return sizes;
	}

	public String getCreated_at() {
		return pinItem.optString(PindJsonParser.CREATED_AT);
	}

	public ArrayList<Comment> getComments() {
		return comments;
	}

	public void setComments(ArrayList<Comment> comments) {
		this.comments = comments;
	}

	public boolean isIs_repin() {
		return pinItem.optBoolean(PindJsonParser.IS_REPIN);
	}

	public String getSource() {
		return pinItem.optString(PindJsonParser.SOURCE);
	}

	public Board getBoard() {
		return board;
	}

	public void setBoard(Board board) {
		this.board = board;
	}

	public boolean isIs_video() {
		return pinItem.optBoolean(PindJsonParser.IS_VIDEO);
	}

	public int getRowNum() {
		return rowNum;
	}

	public void setRowNum(int rowNum) {
		this.rowNum = rowNum;
	}

	public JSONObject getPinItem() {
		return pinItem;
	}

	public void setPinItem(JSONObject pinItem) throws JSONException {
		this.pinItem = pinItem;
	}

}
