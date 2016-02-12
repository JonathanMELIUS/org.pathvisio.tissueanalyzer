package org.pathvisio.tissueanalyzer.utils;

import java.util.ArrayList;
import org.pathvisio.tissueanalyzer.gui.TissueWizard;

/**
* Interface to notify the view about the tissues list's change.
* @author Jonathan Melius
* @see TissueWizard
*/ 

public interface ObservableSidePanel {
	public void addObserver(ObserverSidePanel obs);
	public void notifyObservers(ArrayList<String> progress,ArrayList<String> selected);
	public void delOneObserver(ObserverSidePanel obs);
	public void delAllObservers();
}
