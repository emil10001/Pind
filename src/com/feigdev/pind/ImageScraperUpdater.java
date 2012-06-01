package com.feigdev.pind;

import com.feigdev.pind.ImageScraper.ResultsHolder;

public interface ImageScraperUpdater {
	public void onNewImage(ResultsHolder result);
	public void onFailed();
}
