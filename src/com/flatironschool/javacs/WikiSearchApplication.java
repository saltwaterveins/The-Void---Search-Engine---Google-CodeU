package com.flatironschool.javacs;

import java.awt.BorderLayout;

import javax.swing.JFrame;

public class WikiSearchApplication {

	/**
	 * Create a JFrame that holds the Roster.
	 * 
	 **/
	public static void main( String[] args )
	{
		JFrame guiFrame;
			
		// create a new JFrame to hold the roster
		guiFrame = new JFrame( "Search");
		
		// set size
		guiFrame.setSize( 1000, 1000 );

		guiFrame.add(new WikiSearchView(), BorderLayout.SOUTH);

		// exit normally on closing the window
		guiFrame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );

		// show frame
		guiFrame.setVisible( true );
	}
	
}
