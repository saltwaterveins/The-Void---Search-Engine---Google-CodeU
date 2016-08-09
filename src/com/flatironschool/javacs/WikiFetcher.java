package com.flatironschool.javacs;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Scanner;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


public class WikiFetcher {
	private long lastRequestTime = -1;
	private long minInterval = 1000;

	/**
	 * Fetches and parses a URL string, returning a list of paragraph elements.
	 *
	 * @param url
	 * @return
	 * @throws IOException
	 */
	public Elements fetchWikipedia(String url) throws IOException {
		sleepIfNeeded();

		// download and parse the document
		Connection conn = Jsoup.connect(url);
		Document doc = conn.get();

		// select the content text and pull out the paragraphs.
		Element content = doc.getElementById("mw-content-text");

		// TODO: avoid selecting paragraphs from sidebars and boxouts
		Elements paras = content.select("li");
		return paras;
	}

	/**
	 * Reads the contents of a Wikipedia page from src/resources.
	 *
	 * @param url
	 * @return
	 * @throws IOException
	 */
	public Elements readWikipedia(URL url) throws IOException {

		// read the url
		
		//URL site = new URL("http://www.puzzlers.org/pub/wordlists/pocket.txt");
		InputStream stream = url.openStream();
		
		Document doc = Jsoup.parse(stream, "UTF-8", url.toString());

		// TODO: factor out the following repeated code
		Element content = doc.getElementById("mw-content-text");
		Elements paras = content.select("p");
		return paras;
	}

	/**
	 * Rate limits by waiting at least the minimum interval between requests.
	 */
	private void sleepIfNeeded() {
		if (lastRequestTime != -1) {
			long currentTime = System.currentTimeMillis();
			long nextRequestTime = lastRequestTime + minInterval;
			if (currentTime < nextRequestTime) {
				try {
					//System.out.println("Sleeping until " + nextRequestTime);
					Thread.sleep(nextRequestTime - currentTime);
				} catch (InterruptedException e) {
					System.err.println("Warning: sleep interrupted in fetchWikipedia.");
				}
			}
		}
		lastRequestTime = System.currentTimeMillis();
	}
}
