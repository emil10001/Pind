package com.feigdev.pind;

import org.json.JSONObject;

public class Board{
	private JSONObject board;
	
	public Board(JSONObject obj){
		board = obj;
	}
	
	public String getUrl() {
		return board.optString(PindJsonParser.URL);
	}
	public String getDescription() {
		return board.optString(PindJsonParser.DESCRIPTION);
	}
	public String getUser_id() {
		return board.optString(PindJsonParser.USER_ID);
	}
	public String getName() {
		return board.optString(PindJsonParser.NAME);
	}
	public String getId() {
		return board.optString(PindJsonParser.ID);
	}

	public JSONObject getBoard() {
		return board;
	}

	public void setBoard(JSONObject board) {
		this.board = board;
	}
	
	public String getCategory(){
		return board.optString("category");
	}
}
