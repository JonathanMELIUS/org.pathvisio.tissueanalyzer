package org.pathvisio.tissueanalyzer.plugin;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.Arrays;

import org.bridgedb.DataSource;
import org.pathvisio.gexplugin.ImportInformation;
import org.pathvisio.tissueanalyzer.utils.ObservableTissue;
import org.pathvisio.tissueanalyzer.utils.ObserverTissue;

/**
* Query Expression Atlas database by the web service.
* Retrieve the list of tissues and the dataset.
* @see AbstractAnalyser
* @author Jonathan Melius
*/
public class TissueAnalyser extends AbstractAnalyser {

	public TissueAnalyser () {
		super();
	}
	
	public TissueAnalyser (ImportInformation importInformation,String experiment) {
		this.importInformation=importInformation;
		this.experiment=experiment;
	}
	
	public void queryExperiment(){
		String organQuery="";
		for (String organ : selectedTissues){
			organ = organ.replaceAll("\\s", "+");
			organQuery += "&queryFactorValues="+organ;
		}	

		URL url = null;
		try {
			url = new URL("http://www.ebi.ac.uk/gxa/experiments/"+
					experiment+".tsv?"+
					"accessKey=&serializedFilterFactors="+
					"&queryFactorType=ORGANISM_PART" +
					"&rootContext=&heatmapMatrixSize=50"+
					"&displayLevels=false"+
					"&displayGeneDistribution=false" +
					"&geneQuery=&exactMatch=true"+ 
					"&_exactMatch=on&_geneSetMatch=on"+
					organQuery+
					"&_queryFactorValues=1" +
					"&specific=true" +
					"&_specific=on" +
					"&cutoff="+cutoff);						

			String tDir = System.getProperty("java.io.tmpdir");
			File filename = File.createTempFile(tDir+"AtlasQuery", ".tmp");
			filename.deleteOnExit();


			ReadableByteChannel rbc = Channels.newChannel(url.openStream());
			FileOutputStream fos = new FileOutputStream(filename);		
			fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);

			DataSource ds = DataSource.getBySystemCode("En");

			importInformation.setTxtFile(filename);
			importInformation.setFirstDataRow(4);
			importInformation.setFirstHeaderRow(3);
			importInformation.guessSettings();
			importInformation.setDelimiter("\t");
			importInformation.setSyscodeFixed(true);
			importInformation.setDataSource(ds);
			importInformation.setIdColumn(0);

			notifyObservers(importInformation);

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * update the list of tissues from the experiment
	 */
	public void queryTissuesList(String experiment) {
		ArrayList<String> tissuesList = new ArrayList<String>();
		try { 
			URL url = new URL ("http://www.ebi.ac.uk/gxa/experiments/"
					+ experiment+".tsv?accessKey=&serializedFilterFactors="
					+ "&queryFactorType=ORGANISM_PART&rootContext="
					+ "&heatmapMatrixSize=50"
					+ "&displayLevels=false&displayGeneDistribution=true"
					+ "&geneQuery=&exactMatch=true&_exactMatch=on"
					+ "&_geneSetMatch=on&_queryFactorValues=1"
					+ "&specific=true&_specific=on&cutoff=10000");

			InputStream is = url.openStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			String line;
			while ((line = br.readLine()) != null )
			{								
				if ( line.contains(gene_ID) ) {
					tissuesList = new ArrayList<String>(Arrays.asList(line.split("\t")));
					tissuesList.remove(gene_Name);
					tissuesList.remove(gene_ID);
				}				
			}
			br.close();
			is.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		notifyObservers(tissuesList,experiment);
	}

	public void settings(String experiment,String outFile){
		importInformation.setGexName(outFile);
		this.experiment=experiment;
		notifyObservers(importInformation);
	}
}
