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

import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.SwingWorker;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/********************************************************************
 * Class: DownloadDialog
 * Purpose: used to log into Oakland server and download course data
/*******************************************************************/
@SuppressWarnings("deprecation") class DownloadDialog extends JDialog implements ActionListener {

	
	private static final long serialVersionUID = 1L;	// default serialization
	private JTextField user,pass;						// username and password for user
	private JComboBox termCB;					// terms drop down box
	private JPanel panel;								// main container
	private Scheduler scheduler;						// scheduler reference to store data into
	private JList courseListAll;				// reference for entire list
	private JList courseListSelected;			// reference for selected list
	private SwingWorker<Void,Void> worker;				// worker to download data!
	private List<String> termsName;						// term names available for download
	private List<String> termsValue;					// term values available for download
	
	// Subject list to download
	final String[] SUBJECTS = {"ACC", "ACS", "AED", "AH", "AHS", "ALS", "AMS", "AN", "APM",
			"ARB", "ATB", "BCM", "BIO", "CHE", "CHM", "CIN", "CIT", "CNS", "COM", "CRJ", 
			"CSE", "CT", "DAN", "DES", "EA", "EC", "ECE", "ECN", "ED", "EED", "EEI", "EGB", 
			"EGR", "EL", "ENG", "ENT", "ENV", "ESL", "EST", "EXS", "FE", "FIN", "FRH", "GEO",
			"GRM", "HBR", "HC", "HRD", "HS", "HST", "HT", "IB", "IS", "ISE", "IST", "IT", 
			"JPN", "JRN", "LBS", "LIB", "LIN", "LIT", "LTN", "ME", "MGT", "MIS", "MKT", "ML",
			"MLS", "MOR", "MTD", "MTE", "MTH", "MTS", "MUA", "MUE", "MUS", "MUT", "NH", "NMT",
			"NRS", "ORG", "OSH", "PA", "PH", "PHL", "PHY", "POM", "PS", "PSY", "PT", "QMM", 
			"RAD", "RDG", "REL", "RT", "SA", "SBC", "SCI", "SCS", "SE", "SED", "SOC", "SPN", 
			"SST", "STA", "SW", "SYS", "TD", "THA", "WGS", "WHP", "WRT"};


	// Title of Dialog
	private final static String TITLE = "Oakland Downloader";
	
