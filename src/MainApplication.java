/*
 	This file is part of Oakland-Scheduler.

	Oakland-Scheduler is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Oakland-Scheduler is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Oakland-Scheduler.  If not, see <http://www.gnu.org/licenses/>.
*/

import java.awt.Color;
import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.awt.print.*;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;

public class MainApplication extends JPanel implements ActionListener{

	private static final long serialVersionUID = 1L;
	
	// Colors used for displaying schedule!
	private final String[] colors = {"#87CEEB","#8FBC8F","#F0E68C","#FFC0CB",
			"#D8BFD8","#BFEFFF","#C1FFC1","#BCEE68","#FFEC8B"};
	
	private final int TIME_START = 8, TIME_END = 21;

	private final double TIME_LEN_SORT_RATIO = .05;
	
	// Campus constants
	private final int CMP_INTERNET = 7, CMP_MACOMB = 9,
			CMP_MAIN = 1, CMP_MT_CLEMENS = 10, CMP_DOMESTIC = 2,
			CMP_INTERNATIONAL = 3;
	
	// Color constants
	private final Color DEFAULT_COLOR = Color.white;
	private final Color ALT_COLOR = Color.decode("#f4f4f4");
	
	CheckComboBox campusBox;
	
	// Time column height and width
	int TIME_ROW_H = 18, TIME_ROW_W = 60;
	
	JFileChooser fc;
	JList courseListSelected, courseListAll;
	JPanel panel1, panel2, panel3;
	String[] sortingMethods = {"None", "Early", "Afternoon", "Night"};
	JComboBox sortingCB;
	JCheckBox nrCB, closedCB, srCB, mCB, tCB, wCB, rCB, fCB;
	JButton loadB, saveB, downloadB;
	Scheduler scheduler;
	List<List<Course>> filteredSchedules;
	List<List<Course>> unfilteredSchedules;
	JButton backB, forwardB;
	JPanel scheduleP;
	JTextField scheduleIndexT;
	JLabel scheduleTotalT;
	int index; 
	List<String> lastKnownClassSet;
	
	boolean downloadShowing;
	
	JMenuItem saveItem, loadItem, printItem, exportPNG, jsoupItem, htmlComponents, schedulerItem,
		backgroundItem, contactItem, howtoItem;
	
	static Map<String, Boolean> courseChecks;
	Map<Integer, Boolean> blacklistCRN;
	
	JList configCourseList;
	
	// Default size of schedule window
	static final int W = 800, H = 600;
	
	/********************************************************************
	 * Method: main
	 * Purpose: executed code when program starts up!
	/*******************************************************************/
	public static void main(String[] args){
		
		createGUI();
	}
	
	/********************************************************************
	 * Constructor: MainApplication
	 * Purpose: creates a default main application frame
	/*******************************************************************/
	public MainApplication(){
		
		scheduler = new Scheduler();
		blacklistCRN = new HashMap<Integer, Boolean>();
		fc = new JFileChooser();
		downloadShowing = false;
		unfilteredSchedules = new ArrayList<List<Course>>();
		filteredSchedules = new ArrayList<List<Course>>();
		lastKnownClassSet = new ArrayList<String>();
	
		// Setup for frame
		this.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
	    this.setLayout(new GridBagLayout());
	    GridBagConstraints c = new GridBagConstraints();
	    c.fill = GridBagConstraints.BOTH;
	    
	    // Setup menu bar
	    JMenuBar menuBar = new JMenuBar();
	    menuBar.setPreferredSize(new Dimension((int)(W*.1), (int)(H * .035)));
	    menuBar.setMinimumSize(new Dimension((int)(W*.1), (int)(H * .035)));

	    // File menu
	    JMenu fileMenu = new JMenu("File");
	    fileMenu.setMnemonic('F');
	    menuBar.add(fileMenu);
	    
	    // Load item
	    loadItem = new JMenuItem("Load courses");
	    loadItem.addActionListener(this);
	    //fileMenu.add(loadItem);
	    
	    // Save item
	    saveItem = new JMenuItem("Save courses");
	    saveItem.addActionListener(this);
	    //fileMenu.add(saveItem);
	    
	    // Export PNG
	    exportPNG = new JMenuItem("Export");
	    exportPNG.addActionListener(this);
	    fileMenu.add(exportPNG);
	    
	    // Print item
	    printItem = new JMenuItem("Print");
	    printItem.addActionListener(this);
	    fileMenu.add(printItem);
	    
	    // About menu
	    JMenu aboutMenu = new JMenu("About");
	    aboutMenu.setMnemonic('A');
	    menuBar.add(aboutMenu);

	    JMenu licenseMenu = new JMenu("Licenses");
	    aboutMenu.add(licenseMenu);
	    
	    // Oakland Scheduler License
	    schedulerItem= new JMenuItem("Oakland Scheduler");
	    schedulerItem.addActionListener(this);
	    licenseMenu.add(schedulerItem);
	    
	    // HTMLUnit License
	    htmlComponents = new JMenuItem("Apache HttpComponents Client");
	    htmlComponents.addActionListener(this);
	    licenseMenu.add(htmlComponents);
	    
	    // JSoup license
	    jsoupItem = new JMenuItem("jsoup");
	    jsoupItem.addActionListener(this);
	    licenseMenu.add(jsoupItem);
	    
	    // Background license
	    backgroundItem = new JMenuItem("Purpose");
	    backgroundItem.addActionListener(this);
	    aboutMenu.add(backgroundItem);
	    
	    // Help Menu
	    JMenu helpMenu = new JMenu("Help");
	    helpMenu.setMnemonic('H');
	    menuBar.add(helpMenu);
	    
	    // How to 
	    howtoItem = new JMenuItem("How to Use");
	    howtoItem.addActionListener(this);
	    helpMenu.add(howtoItem);
	    
	    contactItem = new JMenuItem("Contact");
	    contactItem.addActionListener(this);
	    helpMenu.add(contactItem);
	    
	    
        c.gridx = 0;
        c.gridy = 0;
        c.weighty = 0;
        c.weightx = 0;
        c.gridheight = 1;
        c.gridwidth= 2;
	    add(menuBar,c);
	    
	    // Defaults
	    index = 1; 
	    
	    this.panel1 = new JPanel(new GridBagLayout());	// current course list
	    this.panel2 = new JPanel(new GridBagLayout());	// entire course list
	    this.panel3 = new JPanel(new GridBagLayout());	// scheduler panel

	    // Add all elements to panels
	    addCourseListAll();
	    addCourseButtons();
	    addCourseListSelected();
	    addHeader();
	    addSchedulePane();
	    addFooter();
	            
	    // Add Panel 1
        c.gridx = 0;
        c.gridy = 2;
        c.weighty = 1;
        c.weightx = 0;
        c.gridheight = 1;
        c.gridwidth=1;
        add(panel1, c);
        
        // Add Panel 2
        c.gridx = 0;
        c.gridy = 3;
        c.weighty = 1;
        c.weightx = 0;
        c.gridheight = 1;
        add(panel2, c);
        
        // Add Panel 3
        c.gridx = 1;
        c.gridy = 2;
        c.gridheight = 2;
        c.weightx = 1;
        c.weighty = 1;
        add(panel3, c);
        
        
        KeyEventDispatcher myKeyEventDispatcher = new KeyEventDispatcher() {
			
			@Override
			public boolean dispatchKeyEvent(KeyEvent key) {

				if(!(key.getID() == KeyEvent.KEY_RELEASED || downloadShowing || fc.isShowing() 
						|| key.getSource() == scheduleIndexT) && key.getKeyCode() == KeyEvent.VK_LEFT){
					
					// Deduct one from current index
					if(filteredSchedules != null && !((index - 1 < 0) || (index - 1 >= filteredSchedules.size()))){
						scheduleIndexT.setText((--index  + 1) + "");
					}
				}
				else if(!(key.getID() == KeyEvent.KEY_RELEASED || downloadShowing || fc.isShowing() 
						|| key.getSource() == scheduleIndexT) && key.getKeyCode() == KeyEvent.VK_RIGHT){
					
					// Add one to index
					if(filteredSchedules != null && !((index + 1 < 0) || (index + 1 >= filteredSchedules.size()))){
						scheduleIndexT.setText((++index + 1) + "");
					}

				}
				else{
					
					// Send like normal !
					KeyboardFocusManager.getCurrentKeyboardFocusManager().redispatchEvent((Component)key.getSource(), key);
				}
				return true;
			}
		};
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(myKeyEventDispatcher);
        
	}
	
