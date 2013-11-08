<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:output method="text"/>

    <xsl:template match="/">
        <xsl:value-of select="docBody/docTitle"/>
        <xsl:text>            
</xsl:text>
        <xsl:call-template name="underscore">
            <xsl:with-param name="title" select="docBody/docTitle" />
        </xsl:call-template>
        <xsl:text>
            
</xsl:text>
        <xsl:value-of select="docBody/docSubTitle"/>
            
        <xsl:text>
        
</xsl:text>
    </xsl:template>
    
    <xsl:template name="underscore">
        <xsl:param name="title" />
        <xsl:variable name="rest" select="substring($title, 2)" />
        <xsl:choose>
            <xsl:when test="$rest">
                <xsl:variable name="line">
                    <xsl:call-template name="underscore">
                        <xsl:with-param name="title" select="$rest" />
                    </xsl:call-template>
                </xsl:variable>
                <xsl:value-of select="concat('=', $line)"/>
            </xsl:when>
            <xsl:otherwise>=</xsl:otherwise>
        </xsl:choose>
    </xsl:template>    
</xsl:stylesheet>