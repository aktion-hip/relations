/***************************************************************************
 * This package is part of Relations application.
 * Copyright (C) 2004-2013, Benno Luthiger
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 ***************************************************************************/
package org.elbe.relations.internal.wizards;

import javax.inject.Inject;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.jface.wizard.Wizard;
import org.elbe.relations.RelationsMessages;
import org.elbe.relations.services.IPrintOut;

/**
 * Wizard to collect the information needed for the content print out.
 * 
 * @author Luthiger
 */
public class PrintOutWizard extends Wizard {
	private PrintOutWizardPage page;

	private IPrintOut printOut;
	private String printOutFileName;
	private int printOutScope;
	private boolean printOutReferences = true;
	private boolean initNew = false;

	@Inject
	private IEclipseContext context;

	/**
	 * PrintOutWizard constructor
	 */
	public PrintOutWizard() {
		setWindowTitle(RelationsMessages.getString("PrintOutWizard.title")); //$NON-NLS-1$
	}

	@Override
	public void addPages() {
		page = ContextInjectionFactory.make(PrintOutWizardPage.class, context);
		addPage(page);
	}

	@Override
	public boolean performFinish() {
		page.saveToHistory();

		printOut = page.getSelectedPrintOut();
		printOutFileName = page.getFileName();
		printOutScope = page.getPrintOutScope();
		printOutReferences = page.getPrintOutReferences();
		initNew = page.isInitNew();

		return true;
	}

	/**
	 * @return {@link IPrintOut} the print out plug-in selected for the print
	 *         out.
	 */
	public IPrintOut getPrintOutPlugin() {
		return printOut;
	}

	/**
	 * @return String the name of the file the selected content should be
	 *         printed out.
	 */
	public String getPrintOutFileName() {
		return printOutFileName;
	}

	/**
	 * @return int constant denoting the scope of items to be printed out
	 * @see PrintOutWizardPage#SELECTED_RELATED etc.
	 */
	public int getPrintOutScope() {
		return printOutScope;
	}

	/**
	 * Returns the value of the printOutReferences checkbox.
	 * 
	 * @return boolean <code>true</code> if checkbox is selected, i.e. the
	 *         item's references have to be printed out.
	 */
	public boolean getPrintOutReferences() {
		return printOutReferences;
	}

	/**
	 * Tells whether the user selected a new print out or chooses to proceed
	 * with a print out already set up.
	 * 
	 * @return boolean <code>true</code> if the user selected the
	 *         <code>PROCESS_NEW</code> process.
	 */
	public boolean isInitNew() {
		return initNew;
	}

	@Override
	public void dispose() {
		page.dispose();
		super.dispose();
	}

}
