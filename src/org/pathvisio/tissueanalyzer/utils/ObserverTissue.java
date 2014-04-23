package org.pathvisio.tissueanalyzer.utils;

import java.util.ArrayList;

import org.pathvisio.gexplugin.ImportInformation;

public interface ObserverTissue {
	public void update(ArrayList<String> selected, String exp);
	public void update(ImportInformation importInformation);
}
