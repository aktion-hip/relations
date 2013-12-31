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
package org.elbe.relations.internal.style;

import java.io.IOException;
import java.io.StringReader;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Stack;
import java.util.Vector;

import javax.xml.parsers.SAXParserFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.Bullet;
import org.eclipse.swt.custom.ST;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.elbe.relations.data.bom.IStyleParser;
import org.hip.kernel.exc.DefaultExceptionWriter;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Singleton class to parse tagged text (stored in the item's text field) and
 * transformes it to styled text to be displayed in a <code>StyledText</code>
 * widget.
 * <p>
 * Use (to set the tagged text to the widget):
 * 
 * <pre>
 * StyleParser.getInstance().parseTagged(tagged_text, styled_text_widget);
 * </pre>
 * 
 * To get the tagged text from the widget:
 * 
 * <pre>
 * String toStore = StyleParser.getInstance().getTagged(styled_text_widget);
 * </pre>
 * 
 * @author Luthiger Created on 03.09.2007
 */
public class StyleParser extends DefaultHandler implements IStyleParser {
	private static StyleParser singleton = null;

	private final static String NL = System.getProperty("line.separator"); //$NON-NLS-1$

	private final static String XML_TEMPL = "<tagged>%s</tagged>"; //$NON-NLS-1$
	private final static String DEFAULT_TEMPL = "<%s>%%s</%s>"; //$NON-NLS-1$
	private final static String INDENT_ATTR = "indent"; //$NON-NLS-1$
	private final static String LIST_EL_TAG = "li"; //$NON-NLS-1$
	private final static String TAG_ROOT = "tagged"; //$NON-NLS-1$

	// Note: entry {"&amp;lt;", "&lt;"} must be the last one!
	private final static String[][] ENTITIES = { { "&", "&amp;" }, //$NON-NLS-1$ //$NON-NLS-2$
	        { "&amp;lt;", "&lt;" } }; //$NON-NLS-1$ //$NON-NLS-2$

	// tags
	private enum InlineTag {
		Bold("b", SWT.BOLD, false), //$NON-NLS-1$
		Italic("i", SWT.ITALIC, false), //$NON-NLS-1$
		Underline("u", SWT.NORMAL, true); //$NON-NLS-1$

		private String name;
		private int swtValue;
		private boolean underline;

		InlineTag(final String inName, final int inSWTValue,
		        final boolean inUnderline) {
			name = inName;
			swtValue = inSWTValue;
			underline = inUnderline;
		}

		public String getName() {
			return name;
		}

		public int getSWT() {
			return swtValue;
		}

		public String getFormat() {
			return String.format(DEFAULT_TEMPL, name, name);
		}
	}

	private static enum ListTag {
		Unordered("ul"), //$NON-NLS-1$
		OrderedNumeric("ol_number"), //$NON-NLS-1$
		OrderedLetterUpper("ol_upper"), //$NON-NLS-1$
		OrderedLetterLower("ol_lower"); //$NON-NLS-1$

		private String name;

		ListTag(final String inName) {
			name = inName;
		}

		public String getName() {
			return name;
		}
	}

	private static final char CR = '\r';
	private static final char LF = '\n';

	private Collection<StyleRangeHelper> ranges;
	private Collection<BulletHelper> bullets;
	private Stack<StyleRangeHelper> styleStack;
	private Stack<BulletHelperFactory> bulletStack;
	private XMLReader parser;

	private StringBuffer text;
	private boolean inListElement;
	private int lineCount = 0;

	private StyleParser() {
		super();
	}

	/**
	 * Returns singleton instance of <code>StyleParser</code>.
	 * 
	 * @return StyleParser
	 */
	public static StyleParser getInstance() {
		if (singleton == null) {
			singleton = new StyleParser();
		}
		return singleton;
	}

	@Override
	public void startDocument() throws SAXException {
		text = new StringBuffer();
		ranges = new Vector<StyleRangeHelper>();
		styleStack = new Stack<StyleRangeHelper>();
		bullets = new Vector<BulletHelper>();
		bulletStack = new Stack<BulletHelperFactory>();
		inListElement = false;
		lineCount = 0;
	}

	@Override
	public void startElement(final String inUri, final String inLocalName,
	        final String inName, final Attributes inAttributes)
	        throws SAXException {
		if (inName.equals(TAG_ROOT))
			return;
		if (inName.equals(LIST_EL_TAG)) {
			inListElement = true;
			final BulletHelperFactory lFactory = bulletStack.peek();
			bullets.add(lFactory.getBulletHelper(lineCount, 1));
			if (text.length() > 0) {
				text.append(NL);
			}
			lineCount++;
			return;
		}

		if (isInlineTag(inName)) {
			handleInlineTag(inName);
		} else {
			handleListTag(inName, inAttributes);
		}
	}

	private void handleListTag(final String inName,
	        final Attributes inAttributes) {
		BulletHelperFactory lFactory = null;
		if (inName.equals(ListTag.Unordered.getName())) {
			lFactory = new SimpleListFactory(ST.BULLET_DOT,
			        getBulletWidth(inAttributes));
		} else if (inName.equals(ListTag.OrderedNumeric.getName())) {
			lFactory = new CustomListFactory(getBulletWidth(inAttributes));
		} else if (inName.equals(ListTag.OrderedLetterUpper.getName())) {
			lFactory = new SimpleListFactory(ST.BULLET_LETTER_UPPER,
			        getBulletWidth(inAttributes));
		} else if (inName.equals(ListTag.OrderedLetterLower.getName())) {
			lFactory = new SimpleListFactory(ST.BULLET_LETTER_LOWER,
			        getBulletWidth(inAttributes));
		}
		bulletStack.push(lFactory);

		final int lLength = text.length();
		if (lLength == 0)
			return;
		if (text.substring(lLength - 1).charAt(0) == LF) {
			text.delete(lLength - 1, lLength);
		}
	}

	private void handleInlineTag(final String inName) {
		StyleRangeHelper lRange = null;
		if (inName.equals(InlineTag.Bold.getName())) {
			lRange = new BoldRange();
		} else if (inName.equals(InlineTag.Italic.getName())) {
			lRange = new ItalicRange();
		} else if (inName.equals(InlineTag.Underline.getName())) {
			lRange = new UnderlineRange();
		}
		styleStack.push(lRange);
	}

	private boolean isInlineTag(final String inName) {
		final InlineTag[] lTags = InlineTag.values();
		for (int i = 0; i < lTags.length; i++) {
			if (lTags[i].getName().equals(inName))
				return true;
		}
		return false;
	}

	private int getBulletWidth(final Attributes inAttributes) {
		final int lIndent = Integer
		        .parseInt(inAttributes.getValue(INDENT_ATTR));
		return Styles.BULLET_WIDTH + lIndent * Styles.INDENT;
	}

	@Override
	public void endElement(final String inUri, final String inLocalName,
	        final String inName) throws SAXException {
		if (inName.equals(TAG_ROOT))
			return;
		if (inName.equals(LIST_EL_TAG)) {
			inListElement = false;
			return;
		}

		if (isInlineTag(inName)) {
			ranges.add(styleStack.pop());
		} else {
			bulletStack.pop();
			if (bulletStack.size() == 0) {
				text.append(NL);
			}
		}
	}

	@Override
	public void characters(final char[] inCharacters, int inStart, int inLength)
	        throws SAXException {
		final int lStartPosition = text.length();

		if (isInList() && startsWithLineFeed(inCharacters, inStart)) {
			inStart++;
			inLength--;
		}
		if (inLength <= 0)
			return;

		final char[] lTarget = new char[inLength];
		System.arraycopy(inCharacters, inStart, lTarget, 0, inLength);
		text.append(lTarget);

		for (final StyleRangeHelper lHelper : styleStack) {
			if (lHelper.getStart() == 0) {
				lHelper.setStart(lStartPosition);
			}
			lHelper.setLength(lHelper.getLength() + inLength);
		}

		// if we're in a list, we handle new lines in the startElement() method.
		if (bulletStack.size() != 0)
			return;
		final int lNewLines = countNewLines(lTarget);
		lineCount += lNewLines;
	}

	private boolean startsWithLineFeed(final char[] inCharacters,
	        final int inStart) {
		return inCharacters[inStart] == LF;
	}

	/**
	 * @return boolean <code>true</code> if characters are in list but not a
	 *         list element, e.g. after the list opening tag but before the
	 *         first list element tag.
	 */
	private boolean isInList() {
		if (bulletStack.size() == 0)
			return false;
		return !inListElement;
	}

	private int countNewLines(final char[] inTarget) {
		int outLines = 0;
		final int lLength = inTarget.length;
		for (int i = 0; i < inTarget.length; i++) {
			final char lChar = inTarget[i];
			if (lChar == CR) {
				if (i + 1 < lLength && (inTarget[i + 1] == LF)) {
					i++;
				}
				outLines++;
			} else if (lChar == LF) {
				outLines++;
			}
		}
		return outLines;
	}

	/**
	 * Parses the specified tagged text and creates styled text that is set to
	 * the provided <code>StyledText</code> widget.
	 * 
	 * @param inTagged
	 *            String
	 * @param inWidget
	 *            StyledText
	 * @throws SAXException
	 * @throws IOException
	 */
	public void parseTagged(final String inTagged, final StyledText inWidget)
	        throws IOException, SAXException {
		doParsing(stripNonValidXML(handleXMLEntities(unescapeBackSlashes(inTagged))));
		applyStyles(inWidget);
	}

	private String handleXMLEntities(final String inTagged) {
		String out = inTagged;
		for (final String[] lEntity : ENTITIES) {
			out = out.replaceAll(lEntity[0], lEntity[1]);
		}
		return out;
	}

	private String stripNonValidXML(final String inTagged) {
		final StringBuilder out = new StringBuilder();
		if (inTagged == null || ("".equals(inTagged))) //$NON-NLS-1$
			return ""; //$NON-NLS-1$

		char lCurrent;
		for (int i = 0; i < inTagged.length(); i++) {
			lCurrent = inTagged.charAt(i);
			if ((lCurrent == 0x9) || (lCurrent == 0xA) || (lCurrent == 0xD)
			        || (lCurrent >= 0x20) && (lCurrent <= 0xD7FF)
			        || (lCurrent >= 0xE000) && (lCurrent <= 0xFFFD)
			        || (lCurrent >= 0x10000) && (lCurrent <= 0x10FFFF)) {
				out.append(lCurrent);
			}
		}
		return new String(out);
	}

	/**
	 * Parses the specified tagged text and returns it all tags removed.
	 * 
	 * @param inTagged
	 *            String
	 * @return String the text without style information.
	 * @throws IOException
	 * @throws SAXException
	 */
	@Override
	public String getUntaggedText(final String inTagged) throws IOException,
	        SAXException {
		doParsing(stripNonValidXML(handleXMLEntities(inTagged)));
		return new String(text);
	}

	private void doParsing(final String inTagged) throws IOException,
	        SAXException {
		final String lXML = String.format(XML_TEMPL, inTagged);
		final StringReader lReader = new StringReader(lXML);
		final InputSource lInputSource = new InputSource(lReader);
		parser().parse(lInputSource);
	}

	/**
	 * Evaluates the specified <code>StyledText</code> and retrieves its styled
	 * text as tagged string that can be stored in the database.
	 * 
	 * @param inWidget
	 *            StyledText
	 * @return String the styled text as tagged string
	 */
	public String getTagged(final StyledText inWidget) {
		List<String> lLines = createLines(inWidget);

		if (inWidget.getStyleRanges().length != 0) {
			lLines = applyInlineTags(lLines, inWidget);
		} else {
			lLines = processLTs(lLines);
		}

		String outTagged = applyBlockTags(lLines, inWidget);
		if (!inWidget.getText().endsWith(NL)) {
			outTagged = outTagged.trim();
		}

		return escapeBackSlashes(outTagged);
	}

	private String escapeBackSlashes(final String inText) {
		return inText.replace("\\", "\\\\"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	private String unescapeBackSlashes(final String inText) {
		return inText.replace("\\\\", "\\"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * Creates a list of text lines.
	 * 
	 * @param inWidget
	 *            StyledText
	 * @return List<String> containing the text each entry one line
	 */
	private List<String> createLines(final StyledText inWidget) {
		final int lLength = inWidget.getLineCount();
		final String lText = inWidget.getText();
		final List<String> outLines = new Vector<String>(lLength);
		int i = 0;
		int lStart = 0;
		while (i < lLength - 1) {
			final int lEnd = inWidget.getOffsetAtLine(i + 1);
			outLines.add(lText.substring(lStart, lEnd));
			lStart = lEnd;
			i++;
		}
		// remaining line
		outLines.add(lText.substring(lStart));
		return outLines;
	}

	private List<String> processLTs(final List<String> inLines) {
		final List<String> outLines = new Vector<String>(inLines.size());
		for (final String lLine : inLines) {
			outLines.add(processLT(lLine));
		}
		return outLines;
	}

	private List<String> applyInlineTags(final List<String> inLines,
	        final StyledText inWidget) {
		final List<String> outLines = new Vector<String>(inLines.size());
		int lStart = 0;
		for (final String lLine : inLines) {
			final int lLength = lLine.length();
			outLines.add(tagLine(lLine, lStart,
			        inWidget.getStyleRanges(lStart, lLength)));
			lStart += lLength;
		}
		return outLines;
	}

	private String tagLine(final String inText, final int inOffset,
	        final StyleRange[] inStyles) {
		int lPosition = 0;
		final StringBuffer outText = new StringBuffer();
		for (int i = 0; i < inStyles.length; i++) {
			final StyleRange lRange = inStyles[i];
			final int lStart = lRange.start - inOffset;

			// before range
			outText.append(processLT(inText.substring(lPosition, lStart)));

			final Tagger lTagger = new Tagger(lRange);
			outText.append(lTagger.getTagged(processLT(inText.substring(lStart,
			        lStart + lRange.length))));
			lPosition = lStart + lRange.length;
		}
		// the tail
		if (lPosition != inText.length()) {
			outText.append(processLT(inText.substring(lPosition)));
		}
		return new String(outText);
	}

	private String processLT(final String inText) {
		return inText.replaceAll("<", "&lt;"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	private String applyBlockTags(final List<String> inLines,
	        final StyledText inWidget) {
		int lLine = 0;
		final int lLength = inWidget.getLineCount();

		final ListSerializer lRoot = new ListSerializer(null, lLine, null);
		ListSerializer lSerializer = lRoot;
		while (lLine < lLength) {
			final Bullet lBullet = inWidget.getLineBullet(lLine);
			if (lBullet != null) {
				lSerializer = lSerializer.processBullet(lBullet, lLine);
			} else {
				lSerializer = lSerializer.consolidate(lLine, lRoot);
			}
			lLine++;
		}
		lSerializer.consolidate(lLength, lRoot);
		return lRoot.render(inLines);
	}

	private void applyStyles(final StyledText inWidget) {
		inWidget.setText(new String(text));
		for (final StyleRangeHelper lRange : ranges) {
			lRange.applyStyle(inWidget);
		}
		bullets = consolidateBullets(bullets);
		for (final BulletHelper lBullet : bullets) {
			lBullet.setLineBullet(inWidget);
		}
	}

	private Collection<BulletHelper> consolidateBullets(
	        final Collection<BulletHelper> inBullets) {
		if (inBullets.size() == 0)
			return inBullets;

		final Collection<BulletHelper> outBullets = new Vector<BulletHelper>();
		final BulletHelper[] lBullets = inBullets
		        .toArray(new BulletHelper[] {});
		int i = 0;
		BulletHelper lCursor = lBullets[i++];
		BulletHelper lNext;
		outBullets.add(lCursor);
		while (i < lBullets.length) {
			lNext = lBullets[i++];
			if (lCursor.bullet == lNext.bullet) {
				lCursor.lineCount += lNext.lineCount;
			} else {
				lCursor = lNext;
				outBullets.add(lCursor);
			}
		}
		return outBullets;
	}

	private XMLReader parser() {
		if (parser == null) {
			try {
				parser = SAXParserFactory.newInstance().newSAXParser()
				        .getXMLReader();
				parser.setContentHandler(this);
				parser.setErrorHandler(this);
			}
			catch (final Exception exc) {
				DefaultExceptionWriter.printOut(this, exc, true);
			}

		}
		return parser;
	}

	private TagTemplate getTagTemplate(final Bullet inBullet) {
		final String template = "<%s %s=\"%%s\">%%s</%s>"; //$NON-NLS-1$
		final String item_template = "<li>%s</li>"; //$NON-NLS-1$

		String list_template = "<ul indent=\"%s\">%s</ul>"; //$NON-NLS-1$
		if ((inBullet.type & ST.BULLET_DOT) != 0) {
			list_template = String.format(template,
			        ListTag.Unordered.getName(), INDENT_ATTR,
			        ListTag.Unordered.getName());
		} else if ((inBullet.type & ST.BULLET_CUSTOM) != 0) {
			list_template = String.format(template,
			        ListTag.OrderedNumeric.getName(), INDENT_ATTR,
			        ListTag.OrderedNumeric.getName());
		} else if ((inBullet.type & ST.BULLET_LETTER_UPPER) != 0) {
			list_template = String.format(template,
			        ListTag.OrderedLetterUpper.getName(), INDENT_ATTR,
			        ListTag.OrderedLetterUpper.getName());
		} else if ((inBullet.type & ST.BULLET_LETTER_LOWER) != 0) {
			list_template = String.format(template,
			        ListTag.OrderedLetterLower.getName(), INDENT_ATTR,
			        ListTag.OrderedLetterLower.getName());
		}

		return new TagTemplate(list_template, item_template);
	}

	/**
	 * Returns a collection containing the names of all list tags.
	 * 
	 * @return Collection<String> of names of <code>ListTag</code>s.
	 */
	public static Collection<String> getListTags() {
		final Collection<String> outListTags = new Vector<String>();
		for (final ListTag lListTag : ListTag.values()) {
			outListTags.add(lListTag.getName());
		}
		return outListTags;
	}

	// --- private classes ---

	private interface StyleRangeHelper {
		void setStart(int inStart);

		void setLength(int inLength);

		int getStart();

		int getLength();

		void applyStyle(StyledText inWidget);
	}

	private abstract class RangeHelper {
		private int length;
		private int start;

		protected StyleRange getInitialRange() {
			final StyleRange outRange = new StyleRange();
			outRange.start = start;
			outRange.length = length;
			return outRange;
		}

		// protected StyleRange getDefaultRange() {
		// StyleRange outRange = new StyleRange();
		// outRange.start = 0;
		// outRange.length = 0;
		// outRange.metrics = new GlyphMetrics(10, 1, 17);
		// return outRange;
		// }
		protected void applyInlineStyle(final StyledText inWidget,
		        final int inStyle, final boolean inUnderline) {
			final StyleRange lInitRange = getInitialRange();
			final StyleRange[] lRanges = inWidget.getStyleRanges(
			        lInitRange.start, lInitRange.length);
			if (lRanges == null || lRanges.length == 0) {
				lInitRange.fontStyle = inStyle;
				lInitRange.underline = inUnderline;
				inWidget.setStyleRange(lInitRange);
			} else {
				for (final StyleRange lRange : lRanges) {
					lRange.fontStyle |= inStyle;
					lRange.underline = inUnderline;
					inWidget.setStyleRange(lRange);
				}
			}
		}

		public int getLength() {
			return length;
		}

		public int getStart() {
			return start;
		}

		public void setLength(final int inLength) {
			length = inLength;
		}

		public void setStart(final int inStart) {
			start = inStart;
		}
	}

	private class BoldRange extends RangeHelper implements StyleRangeHelper {
		@Override
		public void applyStyle(final StyledText inWidget) {
			applyInlineStyle(inWidget, SWT.BOLD, false);
		}
	}

	private class ItalicRange extends RangeHelper implements StyleRangeHelper {
		@Override
		public void applyStyle(final StyledText inWidget) {
			applyInlineStyle(inWidget, SWT.ITALIC, false);
		}
	}

	private class UnderlineRange extends RangeHelper implements
	        StyleRangeHelper {
		@Override
		public void applyStyle(final StyledText inWidget) {
			applyInlineStyle(inWidget, SWT.NORMAL, true);
		}
	}

	private class BulletHelper {
		public int startLine;
		public int lineCount;
		public Bullet bullet;

		public BulletHelper(final int inStart, final int inLength,
		        final Bullet inBullet) {
			startLine = inStart;
			lineCount = inLength;
			bullet = inBullet;
		}

		public void setLineBullet(final StyledText inWidget) {
			inWidget.setLineBullet(startLine, lineCount, bullet);
		}

		@Override
		public String toString() {
			return String
			        .format("BulletHelper type: %s, start: %s, line count: %s", bullet.type, startLine, lineCount); //$NON-NLS-1$
		}
	}

	private class CustomBulletHelper extends BulletHelper {
		public CustomBulletHelper(final int inStart, final int inLength,
		        final Bullet inBullet) {
			super(inStart, inLength, inBullet);
		}

		@Override
		public void setLineBullet(final StyledText inWidget) {
			inWidget.setLineBullet(startLine, lineCount, bullet);
			inWidget.addPaintObjectListener(Styles
			        .getPaintObjectListener(inWidget));
		}
	}

	private interface BulletHelperFactory {
		BulletHelper getBulletHelper(int inStart, int inLength);
	}

	private abstract class HelperFactory implements BulletHelperFactory {
		protected Bullet bullet;

		public HelperFactory(final Bullet inBullet) {
			bullet = inBullet;
		}

		@Override
		public BulletHelper getBulletHelper(final int inStart,
		        final int inLength) {
			return new BulletHelper(inStart, inLength, bullet);
		}
	}

	private class SimpleListFactory extends HelperFactory implements
	        BulletHelperFactory {
		public SimpleListFactory(final int inBulletStyle, final int inWidth) {
			super(Styles.getBullet(inBulletStyle, inWidth));
		}
	}

	private class CustomListFactory extends HelperFactory implements
	        BulletHelperFactory {
		public CustomListFactory(final int inWidth) {
			super(Styles.getBullet(ST.BULLET_CUSTOM, inWidth));
		}

		@Override
		public BulletHelper getBulletHelper(final int inStart,
		        final int inLength) {
			return new CustomBulletHelper(inStart, inLength, bullet);
		}
	}

	private class Tagger {
		private final Collection<InlineTag> lStyles = new Vector<InlineTag>();

		public Tagger(final StyleRange inRange) {
			final InlineTag[] lTags = InlineTag.values();
			for (int i = 0; i < lTags.length; i++) {
				if ((inRange.fontStyle & lTags[i].getSWT()) != 0) {
					lStyles.add(lTags[i]);
				}
				if (inRange.underline && lTags[i].underline) {
					lStyles.add(lTags[i]);
				}
			}
		}

		public String getTagged(final String inToTag) {
			String outTagged = inToTag;
			for (final InlineTag lTag : lStyles) {
				outTagged = String.format(lTag.getFormat(), outTagged);
			}
			return outTagged;
		}
	}

	private class TagTemplate {
		String item_template;
		String list_template;

		public TagTemplate(final String inListTemplate,
		        final String inItemTemplate) {
			list_template = inListTemplate;
			item_template = inItemTemplate;
		}

		public String getListTemplate() {
			return list_template;
		}

		public String getItemTemplate() {
			return item_template;
		}
	}

	private class ListSerializer implements Comparable<ListSerializer> {
		private final Vector<LineSerializer> specialLines = new Vector<LineSerializer>();
		private final Bullet bullet;
		public int startLine;
		public int endLine = -1;
		private final ListSerializer parent;
		private TagTemplate tagTemplate = new TagTemplate("%s", "%s"); //$NON-NLS-1$ //$NON-NLS-2$

		public ListSerializer(final Bullet inBullet, final int inStart,
		        final ListSerializer inParent) {
			bullet = inBullet;
			startLine = inStart;
			parent = inParent;
			if (inBullet != null) {
				tagTemplate = getTagTemplate(inBullet);
			}
		};

		/**
		 * Processed the specified bullet. Check's whether the specified bullet
		 * belongs to the acutal list, the parent list or a new sublist.
		 * 
		 * @param inBullet
		 *            Bullet
		 * @param inLine
		 *            int line index
		 * @return BulletSerializer either the actual, the parent or a new
		 *         sublist's serializer.
		 */
		public ListSerializer processBullet(final Bullet inBullet,
		        final int inLine) {
			// we're still in the same list
			if (inBullet == bullet)
				return this;

			// we returned to the parent list
			final ListSerializer lAncestor = getAncestor(inBullet);
			if (lAncestor != null) {
				return consolidate(inLine, lAncestor);
			}

			// create new list or sublist
			endLine = inLine; // at this line, we switched to another bullet
			return new ListSerializer(inBullet, inLine, this);
		}

		public ListSerializer getAncestor(final Bullet inBullet) {
			if (isRoot())
				return null;
			if (inBullet == parent.bullet)
				return parent;
			return parent.getAncestor(inBullet);
		}

		/**
		 * Consolidates pending lists. This method is called when a line with no
		 * bullet is encountered. The aim of this method is to set the lists
		 * endLine variable for that the serializer knows at which line the list
		 * ends.
		 * 
		 * @param inLine
		 *            the index of the actual line without bullet.
		 * @param inAncestor
		 *            BulletSerializer
		 * @return BulletSerializer the root serializer
		 */
		public ListSerializer consolidate(final int inLine,
		        final ListSerializer inAncestor) {
			endLine = inLine;
			if (isRoot()) {
				return this;
			}
			if (inAncestor == this)
				return this;

			inAncestor.addChild(this);
			return parent.consolidate(startLine, inAncestor);
		}

		public void addChild(final ListSerializer inSerializer) {
			// do we have a special line yet?
			LineSerializer lLine = searchSpecial(endLine);
			if (lLine == null) {
				lLine = new LineSerializer(endLine,
				        tagTemplate.getItemTemplate());
				specialLines.add(lLine);
			}
			lLine.addSublist(inSerializer);
		}

		private LineSerializer searchSpecial(final int inLine) {
			for (final LineSerializer lLine : specialLines) {
				if (lLine.line == inLine)
					return lLine;
			}
			return null;
		}

		private boolean isRoot() {
			return parent == null;
		}

		public String render(final List<String> inLines) {
			final StringBuffer lRendered = new StringBuffer();

			// short cut for root list only
			if ((specialLines.size() == 0) && isRoot()) {
				for (final String lLine : inLines) {
					lRendered.append(lLine);
				}
				return finish(lRendered);
			}

			// render the lines up to a special line and this line (including
			// the sublists)
			int i = startLine;
			for (final LineSerializer lLineWithSublist : specialLines) {
				lRendered.append(renderToChild(i, lLineWithSublist.line,
				        inLines));
				lRendered.append(lLineWithSublist.render(inLines));
				i = lLineWithSublist.getEndLine();
			}

			// render the lines from the last child to the end
			while (i < endLine) {
				lRendered.append(
				        String.format(tagTemplate.getItemTemplate(), inLines
				                .get(i++).trim())).append(NL);
			}
			return finish(lRendered);
		}

		private StringBuffer renderToChild(final int inStart, final int inEnd,
		        final List<String> inLines) {
			final StringBuffer outRendered = new StringBuffer();
			int i = inStart;
			while (i < inEnd - 1) {
				outRendered.append(
				        String.format(tagTemplate.getItemTemplate(), inLines
				                .get(i).trim())).append(NL);
				i++;
			}
			return outRendered;
		}

		private String finish(final StringBuffer inRendered) {
			final String outRendered = new String(inRendered);
			if (isRoot()) {
				return outRendered;
			}
			return String.format(tagTemplate.getListTemplate(),
			        computeIndent(), outRendered);
		}

		private int computeIndent() {
			final int lWidth = bullet.style.metrics.width;
			return (lWidth - Styles.BULLET_WIDTH) / Styles.INDENT;
		}

		@Override
		public int compareTo(final ListSerializer inList) {
			return startLine - inList.startLine;
		}

		@Override
		public String toString() {
			if (isRoot())
				return "<root/>"; //$NON-NLS-1$
			return String.format(tagTemplate.getListTemplate(),
			        computeIndent(), "*"); //$NON-NLS-1$
		}
	}

	private class LineSerializer {
		private final Vector<ListSerializer> subLists = new Vector<ListSerializer>();
		public int line;
		private String item_template = "%s"; //$NON-NLS-1$

		public LineSerializer(final int inLine, final String inItemTemplate) {
			line = inLine;
			item_template = inItemTemplate;
		}

		public void addSublist(final ListSerializer inList) {
			subLists.add(inList);
		}

		public String render(final List<String> inLines) {
			Collections.sort(subLists);
			final StringBuffer lRendered = new StringBuffer();
			for (final ListSerializer lSublist : subLists) {
				lRendered.append(lSublist.render(inLines));
			}
			final String outRendered = new String(lRendered);
			if (line == 0)
				return outRendered;
			return String.format(item_template, inLines.get(line - 1)
			        + outRendered);
		}

		public int getEndLine() {
			int outLine = line;
			for (final ListSerializer lSublist : subLists) {
				outLine = Math.max(outLine, lSublist.endLine);
			}
			return outLine;
		}

		@Override
		public String toString() {
			return String.format(item_template, "line " + line); //$NON-NLS-1$
		}
	}
}