	/********************************************************************
	 * Method: addHeader
	 * Purpose: creates and adds the header to the schedule application
	/*******************************************************************/
	private void addHeader(){
		
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		JPanel headerP = new JPanel(new GridBagLayout());
        
        // Back button
        backB = new JButton("<");
        backB.addActionListener(this);
        //backB.setPreferredSize(new Dimension((int)(W*.055), (int)(H * .055*.5)));
        //backB.setMinimumSize(new Dimension((int)(W*.055), (int)(H * .055*.5)));
        c.insets = new Insets(0,1,0,0);
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 0;
        c.weighty = 0;
        headerP.add(backB, c);
        
        // Forward button
        forwardB = new JButton(">");
        forwardB.addActionListener(this);
        //forwardB.setPreferredSize(new Dimension((int)(W*.055), (int)(H * .055)));
        //forwardB.setMinimumSize(new Dimension((int)(W*.055), (int)(H * .055)));
        //c.insets = new Insets(6,0,10,0);
        c.gridx = 1;
        c.gridy = 0;
        c.weightx = 0;
        c.weighty = 0;
        headerP.add(forwardB, c);
                
        // Schedule Label 
        JLabel scheduleL = new JLabel("Schedule", JLabel.CENTER);
        //c.insets = new Insets(10,0,10,0);
        c.gridx = 2;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 0;
        headerP.add(scheduleL, c);
        
        // Schedule Index
        scheduleIndexT = new JTextField("1");
        scheduleIndexT.addActionListener(this);
        scheduleIndexT.setHorizontalAlignment(JTextField.CENTER);
        scheduleIndexT.setPreferredSize(new Dimension((int)(W*.075), (int)(H * .04)));
        scheduleIndexT.setMinimumSize(new Dimension((int)(W*.075), (int)(H * .04)));
    
        // Listener for change in index
        scheduleIndexT.getDocument().addDocumentListener(new DocumentListener() {
        	  
        	  // Catch all updates
        	  public void changedUpdate(DocumentEvent e) { update(); }
  			  public void removeUpdate(DocumentEvent e) { update(); }
  			  public void insertUpdate(DocumentEvent e) { update(); }
  	
  			  // Update current schedule
  			  public void update() {
  				  	
  				  // Get inputed number
  				  String text = scheduleIndexT.getText();
	
  				  try {
  					  
  					  // Convert to integer and show schedule
  					  int indexNew = Integer.parseInt(text);
  					  index = indexNew - 1;
  					  showSchedule(index);
  				  }
  				  catch(Exception e){ /* Do nothing */}
  			  }
  		});
        
        
        c.gridx = 3;
        c.gridy = 0;
        c.weightx = 0;
        c.weighty = 0;
        //c.insets = new Insets(10,0,10,0);
        headerP.add(scheduleIndexT, c);
        
        // Schedule Total
        scheduleTotalT = new JLabel("/ 0");
        scheduleTotalT.setHorizontalAlignment(JTextField.CENTER);
        scheduleTotalT.setPreferredSize(new Dimension((int)(W*.075), (int)(H * .055)));
        scheduleTotalT.setMinimumSize(new Dimension((int)(W*.075), (int)(H * .055)));
        c.gridx = 4;
        c.gridy = 0;
        c.weightx = 0;
        c.weighty = 0;
        //c.insets = new Insets(10,0,10,0);
        headerP.add(scheduleTotalT, c);
        
        // Add header
        c.insets = new Insets(0,0,0,0);
        c.gridx = 0;
        c.gridy = 0;
        //c.gridwidth=2;
        c.weightx = 1;
        c.weighty = 0;
        panel3.add(headerP, c);
        
	}
	
	/********************************************************************
	 * Method: addSchedulePane
	 * Purpose: creates and adds the schedule pane to the application
	/*******************************************************************/
	private void addSchedulePane(){
		
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		
		// Blank schedule panel
		scheduleP = new JPanel(new GridBagLayout());
        scheduleP.setBackground(Color.LIGHT_GRAY);
        scheduleP.setFocusable(true);
        scheduleP.setRequestFocusEnabled(true);
        
        // Scrollable schedule pane
        JScrollPane scheduleScroller = new JScrollPane(scheduleP);
        scheduleScroller.setFocusable(true);
        
        // Add to panel 3
        c.gridy = 1;
        c.gridx = 0;
        c.weighty = 1;
        c.weightx = 1;
        c.insets = new Insets(0,0,0,0);
        panel3.add(scheduleScroller, c);
        
	}
	
	/********************************************************************
	 * Method: addFooter
	 * Purpose: creates and adds the footer to the schedule application
	/*******************************************************************/
	private void addFooter(){
		
		// Constraints
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		
		// Footer container
		JPanel footerP = new JPanel(new GridBagLayout());
        
        // Sorting Label
        JLabel sortingL = new JLabel("Sorting");
        c.insets = new Insets(10,10,10,0);
        c.gridx = 1;
        c.gridy = 0;
        c.weightx = 0;
        c.weighty = 0;
        footerP.add(sortingL, c);
        
        // Days Label
        JLabel daysL = new JLabel("Days");
        c.gridx = 7;
        c.gridy = 0;
        c.insets = new Insets(10,20,10,0);
        footerP.add(daysL, c);
        
        // Sorting drop down
        sortingCB = new JComboBox(sortingMethods);
        sortingCB.addActionListener(this);
        c.insets = new Insets(10,10,10,10);
        c.gridwidth = 1;
        c.gridx = 2;
        c.gridy = 0;
        c.weightx = .2;
        c.weighty = 0;
        footerP.add(sortingCB, c);
        
        JLabel spacer = new JLabel("");
        c.gridx = 0;
        c.weightx = .05;
        footerP.add(spacer, c);
        spacer = new JLabel("");
        c.weightx = .05;
        c.gridx = 3; 
        footerP.add(spacer, c);
        spacer = new JLabel("");
        c.gridx = 6;
        footerP.add(spacer,c);
        
        // NR Checkbox
        nrCB = new JCheckBox("NR");
        nrCB.addActionListener(this);
        nrCB.setSelected(true);
        c.insets = new Insets(0,20,0,10);
        c.gridwidth = 1;
        c.gridx = 11;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 0;
        
        // Closed Checkbox
        closedCB = new JCheckBox("Closed");
        closedCB.addActionListener(this);
        c.insets = new Insets(0,20,0,10);
        c.gridwidth = 1;
        c.gridx = 13;
        c.gridy = 0;
        c.weightx = .1;
        c.weighty = 0;
        footerP.add(closedCB, c);

        // SR Checkbox
        srCB = new JCheckBox("SR");
        srCB.addActionListener(this);
        srCB.setSelected(true);
        c.insets = new Insets(0,20,0,10);
        c.gridwidth = 1;
        c.gridx = 12;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 0;
        //footerP.add(srCB, c);
        
        // Days 
        mCB = new JCheckBox("M");
        tCB = new JCheckBox("T");
        wCB = new JCheckBox("W");
        rCB = new JCheckBox("R");
        fCB = new JCheckBox("F");
		
        // Default is selected
		mCB.setSelected(true);
		tCB.setSelected(true);
		wCB.setSelected(true);
		rCB.setSelected(true);
		fCB.setSelected(true);
		
		// All days actions are handled by MainApplication
		mCB.addActionListener(this);
		tCB.addActionListener(this);
		wCB.addActionListener(this);
		rCB.addActionListener(this);
		fCB.addActionListener(this);
		
		// Days constraints
        
        c.gridwidth = 1;
        
        c.gridy = 0;
        c.weightx = 0;
        c.weighty = 0;
        footerP.setFocusable(true);
        
        // Add Monday
        c.insets = new Insets(0,10,0,0);
        c.gridx = 8;
        footerP.add(mCB, c);
        
        // Add Tuesday
        c.insets = new Insets(0,1,0,0);
        c.gridx = 9;
        footerP.add(tCB, c);
        
        // Add Wednesday
        c.gridx = 10;
        footerP.add(wCB, c);
        
        // Add Thursday
        c.gridx = 11;
        footerP.add(rCB, c);
        
        // Add Friday
        c.gridx = 12;
        c.weightx = .1;
        footerP.add(fCB, c);
        
        // Campus drop down label
        JLabel campusL = new JLabel("Campus");
        c.insets = new Insets(0,20,0,10);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridwidth = 1;
        c.gridx = 4;
        c.gridy = 0;
        c.weightx = 0;
        c.weighty = 0;
        footerP.add(campusL, c);
        c.fill = GridBagConstraints.BOTH;
        
        JCheckBox[] boxes = new JCheckBox[7];
		
		boxes[0] = new JCheckBox("All",true);
		boxes[1] = new JCheckBox("Internet",true);
		boxes[2] = new JCheckBox("Main Campus",true);
		boxes[3] = new JCheckBox("Mt. Clemens",true);
		boxes[4] = new JCheckBox("Macomb",true);
		boxes[5] = new JCheckBox("Domestic",true);
		boxes[6] = new JCheckBox("International",true);
		
        
        // Campus dropdown
        c.insets = new Insets(10,10,10,10);
        c.gridwidth = 1;
        c.gridx = 5;
        c.gridy = 0;
        c.weightx = .18;
        c.weighty = 0;
        campusBox = new CheckComboBox(boxes, this);
        footerP.add(campusBox, c);
        

        // Add footer to panel 3
        c.insets = new Insets(0,0,0,0);
        c.gridx = 0;
        c.gridy = 2;
        panel3.add(footerP, c);
	}
	
