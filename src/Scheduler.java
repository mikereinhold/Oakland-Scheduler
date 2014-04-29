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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.jsoup.Jsoup;
import org.jsoup.select.Elements;


/********************************************************************
 * Class: Scheduler
 * Purpose: holds dynamic courses and related functions
/*******************************************************************/
public class Scheduler implements Serializable {

	private static final long serialVersionUID = 1L;	// serialization version
	private Map<String, List<Course>> catalog;			// full mapping of all courses
	private List<String> courseIDs;						// course ID list
	private List<String> currentCourseList; 			// current selected course
	private boolean loaded; 							// currently loaded latest information
	private boolean exception; 							// whether or not an exception occurred
	private String termIn;								// term currently in
	
	/********************************************************************
	 * Constructor: Scheduler
	 * Purpose: default constructor for scheduler object
	/*******************************************************************/
	public Scheduler(){
		
		// Defaults
		this.catalog = new HashMap<String, List<Course>>();
		this.courseIDs = new ArrayList<String>();
		this.currentCourseList = new ArrayList<String>();
		MainApplication.courseChecks = new HashMap<String, Boolean>();
		this.termIn = "";
	}
	
	/********************************************************************
	 * Method: resetCourses
	 * Purpose: resets all courses to be blank for loading of new terms
	/*******************************************************************/
	public void resetCourses(){
		
		// Defaults
		this.catalog = new HashMap<String, List<Course>>();
		this.courseIDs = new ArrayList<String>();
		this.currentCourseList = new ArrayList<String>();
		MainApplication.courseChecks = new HashMap<String, Boolean>();
		this.termIn = "";
	}
	
