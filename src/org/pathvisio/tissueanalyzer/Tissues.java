package org.pathvisio.tissueanalyzer;
// PathVisio,
// a tool for data visualization and analysis using Biological Pathways
// Copyright 2006-2011 BiGCaT Bioinformatics
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * This class query the atlas experiment that the user chose and retrieve the list of tissues.
 * @author Jonathan Melius
 */
public class Tissues {
	private URL url;
	private final String gene_ID = "Gene ID";
	private final String gene_Name = "Gene Name";
	private ArrayList<String> tissuesList;
	
	/**
	 * During construction, the url will be initialized with the chosen experiment.
	 * Then proceed to the extract of the list of tissues.
	 */
	Tissues(String experiment){
		try { 
			url = new URL ("http://www.ebi.ac.uk/gxa/experiments/"
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
	}
	/**
	 * returns the list of tissues from the experiment
	 */
	public ArrayList<String> getTissuesList() {
		return tissuesList;
	}
}
