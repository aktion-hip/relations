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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.SimpleTimeZone;
import java.util.TimeZone;

import org.elbe.relations.biblio.meta.internal.pdf.cos.COSString;

/**
 * This class is used to convert dates to strings and back using the PDF
 * date standards.  Date are described in PDFReference1.4 section 3.8.2
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version copied from org.apache.pdfbox (1.0.0)
 */
public class DateConverter {
	private static final SimpleDateFormat PDF_DATE_FORMAT = new SimpleDateFormat("yyyyMMddHHmmss"); //$NON-NLS-1$

	//The Date format is supposed to be the PDF_DATE_FORMAT, but not all PDF documents
    //will use that date, so I have added a couple other potential formats
    //to try if the original one does not work.
    private static final SimpleDateFormat[] POTENTIAL_FORMATS = new SimpleDateFormat[] {
        new SimpleDateFormat("EEEE, dd MMM yyyy hh:mm:ss a"), //$NON-NLS-1$
        new SimpleDateFormat("EEEE, MMM dd, yyyy hh:mm:ss a"), //$NON-NLS-1$
        new SimpleDateFormat("MM/dd/yyyy hh:mm:ss"), //$NON-NLS-1$
        new SimpleDateFormat("MM/dd/yyyy")}; //$NON-NLS-1$

    private DateConverter() {}

    /**
     * This will convert a string to a calendar.
     *
     * @param date The string representation of the calendar.
     * @return The calendar that this string represents.
     * @throws IOException If the date string is not in the correct format.
     */
	public static Calendar toCalendar(COSString inDate) throws IOException {
        Calendar outValue = null;
        if (inDate != null) {
            outValue = toCalendar(inDate.getString());
        }
        return outValue;
	}

    /**
     * This will convert a string to a calendar.
     *
     * @param inDate The string representation of the calendar.
     * @return The calendar that this string represents.
     * @throws IOException If the date string is not in the correct format.
     */
	public static Calendar toCalendar(String inDate) throws IOException {
        Calendar outValue = null;
        if (inDate != null && inDate.trim().length() > 0) {
            //these are the default values
            int lYear = 0;
            int lMonth = 1;
            int lDay = 1;
            int lHour = 0;
            int lMinute = 0;
            int lSecond = 0;
            //first string off the prefix if it exists
            try {
                SimpleTimeZone lZone = null;
                if (inDate.startsWith( "D:" ) ) { //$NON-NLS-1$
                    inDate = inDate.substring( 2, inDate.length() );
                }
                if (inDate.length() < 4 ) {
                    throw new IOException( "Error: Invalid date format '" + inDate + "'" ); //$NON-NLS-1$ //$NON-NLS-2$
                }
                lYear = Integer.parseInt( inDate.substring( 0, 4 ) );
                if (inDate.length() >= 6 ) {
                    lMonth = Integer.parseInt( inDate.substring( 4, 6 ) );
                }
                if (inDate.length() >= 8 ) {
                    lDay = Integer.parseInt( inDate.substring( 6, 8 ) );
                }
                if (inDate.length() >= 10 ) {
                    lHour = Integer.parseInt( inDate.substring( 8, 10 ) );
                }
                if (inDate.length() >= 12 ) {
                    lMinute = Integer.parseInt( inDate.substring( 10, 12 ) );
                }
                if (inDate.length() >= 14 ) {
                    lSecond = Integer.parseInt( inDate.substring( 12, 14 ) );
                }

                if (inDate.length() >= 15 ) {
                    char lSign = inDate.charAt( 14 );
                    if (lSign == 'Z' ) {
                        lZone = new SimpleTimeZone(0,"Unknown"); //$NON-NLS-1$
                    }
                    else {
                        int lHours = 0;
                        int lMinutes = 0;
                        if (inDate.length() >= 17 ) {
                            if (lSign == '+' ) {
                                //parseInt cannot handle the + sign
                                lHours = Integer.parseInt( inDate.substring( 15, 17 ) );
                            }
                            else {
                                lHours = -Integer.parseInt( inDate.substring( 14, 16 ) );
                            }
                        }
                        if (inDate.length() > 20 ) {
                            lMinutes = Integer.parseInt( inDate.substring( 18, 20 ) );
                        }
                        lZone = new SimpleTimeZone( lHours*60*60*1000 + lMinutes*60*1000, "Unknown" ); //$NON-NLS-1$
                    }
                }
                if (lZone != null ) {
                    outValue = new GregorianCalendar( lZone );
                }
                else {
                    outValue = new GregorianCalendar();
                }

                outValue.set(lYear, lMonth-1, lDay, lHour, lMinute, lSecond);
                // PDFBOX-598: PDF dates are only accurate up to a second
                outValue.set(Calendar.MILLISECOND, 0);
            }
            catch (NumberFormatException exc) {
                for (int i=0; outValue == null && i<POTENTIAL_FORMATS.length; i++) {
                    try {
                        Date lUtilDate = POTENTIAL_FORMATS[i].parse( inDate );
                        outValue = new GregorianCalendar();
                        outValue.setTime(lUtilDate);
                    }
                    catch (ParseException pexc) {
                        //ignore and move to next potential format
                    }
                }
                if (outValue == null) {
                    //we didn't find a valid date format so throw an exception
                    throw new IOException( "Error converting date:" + inDate ); //$NON-NLS-1$
                }
            }
        }
        return outValue;
	}

    /**
     * This will convert the calendar to a string.
     *
     * @param inDate The date to convert to a string.
     * @return The date as a String to be used in a PDF document.
     */
    public static String toString(Calendar inDate) {
        String outValue = null;
        if (inDate != null) {
            StringBuilder lBuffer = new StringBuilder();
            TimeZone lZone = inDate.getTimeZone();
            long lOffsetInMinutes = lZone.getOffset( inDate.getTimeInMillis() )/1000/60;
            long lHours = Math.abs( lOffsetInMinutes/60 );
            long lMinutes = Math.abs( lOffsetInMinutes%60 );
            lBuffer.append( "D:" ); //$NON-NLS-1$
            lBuffer.append( PDF_DATE_FORMAT.format( inDate.getTime() ) );
            if (lOffsetInMinutes == 0 ) {
                lBuffer.append( "Z" ); //$NON-NLS-1$
            }
            else if( lOffsetInMinutes < 0 ) {
                lBuffer.append( "-" ); //$NON-NLS-1$
            }
            else {
                lBuffer.append( "+" ); //$NON-NLS-1$
            }
            if (lHours < 10 ) {
                lBuffer.append( "0" ); //$NON-NLS-1$
            }
            lBuffer.append( lHours );
            lBuffer.append( "'" ); //$NON-NLS-1$
            if( lMinutes < 10 ) {
                lBuffer.append( "0" ); //$NON-NLS-1$
            }
            lBuffer.append(lMinutes );
            lBuffer.append("'" ); //$NON-NLS-1$
            outValue = lBuffer.toString();
        }
        return outValue;
    }

}
