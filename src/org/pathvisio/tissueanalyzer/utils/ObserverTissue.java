package org.pathvisio.tissueanalyzer.utils;

import java.util.ArrayList;

import org.pathvisio.gexplugin.ImportInformation;
import org.pathvisio.tissueanalyzer.gui.TissueWizard;

/**
* Interface to update the view about the change of the importation
* @author Jonathan Melius
* @see TissueWizard
*/
public interface ObserverTissue {
	public void update(ArrayList<String> selected, String exp);
	public void update(ImportInformation importInformation);
}
