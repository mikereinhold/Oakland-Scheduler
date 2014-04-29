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
import java.io.Serializable;

/********************************************************************
 * Class: Meeting
 * Purpose: stores information for a single meeting time
/*******************************************************************/
public class Meeting implements Serializable{
	
	// Default serialization
	private static final long serialVersionUID = 1L;
	
	
	Course courseReference;			// reference to parent course
	private String location;		// location of meeting
	private String professor; 		// professor of meeting
	private TimeBlock time;			// time for meeting comparisons
	private String timeStr;			// original time string for displaying
	public boolean M, T, W, R, F;	// days for easy conflict comparisons
	private Color color;			// color of meeting (used in UI)
	private int meetingNumber; 		// index of meeting in its parent's list 
	private String room; 			// room for meeting
	
	/********************************************************************
	 * Constructor: Meeting
	 * Purpose: creates a meeting object out of a given time and days
	/*******************************************************************/
	public Meeting(Course course, String timeStr, String daysStr, 
			String location, String professor, String room, int index){
		
		// Store information
		this.timeStr = timeStr;
		this.courseReference = course;
		this.time = TimeBlock.convertToTimeBlock(timeStr, this);
		this.meetingNumber = index;
		this.color = Color.white;
		this.professor = professor;
		this.location = location;
		this.room = room;
		
		// Store days
		M = daysStr.contains("M");
		T = daysStr.contains("T");
		W = daysStr.contains("W");
		R = daysStr.contains("R");
		F = daysStr.contains("F");
		
	}
	
	/********************************************************************
	 * Accessors
	 * Purpose: access respective private variables
	/*******************************************************************/
	public String getTimeStr(){ return this.timeStr; }
	public TimeBlock getTime(){ return this.time; }
	public String getProfessor(){ return this.professor; }
	public String getLocation(){ return this.location; }
	public Color getColor(){ return this.color; }
	public Course getCourseReference(){ return this.courseReference; }
	public int getMeetingNumber(){ return this.meetingNumber; }
	public String getRoom(){ return this.room; }
	
	/********************************************************************
	 * Method: getDays
	 * Purpose: re-creates days string when needed
	/*******************************************************************/
	public String getDays(){
		
		String days = "";
		
		if(M) days += "M";
		if(T) days += "T";
		if(W) days += "W";
		if(R) days += "R";
		if(F) days += "F";
		
		return days;
	}
	
	/********************************************************************
	 * Mutators
	 * Purpose: access respective private variables
	/*******************************************************************/
	public void setColor(Color color){ this.color = color; }
	
}
