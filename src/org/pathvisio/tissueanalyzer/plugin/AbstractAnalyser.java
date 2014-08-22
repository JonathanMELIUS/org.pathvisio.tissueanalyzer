package org.pathvisio.tissueanalyzer.plugin;

import java.util.ArrayList;

import org.pathvisio.gexplugin.ImportInformation;
import org.pathvisio.tissueanalyzer.utils.ObservableTissue;
import org.pathvisio.tissueanalyzer.utils.ObserverTissue;

/**
* Abstract class to query Expression Atlas human tissues dataset.
* Retrieve the list of tissues and the dataset.
* @author Jonathan Melius
* @see ObservableTissue
*/
public abstract class AbstractAnalyser implements ObservableTissue {

	protected ImportInformation importInformation;
	protected String cutoff;
	protected ArrayList<String> selectedTissues;
	protected String experiment;
	protected final String gene_ID = "Gene ID";
	protected final String gene_Name = "Gene Name";
	protected ArrayList<ObserverTissue> observers;

	public AbstractAnalyser() {
		importInformation = new ImportInformation();
		observers = new ArrayList<ObserverTissue>();
	}

	public abstract void queryExperiment();

	/**
	 * returns the list of tissues from the experiment
	 */
	public abstract void queryTissuesList(String experiment);

	public abstract void settings(String experiment, String outFile);

	public ArrayList<String> getSelectedTissues() {
		return selectedTissues;
	}

	public void setSelectedTissues(ArrayList<String> selectedTissues) {
		this.selectedTissues = selectedTissues;
	}

	public String getCutoff() {
		return cutoff;
	}

	public void setCutoff(String cutoff) {
		this.cutoff = cutoff;
	}

	@Override
	public void addObserver(ObserverTissue obs) {
		// TODO Auto-generated method stub
		observers.add(obs);
	}

	@Override
	public void notifyObservers(ArrayList<String> listOfTissues, String exp) {
		// TODO Auto-generated method stub
		for (ObserverTissue obs : observers){
			obs.update(listOfTissues,exp);
		}
	}

	@Override
	public void notifyObservers(ImportInformation importInformation) {
		// TODO Auto-generated method stub
		for (ObserverTissue obs : observers){
			obs.update(importInformation);
		}
	}

	@Override
	public void delOneObserver(ObserverTissue obs) {
		// TODO Auto-generated method stub
		observers.remove(obs);
	}

	@Override
	public void delAllObservers() {
		// TODO Auto-generated method stub
		
	}

	public void querySelect() {
		// TODO Auto-generated method stub
		
	}

}