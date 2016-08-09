package com.flatironschool.javacs;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import javax.swing.AbstractAction;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import org.jdesktop.swingx.autocomplete.AutoCompleteDecorator;

import redis.clients.jedis.Jedis;

public class WikiSearchView extends JComponent {
	
	private Map<String, Integer> map = new HashMap<String, Integer>();
	private WikiSearch wikiSearch = new WikiSearch(map);
	private JedisMaker jedisMaker = new JedisMaker();
	private JPanel northP;
	private JPanel centerP;
	private JPanel southP;
	private JEditorPane jep;
	//private JTextField searchField;
	private JTextArea resultArea = new JTextArea();
    AutoCompleteDecorator decorator;
    JComboBox combobox;
    private Object[] object = new Object[178692];
    private int size = 0;
	
	//constructor
	public WikiSearchView() {
		openFile();
		initGUI();
		
	}
	
	private void openFile() {

    	//Create a Buffered Reader to enable readLine functionality
    	BufferedReader reader;
        try {
        	//Create a new fileRead with the dictionary file
            FileReader fileRead = new FileReader("dictionary.txt");
 
            //Populate the BufferedReader with the FileReader data
            reader = new BufferedReader(fileRead);
 
    		//Get the first line of the file
    		String currentLine = reader.readLine();
    		//put the word into the array
    		object[size] = currentLine.toLowerCase();
    		
 
    		// While the current line isn't null
    		while (currentLine != null)
    		{	
    			// get next line of the file
    			currentLine = reader.readLine();
    			if (currentLine != null){
        			//words.add(currentLine);
        			size ++;
        			object[size] = currentLine.toLowerCase();
    			}
		
    		}
    		//Close the BufferedReader to prevent memory leaks
    		reader.close();
        }
        //If an error occurs throw an exception
        catch (FileNotFoundException ex) 
        {
            System.err.println(ex.getMessage());
        } 
        catch (IOException ex) 
        {
        	 System.err.println(ex.getMessage());
        }
	}
	
	
	/**
	 * private method to create the GUI components
	 * returns the created main panel
	 */	
	private void initGUI() {
		this.setBackground(new Color(new Random().nextInt()));
		/** use a BorderLayout **/
		setLayout(new BorderLayout());
		add(createNorthP(), BorderLayout.NORTH);
		add(createCenterP(), BorderLayout.CENTER);	
		add(createSouthP(), BorderLayout.SOUTH);
	}
	
	/**
	 * create a JPanel at the North of the frame
	 * add the Start button so that user can start playing
	 */	
	private JPanel createNorthP(){
		northP = new JPanel();
		northP.setPreferredSize(new Dimension(700, 300));
		JLabel contentPane = new JLabel();
		ImageIcon backgroundImage = new ImageIcon("void.png");
		contentPane.setIcon( backgroundImage );
		contentPane.setLayout( new BorderLayout() );
		northP.add( contentPane, BorderLayout.CENTER );

		return northP;
	}
	
	
	/**
	 * create a JPanel at the Center of the frame
	 * add the Start button so that user can start playing
	 */	
	private JPanel createCenterP(){
		centerP = new JPanel(new CardLayout());
		JPanel subP = new JPanel(new GridLayout (2,1));
		centerP.setPreferredSize(new Dimension(700, 350));
	//	centerP.setLayout(new BorderLayout());
        combobox = new JComboBox(object);
        AutoCompleteDecorator.decorate(combobox);
		//subP.add(searchField());
		subP.add(combobox);
		subP.add(searchButton());
		centerP.add(subP, BorderLayout.NORTH);

        
		return centerP;
	}
	
	/**
	 * create a JPanel at the South of the frame
	 * add the Start button so that user can start playing
	 */	
	private JPanel createSouthP(){
		southP = new JPanel();
		southP.setPreferredSize(new Dimension(700, 50));
		southP.setLayout(new BorderLayout());
//		southP.add(frontButton(), BorderLayout.WEST);
//		southP.add(backButton(), BorderLayout.EAST);
		
        JPanel control = new JPanel();
        control.add(new JButton(new AbstractAction("\u22b2Prev") {

            @Override
            public void actionPerformed(ActionEvent e) {
                CardLayout cl = (CardLayout) centerP.getLayout();
                cl.previous(centerP);
            }
        }));
        
        control.add(new JButton(new AbstractAction("Next\u22b3") {

            @Override
            public void actionPerformed(ActionEvent e) {
                CardLayout cl = (CardLayout) centerP.getLayout();
                cl.next(centerP);
            }
        }));
        southP.add(control);
		return southP;
	}
	
	private JButton searchButton() {
		JButton search = new JButton("Search");
		search.addActionListener (
				new ActionListener() {
					public void actionPerformed (ActionEvent e) {
						try {
							createResultPanel();
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
						validate();
						repaint();
					}
					});
		return search;
	}
	
//	private JTextField searchField() {
//		searchField = new JTextField("enter...");
//		
//		return searchField;
//	}
	
	private JButton frontButton() {
		JButton front = new JButton("Front");
		front.addActionListener (
				new ActionListener() {
					public void actionPerformed (ActionEvent e) {
						removeAll();
						initGUI();
						validate();
						repaint();
						System.out.println("front enter");
					}
					});
		return front;
	}
	
	private JButton backButton() {
		JButton back = new JButton("Back");
		back.addActionListener (
				new ActionListener() {
					public void actionPerformed (ActionEvent e) {
						
						validate();
						repaint();
					}
					});
		return back;
	}
	
	private void createResultPanel() throws IOException {
		centerP.removeAll();
		centerP.validate();
		centerP.setBorder ( new TitledBorder (new EtchedBorder (), "Result Area" ) );
		resultArea.setEditable(false);
		JScrollPane scroll = new JScrollPane(resultArea);
		scroll.setVerticalScrollBarPolicy ( ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS );
		//centerP.add(scroll);
		resultArea.setLineWrap(true);
		resultArea.setWrapStyleWord(true);
		
		 jep = new JEditorPane();
		 centerP.add(jep);
		//centerP.add(resultArea);
		//addResult(searchField.getText());
		addResult(combobox.getSelectedItem().toString());
		
	}
	
	private void addResult (String term) throws IOException{
		
		// make a JedisIndex
		Jedis jedis = jedisMaker.make();
		JedisIndex index = new JedisIndex(jedis);
		
		System.out.println("Query: " + term);
		WikiSearch search = wikiSearch.search(term, index);
		
		String string = "";
		List<Entry<String, Integer>> entries = search.sort();
		for (Entry<String, Integer> entry: entries) {
			string += "<a href='" + entry.getKey() + "'" + ">" + entry.getKey() + "</a><br>";
		}

		if (string != "") {
			System.out.println (string);
			//resultArea.setText(string);
	         jep.setContentType("text/html");//set content as html
	        
	         jep.setText(string);
	         jep.setEditable(false);//so its not editable

	         jep.addHyperlinkListener(new HyperlinkListener() {
	             @Override
	             public void hyperlinkUpdate(HyperlinkEvent hle) {
	                 if (HyperlinkEvent.EventType.ACTIVATED.equals(hle.getEventType())) {
	                     System.out.println(hle.getURL());
	                     Desktop desktop = Desktop.getDesktop();
	                     try {
	                         desktop.browse(hle.getURL().toURI());
	                     } catch (Exception ex) {
	                         ex.printStackTrace();
	                     }
	                 }
	             }
	         });
		}
		else {
			jep.setText("no result found");
			//resultArea.setText("no result found");
		}
	}
}
