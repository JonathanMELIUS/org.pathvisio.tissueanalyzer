package org.pathvisio.tissueanalyzer.utils;

import java.util.ArrayList;

import org.pathvisio.gexplugin.ImportInformation;
import org.pathvisio.tissueanalyzer.plugin.AbstractAnalyser;

/**
* Interface to notify the view about the importation's change.
* @author Jonathan Melius
* @see AbstractAnalyser
*/
public interface ObservableTissue {
	public void addObserver(ObserverTissue obs);
	public void notifyObservers(ArrayList<String> listOfTissues, String exp);
	public void notifyObservers(ImportInformation importInformation);
	public void delOneObserver(ObserverTissue obs);
	public void delAllObservers();

}
