package com.flatironschool.javacs;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import redis.clients.jedis.Jedis;


/**
 * Represents the results of a search query.
 *
 */
public class WikiSearch {
	
	// map from URLs that contain the term(s) to relevance score
	private Map<String, Integer> map;

	/**
	 * Constructor.
	 * 
	 * @param map
	 */
	public WikiSearch(Map<String, Integer> map) {
		this.map = map;
	}
	
	/**
	 * Looks up the relevance of a given URL.
	 * 
	 * @param url
	 * @return
	 */
	public Integer getRelevance(String url) {
		Integer relevance = map.get(url);
		return relevance==null ? 0: relevance;
	}
	
	/**
	 * Prints the contents in order of term frequency.
	 * 
	 * @param map
	 */
	private  void print() {
		List<Entry<String, Integer>> entries = sort();
		for (Entry<String, Integer> entry: entries) {
			System.out.println(entry);
		}
	}
	
	/**
	 * Computes the union of two search results.
	 * 
	 * @param that
	 * @return New WikiSearch object.
	 */
	public WikiSearch or(WikiSearch that) {
        // FILL THIS IN!
		List<Entry<String, Integer>> listOne = this.sort();
		List<Entry<String, Integer>> listTwo = that.sort();

        Map<String, Integer> map = new HashMap<String, Integer>();

		for (int i = 0; i < listOne.size(); i++){
			if (listOne.get(i).getValue() > 0){
				map.put(listOne.get(i).getKey(), listOne.get(i).getValue());
			}
		}

		for (int i = 0; i < listTwo.size(); i++){
			if (listTwo.get(i).getValue() > 0){
				if (map.containsKey(listTwo.get(i).getKey())){
					map.put(listTwo.get(i).getKey(), totalRelevance(listTwo.get(i).getValue(), map.get(listTwo.get(i).getKey())));
				} else {
					map.put(listTwo.get(i).getKey(), listTwo.get(i).getValue());
				}
			}
		}
			return new WikiSearch(map);
	}
	
	/**
	 * Computes the intersection of two search results.
	 * 
	 * @param that
	 * @return New WikiSearch object.
	 */
	public WikiSearch and(WikiSearch that) {
        // FILL THIS IN!
		Map<String, Integer> intersection = new HashMap<String, Integer>();
		for (String url : map.keySet()) {
			if (getRelevance(url) != 0 && that.getRelevance(url) != 0) {
				intersection.put(url, totalRelevance(getRelevance(url), that.getRelevance(url)));
			} else {
				intersection.put(url, 0);
			}
		}
		return new WikiSearch(intersection);
	}
	
	/**
	 * Computes the intersection of two search results.
	 * 
	 * @param that
	 * @return New WikiSearch object.
	 */
	public WikiSearch minus(WikiSearch that) {
        // FILL THIS IN!
		Map<String, Integer> intersection = new HashMap<String, Integer>();
		for (String url : map.keySet()) {
			if (that.getRelevance(url) == 0) intersection.put(url, getRelevance(url));
		}
		return new WikiSearch(intersection);
	}
	
	/**
	 * Computes the relevance of a search with multiple terms.
	 * 
	 * @param rel1: relevance score for the first search
	 * @param rel2: relevance score for the second search
	 * @return
	 */
	protected int totalRelevance(Integer rel1, Integer rel2) {
		// simple starting place: relevance is the sum of the term frequencies.
		return rel1 + rel2;
	}

	/**
	 * Sort the results by relevance.
	 * 
	 * @return List of entries with URL and relevance.
	 */
	public List<Entry<String, Integer>> sort() {
        // FILL THIS IN!
		List<Entry<String, Integer>> results = new LinkedList<Entry<String, Integer>>(map.entrySet());
		Comparator<Entry<String, Integer>> comparator = new Comparator<Entry<String, Integer>>() {
			@Override
			public int compare(Entry<String, Integer> entry1, Entry<String, Integer> entry2) {
				if (entry1.getValue() > entry2.getValue()) return -1;
				if (entry1.getValue() < entry2.getValue()) return 1;
				return 0;
			}
		};
		Collections.sort(results, comparator);
		return results;
	}

	/**
	 * Performs a search and makes a WikiSearch object.
	 * 
	 * @param term
	 * @param index
	 * @return
	 */
	public static WikiSearch search(String term, JedisIndex index) {
		Map<String, Integer> map = index.getCounts(term);
		return new WikiSearch(map);
	}
	
	final static WikiFetcher wf = new WikiFetcher();

	static ArrayList<Element> alreadyBeen = new ArrayList<Element>();

	
	public static void fetchWantedPages() throws IOException {
		// make a JedisIndex
		
		Jedis jedis = JedisMaker.make();
		JedisIndex index = new JedisIndex(jedis);
		
		String url = "https://en.wikipedia.org/wiki/Wikipedia:Vital_articles/Level/2";
		
		for (int i = 0; i < 80; i++) {
			//System.out.println("Fetching..." + url);
			Elements paragraphs = wf.fetchWikipedia(url);
			WIkiParser wp = new WIkiParser(paragraphs);
			Element e = wp.findFirstLink();

			if (e == null) {
				//System.err.println("Got to a page with no valid links.");
				return;
			}

			alreadyBeen.add(e);
			System.out.println("Page: " + e.text());
			
			
		}
		
//		// search for the first term
//		String term1 = "java";
//		System.out.println("Query: " + term1);
//		WikiSearch search1 = search(term1, index);
//		search1.print();
//				
//		// search for the second term
//		String term2 = "programming";
//		System.out.println("Query: " + term2);
//		WikiSearch search2 = search(term2, index);
//		search2.print();
//				
//		// compute the intersection of the searches
//		System.out.println("Query: " + term1 + " AND " + term2);
//		WikiSearch intersection = search1.and(search2);
//		intersection.print();
	}
	
	
	private static void loadIndex(JedisIndex index) throws IOException {
		WikiFetcher wf = new WikiFetcher();
		String urlSubstring = "";
		
		for (Element e : alreadyBeen) {
			urlSubstring = "https://en.wikipedia.org" + e.toString().substring(e.toString().indexOf('\"') + 1, (e.toString().indexOf("title") - 2));
			URL url = new URL(urlSubstring);
			Elements paragraphs = wf.readWikipedia(url);
			index.indexPage(url, paragraphs);
		}

	}
	
	public static void main(String[] args) throws IOException {
		//fetchWantedPages();
		
		Jedis jedis = JedisMaker.make();
		JedisIndex index = new JedisIndex(jedis);
		loadIndex(index);
		
		//jedis.flushAll();
		
		String term1 = "history";
		System.out.println("Query: " + term1);
		WikiSearch search1 = search(term1, index);
		search1.print();
		
	}
}
