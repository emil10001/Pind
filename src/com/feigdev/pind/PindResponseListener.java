package com.feigdev.pind;

public interface PindResponseListener {
	public void onLoginSuccess();
	public void onLoginFailure();
	public void onLogoutSuccess();
	public void onLoginCancel();
	public void onLoginFinish();
	public void onPopularUpdate();
	public void onEverythingUpdate();
	public void onSearchUpdate();
	public void onCategoryUpdate();
	public void onCategoryFailure();
	public void onPopularFailure();
	public void onEverythingFailure();
	public void onSearchFailure();
	public void onUpdateCurView();
	public void onRequestFailure(int curCategory);
	public void onContentUpdate(GridContent gc);
	public void onUnAuthenticated();
	
	public void onLikeFailure();
	public void onLikeSuccess();
	public void onCommentFailure();
	public void onCommentSuccess();
	public void onRepinFailure();
	public void onRepinSuccess();
	public void onBoardsFailure();
	public void onBoardsSuccess();
	public void onPinFailure();
	public void onPinSuccess();
	

}
