/*
 * JasperReports Modern PDF Exporter
 * Copyright © 2005 - 2018 TIBCO Software Inc.
 * http://www.jaspersoft.com.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.jaspersoft.jasperreports.export.pdf;

import java.awt.font.GlyphVector;
import java.util.Locale;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfDictionary;
import com.itextpdf.text.pdf.PdfName;
import com.itextpdf.text.pdf.PdfObject;
import com.itextpdf.text.pdf.PdfString;

import net.sf.jasperreports.engine.JRRuntimeException;
import net.sf.jasperreports.engine.JasperReportsContext;


/**
 * @author Lucian Chirita (lucianc@users.sourceforge.net)
 */
public class PdfGlyphRenderer extends AbstractPdfTextRenderer
{
	private static final Log log = LogFactory.getLog(PdfGlyphRenderer.class);
	
	private static final boolean PATCHED_ITEXT;
	static
	{
		PATCHED_ITEXT = determinePatchedItext();
	}

	private static boolean determinePatchedItext()
	{
		try
		{
			PdfContentByte.class.getMethod("showText", GlyphVector.class);
			return true;
		} 
		catch (NoSuchMethodException e) 
		{
			log.warn("Unpatched iText found, cannot use glyph rendering");
			return false;
		} 
		catch (SecurityException e) 
		{
			throw new JRRuntimeException(e);
		}
	}
	
	public static boolean supported()
	{
		return PATCHED_ITEXT;
	}
	
	private boolean addActualText;	
	private PdfGlyphGraphics2D pdfGraphics2D;
	
	public PdfGlyphRenderer(JasperReportsContext jasperReportsContext, boolean ignoreMissingFont, 
			boolean addActualText)
	{
		super(jasperReportsContext, ignoreMissingFont);
		this.addActualText = addActualText;
	}


	@Override
	public void render()
	{
		Locale locale = pdfExporter.getTextLocale(text);
		pdfGraphics2D = new PdfGlyphGraphics2D(pdfContentByte, pdfExporter, locale);
		super.render();
		pdfGraphics2D.dispose();
	}
	
	
	@Override
	public void draw()
	{
		if (addActualText)
		{
			PdfDictionary markedContentProps = new PdfDictionary();
			markedContentProps.put(PdfName.ACTUALTEXT, new PdfString(allText, PdfObject.TEXT_UNICODE));
			pdfContentByte.beginMarkedContentSequence(PdfName.SPAN, markedContentProps, true);
		}
		
		TabSegment segment = segments.get(segmentIndex);
		segment.layout.draw(
				pdfGraphics2D,
				x + drawPosX,// + leftPadding,
				y + topPadding + verticalAlignOffset + drawPosY
				);
		
		if (addActualText)
		{
			pdfContentByte.endMarkedContentSequence();
		}
		
		return;
	}
	

}
