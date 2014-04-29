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
import java.util.List;

/********************************************************************
 * Class: TimeBlock
 * Purpose: used to compare various time slots
/*******************************************************************/
public class TimeBlock implements Serializable, Comparable<TimeBlock> {
	
	// Default serialization
	private static final long serialVersionUID = 1L;

	// Necessary time variables
	int startHours, startMin, endHours, endMin, 
		length, start, end, startRounded, endRounded;
	
	// Meeting reference
	private Meeting meetingReference;
	
	/********************************************************************
	 * Constructor: TimeBlock
	 * Purpose: creates TimeBlock object with given parameters
	/*******************************************************************/
	public TimeBlock(int startH, int startM, int endH, int endM, Meeting meetingReference){
		
		// Assign variables
		this.startHours = startH;
		this.startMin = startM;
		this.endHours = endH;
		this.endMin = endM;
		this.start = (startH * 60 + startM);
		this.end = (endH * 60 + endM);
		this.length = end - start;
		this.meetingReference = meetingReference;
		
		// Rounding
		if(((start % 15) / 6.0) > 1) this.startRounded = ((start / 15) + 1) * 15;
		else this.startRounded = (start / 15) * 15;
		if(((end % 15) / 6.0) > 1) this.endRounded = ((end / 15) + 1) * 15;
		else this.endRounded = (end / 15) * 15;
		
	}
	
	/********************************************************************
	 * Method: testDay
	 * Purpose: test to see if a TimeBlock fits into a day
	/*******************************************************************/
	public static boolean testDay(List<TimeBlock> day, TimeBlock block){
		
		 // Test conflicts
		 for (TimeBlock currentBlock : day ) if(currentBlock.conflicts(block)) return false;

		 
		 // No conflicts
		 return true;		 
	}

	
	/********************************************************************
	 * Method: convertToTimeBlock
	 * Purpose: converts a time string into a TimeBlock object
	/*******************************************************************/
	public static TimeBlock convertToTimeBlock(String time, Meeting course){
		
		// Properties
		int startHours, startMinutes, endHours, endMinutes;
		String[] splitTime = time.split("-");
		
		
		// Split into correct pieces ?
		if(splitTime.length == 2){
			
			try{

				
				 // Start
				 startHours = Integer.parseInt(splitTime[0].split(":")[0]);
				 startMinutes = Integer.parseInt((splitTime[0].split(":")[1].replaceAll("[^\\d]", "")));
				 
				 // End
				 endHours = Integer.parseInt(splitTime[1].split(":")[0]);
				 endMinutes = Integer.parseInt((splitTime[1].split(":")[1].replaceAll("[^\\d]", "")));
				 
				 
				 // Convert start to military 
				 if(splitTime[0].replaceAll("[^(am|pm)]", "").equals("pm") && startHours < 12) startHours += 12;
				 else if (splitTime[0].replaceAll("[^(am|pm)]", "").equals("am") && startHours == 12) startHours = 0;
				 

				 // Convert end to military 
				 if(splitTime[1].replaceAll("[^(am|pm)]", "").equals("pm") && endHours < 12) endHours += 12;
				 else if (splitTime[1].replaceAll("[^(am|pm)]", "").equals("am") && endHours == 12) endHours = 0;
				 
				 
				// Make a new TimeBlock and return
				return new TimeBlock(startHours, startMinutes, endHours, endMinutes, course);
				
			}
			
			// Return null for fail
			catch(Exception e){ return null; }
		}
		
		// Return null for fail
		else return null;
		
	}

	/********************************************************************
	 * Method: isConvertable
	 * Purpose: figures out if a timeblock is convertable or not
	/*******************************************************************/
	public static boolean isConvertable(String time){
		
		// Properties
		
		String[] splitTime = time.split("-");
		
		
		// Split into correct pieces ?
		if(splitTime.length == 2){
			int startHours, endHours;	
			try{

				
				 // Start
				 startHours = Integer.parseInt(splitTime[0].split(":")[0]);
				 Integer.parseInt((splitTime[0].split(":")[1].replaceAll("[^\\d]", "")));
				 
				 // End
				 endHours = Integer.parseInt(splitTime[1].split(":")[0]);
				 Integer.parseInt((splitTime[1].split(":")[1].replaceAll("[^\\d]", "")));
				 
				 
				 // Convert start to military 
				 if(splitTime[0].replaceAll("[^(am|pm)]", "").equals("pm") && startHours < 12) startHours += 12;
				 else if (splitTime[0].replaceAll("[^(am|pm)]", "").equals("am") && startHours == 12) startHours = 0;
				 

				 // Convert end to military 
				 if(splitTime[1].replaceAll("[^(am|pm)]", "").equals("pm") && endHours < 12) endHours += 12;
				 else if (splitTime[1].replaceAll("[^(am|pm)]", "").equals("am") && endHours == 12) endHours = 0;
				 
				 return true;
			}
			
			// Return false for fail
			catch(Exception e){ return false; }
		}
		
		// Return false for fail
		else return false;
		
	}

	
	/********************************************************************
	 * Method: getRowSpan
	 * Purpose: returns the number of rows a time should span
	/*******************************************************************/
	public int getRowSpan(){
		
		// Calculate length in minutes
		double minuteDif = (this.endHours * 60 + this.endMin)  - (this.startHours * 60 + this.startMin);
	
		// Calculate the rows needed
		int rowCount = (int) ((minuteDif)/5 + .5);
	
		return rowCount;
	}
	
	
	/********************************************************************
	 * Method: compareTo
	 * Purpose: basic compare method to determine natural order
	/*******************************************************************/
	public int compareTo(TimeBlock compareBlock){
		
		// First check hours <
		if (this.startHours < compareBlock.getStartHours()){
			return -1;
		}
		
		// Second check hours > 
		else if(this.startHours > compareBlock.getStartHours()){
			return 1;
		}
		
		// If hours are same, check minutes
		else {
			
			// Check minutes, and return 0 if they are equal
			if (this.startMin < compareBlock.getStartMin()) return -1;
			else if (this.startMin > compareBlock.getStartMin()) return 1;
			else return 0;
		}
	}
	
	/********************************************************************
	 * Method: conflicts
	 * Purpose: checks to see if the given TimeBlock conflicts
	/*******************************************************************/
	public boolean conflicts(TimeBlock block){
		
		// Problems ?
		if(block == null || (this.compareTo(block) <= 0 && this.compareTo(block) >= 0)) return true;
		else if(block == null || (this.compareTo(block) <= 0 && this.compareTo(block) >= 0)) return true;
		
		else return false;
	}
	
	/********************************************************************
	 * Accessors
	 * Purpose: get the corresponding data
	/*******************************************************************/
	public int getStartHours(){ return this.startHours; }
	public int getStartMin(){ return this.startMin; }
	public int getEndHours(){ return this.endHours; }
	public int getEndMin(){ return this.endMin; }
	public int getStart(){ return this.start; }
	public int getEnd(){ return this.end; }
	public int getLength(){ return this.length; }
	public int getStartRounded(){ return this.startRounded; }
	public int getEndRounded(){ return this.endRounded; }
	public Meeting getMeetingReference(){ return this.meetingReference; }
	
	/********************************************************************
	 * Method: toString
	 * Purpose: convert to a readable string
	/*******************************************************************/
	public String toString(){
		return  this.getMeetingReference().getTimeStr();
	}
}
