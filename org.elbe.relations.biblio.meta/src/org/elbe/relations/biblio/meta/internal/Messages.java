/*
 This package is part of the Relations application.
 Copyright (C) 2010, Benno Luthiger

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 General Public License for more details.

 You should have received a copy of the GNU General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.elbe.relations.biblio.meta.internal;

import org.eclipse.osgi.util.NLS;

/**
 * Accessor class for the plugins externalized strings.
 *
 * @author Luthiger
 * Created on 21.03.2010
 */
public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.elbe.relations.biblio.meta.internal.messages"; //$NON-NLS-1$
	public static String COinSHelper_lbl_corp;
	public static String COinSHelper_lbl_date;
	public static String COinSHelper_lbl_isbn;
	public static String COinSHelper_lbl_issn;
	public static String COinSHelper_lbl_genre;
	public static String COinSHelper_lbl_ed;
	public static String COinSHelper_lbl_series;
	public static String COinSHelper_lbl_pages;
	public static String COinSHelper_lbl_bici;
	public static String COinSHelper_lbl_number;
	public static String COinSHelper_lbl_sici;
	public static String COinSHelper_lbl_coden;
	public static String COinSHelper_lbl_chronology;
	public static String COinSHelper_lbl_elissn;
	public static String COinSHelper_lbl_season;
	public static String COinSHelper_lbl_quarter;
	public static String COinSHelper_lbl_part;
	public static String RDFaExtractor_lbl_type;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {}

}