	/********************************************************************
	 * Method: addCourseButtons
	 * Purpose: creates and adds the course buttons to panel 1
	/*******************************************************************/
	private void addCourseButtons(){
		
		// Constraints
		GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1;
        c.weighty = 0;
		
		// Make all three buttons
		loadB = new JButton("Load");
		saveB = new JButton("Save");
		downloadB = new JButton("Download");
        
		// Set MainApplication to be the listener for each button
		loadB.addActionListener(this);
        saveB.addActionListener(this);
        downloadB.addActionListener(this);
        
        // Add loading button
        c.insets = new Insets(0,1,0,1);
        c.gridx = 0;
        c.gridy = 1;
        c.gridwidth = 1;
        panel2.add(loadB, c);
        
        // Add save button
        c.insets = new Insets(0,0,0,1);
        c.gridx = 1;
        c.gridy = 1;
        c.gridwidth = 1;
        panel2.add(saveB, c);
        
        // Add download button
        c.insets = new Insets(1,1,0,1);
        c.gridx = 0;
        c.gridy = 2;
        c.gridwidth = 2;
        panel2.add(downloadB,c);
		
	}
	
	/********************************************************************
	 * Method: addCourseListAll
	 * Purpose: creates and adds the entire course list to panel 1
	/*******************************************************************/
	private void addCourseListAll(){
      
		// Constraints
		GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1;
        c.weighty = 1;
        c.gridwidth = 2;
        
		// Basic JList setup
        courseListAll = new JList();
        courseListAll.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        courseListAll.setLayoutOrientation(JList.VERTICAL);
        courseListAll.setVisibleRowCount(-1);
        
        courseListAll.addMouseMotionListener(new MouseMotionListener() {
			
			@Override
			public void mouseMoved(MouseEvent e) {
				
				int index = courseListAll.locationToIndex(e.getPoint());
				
				if(index >= 0){
					courseListAll.setToolTipText(null);
					courseListAll.setToolTipText(scheduler.getCatalog().get(scheduler.getCourseIDs().get(index)).get(0).getCourseName());
				}
			}
			
			@Override
			public void mouseDragged(MouseEvent arg0) {
			}
		});
        
        ToolTipManager.sharedInstance().registerComponent(courseListAll);
        
        courseListAll.addKeyListener(new KeyAdapter() {
        	public void keyReleased(KeyEvent k){
        		
        		// Enter key
        		if(k.getKeyCode() == KeyEvent.VK_ENTER){
        			
    				JList list = (JList)k.getSource();
                    
        			// Get selected item index
        			int index = list.getSelectedIndex();
        			
        			
        			if(index >= 0){
	        		
        				// Course to add
        				String courseAdd = scheduler.getCourseIDs().get(index);
	                    
        				if(!scheduler.getCurrentCourseList().contains(courseAdd)){
        					
        					lastKnownClassSet = new ArrayList<String>();
        					for(String s : scheduler.getCurrentCourseList()){
        						lastKnownClassSet.add(s);
        					}
        					
        					// Add course then update
		                    scheduler.getCurrentCourseList().add(courseAdd);
		                    courseListSelected.setListData(scheduler.getCurrentCourseList().toArray());
		                    updatePermutations();
		                    updateSchedule();
	                    } 
	        		}
        		}
        	}
		});
        
        courseListAll.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                
				JList list = (JList)evt.getSource();
                
                if (evt.getClickCount() == 2) {
                    int index = list.locationToIndex(evt.getPoint());
                    String courseAdd = scheduler.getCourseIDs().get(index);
                    
                    if(!scheduler.getCurrentCourseList().contains(courseAdd)){
                    
    					lastKnownClassSet = new ArrayList<String>();
    					for(String s : scheduler.getCurrentCourseList()){
    						lastKnownClassSet.add(s);
    					}
                    	
	                    scheduler.getCurrentCourseList().add(courseAdd);
	                    courseListSelected.setListData(scheduler.getCurrentCourseList().toArray());
	                    updatePermutations();
	                    updateSchedule();
                    }
                } 
           }

