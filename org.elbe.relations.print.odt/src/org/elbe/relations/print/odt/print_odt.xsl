<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
    xmlns:office="urn:oasis:names:tc:opendocument:xmlns:office:1.0"
    xmlns:text="urn:oasis:names:tc:opendocument:xmlns:text:1.0">
    
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
        <text:h text:style-name="Heading_20_3" text:outline-level="3">
            <xsl:choose>
                <xsl:when test="Title"><xsl:value-of select="normalize-space(Title)"/></xsl:when>
                <xsl:otherwise><xsl:value-of select="normalize-space(concat(Name, ', ', Firstname))"/></xsl:otherwise>
            </xsl:choose>            
        </text:h>
        <xsl:if test="From">
            <text:p text:style-name="P2"><xsl:value-of select="From"/>
                <xsl:if test="To">&#160;-&#160;<xsl:value-of select="To"/></xsl:if>
            </text:p>
        </xsl:if>
        
        <xsl:apply-templates select="Text" />
    </xsl:template>
    
    <!-- write the item's related section -->
    <xsl:template match="related">
        <text:p text:style-name="Standard"/>
        <text:p text:style-name="Standard">
            <text:span text:style-name="T1"><xsl:value-of select="$RelatedWithLbl"/>&#160;</text:span>
    
            <xsl:apply-templates select="related_item" />            
        </text:p>
    </xsl:template>
    
    <!-- format an entry in the list of related items -->
    <xsl:template match="related_item">
        <xsl:choose>
            <xsl:when test="@type=2">&quot;<xsl:value-of select="."/>&quot;</xsl:when>
            <xsl:when test="@type=3">
                <text:span text:style-name="T2">
                    <xsl:value-of select="."/>
                </text:span>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="."/>
            </xsl:otherwise>
        </xsl:choose>
        <xsl:if test="not(position()=last())">, </xsl:if>
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
        <text:p text:style-name="Standard">
            <xsl:apply-templates />
        </text:p>        
    </xsl:template>
    
    <!-- format inline styles: bold, italic, underline -->    
    <xsl:template match="b | i | u">
        <text:span>
            <xsl:attribute name="text:style-name">
                <xsl:choose>
                    <xsl:when test="self::b">T1</xsl:when>
                    <xsl:when test="self::i">T2</xsl:when>
                    <xsl:when test="self::u">T3</xsl:when>
                </xsl:choose>
            </xsl:attribute>
            <xsl:apply-templates />
        </text:span>
    </xsl:template>
    
    <!-- format a list: ul, ol_number, ol_upper, ol_lower -->
    <xsl:template match="ul | ol_number | ol_upper | ol_lower">
        <text:list>
            <xsl:if test="not((ancestor::ul) | (ancestor::ol_number) | (ancestor::ol_upper) | (ancestor::ol_lower))">
                <xsl:attribute name="text:style-name">
                    <xsl:choose>
                        <xsl:when test="self::ol_number">L2</xsl:when>
                        <xsl:when test="self::ol_upper">L3</xsl:when>
                        <xsl:when test="self::ol_lower">L4</xsl:when>
                        <xsl:otherwise>L1</xsl:otherwise>
                    </xsl:choose>
                </xsl:attribute>
            </xsl:if>
            <xsl:for-each select="li">
                <text:list-item>
                    <text:p>
                        <xsl:attribute name="text:style-name">
                            <xsl:choose>
                                <xsl:when test="self::ol_number">P5</xsl:when>
                                <xsl:when test="self::ol_upper">P6</xsl:when>
                                <xsl:when test="self::ol_lower">P7</xsl:when>
                                <xsl:otherwise>P4</xsl:otherwise>
                            </xsl:choose>
                        </xsl:attribute>
                        <xsl:apply-templates select="text() | b | i | u" />
                    </text:p>
                    <xsl:apply-templates select="ul | ol_number | ol_upper | ol_lower" />  
                </text:list-item>
            </xsl:for-each>
        </text:list>
    </xsl:template>
    
</xsl:stylesheet>