	/********************************************************************
	 * Method: storeDynamicCourses()
	 * Purpose: store all dynamic courses to memory
	/*******************************************************************/
	public boolean storeDynamicCourse(String coursePrefix, DefaultHttpClient client, String term){
		
		try {

				// Debug message
				//System.out.println("Storing " + coursePrefix);

				this.termIn = term;
				
				// Parameters for GET call
				String params = "?rsts=dummy&crn=dummy&term_in=" + term 
						+ "&sel_subj=dummy&sel_day=dummy&sel_schd=dummy&sel_insm=dummy&sel_camp=dummy"
						+ "&sel_levl=dummy&sel_sess=dummy&sel_instr=dummy&sel_ptrm=dummy&sel_attr=dummy"
						+ "&sel_subj=" + coursePrefix + "&sel_crse=&sel_title=&sel_schd=%25&sel_from_cred=&sel_to_cred="
						+ "&sel_camp=%25&sel_levl=%25&sel_ptrm=%25&sel_instr=%25&sel_attr=%25&begin_hh=0&"
						+ "begin_mi=0&begin_ap=a&end_hh=0&end_mi=0&end_ap=a&SUB_BTN=Section+Search&path=1";
				

				// Create GET call with correct referer
				HttpGet courseSections = new HttpGet("https://sail.oakland.edu/PROD/bwskfcls.P_GetCrse_Advanced" + params);
				courseSections.setHeader("Referer", "https://sail.oakland.edu/PROD/bwskfcls.P_GetCrse");
				
				// Execute
				HttpResponse response = client.execute(courseSections);
			
			
				// Get main container of courses
				Elements mainContainer = Jsoup.parse(response.getEntity().getContent(), "UTF-8", "https://sail.oakland.edu").getElementsByClass("datadisplaytable");
				
				// Valid container ?
				if(mainContainer.size() > 0 && mainContainer.get(0).getElementsByTag("tbody").size() > 0){
						
					// Get courses offered for subject
					Elements coursesOffered = mainContainer.get(0).getElementsByTag("tbody").get(0).children();
					
					// Remove first two rows (headers)
					if(coursesOffered.size() > 0) coursesOffered.remove(0);
					if(coursesOffered.size() > 0) coursesOffered.remove(0);

					// Properties
					String courseName = "", courseID = "", instructor = ""; 
					String location = "", time = "", days = ""; 
					boolean closed = false, nr = false, sr = false;
					double credits = 0; int crn = 0;
					Course currentCourse = new Course();
					String room = "";

					// Go through all courses offered
					for(int j = 0; j < coursesOffered.size(); j++) {
					
						boolean courseFailed = false;
						
						// Get all sections
						Elements e = coursesOffered.get(j).getElementsByTag("td");

						// Correctly formatted line ?
						if(e.size() == 20 
							&& !e.get(1).text().replace("\u00a0","").equals("")
							&& !e.get(2).text().replace("\u00a0","").equals("")
							&& !e.get(3).text().replace("\u00a0","").equals("")
							&& !e.get(4).text().replace("\u00a0","").equals("")
							&& !e.get(5).text().replace("\u00a0","").equals("")
							&& !e.get(6).text().replace("\u00a0","").equals("")
							&& !e.get(7).text().replace("\u00a0","").equals("")
							&& !e.get(8).text().replace("\u00a0","").equals("")
							&& !e.get(9).text().replace("\u00a0","").equals("")
							&& !e.get(16).text().replace("\u00a0","").equals("")
							&& !e.get(18).text().replace("\u00a0","").equals("")
							&& (e.get(8).text().contains("M")
							|| e.get(8).text().contains("T")
							|| e.get(8).text().contains("W")
							|| e.get(8).text().contains("R")
							|| e.get(8).text().contains("F"))
							&& TimeBlock.isConvertable(e.get(9).text())){
						
							
							// Store information (numbers are based on HTML table)
							courseID = e.get(2).text() + " " + e.get(3).text();
							courseName = e.get(7).text();
							days = e.get(8).text();
							time = e.get(9).text();
							instructor = e.get(16).text();
							room = e.get(18).text();
							closed = e.get(0).text().contains("C");
							nr =  e.get(0).text().contains("NR");
							sr =  e.get(0).text().contains("SR");
							location = e.get(5).text();
							

							try{ 
								
								// Convert CRN and credits
								crn = Integer.parseInt(e.get(1).text());
								credits = Double.parseDouble(e.get(6).text());
							} 
							catch(Exception ex){ 
								
								// Set invalid values
								crn = 0;
								credits = -1;
								courseFailed = true;
							}
							
							
							// Special case for lab
							if(credits == 0.0) courseID += "L";
							
							if (!courseFailed){
								
								// Create course
								currentCourse = new Course(courseName, courseID, crn, credits, closed, nr, sr);

								// Create meeting
								Meeting currentMeeting = new Meeting(currentCourse, time, days, location,instructor, room, 0);
								currentCourse.getMeetings().add(currentMeeting);
								
								
								if(this.catalog.get(courseID) == null){
									
									// Brand new course set
									List<Course> courseSet = new ArrayList<Course>();
									courseSet.add(currentCourse);
									this.catalog.put(courseID, courseSet);
									this.courseIDs.add(courseID);
									
								}
								else{
									
									// Simply add the course
									this.catalog.get(courseID).add(currentCourse);
							}							
						}
						
						// Only meeting information (missing column info)
						else if(e.size() == 20
								&& (e.get(8).text().contains("M")
								|| e.get(8).text().contains("T")
								|| e.get(8).text().contains("W")
								|| e.get(8).text().contains("R")
								|| e.get(8).text().contains("F"))
								&& TimeBlock.isConvertable(e.get(9).text())
								&& currentCourse.getMeetings().size() > 0){
							

							// Update subcourse info
							days = e.get(8).text();
							time = e.get(9).text();
							instructor = e.get(16).text(); 
							room = e.get(18).text();
							
							Meeting currentSub = new Meeting(currentCourse, time,days,location,instructor, room, currentCourse.getMeetings().size());
							currentCourse.getMeetings().add(currentSub);
						}
					}
				}	
			}
			
			// Updated loaded status
			this.loaded = true;
			return true;
		}
		
		// Print all exceptions
		catch(Exception e){ 
			e.printStackTrace(); 
			this.loaded = false; 
			return false; 
		}
	}

	
	/********************************************************************
	 * Method: generatePermutations
	 * Purpose: generates all working permutations for current list (3rd Implementation)
	/*******************************************************************/
	public List<List<Course>> generatePermutations(){
		
		// Try to persuade GC to run
		System.gc();
		Runtime.getRuntime().gc();
		
		try{
		
			// Reset exception
			exception = false;
			
			// Initialize working set
			List<List<Course>> workingSet = new ArrayList<List<Course>>();
			
			// For each course
			for(String courseID : this.currentCourseList){
				
				// Create a new working set
				List<List<Course>> newSet = new ArrayList<List<Course>>();
				
				// For each section in the course
				for(Course course : this.catalog.get(courseID)){
					
					if(workingSet.size() == 0){
						List<Course> cloneSet = new ArrayList<Course>();
						testClasses(new ArrayList<Course>(),course,cloneSet);
						newSet.add(cloneSet);
					}
					else{
						// Check all current workingSets
						for(List<Course> courseSet : workingSet){
							
							List<Course> cloneSet = new ArrayList<Course>();
							
							// If fits into working set then add to newSet
							if(testClasses(courseSet, course, cloneSet)){
								newSet.add(cloneSet);
							}
						}
					}
					if(exception) break;
				}
				
				if(exception) break;
			
				// Update working set
				workingSet = newSet;
			}
			
			// Return working set
			this.exception = false;
			return workingSet;
		}
		catch(java.lang.OutOfMemoryError e){
			this.exception = true;
		}

		return null;
	}
	