	/********************************************************************
	 * Constructor: DownloadDialog
	 * Purpose: constructor for download, with necessary references
	/*******************************************************************/
	public DownloadDialog(final MainApplication context, Scheduler scheduler_Ref, JList courseListSelected_Ref, JList courseListAll_Ref){
		
		// Basic setup for dialog
		setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		
		setTitle(TITLE);
		
		// Setup proper references
		this.scheduler = scheduler_Ref;
		this.courseListSelected = courseListSelected_Ref;
		this.courseListAll = courseListAll_Ref;
		
		// Store available terms
		storeTerms();
		
		// Constraints
		GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.insets = new Insets(10,10,10,10);
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 0;
        c.gridwidth = 2;
        
        // Main panel
     	panel = new JPanel(new GridBagLayout());
        
     	// Add term select box
     	termCB = new JComboBox(termsName.toArray());
        panel.add(termCB, c);
        
        // Setup username and password labels
        JLabel userL = new JLabel("Username:");
        JLabel passL = new JLabel("Password:");
        c.gridwidth = 1;
        c.gridx = 0;
        c.weightx = 0;
        
        // Add user label
        c.gridy = 1;
        c.insets = new Insets(5,10,0,10);
        panel.add(userL, c);
        
        // Add password label
        c.gridy = 2;
        c.insets = new Insets(0,10,5,10);
        panel.add(passL,c);
        
        // Setup user and pass text fields
        user = new JTextField();
        pass = new JPasswordField();
        c.weightx = 1;
        c.gridx = 1;
        
        // Add user field
        c.gridy = 1;
        c.insets = new Insets(5,10,2,10);
        panel.add(user,c);
        
        // Add pass field
        c.gridy = 2;
        c.insets = new Insets(2,10,5,10);
        panel.add(pass,c);
        
        // Setup login button
        JButton login = new JButton("Login");
        login.addActionListener(this);
        c.insets = new Insets(10,10,10,10);
        c.gridx = 0;
        c.gridy = 3;
        c.gridwidth = 2;
        c.weightx = 1;
        panel.add(login,c);
        
        // Add panel to main box
        add(panel);
        
        // Pack the components and give userbox focus
        pack();
        user.requestFocus();
		
        
        // Create worker to download courses
		worker = new SwingWorker<Void,Void>(){
			protected Void doInBackground() throws Exception{
				
				// Reset courses
				scheduler.resetCourses();
				
				// Constraints
				GridBagConstraints c = new GridBagConstraints();
		        c.fill = GridBagConstraints.BOTH;
		        c.insets = new Insets(10,10,10,10);
		        c.gridx = 0;
		        c.weightx = 1; 
		        c.weighty = 0;
		        
		        // Remove all elements
				panel.removeAll();
				
				// Add status
				JLabel status = new JLabel("Connecting...");
				c.gridy = 0;
				panel.add(status, c);
				
				// Add progress bar
				JProgressBar progressBar = new JProgressBar(0, SUBJECTS.length);
				c.gridy = 1;
				panel.add(progressBar,c);
				progressBar.setPreferredSize(new Dimension(275, 12));
				
				// Revalidate, repaint, and pack
				//revalidate();
				repaint();
				pack();
				
				try{
				
					// Create client
					DefaultHttpClient client = new DefaultHttpClient();
					
					// Setup and execute initial login
					HttpGet initialLogin = new HttpGet("https://sail.oakland.edu/PROD/twbkwbis.P_WWWLogin");
					HttpResponse response = client.execute(initialLogin);
					HTMLParser.parse(response);
					
					// Get current term
					String term = termsValue.get(termCB.getSelectedIndex());
					
			        // Consume entity (cookies)
		            HttpEntity entity = response.getEntity();
					if (entity != null) entity.consumeContent();	
					
					// Create post for login
		            HttpPost login = new HttpPost("https://sail.oakland.edu/PROD/twbkwbis.P_ValLogin");
		            
		            // Parameters
		            List <NameValuePair> parameters = new ArrayList <NameValuePair>();
		            parameters.add(new BasicNameValuePair("sid", user.getText()));
		            parameters.add(new BasicNameValuePair("pin", pass.getText()));
		            login.setEntity(new UrlEncodedFormEntity(parameters));
		            login.setHeader("Referer", "https://sail.oakland.edu/PROD/twbkwbis.P_WWWLogin");
		            
		            // Login !
		            response = client.execute(login);
					
		            // Store proper cookies
		            List<Cookie> cookies = client.getCookieStore().getCookies();
		            
		            // Start off assuming logging in failed
		            boolean loggedIn = false;
		            
		            // Check cookies for successful login
		            for (int i = 0; i < cookies.size(); i++) 
		            	if(cookies.get(i).getName().equals("SESSID")) loggedIn = true;
		            
		            // Success?
		            if(loggedIn){
		            
		            	// Consumption of feed
						HTMLParser.parse(response);

						// Execute GET class list page
						HttpGet classList = new HttpGet("https://sail.oakland.edu/PROD/bwskfcls.p_sel_crse_search");
			            classList.setHeader("Referer", "https://sail.oakland.edu/PROD/twbkwbis.P_GenMenu");
						response = client.execute(classList);
						HTMLParser.parse(response);
						
						// Execute GET for course page
						HttpGet coursePage = new HttpGet("https://sail.oakland.edu/PROD/bwckgens.p_proc_term_date?p_calling_proc=P_CrseSearch&p_term=" + term);
						coursePage.setHeader("Referer", "https://sail.oakland.edu/PROD/bwskfcls.p_sel_crse_search"); 
						response = client.execute(coursePage);
						HTMLParser.parse(response);
						
						
						// Download every subject's data
						for(int index = 0; index < SUBJECTS.length; index++){
							
							// Don't download if cancel was pressed
							if(isCancelled()) break;
						
							// Update status, progress bar, then store course
							String subject = SUBJECTS[index];
							status.setText("Downloading " + subject);
							progressBar.setValue(index);
							scheduler.storeDynamicCourse(subject, client, term);
						}
						

						
						// Update course list data
						courseListAll.setListData(scheduler.getCourseIDs().toArray());
						
						// Clear course list data
						String[] empty = {};
						courseListSelected.setListData(empty);
						context.updatePermutations();
						context.updateSchedule();
						
						// Dispose of dialog if cancelled
						if(!isCancelled()){ dispose(); } 
						
		            }
		            
		            // Invalid login?
		            else{
		            	
		            	// Update status
		            	status.setText("Invalid login.");
		            }
				}
			
				// Failed to download?
	            catch(Exception exc){
	            	
	            	// Show stack trace, and update status
					exc.printStackTrace();
					status.setText("Failed downloading.");
				}
				
				return null;
			}
		};

		// Setup window close event to be same as cancel
		this.addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent e) {
                
            	// Cancel all downloads then dispose
            	worker.cancel(true);
            	dispose();
            }
        });
        
		// Make sure dialog is visible
		setLocationRelativeTo(context);
		setVisible(true);
        
	}
	
	/********************************************************************
	 * Method: storeTerms
	 * Purpose: store available terms to use
	/*******************************************************************/
	public void storeTerms(){
		
		try {
			
			// Default terms
			termsName = new ArrayList<String>();
			termsValue = new ArrayList<String>();
			
			// Create client for terms
			DefaultHttpClient client = new DefaultHttpClient();
			HttpGet dynamicGet = new HttpGet("https://sail.oakland.edu/PROD/bwckschd.p_disp_dyn_sched");
			
			// Execute post call
			HttpResponse response = client.execute(dynamicGet);
			Document doc = Jsoup.parse(HTMLParser.parse(response));
			Elements options = doc.getElementsByTag("option");
			
			// Store every option
			for(Element option : options){
				
				// First term option
				if(!option.text().contains("None")){
					
					this.termsName.add(option.text());
					this.termsValue.add(option.val());
				}
			}
			
			//client.close();
		}
		
		// Catch all exceptions
		catch(Exception e){ 
			
			// Print track, set false, return false
			e.printStackTrace();
		}
	}
	

	/********************************************************************
	 * Method: actionPerformed
	 * Purpose: only action that can be performed is to click on login
	/*******************************************************************/
	@Override public void actionPerformed(ActionEvent e) {
						
			worker.execute();
	}

}