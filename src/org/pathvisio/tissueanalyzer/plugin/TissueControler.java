package org.pathvisio.tissueanalyzer.plugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.ButtonGroup;
import javax.swing.JTextField;

import org.pathvisio.core.util.FileUtils;

/**
* Choose the way to query the Expression Atlas human tissues datasets.
* @author Jonathan Melius
*/

public class TissueControler {
	private AbstractAnalyser analyser;

	public TissueControler(AbstractAnalyser tissueanalyser){
		this.analyser = tissueanalyser;
	}

	public void control(String exp, String txtOutput){
		String outFile = null;
		File f = new File(txtOutput);
		try {
			f.getCanonicalPath();
			f=FileUtils.replaceExtension(f, "pgex");
			outFile = f.getCanonicalPath();
		} catch (IOException e) {
			e.printStackTrace();
		}
		analyser.settings(exp,outFile);
	}

	public void queryTissuesList(String experiment){		
		analyser.queryTissuesList(experiment);		
	}

	public void query(ArrayList<String> selectedTissues, String cutoff){
		analyser.setCutoff(cutoff);
		analyser.setSelectedTissues(selectedTissues);
		analyser.queryExperiment();
	}
	
	public void setAbstractAnalyser(AbstractAnalyser analyser){
		this.analyser=analyser;
	}
}
