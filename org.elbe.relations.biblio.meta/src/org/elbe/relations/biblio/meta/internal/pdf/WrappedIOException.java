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
package org.elbe.relations.biblio.meta.internal.pdf;

import java.io.IOException;

/**
 * An simple class that allows a sub exception to be stored.
 * 
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version copied from org.apache.pdfbox (1.0.0)
 */
@SuppressWarnings("serial")
public class WrappedIOException extends IOException {
	/**
	 * constructor comment.
	 * 
	 * @param exc
	 *            The root exception that caused this exception.
	 */
	public WrappedIOException(final Throwable exc) {
		initCause(exc);
	}

	/**
	 * constructor comment.
	 * 
	 * @param inMessage
	 *            Descriptive text for the exception.
	 * @param exc
	 *            The root exception that caused this exception.
	 */
	public WrappedIOException(final String inMessage, final Throwable exc) {
		super(inMessage);
		initCause(exc);
	}

}