            // Single press
            public void mousePressed(MouseEvent e){

            	// Click click
			    if (SwingUtilities.isRightMouseButton(e) && courseListAll.getSelectedIndex() >= 0
			    		&& courseListAll.getCellBounds(courseListAll.getSelectedIndex(), courseListAll.getSelectedIndex()).contains(e.getPoint())){

			    	
			    	// Make popup menu and two options
			    	JPopupMenu menu = new JPopupMenu();	                
			    	JMenuItem preInfoI = new JMenuItem("Prerequisite");
			    	JMenuItem addI = new JMenuItem("Add");
	                
			    	preInfoI.addActionListener(new ActionListener() {
	                    public void actionPerformed(ActionEvent e) {
	                    	
	                    	// Get which item was clicked
	                    	int index = courseListAll.getSelectedIndex();
	                        String courseID = scheduler.getCourseIDs().get(index);
	                    	String preInfo = scheduler.getPreInfo(courseID);
	                    	JOptionPane.showOptionDialog(MainApplication.this,
	                    			"<html><body><p style='width: 250px;'>" + preInfo + "</body></html>", courseID + " Prerequisite Information", 
		                			JOptionPane.DEFAULT_OPTION,JOptionPane.INFORMATION_MESSAGE, null, new Object[]{}, null);	                    	
	                    }
	                });
			    	
			    	addI.addActionListener(new ActionListener() {
	                    public void actionPerformed(ActionEvent e) {
	                    	
	                    	int index = courseListAll.getSelectedIndex();	                    	
        					String courseAdd = scheduler.getCourseIDs().get(index);
                            
                            if(!scheduler.getCurrentCourseList().contains(courseAdd)){
                            
            					lastKnownClassSet = new ArrayList<String>();
            					for(String s : scheduler.getCurrentCourseList()){
            						lastKnownClassSet.add(s);
            					}
                            	
        	                    scheduler.getCurrentCourseList().add(courseAdd);
        	                    courseListSelected.setListData(scheduler.getCurrentCourseList().toArray());
        	                    updatePermutations();
        	                    updateSchedule();
                            }
	                    }
	                });
	                
	                
	                // Add options
			    	menu.add(addI);
			    	menu.add(preInfoI);
	                menu.show(courseListAll,e.getPoint().x + 6, e.getPoint().y);
			    }
			}
        });
        
        // Make a new scroll pane and add course list all into it
        JScrollPane courseScrollerAll = new JScrollPane(courseListAll);
        
        // Adjust size to be 10% of default schedule width, and 60% of default height
        courseScrollerAll.setPreferredSize(new Dimension((int)(W * .14), (int)(H * .60)));
        courseScrollerAll.setMinimumSize(new Dimension((int)(W * .14), (int)(H * .60)));
        
        panel2.add(courseScrollerAll, c);
	}
	
	/********************************************************************
	 * Method: addCourseListSelected
	 * Purpose: creates and adds the selected course list to panel 1
	/*******************************************************************/
	private void addCourseListSelected(){
      
		// Constraints
		GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1;
        c.weighty = 1;
        c.gridwidth = 1;
        
		// Basic JList setup
        courseListSelected = new JList();
        courseListSelected.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        courseListSelected.setLayoutOrientation(JList.VERTICAL);
        courseListSelected.setVisibleRowCount(-1);
       
        courseListSelected.addMouseMotionListener(new MouseMotionListener() {
			
			@Override
			public void mouseMoved(MouseEvent e) {
				
				int index = courseListSelected.locationToIndex(e.getPoint());
				
				if(index >= 0){
					courseListSelected.setToolTipText(null);
					courseListSelected.setToolTipText(scheduler.getCatalog().get(scheduler.getCurrentCourseList().get(index)).get(0).getCourseName());
				}
			}
			
			@Override
			public void mouseDragged(MouseEvent arg0) {
			}
		});
        
        ToolTipManager.sharedInstance().registerComponent(courseListSelected);
        
        // Key listener
        courseListSelected.addKeyListener(new KeyAdapter() {
        	public void keyReleased(KeyEvent k){
        		
        		// Only for enter and delete
        		if(k.getKeyCode() == KeyEvent.VK_ENTER || k.getKeyCode() == KeyEvent.VK_DELETE){
        			
    				JList list = (JList)k.getSource();
                    
        			int index = list.getSelectedIndex();
        			
        			if(index >= 0 && index < scheduler.getCurrentCourseList().size()){
        				
    					lastKnownClassSet = new ArrayList<String>();
    					for(String s : scheduler.getCurrentCourseList()){
    						lastKnownClassSet.add(s);
    					}
        				
        				scheduler.getCurrentCourseList().remove(index);
                    	courseListSelected.setListData(scheduler.getCurrentCourseList().toArray());
                    	updatePermutations();
                    	updateSchedule();
        			}
        		}
        	}
		});
        
        // Mouse Listener
        courseListSelected.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                
				JList list = (JList)evt.getSource();
                
            	// Double Click
                if (evt.getClickCount() == 2) {
                    
					lastKnownClassSet = new ArrayList<String>();
					for(String s : scheduler.getCurrentCourseList()){
						lastKnownClassSet.add(s);
					}
                	
                	// Get which item was clicked and remove
                	int index = list.locationToIndex(evt.getPoint());
                    scheduler.getCurrentCourseList().remove(index);
                    
                    // Update
                    courseListSelected.setListData(scheduler.getCurrentCourseList().toArray());
                    updatePermutations();
                    updateSchedule();
                }
            }
            
            // Single press
            public void mousePressed(MouseEvent e){

            	// Click click
			    if (SwingUtilities.isRightMouseButton(e) && courseListSelected.getSelectedIndex() >= 0
			    		&& courseListSelected.getCellBounds(courseListSelected.getSelectedIndex(), courseListSelected.getSelectedIndex()).contains(e.getPoint())){

			    	
			    	// Make popup menu and two options
			    	JPopupMenu menu = new JPopupMenu();	                
			    	JMenuItem configI = new JMenuItem("Configure");
			    	JMenuItem preInfoI = new JMenuItem("Prerequisite");
			    	JMenuItem deleteI = new JMenuItem("Delete");
	                
			    	preInfoI.addActionListener(new ActionListener() {
	                    public void actionPerformed(ActionEvent e) {
	                    	
	                    	// Get which item was clicked
	                    	int index = courseListSelected.getSelectedIndex();
	                        String courseID = scheduler.getCurrentCourseList().get(index);
	                    	String preInfo = scheduler.getPreInfo(courseID);
	                    	JOptionPane.showOptionDialog(MainApplication.this,
	                    			"<html><body><p style='width: 250px;'>" + preInfo + "</body></html>", courseID + " Prerequisite Information", 
		                			JOptionPane.DEFAULT_OPTION,JOptionPane.INFORMATION_MESSAGE, null, new Object[]{}, null);	                    	
	                    }
	                });
			    	
			    	deleteI.addActionListener(new ActionListener() {
	                    public void actionPerformed(ActionEvent e) {
	                    	
        					lastKnownClassSet = new ArrayList<String>();
        					for(String s : scheduler.getCurrentCourseList()){
        						lastKnownClassSet.add(s);
        					}
	                    	
	                    	// Get which item was clicked and remove
	                    	int index = courseListSelected.getSelectedIndex();
	                        scheduler.getCurrentCourseList().remove(index);
	                        
	                        // Update
	                        courseListSelected.setListData(scheduler.getCurrentCourseList().toArray());
	                        updatePermutations();
	                        updateSchedule();
	                    }
	                });
	                
			    	// Allow the user to configure sections
	                configI.addActionListener(new ActionListener() {
	                    public void actionPerformed(ActionEvent e) {
	                    	
	                    	
	                    	// Grid constraints
	                		GridBagConstraints c = new GridBagConstraints();
	                		c.fill = GridBagConstraints.BOTH;
	                		c.weightx = 1;
	                		c.weighty = 1;
	                		c.gridwidth = 2;
	                    	
	                		
	                		// Get which item was clicked
	                    	int index = courseListSelected.getSelectedIndex();
	                        if(index < 0 || index >= scheduler.getCurrentCourseList().size()) return;
	                    	
	                        String courseID = scheduler.getCurrentCourseList().get(index);
	                        List<Course> courseList = scheduler.getCatalog().get(courseID);

	                    	// Make list of check boxes
	                    	CheckBoxItem[] items = new CheckBoxItem[courseList.size()];

	                    	// Add each course to check box list
	                    	for(int i = 0; i < courseList.size(); i++){
	                    		
	                    		// Get needed course
	                    		Course course = courseList.get(i);
	                    		
	                    		// CRN Starts the course
	                    		String crn = course.getCRN() + ""; 
	                    		String prof = "";	                    		
	                    		
	                    		// Look for prof with (P)
	                    		for(Meeting meeting : course.getMeetings()){
	                    			
	                    			if(meeting.getProfessor().contains("(P)")){
	                    				prof  = meeting.getProfessor();
	                    				break;
	                    			}
	                    		}
	                    		
	                    		// Default professor?
	                    		if(!prof.contains("(P)") && course.getMeetings().size() > 0){ 
	                    			prof = course.getMeetings().get(0).getProfessor();
	                    		} 
	                    		
	                    		// Add the checkbox item
	                    		items[i] = new CheckBoxItem(((prof.equals(""))?crn:(crn + " - " + prof)),
	                    				!blacklistCRN.containsKey(course.getCRN()), course.getCRN());
	                    	}
	                    	
	                    	// Panel to hold components
	                    	JPanel panel = new JPanel(new GridBagLayout());
	                    	
	                    	// Select all button
	                    	JButton selectAll = new JButton("Select All");
	                    	selectAll.addActionListener(new ActionListener() {
	    	                    public void actionPerformed(ActionEvent e) {
	    	                    	
	    	                    	
	    	                    	
	    	                    	// Cycle through all items
	    		                	for(int index = 0; index < configCourseList.getModel().getSize(); index++){
	    		                	
	    		                		CheckBoxItem item = (CheckBoxItem)configCourseList.getModel().getElementAt(index);
	    		                		item.setSelected(true);
	    		                	}
	    		                	
	    		                	configCourseList.repaint();
	    	                    }
	    	                });

	                    	// Select all button
	                    	JButton clearAll = new JButton("Clear All");
	                    	clearAll.addActionListener(new ActionListener() {
	    	                    public void actionPerformed(ActionEvent e) {
	    	                    	
	    	                    	// Cycle through all items
	    		                	for(int index = 0; index < configCourseList.getModel().getSize(); index++){
	    		                	
	    		                		CheckBoxItem item = (CheckBoxItem)configCourseList.getModel().getElementAt(index);
	    		                		item.setSelected(false);
	    		                	}
	    		                	
	    		                	configCourseList.repaint();
	    	                    }
	    	                });
	                    	
	                    	// Make config list based on checkbox items
	                    	configCourseList = new JList(items);
	                    	
	                    	// Mouse Listener for selecting items
	                    	configCourseList.addMouseListener( 
	                    		new MouseAdapter() {
	                		      
	                    			
	                    			// Click on config item
		                    		@Override public void mouseClicked(MouseEvent event){
		                		        
		                    			selectItem(event.getPoint());
		                		    }
		                    	});

	                    	// Setup renderer and selection mode
                		    configCourseList.setCellRenderer(new CheckBoxListRenderer());
                		    configCourseList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		                	// Setup up parameters
		                	String instructions = "Enable or disable specific sections:";
		                	JScrollPane scroller = new JScrollPane(configCourseList);
		                	scroller.setPreferredSize(new Dimension(300,200));
		                	panel.add(scroller,c);
		                	
		                	// Setup constraints
		                	c.gridy = 1;
		                	c.weighty = 0;
		                	c.gridwidth = 1;
		                	
		                	// Add select all then clear all
		                	c.gridx = 0;
		                	panel.add(selectAll, c);
		                	c.gridx = 1;
		                	panel.add(clearAll, c);

		                	// Show message
		                	Object[] params = {instructions, panel};
		                	JOptionPane.showOptionDialog(MainApplication.this,params, courseID + " Configuration", 
		                			JOptionPane.DEFAULT_OPTION,JOptionPane.PLAIN_MESSAGE, null, new Object[]{}, null);
		                	
		                	// Cycle through all items and add blacklist CRNs
		                	for(CheckBoxItem cb : items){
		                		
		                		// Unchecked
		                		if(!cb.getSelected()){
		                			
		                			// Add to blacklist if not already in
		                			if(!blacklistCRN.containsKey(cb.getCRN())){
		                				blacklistCRN.put(cb.getCRN(), true);
		                			}
		                		}
		                		
		                		// Checked
		                		else{
		                		
		                			// Remove from blacklist if not already in
		                			if(blacklistCRN.containsKey(cb.getCRN())){
		                				blacklistCRN.remove(cb.getCRN());
		                			}
		                		}
		                	}
		                	
		                	// Update schedule
		                	updateSchedule();
	                    }
	                });
	                
	                // Add config and delete options and show
	                menu.add(configI);
	                menu.add(preInfoI);
	                menu.add(deleteI);
	                menu.show(courseListSelected,e.getPoint().x+6, e.getPoint().y);
			    }
			}
        });
        
        // Make a new scroll pane and add course list all into it
        JScrollPane courseScrollerSelected = new JScrollPane(courseListSelected);
        
        // Adjust size to be 10% of default schedule width, and 30% of default height
        courseScrollerSelected.setPreferredSize(new Dimension((int)(W * .1), (int)(H * .25)));
        courseScrollerSelected.setMinimumSize(new Dimension((int)(W * .1), (int)(H * .25)));
        
        panel1.add(courseScrollerSelected, c);
	}
	

	/********************************************************************
	 * Method: createGUI
	 * Purpose: creates the user interface for the application
	/*******************************************************************/
	private static void createGUI() {
		

        // Set up window
		JFrame.setDefaultLookAndFeelDecorated(true);
		JFrame frame = new JFrame("Oakland Scheduler Beta");
        frame.setMaximizedBounds(GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(960, 720);
        frame.add(new MainApplication());
        
        // Display window
        frame.setVisible(true);
	}

	/********************************************************************
	 * Method: updatePermutations
	 * Purpose: updates a schedules permutations
	/*******************************************************************/
	public void updatePermutations(){
		
		unfilteredSchedules = null;
		filteredSchedules = null;
		List<List<Course>> temp = scheduler.generatePermutations();
		
		if(temp != null){

			// Generate working permutations without filtering
			unfilteredSchedules = scheduler.generatePermutations();
		}
		else{
			
			// Show error message
			JOptionPane.showMessageDialog(this,
					"An out of memory error occured because the Java heap filled up."
					+ "\n\n"
					+ "This is very much likely to be the causse of a very large "
					+ "\nnumber of permutations (billions most likely) that were "
					+ "\nbeing checked.\n\n"
					+ "If you are on a 32 bit machine or are running with a 32 bit"
					+ "\nJVM this could be the reason. If switching to a 64 bit JVM"
					+ "\nis not an option then please run this program with VM arguments"
					+ "\n-Xmx2048M or run the .cmd (Windows) or .sh (Linux) scripts "
					+ "\nattached with the application."
					,"Out of memory error", JOptionPane.PLAIN_MESSAGE);
			
			
			courseListSelected.setListData(this.lastKnownClassSet.toArray());
			scheduler.getCurrentCourseList().clear();
			
			// Add latest working set
			for(String s: lastKnownClassSet){
				scheduler.getCurrentCourseList().add(s);
			}
			
		}
	}
	
	/********************************************************************
	 * Method: updateSchedule
	 * Purpose: updates viewable schedule
	/*******************************************************************/
	public void updateSchedule() {
		
		filteredSchedules = new ArrayList<List<Course>>();
		if(unfilteredSchedules == null) return;
		
		// Reused index or filtering
		int index;
	
		// Check every permutation for a violation
		for(index = 0; index < unfilteredSchedules.size(); index++){
		
			// Get current set
			List<Course> working = unfilteredSchedules.get(index);
			boolean doNotAdd = false;
			
			// For each course
			for(Course course : working){
				
				// Check if course has any violations
				if((course.getClosed() && !closedCB.isSelected())
					|| (course.getNR() && !nrCB.isSelected())
					|| (course.getSR() && !srCB.isSelected())
					|| blacklistCRN.containsKey(course.getCRN())
					){
					doNotAdd = true;
					break;
				}
				
				// Test each meeting
				for(Meeting sub : course.getMeetings()){
					
					doNotAdd = doNotAdd || (sub.M && !mCB.isSelected()) || 
							(sub.T && !tCB.isSelected()) || 
							(sub.W && !wCB.isSelected()) || 
							(sub.R && !rCB.isSelected()) || 
							(sub.F && !fCB.isSelected()) ||
							(sub.getLocation().equals(CMP_INTERNET +"") && !((JCheckBox)campusBox.getItemAt(1)).isSelected()) ||
							(sub.getLocation().equals(CMP_MAIN +"") && !((JCheckBox)campusBox.getItemAt(2)).isSelected()) ||
							(sub.getLocation().equals(CMP_MT_CLEMENS+"") && !((JCheckBox)campusBox.getItemAt(3)).isSelected()) ||
							(sub.getLocation().equals(CMP_MACOMB+"") && !((JCheckBox)campusBox.getItemAt(4)).isSelected()) ||
							(sub.getLocation().equals(CMP_DOMESTIC+"") && !((JCheckBox)campusBox.getItemAt(5)).isSelected()) ||
							(sub.getLocation().equals(CMP_INTERNATIONAL+"") && !((JCheckBox)campusBox.getItemAt(6)).isSelected());
					
					// No need to keep checking
					if(doNotAdd) break;
				}
				
				// No need to keep checking
				if(doNotAdd) break;
			}

			// Add schedule?
			if(!doNotAdd) filteredSchedules.add(unfilteredSchedules.get(index));
		}		
		
		// Early sorting
		if(sortingCB.getSelectedItem().equals("Early")){

			// Sort schedules based on new comparator
			java.util.Collections.sort(filteredSchedules, new java.util.Comparator<List<Course>>() {
				public int compare(List<Course> a, List<Course> b) {
					
					// Default values
					double aVal = 0;
					double bVal = 0;
					
					// Target time military hours x minutes
					double targetTime = 8*60;
					
					// Cycle through all courses in A
					for(Course c : a){
						
						// Cycle through all meetings in A
						for(Meeting sc : c.getMeetings() ){
							
							
							// Get time
							TimeBlock aTime = sc.getTime();
							
							// Get day count
							int dayCount = 0;
							if(sc.M) dayCount++;
							if(sc.T) dayCount++;
							if(sc.W) dayCount++;
							if(sc.R) dayCount++;
							if(sc.F) dayCount++;
							
							// Calculate A
							aVal += Math.abs(aTime.getEnd() - targetTime) 
								 + aTime.getLength() * dayCount* TIME_LEN_SORT_RATIO;
						}
					}
					
					// Cycle through all B courses
					for(Course c : b){
						
						// Cycle through all meetings
						for(Meeting sc : c.getMeetings() ){
							
							// Get time
							TimeBlock bTime = sc.getTime();
							
							// Get day count
							int dayCount = 0;
							if(sc.M) dayCount++;
							if(sc.T) dayCount++;
							if(sc.W) dayCount++;
							if(sc.R) dayCount++;
							if(sc.F) dayCount++;
							
							// Calculate A
							bVal += Math.abs(bTime.getEnd() - targetTime) 
								 + bTime.getLength() * dayCount* TIME_LEN_SORT_RATIO;
						}
					}
					
					// Compare variables
					return Double.compare(aVal, bVal);
				}
			});
		}
		
		// Afternoon sorting
		else if(sortingCB.getSelectedItem().equals("Afternoon")){
		
			java.util.Collections.sort(filteredSchedules, new java.util.Comparator<List<Course>>() {
				public int compare(List<Course> a, List<Course> b) {
					
					// Default values
					double aVal = 0;
					double bVal = 0;
					
					// Target time military hours x minutes
					double targetTime = 14.5*60;
					
					// Cycle through all courses in A
					for(Course c : a){
						
						// Cycle through all meetings in A
						for(Meeting sc : c.getMeetings() ){
							
							
							// Get time
							TimeBlock aTime = sc.getTime();
							
							// Get day count
							int dayCount = 0;
							if(sc.M) dayCount++;
							if(sc.T) dayCount++;
							if(sc.W) dayCount++;
							if(sc.R) dayCount++;
							if(sc.F) dayCount++;
							
							// Calculate A
							aVal += Math.abs(aTime.getEnd() - targetTime) 
								 + aTime.getLength() * dayCount* TIME_LEN_SORT_RATIO;
						}
					}
					
					// Cycle through all B courses
					for(Course c : b){
						
						// Cycle through all meetings
						for(Meeting sc : c.getMeetings() ){
							
							// Get time
							TimeBlock bTime = sc.getTime();
							
							// Get day count
							int dayCount = 0;
							if(sc.M) dayCount++;
							if(sc.T) dayCount++;
							if(sc.W) dayCount++;
							if(sc.R) dayCount++;
							if(sc.F) dayCount++;
							
							// Calculate A
							bVal += Math.abs(bTime.getEnd() - targetTime) 
								 + bTime.getLength() * dayCount* TIME_LEN_SORT_RATIO;
						}
					}
					
					// Compare variables
					return Double.compare(aVal, bVal);
				}
			});
		}
		
		// Sort based on night
		else if(sortingCB.getSelectedItem().equals("Night")){
		
			java.util.Collections.sort(filteredSchedules, new java.util.Comparator<List<Course>>() {
				public int compare(List<Course> a, List<Course> b) {
					
					// Default values
					double aVal = 0;
					double bVal = 0;
					
					// Target time military hours x minutes
					double targetTime = 21*60;
					
					// Cycle through all courses in A
					for(Course c : a){
						
						// Cycle through all meetings in A
						for(Meeting sc : c.getMeetings() ){
							
							
							// Get time
							TimeBlock aTime = sc.getTime();
							
							// Get day count
							int dayCount = 0;
							if(sc.M) dayCount++;
							if(sc.T) dayCount++;
							if(sc.W) dayCount++;
							if(sc.R) dayCount++;
							if(sc.F) dayCount++;
							
							// Calculate A
							aVal += Math.abs(aTime.getEnd() - targetTime) 
								 + aTime.getLength() * dayCount* TIME_LEN_SORT_RATIO;
						}
					}
					
					// Cycle through all B courses
					for(Course c : b){
						
						// Cycle through all meetings
						for(Meeting sc : c.getMeetings() ){
							
							// Get time
							TimeBlock bTime = sc.getTime();
							
							// Get day count
							int dayCount = 0;
							if(sc.M) dayCount++;
							if(sc.T) dayCount++;
							if(sc.W) dayCount++;
							if(sc.R) dayCount++;
							if(sc.F) dayCount++;
							
							// Calculate A
							bVal += Math.abs(bTime.getEnd() - targetTime) 
								 + bTime.getLength() * dayCount* TIME_LEN_SORT_RATIO;
						}
					}
					
					// Compare variables
					return Double.compare(aVal, bVal);
				}
			});
		}
		
		index = 0;
		scheduleIndexT.setText((1) + "");
		scheduleTotalT.setText("/ " + filteredSchedules.size());
		
		showSchedule(0);
		
	}

	/********************************************************************
	 * Method: loadScheduleFile
	 * Purpose: loads schedule data from file
	/*******************************************************************/
	public static Scheduler loadScheduleFile(String filename){
		
		try{ 
			
			// Attempt to read file
			FileInputStream stream = new FileInputStream(filename); 
			ObjectInputStream reader = new ObjectInputStream(stream);
			Scheduler load = (Scheduler) reader.readObject();
			reader.close();
			return load;
		}
		catch(Exception e){
			
			// Show exception
			e.printStackTrace();
			return null;
		}
	}
	
	
	/********************************************************************
	 * Method: storeSchedulerFile
	 * Purpose: stores schedule file from the current scheduler data
	/*******************************************************************/
	public static void storeScheduleFile(String filename, Scheduler save){

		try{
			
			// Fix extension if needed
			if(!filename.matches(".*\\.oksch")) filename += ".oksch";
			
			// Write file!
			ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(filename));
			out.writeObject(save);
			out.close();
		}
		
		catch(Exception e){
			
			// Show exception
			e.printStackTrace();
		}
	}

	/********************************************************************
	 * Method: actionPerformed
	 * Purpose: handles most of the actions in the UI
	/*******************************************************************/
	@Override public void actionPerformed(ActionEvent event) {
		
		
		// Save button
		if(event.getSource() == saveB || event.getSource() == saveItem){
			
			// Setup file chooser
			fc = new JFileChooser();
			FileNameExtensionFilter filter = new FileNameExtensionFilter(
	                "Oakland schedule files (*.oksch)", "oksch");
	        fc.setFileFilter(filter);
			fc.setApproveButtonText("Save");
			fc.setDialogTitle("Save File");
			
			// Ask user for file with correct extention oksch
			int returnVal = fc.showOpenDialog(MainApplication.this);
			
			// Only store based on approve button
			if(returnVal == JFileChooser.APPROVE_OPTION){
				
				// Store given file
				storeScheduleFile(fc.getSelectedFile().getAbsolutePath(), scheduler);
			}
		}
		
		// Print item
		else if(event.getSource() == printItem){
			

			  PrinterJob pj = PrinterJob.getPrinterJob();
			  pj.setJobName(" Print Component ");
			  pj.setPrintable (new Printable() {    
			    
				  @Override public int print(Graphics pg, PageFormat pf, int pageNum){
			      if (pageNum > 0){
			    	  return Printable.NO_SUCH_PAGE;
			      }

			      Graphics2D g2 = (Graphics2D) pg;
			      //g2.translate(pf.getImageableX(), pf.getImageableY());
			      AffineTransform originalTransform = g2.getTransform();

			      double scaleX = pf.getImageableWidth() / scheduleP.getWidth();
			      double scaleY = pf.getImageableHeight() / scheduleP.getHeight();
			      // Maintain aspect ratio
			      double scale = Math.min(scaleX, scaleY);
			      g2.translate(pf.getImageableX(), pf.getImageableY());
			      g2.scale(scale, scale);
			      scheduleP.printAll(g2);

			      g2.setTransform(originalTransform);
			      //scheduleP.printAll(g2);
			      return Printable.PAGE_EXISTS;
			    }
			  });
			  if (pj.printDialog() == false)
			  return;

			  try {
			        pj.print();
			  } catch (PrinterException ex) {
			        // handle exception
			  }		
		}
		
		// Export PNG
		else if(event.getSource() == exportPNG){

			// Setup file chooser
			fc = new JFileChooser();
			FileNameExtensionFilter filter = new FileNameExtensionFilter(
	                "PNG", "png");
	        fc.setFileFilter(filter);
	        fc.setApproveButtonText("Export");
	        fc.setDialogTitle("Export image");
			
	        // Ask user where to load from
	        int returnVal = fc.showOpenDialog(MainApplication.this);
		
	        // Only load if approved
			if(returnVal == JFileChooser.APPROVE_OPTION){
				
			
				// Setup print
				BufferedImage bi = new BufferedImage(scheduleP.getSize().width, scheduleP.getSize().height, 
						BufferedImage.TYPE_INT_ARGB); 
				Graphics g2 = bi.createGraphics();
				scheduleP.printAll(g2);
				g2.dispose();
				
				// Adjust extention
				String file = fc.getSelectedFile().getAbsolutePath();
				if(!file.endsWith(".png")) file += ".png";
				
				// Attempt print
				try{ ImageIO.write(bi,"png",new File(file)); }
				
				// Catch exception
				catch (Exception e) { e.printStackTrace(); }
			}
		}
		
		// Load button
		else if(event.getSource() == loadB || event.getSource() == loadItem){
			
			// Setup file chooser
			fc = new JFileChooser();
			FileNameExtensionFilter filter = new FileNameExtensionFilter(
	                "Oakland schedule files (*.oksch)", "oksch");
	        fc.setFileFilter(filter);
	        fc.setApproveButtonText("Load");
	        fc.setDialogTitle("Load File");
			
	        // Ask user where to load from
	        int returnVal = fc.showOpenDialog(MainApplication.this);
			
	        // Only load if approved
			if(returnVal == JFileChooser.APPROVE_OPTION){
				
				// Load the schedule file
				scheduler = loadScheduleFile(fc.getSelectedFile().getAbsolutePath());
				
				lastKnownClassSet = new ArrayList<String>();
				for(String s : scheduler.getCurrentCourseList()){
					lastKnownClassSet.add(s);
				}
				
				// Update both lists and scheudle
				this.courseListAll.setListData(scheduler.getCourseIDs().toArray());
				this.courseListSelected.setListData(scheduler.getCurrentCourseList().toArray());
				updatePermutations();
				updateSchedule();
			}
			
		}
		
		// Download button pressed
		else if(event.getSource() == downloadB){
						
			// Show download dialog
			downloadShowing = true;
			new DownloadDialog(this, scheduler, courseListSelected, courseListAll);			
			downloadShowing = false;
		}
		
		
		// Back button
		else if(event.getSource() == backB){
			
			// Deduct one from current index
			if(filteredSchedules != null && !((index - 1 < 0) || (index - 1 >= filteredSchedules.size()))){
				scheduleIndexT.setText((--index  + 1) + "");
			}
		}
		
		// Forward button
		else if(event.getSource() == forwardB){
			
			// Add one to index
			if(filteredSchedules != null && !((index + 1 < 0) || (index + 1 >= filteredSchedules.size()))){
				scheduleIndexT.setText((++index + 1) + "");
			}
		}
		
		// If settings have a state change
		else if(event.getSource() == sortingCB || (event.getSource() == mCB) || (event.getSource() == tCB)
				|| (event.getSource() == wCB) || (event.getSource() == rCB) || (event.getSource() == fCB)
				|| event.getSource() == srCB || event.getSource() == closedCB || event.getSource() == nrCB){
			
			
			// Update schedule
			updateSchedule();
		}
		
		else if(event.getSource() == schedulerItem){
			
			// Show this applications license
			JOptionPane.showOptionDialog(this,
					"This application is part of Oakland Scheduler." +
					"\n" +
				    "\nOakland Scheduler is free software: you can redistribute it and/or modify" +
					"\nit under the terms of the GNU General Public License as published by the" +
					"\nFree Software Foundation, either version 3 of the License, or (at your" +
					"\noption) any later version." +
					"\n" +
					"\nOakland Scheduler is distributed in the hope that it will be useful," +
					"\nbut WITHOUT ANY WARRANTY; without even the implied warranty of" +
					"\nMERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See" +
					"\nthe GNU General Public License for more details." +
					"\n" +
					"\nFor the full license, see http://www.gnu.org/licenses/."
					,"Oakland Scheduler License", JOptionPane.DEFAULT_OPTION,JOptionPane.INFORMATION_MESSAGE, null, new Object[]{}, null);
		}
		
		else if(event.getSource() == htmlComponents){
					  
			// Show this applications license
			JOptionPane.showOptionDialog(this,
					   "Apache HttpComponents Client\n" +
						"Copyright 1999-2014 The Apache Software Foundation\n" +
						"\n" +
						"This product includes software developed at\n" +
						"The Apache Software Foundation (http://www.apache.org/).\n"+
						"\n" +
						"Apache License" +
                        "Version 2.0, January 2004\n" +
                        "http://www.apache.org/licenses/LICENSE-2.0.html",
						"Apache HttpComponents Client", JOptionPane.DEFAULT_OPTION,JOptionPane.INFORMATION_MESSAGE, null, new Object[]{}, null);
		}
		
		else if(event.getSource() == backgroundItem){
			
			// Show this applications license
			JOptionPane.showOptionDialog(this,
					   "The purpose of the application is to help Oakland University\n" +
					   "students determine the best schedule for them utilizing various\n" +
					   "filter techniques and a visual display.",
						"Oakland Scheduler Purpose", JOptionPane.DEFAULT_OPTION,JOptionPane.INFORMATION_MESSAGE, null, new Object[]{}, null);

		}

		else if(event.getSource() == contactItem){
			
			// Show this applications license
			JOptionPane.showOptionDialog(this,
					   "To contact the developer, contribute, report bugs, or\n" +
					   "make requests please email garrick@garrickmail.net\n" +
					   "\n" +
					   "To view or download the source code please visit\n" + 
					   "www.github.com/garrickbrazil/Oakland-Scheduler",
						"Contact", JOptionPane.DEFAULT_OPTION,JOptionPane.QUESTION_MESSAGE, null, new Object[]{}, null);

		}
		
		else if(event.getSource() == howtoItem){
			
			// Show this applications license
			JOptionPane.showOptionDialog(this,
					   "Load a course set from a previously saved data file (.oksch)\n" + 
						"or download a fresh data set through the built-in downloader.\n" +
						"Save the data set to your local drive for faster viewing later.\n" + 
						"\n" +
						"Add courses you wish to take through the all course list. Filter\n" +
						"the courses down and/or sort them by time. Navigate through\n" +
						"the schedules using the arrow buttons or arrow keys.\n" +
						"\n" +
						"Print or export a schedule that you like to view later or share."
						,
						"How To Use", JOptionPane.DEFAULT_OPTION,JOptionPane.QUESTION_MESSAGE, null, new Object[]{}, null);

		}
		
		else if(event.getSource() == jsoupItem){
				
				// Show this applications license
				JOptionPane.showOptionDialog(this,
						"jsoup License\n"+
						"\n"+
						"The jsoup code-base (include source and compiled packages) are\n"+
						"distributed under the open source MIT license as described below.\n"+
						"\n" +
						"The MIT License\n"+
						"\n"+
						"Copyright © 2009 - 2013 Jonathan Hedley (jonathan@hedley.net)\n"+
						"http://jsoup.org/license",
						/*"Permission is hereby granted, free of charge, to any person \n"+
						"obtaining a copy of this software and associated documentation \n"+
						"files (the \"Software\"), to deal in the Software without restriction,\n"+
						"including without limitation the rights to use, copy, modify, merge,\n"+
						"publish, distribute, sublicense, and/or sell copies of the Software, \n"+
						"and to permit persons to whom the Software is furnished to do so,\n"+
						"subject to the following conditions:\n"+
						"\n"+
						"The above copyright notice and this permission notice shall be included\n"+
						"in all copies or substantial portions of the Software.\n"+
						"\n"+
						"THE SOFTWARE IS PROVIDED \"AS IS\", WITHOUT WARRANTY OF ANY KIND, \n"+
						"EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF \n"+
						"MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.\n"+
						"IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY \n"+
						"CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, \n"+
						"TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE \n"+
						"SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.",
						*/
						"jsoup license", JOptionPane.DEFAULT_OPTION,JOptionPane.INFORMATION_MESSAGE, null, new Object[]{}, null);
			}
	}
	
	/********************************************************************
	 * Method: showSchedule
	 * Purpose: displays the given schedule requested
	/*******************************************************************/
	public void showSchedule(int num){
		
		// Remove all elements
		scheduleP.removeAll();
		
		// Don't try if num is too big
		if(num >= filteredSchedules.size()) return;
		
		// Setup all days time block
		List<TimeBlock> monday;
		List<TimeBlock> tuesday;
		List<TimeBlock> wednesday;
		List<TimeBlock> thursday;
		List<TimeBlock> friday;
		
		// Get current schedule
		List<Course> coursesMainList = filteredSchedules.get(num);
		List<Meeting> courses = new ArrayList<Meeting>();
		Color currentColor = Color.decode(colors[0]);
		
		// Start off earliest and latest and max and min values
		int earliest = Integer.MAX_VALUE;
		int latest = Integer.MIN_VALUE;
			
		
		// Cycle through list of working schedules
		for(int i = 0; i < coursesMainList.size(); i++){
			
			// If over size limit use first color
			if(i >= colors.length) currentColor = Color.decode(colors[0]);
			else currentColor = Color.decode(colors[i]);
			
			// Check all meetings for earliest and latest
			for(Meeting c : coursesMainList.get(i).getMeetings()){
				
				
				// Get time and set color
				TimeBlock t = c.getTime();
				c.setColor(currentColor);
				
				// Make an overall list of courses
				courses.add(c);
				
				// Check earliest and latest
				if (t.getStart() < earliest) earliest = t.getStart();
				if (t.getEnd() > latest) latest = t.getEnd();
			}
		}
		
		// Days of the week
		monday = new ArrayList<TimeBlock>();
		tuesday = new ArrayList<TimeBlock>();
		wednesday = new ArrayList<TimeBlock>();
		thursday = new ArrayList<TimeBlock>();
		friday = new ArrayList<TimeBlock>();
		
		// Cycle through all courses
		for(int i = 0; i < courses.size(); i++){
			
			// Get course and time block
			Meeting current = courses.get(i);
			TimeBlock newBlock = current.getTime();
			
			if (newBlock != null){
			
				// Add to correct block(s)
				if(current.M) monday.add(newBlock);
				if(current.T) tuesday.add(newBlock);
				if(current.W) wednesday.add(newBlock);
				if(current.R) thursday.add(newBlock);
				if(current.F) friday.add(newBlock);
			}
		}	
		
		// Sort each day
		Collections.sort(monday);
		Collections.sort(tuesday);
		Collections.sort(wednesday);
		Collections.sort(thursday);
		Collections.sort(friday);
				
		// Constraints
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.insets = new Insets(0,0,0,1);
		c.weightx = 1;
		c.weighty = 0;
		c.gridy = 0;
		
		// Header information
		JLabel mL = new JLabel("Monday", JLabel.CENTER);
		JLabel tL = new JLabel("Tuesday", JLabel.CENTER);
		JLabel wL = new JLabel("Wednesday", JLabel.CENTER);
		JLabel rL = new JLabel("Thursday", JLabel.CENTER);
		JLabel fL = new JLabel("Friday", JLabel.CENTER);
		
		// Set default backgrounds and opaque
		mL.setBackground(DEFAULT_COLOR);
		tL.setBackground(DEFAULT_COLOR);
		wL.setBackground(DEFAULT_COLOR);
		rL.setBackground(DEFAULT_COLOR);
		fL.setBackground(DEFAULT_COLOR);
		mL.setOpaque(true);
		tL.setOpaque(true);
		wL.setOpaque(true);
		rL.setOpaque(true);
		fL.setOpaque(true);

		
		// Add each day label
		c.gridx = 1;
		scheduleP.add(mL,c);
		c.gridx = 2;
		scheduleP.add(tL,c);
		c.gridx = 3;
		scheduleP.add(wL,c);
		c.gridx = 4;
		scheduleP.add(rL,c);
		c.gridx = 5;
		scheduleP.add(fL,c);
		
		// No stretching
		c.weightx = 0;
		c.weighty = 0;
		
		// Cycle through every hour
		for (int hourIndex = TIME_START; hourIndex <= TIME_END; hourIndex++){
			
			// Cycle through every row (15 minutes)
			for(int quarterIndex = 0; quarterIndex < 4; quarterIndex++){
				
		
				// Label for left schedule column times
				JLabel time;
				
				// Only zero when beginning of an hour
				if(quarterIndex == 0){
					
					
					// Fix time (military)
					String timeStr = ":00 ";
					if(hourIndex == 24) timeStr = (12) + timeStr + "AM";
					else if(hourIndex > 12) timeStr = (hourIndex - 12) + timeStr + "PM";
					else if(hourIndex == 12) timeStr = hourIndex + timeStr + "PM";
					else if(hourIndex == 0) timeStr = 12 + timeStr + "AM";
					else timeStr = hourIndex + timeStr + "AM";
					
					// Make a time label with current time
					time = new JLabel(timeStr, JLabel.CENTER);
				}
				
				// Make an empty label to fill space
				else time = new JLabel("");
				
				// Current row equal to number of 15 minute increments
				int row = 1 + (hourIndex-TIME_START)*4 + quarterIndex;
				time.setPreferredSize(new Dimension(TIME_ROW_W, TIME_ROW_H));
				time.setMinimumSize(new Dimension(TIME_ROW_W, TIME_ROW_H));
				
				
				// Adjust row color so that every other hour is grey
				if(row%4 == 0 || (row + 1)%4 == 0){time.setBackground(DEFAULT_COLOR);}
				else{time.setBackground(ALT_COLOR);}
				
				// Small gaps for even and odd rows
				if(row%2 == 0) c.insets = new Insets(0,0,0,1);
				else c.insets = new Insets(1,0,0,1);
				
				// Constraint settings
				time.setOpaque(true);
				c.gridheight = 1;
				c.gridwidth = 1;
				c.gridx = 0;
				
				// Grid number based on row span
				c.gridy = row;
				
				// Stretch the last row ?
				if (hourIndex == TIME_END && quarterIndex == 3) c.weighty = 1;
				else c.weighty = 0;
				
				// Add time label to schedule panel
				scheduleP.add(time, c);
			}
		}
		
		// Top spacer
		c.insets = new Insets(0,0,0,1);
		c.weightx = 0;
		c.weighty = 0;
		c.gridheight = 1;
		c.gridx = 0;
		c.gridy = 0;
		
		// Blank spacer for the top
		JLabel spacer = new JLabel("");
		spacer.setOpaque(true);
		spacer.setBackground(DEFAULT_COLOR);
		scheduleP.add(spacer, c);
		
		// Build each day 
		buildDay(monday, 1);
		buildDay(tuesday, 2);
		buildDay(wednesday, 3);
		buildDay(thursday, 4);
		buildDay(friday, 5);
		
		scheduleP.revalidate();
		scheduleP.repaint();
	}
	
	/********************************************************************
	 * Method: buildDay
	 * Purpose: builds the UI for each day column
	/*******************************************************************/
	public void buildDay(List<TimeBlock> day, final int COL){
		
		
		// Latest time for the day (minutes)
		int latestEnd = TIME_START * 60;
		
		// Current row
		int row = 1;
		
		// Constraints
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.insets = new Insets(1,0,0,0);
		c.weightx = 1;
		c.weighty = 0;
		c.gridheight = 1;
		c.gridx = COL;
		
		// Go through each time
		for(TimeBlock curr : day){
		
			
			// Get meeting info
			Meeting meeting = curr.getMeetingReference();
			
			// Create spacer and row size (15 minute increments) 
			int spacerSpan = (curr.getStartRounded() - latestEnd) / 15;
			int rowSpan = (curr.getEndRounded() - curr.getStartRounded()) / 15;
			
			// Reset gridheight to default  1
			c.gridheight = 1;
						
			
			for (int i = 0; i < spacerSpan; i++){
			
				// Spacer
				JLabel spacer = new JLabel("");
				spacer.setOpaque(true);
			
				// Adjust row color so that every other hour is grey
				if(row%4 == 0 || (row + 1)%4 == 0) spacer.setBackground(DEFAULT_COLOR);
				else spacer.setBackground(ALT_COLOR);
				
				// Adjsut spacing for even and odd rows
				if(row%2 == 0) c.insets = new Insets(0,0,0,0);
				else c.insets = new Insets(1,0,0,0);
				
				// Grid Y based on current row
				c.gridy = row++;
				scheduleP.add(spacer, c);
			}

			// Meeting information (fixed character length of 24)
			String name = (meeting.getCourseReference().getCourseName().length() > 24)? meeting.getCourseReference().getCourseName().substring(0, 24) + "..." : meeting.getCourseReference().getCourseName();
			String instructor = (meeting.getProfessor().length() > 24)? meeting.getProfessor().substring(0, 24) + "..." : meeting.getProfessor();
			
			// Make label based on simple HTML
			JLabel courseL = new JLabel("<HTML><CENTER>" + name + "<BR>" + meeting.getCourseReference().getCourseID() + " - " + (meeting.getCourseReference().getCRN()) + "<BR>" + instructor + "<BR>" + meeting.getRoom() + "<BR>" + meeting.getTime() + "</HTML>", JLabel.CENTER);
			courseL.setOpaque(true);
			if(meeting.getCourseReference().getClosed()) courseL.setBorder(BorderFactory.createLineBorder(Color.RED, 1));
			//else courseL.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
			
			// Adjust Y coord and span
			c.gridy = row;
			c.gridheight = rowSpan;
			
			// Increment the row
			row += rowSpan;
			
			// Adjust color then add label
			courseL.setBackground(meeting.getColor());
			scheduleP.add(courseL, c);
			
			// Adjust latest end based on current end time
			latestEnd = curr.getEndRounded();
		}
		
		// Fill end rows?
		if(day.size() == 0){
			
			// Default start and end for an entirely blank day !
			int start = TIME_START * 60;
			int end = (TIME_END + 1) * 60;
			
			// Number of rows needed
			int rowsToInsert = (end - start)/15;
			
			for(int i = 0; i < rowsToInsert; i++){
				
				// Row spacer constraints
				c.weightx = 1;
				c.weighty = 0;
				c.gridheight = 1;
				
				// Spacer with some default spacing
				JLabel spacer = new JLabel("                                 ");
				spacer.setOpaque(true);
				
				// Alternate colors
				if(row%4 == 0 || (row + 1)%4 == 0){spacer.setBackground(DEFAULT_COLOR);}
				else{spacer.setBackground(ALT_COLOR);}
				
				// Spacing for even and odd rows
				if(row%2 == 0) c.insets = new Insets(0,0,0,0);
				else c.insets = new Insets(1,0,0,0);
				
				// Adjust row and add
				c.gridy = row++;
				scheduleP.add(spacer, c);
			}
		}
		
		// The last meeting is less then the end time ?
		else if(day.get(day.size() - 1).getEndRounded() < ((TIME_END + 1) * 60)){
			
			// Setup start and end
			int start = day.get(day.size() - 1).getEndRounded(); 
			int end = (TIME_END + 1) * 60;
			
			// Calculate rows needed
			int rowsToInsert = (end - start)/15;
			
			for(int i = 0; i < rowsToInsert; i++){
				
				// Spacer constraints
				c.weightx = 1;
				c.weighty = 0;
				c.gridheight = 1;
				
				// Spacer with default spacing
				JLabel spacer = new JLabel("                             ");
				spacer.setOpaque(true);
				
				// Alternate colors
				if(row%4 == 0 || (row + 1)%4 == 0){spacer.setBackground(DEFAULT_COLOR);}
				else{spacer.setBackground(ALT_COLOR);}
				
				// Padding for odd and even rows
				if(row%2 == 0) c.insets = new Insets(0,0,0,0);
				else c.insets = new Insets(1,0,0,0);
				
				// Adjsust row and add spacer
				c.gridy = row++;
				scheduleP.add(spacer, c);
			}
		}
	}	
	
	/********************************************************************
	 * Class: selectItem
	 * Purpose: allows a checkbox item to be selected (config a course)
	/*******************************************************************/
	private void selectItem(Point point){
	    
		// Index to select
		int index = configCourseList.locationToIndex(point);

		// Valid?
	    if (index >= 0){
	      
	    	// Select itex
	    	CheckBoxItem item = (CheckBoxItem)configCourseList.getModel().getElementAt(index);
	    	item.setSelected(!item.getSelected());
	    	configCourseList.repaint(configCourseList.getCellBounds(index, index));
	    }
	}

	
	/********************************************************************
	 * Class: CheckBoxItem
	 * Purpose: contains a checkbox and label for the list
	/*******************************************************************/
	public class CheckBoxItem{
		
		
		// Properties
		private String label; 
		private boolean selected;
		private int crn;
		
		// Constructor
		public CheckBoxItem(String label, boolean selected, int crn){
			
			this.label = label;
			this.selected = selected;
			this.crn = crn;
		}
		
		// Mutators
		public void setSelected(Boolean selected){ this.selected = selected; }
		
		// Accessors
		public Boolean getSelected(){ return this.selected; }
		public String getLabel(){ return this.label; }
		public String toString(){ return this.label; }
		public int getCRN(){ return this.crn; }
		
	}
	
	/********************************************************************
	 * Class: CheckBoxListRenderer
	 * Purpose: has the ability to render checkboxes in a JList
	/*******************************************************************/
	public class CheckBoxListRenderer extends JCheckBox implements ListCellRenderer {
		
		// Default serialization
		private static final long serialVersionUID = 1L;

		// Generates component for rendering
		public Component getListCellRendererComponent(JList comp, Object value,
                int index, boolean isSelected, boolean hasFocus){
			
			
			setEnabled(comp.isEnabled());
		    setSelected(((CheckBoxItem) value).getSelected());
		    setFont(comp.getFont());
		    setText(value.toString());

		    // Give item selected background and foregrounds
		    if (isSelected){
		    	
		      setBackground(comp.getSelectionBackground());
		      setForeground(comp.getSelectionForeground());
		    }
		    
		    // Give item regular background and foregrounds
		    else{
		    	
		      setBackground(comp.getBackground());
		      setForeground(comp.getForeground());
		    }
			
			return this;
		}
	}
}