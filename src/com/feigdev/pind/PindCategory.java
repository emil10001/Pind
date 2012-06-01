package com.feigdev.pind;

import java.util.HashMap;

public class PindCategory {
	private static final HashMap<CharSequence, Integer> stringToInt = buildCategories();
	private static final HashMap<Integer, CharSequence> intToString = reverseBuildCategories();
	
	public static String get(int cat){
		return intToString.get(cat).toString();
	}
	
	public static int get(String cat){
		return stringToInt.get(cat);
	}
	
	public static int get(CharSequence cat){
		return stringToInt.get(cat);
	}
	
	private static HashMap<CharSequence, Integer> buildCategories(){
		HashMap<CharSequence,Integer> catMap = new HashMap<CharSequence, Integer>();
		catMap.put("architecture", Constants.ARCHITECTURE);
		catMap.put("art", Constants.ART);
		catMap.put("cars_motorcycles", Constants.CARS_MOTORCYCLES);
		catMap.put("design", Constants.DESIGN);
		catMap.put("diy_crafts", Constants.DIY_CRAFTS);
		catMap.put("education", Constants.EDUCATION);
		catMap.put("film_music_books", Constants.FILM_MUSIC_BOOKS);
		catMap.put("fitness", Constants.FITNESS);
		catMap.put("food_drink", Constants.FOOD_DRINK);
		catMap.put("gardening", Constants.GARDENING);
		catMap.put("geek", Constants.GEEK);
		catMap.put("hair_beauty", Constants.HAIR_BEAUTY);
		catMap.put("history", Constants.HISTORY);
		catMap.put("holidays", Constants.HOLIDAYS);
		catMap.put("home", Constants.HOME);
		catMap.put("humor", Constants.HUMOR);
		catMap.put("kids", Constants.KIDS);
		catMap.put("mylife", Constants.MYLIFE);
		catMap.put("women_apparel", Constants.WOMEN_APPERAL);
		catMap.put("men_apparel", Constants.MEN_APPERAL);
		catMap.put("outdoors", Constants.OUTDOORS);
		catMap.put("people", Constants.PEOPLE);
		catMap.put("pets", Constants.PETS);
		catMap.put("photography", Constants.PHOTOGRAPHY);
		catMap.put("sports", Constants.SPORTS);
		catMap.put("technology", Constants.TECHNOLOGY);
		catMap.put("travel_places", Constants.TRAVEL_PLACES);
		catMap.put("wedding_events", Constants.WEDDING_EVENTS);
		catMap.put("other", Constants.OTHER);
		catMap.put("popular", Constants.POPULAR);
		catMap.put("everything", Constants.EVERYTHING);
		catMap.put("all", Constants.EVERYTHING);
		catMap.put("following", Constants.FOLLOWING);
		return catMap;
	}

	private static HashMap<Integer, CharSequence> reverseBuildCategories() {
		HashMap<Integer, CharSequence> catMap = new HashMap<Integer, CharSequence>();
		catMap.put(Constants.ARCHITECTURE, "architecture");
		catMap.put(Constants.ART, "art");
		catMap.put(Constants.CARS_MOTORCYCLES, "cars_motorcycles");
		catMap.put(Constants.DESIGN, "design");
		catMap.put(Constants.DIY_CRAFTS, "diy_crafts");
		catMap.put(Constants.EDUCATION, "education");
		catMap.put(Constants.FILM_MUSIC_BOOKS, "film_music_books");
		catMap.put(Constants.FITNESS, "fitness");
		catMap.put(Constants.FOOD_DRINK, "food_drink");
		catMap.put(Constants.GARDENING, "gardening");
		catMap.put(Constants.GEEK, "geek");
		catMap.put(Constants.HAIR_BEAUTY, "hair_beauty");
		catMap.put(Constants.HISTORY, "history");
		catMap.put(Constants.HOLIDAYS, "holidays");
		catMap.put(Constants.HOME, "home");
		catMap.put(Constants.HUMOR, "humor");
		catMap.put(Constants.KIDS, "kids");
		catMap.put(Constants.MYLIFE, "mylife");
		catMap.put(Constants.WOMEN_APPERAL, "women_apparel");
		catMap.put(Constants.MEN_APPERAL, "men_apparel");
		catMap.put(Constants.OUTDOORS, "outdoors");
		catMap.put(Constants.PEOPLE, "people");
		catMap.put(Constants.PETS, "pets");
		catMap.put(Constants.PHOTOGRAPHY, "photography");
		catMap.put(Constants.SPORTS, "sports");
		catMap.put(Constants.TECHNOLOGY, "technology");
		catMap.put(Constants.TRAVEL_PLACES, "travel_places");
		catMap.put(Constants.WEDDING_EVENTS, "wedding_events");
		catMap.put(Constants.OTHER, "other");
		catMap.put(Constants.POPULAR, "popular");
		catMap.put(Constants.EVERYTHING, "everything");
		catMap.put(Constants.FOLLOWING, "following");
		return catMap;
	}
}
