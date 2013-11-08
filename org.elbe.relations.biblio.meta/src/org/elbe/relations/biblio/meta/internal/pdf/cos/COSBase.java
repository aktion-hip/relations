/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.elbe.relations.biblio.meta.internal.pdf.cos;

import org.elbe.relations.biblio.meta.internal.pdf.filter.FilterManager;

/**
 * The base object that all objects in the PDF document will extend.
 *
 * @author <a href="ben@benlitchfield.com">Ben Litchfield</a>
 * @version copied from org.apache.pdfbox (1.0.0)
 */
public abstract class COSBase implements COSObjectable{

	/**
	 * This will get the filter manager to use to filter streams.
	 *
	 * @return The filter manager.
	 */
	protected FilterManager getFilterManager() {
		//TODO: move this to PDFdocument or something better
		return new FilterManager();
	}

	@Override
	public COSBase getCOSObject() {
		return this;
	}

}