	/********************************************************************
	 * Method: testClasses
	 * Purpose: tests if classes work together
	/*******************************************************************/
	public boolean testClasses(List<Course> coursesOrig, Course courseCmpOrig, List<Course> clone){
		
		try{
		
			// Add course
			clone.add(courseCmpOrig);
			
			// Generate a complete list of sub courses
			List<Meeting> courseSubs = new ArrayList<Meeting>();
			for(Course courselist : coursesOrig){
				
				// Add course to cloned list
				clone.add(courselist);
				
				// Add all sub courses
				for(Meeting course : courselist.getMeetings()) courseSubs.add(course);
			}
			
			// Add sub courses from compare course (except for first one)
			for (Meeting courseCmp : courseCmpOrig.getMeetings()){
				
				// Get time
				TimeBlock cmpBlock = courseCmp.getTime();
				if(cmpBlock == null) return false;
				
				// Get time info to minutes
				int startC = cmpBlock.getStartHours() * 60 + cmpBlock.getStartMin();
				int endC = cmpBlock.getEndHours() * 60 + cmpBlock.getEndMin();
				int uniqueCrn = courseCmp.getCourseReference().getCRN()*1000 + courseCmp.getMeetingNumber();
				String crnS = "";
				
				// Check all sub courses against the compare course
				for(Meeting course : courseSubs){	
					
					int currentUniqueCRN = course.getCourseReference().getCRN() * 1000 + course.getMeetingNumber();
					
					// Sort crn string as smallest crn first (for hashing value)
					if (uniqueCrn < currentUniqueCRN){
						
						// Correct order
						crnS = uniqueCrn + (currentUniqueCRN + "");
					}
					else crnS = (currentUniqueCRN + "") + uniqueCrn;
					
					
					// Check if the comparison has already been done
					if(MainApplication.courseChecks.containsKey(crnS)){
						
						// If conflict, then simply return false
						if (!MainApplication.courseChecks.get(crnS)) return false;
					}
					
					// Courses have never been compared
					else {
						
						
						// Get time information for comparison block
						TimeBlock block = course.getTime();
						if(block == null) return false;
						
						// Start for block B
						int startB = block.getStartHours() * 60 + block.getStartMin();
						int endB = block.getEndHours() * 60 + block.getEndMin();
							
						// Times overlap?
						if(startB < endC && endB > startC){	
							
							// Days also over lap?
							if ((course.M && courseCmp.M) || (course.T && courseCmp.T) || (course.W && courseCmp.W) 
									|| (course.R && courseCmp.R) || (course.F && courseCmp.F)){
								
								// Store comparison and return false
								MainApplication.courseChecks.put(crnS, false);
								return false;
							}
							
							else{
								
								// Store comparison as successful
								MainApplication.courseChecks.put(crnS,  true);
							}
						}
						else{
							
							// Store comparison as successful
							MainApplication.courseChecks.put(crnS, true);
						}
					}
				}
			}
			
			
			// At least one course must exist for schedule to be successful
			return coursesOrig.size() > 0;
		}
		catch(java.lang.OutOfMemoryError e){
			exception = true;
			return false;
		}
	}
	
	/********************************************************************
	 * Method: getPreInfo
	 * Purpose: gets the prequisite information for a course
	/*******************************************************************/	
	public String getPreInfo(String courseID){

		String html = "Could not get prerequisite information."; 
		String courseSubject = "";
		String courseNumber = "";
		
		try{
		
			courseSubject = courseID.split(" ")[0];
			courseNumber = courseID.split(" ")[1].replaceAll("L", "");
			
			// Create client
			DefaultHttpClient client = new DefaultHttpClient();
		
			HttpPost termPage = new HttpPost("https://sail.oakland.edu/PROD/bwckctlg.p_display_courses?term_in=" + termIn + "&one_subj=" + courseSubject + "&sel_crse_strt="+ courseNumber + "&sel_crse_end=" + courseNumber + "&sel_subj=&sel_levl=&sel_schd=&sel_coll=&sel_divs=&sel_dept=&sel_attr=");
			termPage.setHeader("Referer", "https://sail.oakland.edu/PROD/twbkwbis.P_WWWLogin");
			HttpResponse response = client.execute(termPage);

			Elements mainContainer = Jsoup.parse(response.getEntity().getContent(), "UTF-8", "https://sail.oakland.edu").getElementsByClass("datadisplaytable");
			
			if(mainContainer.size() > 0 && mainContainer.get(0).getElementsByClass("ntdefault").size() > 0
					&& mainContainer.get(0).getElementsByClass("ntdefault").get(0).textNodes().size() > 0){
				
				// Return first text node (prereq info)
				String infoBlock = mainContainer.get(0).getElementsByClass("ntdefault").get(0).textNodes().get(0).text();
				
				if(infoBlock.contains("Prerequisite: ")){
				
					return infoBlock.split("Prerequisite: ")[infoBlock.split("Prerequisite: ").length - 1]; 
				}
				else if(infoBlock.contains("Prerequisite(s): ")){
					return infoBlock.split("Prerequisite: ")[infoBlock.split("Prerequisite: ").length - 1];
				}				
			}
		}
		catch(Exception e){}
		
		return html;
	}	

	
	/********************************************************************
	 * Accessors
	 * Purpose: get the corresponding data
	/*******************************************************************/
	public Map<String, List<Course>> getCatalog(){ return this.catalog; }
	public List<String> getCourseIDs() { return this.courseIDs; }
	public boolean getLoaded(){ return this.loaded; }
	public List<String> getCurrentCourseList(){ return this.currentCourseList; }
	
}
