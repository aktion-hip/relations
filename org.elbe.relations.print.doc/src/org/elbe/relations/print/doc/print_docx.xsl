<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
    xmlns:wpc="http://schemas.microsoft.com/office/word/2010/wordprocessingCanvas"
    xmlns:mc="http://schemas.openxmlformats.org/markup-compatibility/2006"
    xmlns:o="urn:schemas-microsoft-com:office:office"
    xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships"
    xmlns:m="http://schemas.openxmlformats.org/officeDocument/2006/math"
    xmlns:v="urn:schemas-microsoft-com:vml"
    xmlns:wp14="http://schemas.microsoft.com/office/word/2010/wordprocessingDrawing"
    xmlns:wp="http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing"
    xmlns:w10="urn:schemas-microsoft-com:office:word"
    xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main"
    xmlns:w14="http://schemas.microsoft.com/office/word/2010/wordml"
    xmlns:w15="http://schemas.microsoft.com/office/word/2012/wordml"
    xmlns:wpg="http://schemas.microsoft.com/office/word/2010/wordprocessingGroup"
    xmlns:wpi="http://schemas.microsoft.com/office/word/2010/wordprocessingInk"
    xmlns:wne="http://schemas.microsoft.com/office/word/2006/wordml"
    xmlns:wps="http://schemas.microsoft.com/office/word/2010/wordprocessingShape" mc:Ignorable="w14 w15 wp14">
    
    <!-- global parameter -->
    <xsl:param name="RelatedWithLbl" />

    <!-- entry point: write item and related -->
    <xsl:template match="/">
        <section>
            <xsl:apply-templates select="Item//propertySet" />        
            <xsl:apply-templates select="Item/related" />
        </section>            
    </xsl:template>
    
    <!-- write item's title and text, found in the item's property set -->
    <xsl:template match="propertySet">
        <w:p>
            <w:pPr>
                <w:pStyle w:val="Heading1"/>
                <w:rPr>
                    <w:rFonts w:eastAsia="Times New Roman"/>
                    <w:lang w:val="en-US" w:eastAsia="de-CH"/>
                </w:rPr>
            </w:pPr>
            <w:r>
                <w:rPr>
                    <w:rFonts w:eastAsia="Times New Roman"/>
                    <w:lang w:val="en-US" w:eastAsia="de-CH"/>
                </w:rPr>
                <w:t>
                    <xsl:choose>
                        <xsl:when test="Title"><xsl:value-of select="normalize-space(Title)"/></xsl:when>
                        <xsl:otherwise><xsl:value-of select="normalize-space(concat(Name, ', ', Firstname))"/></xsl:otherwise>
                    </xsl:choose>            
                </w:t>
            </w:r>
        </w:p>
        <xsl:if test="From">
            <w:p>
                <w:pPr>
                    <w:spacing w:before="100" w:beforeAutospacing="1" w:after="0" w:line="240" w:lineRule="auto"/>
                    <w:rPr>
                        <w:rFonts w:eastAsia="Times New Roman" w:cs="Times New Roman"/>
                        <w:sz w:val="24"/>
                        <w:szCs w:val="24"/>
                        <w:lang w:val="en-US" w:eastAsia="de-CH"/>
                    </w:rPr>
                </w:pPr>
                <w:r>
                    <w:rPr>
                        <w:rFonts w:eastAsia="Times New Roman" w:cs="Times New Roman"/>
                        <w:i/>
                        <w:sz w:val="24"/>
                        <w:szCs w:val="24"/>
                        <w:lang w:val="en-US" w:eastAsia="de-CH"/>
                    </w:rPr>
                <w:t><xsl:value-of select="From"/>
                <xsl:if test="To">&#160;-&#160;<xsl:value-of select="To"/></xsl:if>
                </w:t>
                </w:r>
            </w:p>
        </xsl:if>
        
        <xsl:apply-templates select="Text" />
    </xsl:template>
    
    <!-- write the item's related section -->
    <xsl:template match="related">
        <w:p>
            <w:pPr>
                <w:spacing w:before="100" w:beforeAutospacing="1" w:after="0" w:line="240" w:lineRule="auto"/>
                <w:rPr>
                    <w:rFonts w:eastAsia="Times New Roman" w:cs="Times New Roman"/>
                    <w:sz w:val="24"/>
                    <w:szCs w:val="24"/>
                    <w:lang w:val="en-US" w:eastAsia="de-CH"/>
                </w:rPr>
            </w:pPr>
            <w:r>
                <w:rPr>
                    <w:rFonts w:eastAsia="Times New Roman" w:cs="Times New Roman"/>
                    <w:b/>
                    <w:bCs/>
                    <w:sz w:val="24"/>
                    <w:szCs w:val="24"/>
                    <w:lang w:val="en-US" w:eastAsia="de-CH"/>
                </w:rPr>
                <w:t><xsl:value-of select="$RelatedWithLbl"/>&#160;</w:t>
            </w:r>
            <xsl:apply-templates select="related_item" />            
        </w:p>
    </xsl:template>
    
    <!-- format an entry in the list of related items -->
    <xsl:template match="related_item">
        <w:r>
            <w:rPr>
                <w:rFonts w:eastAsia="Times New Roman" w:cs="Times New Roman"/>                
                <xsl:if test="@type=3"><w:i/></xsl:if>
                <w:sz w:val="24"/>
                <w:szCs w:val="24"/>
                <w:lang w:val="en-US" w:eastAsia="de-CH"/>
            </w:rPr>
            <w:t><xsl:choose>
                    <xsl:when test="@type=2">&quot;<xsl:value-of select="."/>&quot;</xsl:when>
                    <xsl:otherwise>
                        <xsl:value-of select="."/>
                    </xsl:otherwise>                    
                </xsl:choose>
                <xsl:if test="not(position()=last())">,</xsl:if></w:t>
        </w:r>
        <xsl:if test="not(position()=last())">
            <w:r>
                <w:rPr>
                    <w:rFonts w:eastAsia="Times New Roman" w:cs="Times New Roman"/>
                    <w:sz w:val="24"/>
                    <w:szCs w:val="24"/>
                    <w:lang w:val="en-US" w:eastAsia="de-CH"/>
                </w:rPr>
                <w:t xml:space="preserve"> </w:t>
            </w:r>
        </xsl:if>
    </xsl:template>
    
    <!-- format the list of related items -->
    <xsl:template name="related_formatted">
        <xsl:param name="value" />
        <xsl:param name="enclosing_start" />
        <xsl:param name="enclosing_end" />
        <xsl:value-of select="$enclosing_start"/><xsl:value-of select="normalize-space($value)"/><xsl:value-of select="$enclosing_end"/>
    </xsl:template>
    
    <!-- format the item's text field -->
    <xsl:template match="Text">
        <xsl:apply-templates />
    </xsl:template>

    <!-- format a paragraph -->    
    <xsl:template match="para">
        <w:p>
            <w:pPr>
                <w:spacing w:before="100" w:beforeAutospacing="1" w:after="0" w:line="240" w:lineRule="auto"/>
                <w:rPr>
                    <w:rFonts w:eastAsia="Times New Roman" w:cs="Times New Roman"/>
                    <w:sz w:val="24"/>
                    <w:szCs w:val="24"/>
                    <w:lang w:val="en-US" w:eastAsia="de-CH"/>
                </w:rPr>
            </w:pPr>
            <xsl:for-each select="child::node()">
                <xsl:call-template name="write">
                    <xsl:with-param name="style" select="''" />
                </xsl:call-template>
            </xsl:for-each>
        </w:p>        
    </xsl:template>
    
    <xsl:template name="write">
        <xsl:param name="style" select="''" />
        
        <xsl:choose>
            <xsl:when test="self::text()">
                <w:r>
                    <w:rPr>
                        <w:rFonts w:eastAsia="Times New Roman" w:cs="Times New Roman"/>
                        <xsl:if test="contains($style, 'b')"><w:b/></xsl:if>
                        <xsl:if test="contains($style, 'i')"><w:i/></xsl:if>
                        <w:sz w:val="24"/>
                        <w:szCs w:val="24"/>
                        <xsl:if test="contains($style, 'u')"><w:u w:val="single"/></xsl:if>
                        <w:lang w:val="en-US" w:eastAsia="de-CH"/>
                    </w:rPr>            
                    <w:t><xsl:if test="starts-with(self::text(), ' ') or (substring(self::text(), string-length(self::text())) = ' ')">
                        <xsl:attribute name="xml:space">preserve</xsl:attribute>                            
                    </xsl:if><xsl:value-of select="." /></w:t>
                </w:r>
            </xsl:when>
            <xsl:otherwise>
                <xsl:variable name="curr_name" select="name()" />
                <xsl:for-each select="child::node()">
                    <xsl:call-template name="write">
                        <xsl:with-param name="style" select="concat($style, $curr_name)" />
                    </xsl:call-template>
                </xsl:for-each>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
        
    <!-- format a list: ul, ol_number, ol_upper, ol_lower -->
    <xsl:template match="ul | ol_number | ol_upper | ol_lower">
        <xsl:for-each select="li">
            <w:p>
                <w:pPr>
                    <w:pStyle w:val="ListParagraph"/>
                    <w:numPr>
                        <w:ilvl w:val="0"/>
                        <xsl:choose>
                            <xsl:when test="name(..) = 'ul'"><w:numId w:val="1"/></xsl:when>
                            <xsl:when test="name(..) = 'ol_number'"><w:numId w:val="2"/></xsl:when>
                            <xsl:when test="name(..) = 'ol_upper'"><w:numId w:val="4"/></xsl:when>
                            <xsl:when test="name(..) = 'ol_lower'"><w:numId w:val="3"/></xsl:when>
                            <xsl:otherwise><w:numId w:val="1"/></xsl:otherwise>
                        </xsl:choose>                        
                    </w:numPr>
                    <w:spacing w:before="100" w:beforeAutospacing="1" w:after="0" w:line="240" w:lineRule="auto"/>
                    <w:rPr>
                        <w:rFonts w:eastAsia="Times New Roman" w:cs="Times New Roman"/>
                        <w:sz w:val="24"/>
                        <w:szCs w:val="24"/>
                        <w:lang w:val="en-US" w:eastAsia="de-CH"/>
                    </w:rPr>
                </w:pPr>
                <xsl:call-template name="write">
                    <xsl:with-param name="content" select="child::node()" />
                    <xsl:with-param name="style" select="''" />
                </xsl:call-template>
            </w:p>
        </xsl:for-each>
    </xsl:template>
    
</xsl:stylesheet>