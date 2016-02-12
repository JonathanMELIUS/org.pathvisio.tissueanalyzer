package org.pathvisio.tissueanalyzer.utils;

import java.util.ArrayList;

import org.pathvisio.tissueanalyzer.gui.TissueSidePanel;

/**
* Interface to update the view about the change of the tissues list.
* @author Jonathan Melius
* @see TissueSidePanel 
*/

public interface ObserverSidePanel {
	public void update(ArrayList<String> progress,ArrayList<String> selected);
}
