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

import javax.swing.*;

import java.awt.*;
import java.awt.event.*;

public class CheckComboBox extends JComboBox {
	private static final long serialVersionUID = 1L;
	MainApplication mainRef;
	
	public CheckComboBox(JCheckBox[] boxes, MainApplication mainApplication) { 
	
		// Setup combo box
		super(boxes); 
		mainRef = mainApplication; 
		setup(); 
	}
	  
	
	private void setup() {
	
		
		// Default renderer
		setRenderer(new Renderer());
	    
		// Regular listener
		addActionListener(new ActionListener() {
	      
			// Catch actions
			public void actionPerformed(ActionEvent ae) { 
	    
				
			  // Re-open checkbox
	          SwingUtilities.invokeLater(new Runnable(){ public void run(){ showPopup(); } });
	    	  
	          // Select item
	          itemSelected();
	          
	          // Set selected item to first in the list (all)
	    	  setSelectedIndex(0);
	    	  boolean allTrue = true;
	    	  
	    	  // Check to see if all are checked?
	    	  for(int index = 1; index < getItemCount(); index++){
	    		  
	    		  if (!((JCheckBox)getItemAt(index)).isSelected()){
	    			allTrue = false;
	    		  }
	    	  }
	
	    	  // Set all checked based on whether or not they are all checked
	    	  ((JCheckBox)getItemAt(0)).setSelected(allTrue);
	    	  
	    	  // Update schedules !
	    	  mainRef.updateSchedule();
	    }
	    });
	  }
	
	
	  // Toggle currently selected item
	  private void itemSelected() {
	
		  // Verify type
		  if (getSelectedItem() instanceof JCheckBox) {
			
			  // Toggle selection
			  JCheckBox j = (JCheckBox)getSelectedItem();
			  j.setSelected(!j.isSelected());
		  }
	  }
	  
	  
	  // Setup renderer
	  class Renderer implements ListCellRenderer {
	    
		    // Default renderer
		    public Renderer() { 
		    	
		    	setOpaque(true); 
		    }
	    
			// Component rendering
			public Component getListCellRendererComponent(JList list, Object value, int index,
					    boolean isSelected, boolean cellHasFocus) {
		  
			
				// When index is 0, return blank label component
				if(index == 0){ return new JLabel("Choose campus(s)"); }
		
				// Get component 
				Component comp = (Component)value;
		
				// If selected, set selection backgorund
				if (isSelected) {
					comp.setBackground(list.getSelectionBackground());
					comp.setForeground(list.getSelectionForeground());
				} 
		
				// Otherwise set regular background
		        else {
		        	comp.setBackground(list.getBackground());
		        	comp.setForeground(list.getForeground());
		        }
		        return comp;
		    }
	  }
}