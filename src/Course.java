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
import java.util.List;

/********************************************************************
 * Class: Course
 * Purpose: holds all the information for a course
/*******************************************************************/
public class Course implements Serializable {

	// Default serialization
	private static final long serialVersionUID = 1L;
	
	private String courseName;			// name of the course
	private String courseID; 			// id of the course
	private int crn;					// course CRN
	private List<Meeting> meetings;		// meetings for the course
	private double credits;				// credits for the course
	private boolean closed;				// course closed?
	private boolean NR;					// no registration?
	private boolean SR;					// student requirement?
	
	/********************************************************************
	 * Constructor: Course
	 * Purpose: creates a course with default settings
	/*******************************************************************/
	public Course(){
		this.courseID = "";
		this.courseName = "";
		this.crn = 0;
		this.meetings = new ArrayList<Meeting>();
		this.credits = 0;
		this.closed = false;
		this.NR = false;
	}
	
	/********************************************************************
	 * Constructor: Course
	 * Purpose: creates a course with given meetings
	/*******************************************************************/
	public Course(String courseName, String courseID, int crn, double credits, boolean closed, boolean nr, boolean sr){
		
		this.courseName = courseName;
		this.courseID = courseID;
		this.crn = crn;
		this.meetings = new ArrayList<Meeting>();
		this.credits = credits;
		this.closed = closed;
		this.NR = nr;
		this.SR = sr;
	}
	
	
	/********************************************************************
	 * Accessors
	 * Purpose: allows access to private variables
	/*******************************************************************/
	public int getCRN(){ return this.crn; }
	public String getCourseName(){ return this.courseName; }
	public String getCourseID(){ return this.courseID; }
	public List<Meeting> getMeetings(){ return this.meetings; }
	public double getCredits(){ return this.credits; }
	public boolean getClosed(){ return this.closed; }
	public boolean getNR(){ return this.NR; }
	public boolean getSR(){ return this.SR; }
}
