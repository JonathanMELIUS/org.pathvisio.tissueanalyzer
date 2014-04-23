package org.pathvisio.tissueanalyzer.utils;

import java.util.ArrayList;

public interface ObservableSidePanel {
	public void addObserver(ObserverSidePanel obs);
	public void notifyObservers(ArrayList<String> progress,ArrayList<String> selected);
	public void delOneObserver(ObserverSidePanel obs);
	public void delAllObservers();
}
