package org.pathvisio.tissueanalyzer;
//PathVisio,
//a tool for data visualization and analysis using Biological Pathways
//Copyright 2006-2014 BiGCaT Bioinformatics
//
//Licensed under the Apache License, Version 2.0 (the "License");
//you may not use this file except in compliance with the License.
//You may obtain a copy of the License at
//
//http://www.apache.org/licenses/LICENSE-2.0
//
//Unless required by applicable law or agreed to in writing, software
//distributed under the License is distributed on an "AS IS" BASIS,
//WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//See the License for the specific language governing permissions and
//limitations under the License.
//


import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;

import org.pathvisio.desktop.PvDesktop;
import org.pathvisio.desktop.plugin.Plugin;

/**
* This plugin import data from expression Atlas. Currently only adds menu items
* to import and load the data expression 
* @author Jonathan Melius
*/
public class TissuePlugin implements Plugin
{
	private PvDesktop desktop;
	private TissueSidePanel mySideBarPanel;
	
	@Override
	public void init(final PvDesktop desktop)
	{
		this.desktop = desktop;
		
		mySideBarPanel = new TissueSidePanel (desktop);

		// get a reference to the sidebar
		JTabbedPane sidebarTabbedPane = desktop.getSideBarTabbedPane();

		// add or panel with a given Title
		sidebarTabbedPane.add("Tissues", mySideBarPanel);

		TissueAction action = new TissueAction();
		desktop.registerMenuAction ("Plugins", action);		
	}
	@Override
	public void done() {}
	/**
	 * Import atlas data set and create a new pgex file database from it
	 */
	private class TissueAction extends AbstractAction
	{
		public TissueAction()
		{	
			putValue (NAME, "Tissue Plugin");
			putValue(SHORT_DESCRIPTION, "Test Tissue plugin");
		}

		public void actionPerformed(ActionEvent arg0)
		{				
			if ( desktop.getSwingEngine().getEngine().getActivePathway() == null)
			{
				JOptionPane.showMessageDialog(
						desktop.getFrame(), 
						"Open a pathway");
			}
			else if(!desktop.getSwingEngine().getGdbManager().getCurrentGdb().isConnected())
			{
				JOptionPane.showMessageDialog(
						desktop.getFrame(), 
						"Open a Gene Database");
			}
			else 
			{
				TissueWizard wizard = new TissueWizard(desktop);
				wizard.addObserver(mySideBarPanel);
				wizard.showModalDialog(desktop.getSwingEngine().getFrame());
			}		
		}
	}	
}


