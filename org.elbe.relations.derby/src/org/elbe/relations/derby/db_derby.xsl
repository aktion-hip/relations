<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    
    <xsl:template match="/">
        <CreateObjects>
            <xsl:apply-templates />
        </CreateObjects>    
    </xsl:template>
        
    <xsl:template match="Table">
        <CreateObject>            
        CREATE TABLE <xsl:value-of select="@name"/> (
        <xsl:apply-templates select="Field" />
        <xsl:apply-templates select="PrimaryKey" />)
    </CreateObject>
    </xsl:template>
    
    <xsl:template match="Field">
        <xsl:value-of select="@name" /><xsl:text> </xsl:text>
        <xsl:choose>
            <xsl:when test="@type = 'string'">VARCHAR(<xsl:value-of select="@size"/>)</xsl:when>
            <xsl:when test="@type = 'clob'">CLOB</xsl:when>
            <xsl:when test="@type = 'blob'">BLOB</xsl:when>
            <xsl:when test="@type = 'integer'">INTEGER</xsl:when>
            <xsl:when test="@type = 'tinyinteger'">SMALLINT</xsl:when>
            <xsl:when test="@type = 'biginteger'">BIGINT</xsl:when>
            <xsl:when test="@type = 'real'">REAL</xsl:when>
            <xsl:when test="@type = 'double'">DOUBLE</xsl:when>
            <xsl:when test="@type = 'float'">FLOAT</xsl:when>
            <xsl:when test="@type = 'decimal'">DECIMAL</xsl:when>
            <xsl:when test="@type = 'date'">DATE</xsl:when>
            <xsl:when test="@type = 'time'">TIME</xsl:when>
            <xsl:when test="@type = 'timestamp'">TIMESTAMP</xsl:when>
        </xsl:choose>
        <xsl:if test="@nullable = 'no'">
        	<xsl:choose>
				<xsl:when test="@type = 'timestamp'"> DEFAULT CURRENT_TIMESTAMP</xsl:when>
				<xsl:otherwise> not null</xsl:otherwise>
        	</xsl:choose>
        </xsl:if>
        <xsl:if test="@auto_increment = 'yes'"> generated always as identity</xsl:if>
        <xsl:if test="@default">
        	<xsl:choose>
	            <xsl:when test="@type = 'date'"> not null</xsl:when>
	            <xsl:when test="@type = 'time'"> not null</xsl:when>
	            <xsl:when test="@type = 'timestamp'"> DEFAULT CURRENT_TIMESTAMP</xsl:when>
	            <xsl:otherwise> default <xsl:value-of select="@default"/></xsl:otherwise>
        	</xsl:choose>
        </xsl:if>,
    </xsl:template>
    
    <xsl:template match="PrimaryKey">
        PRIMARY KEY (<xsl:value-of select="Column/@name"/>)
    </xsl:template>
    
    <xsl:template match="Index">
        <CreateObject>
        CREATE INDEX <xsl:value-of select="@name"/> ON <xsl:value-of select="@tablename"/>(<xsl:apply-templates select="Column" />)
    </CreateObject>
    </xsl:template>
    
    <xsl:template match="Column">
        <xsl:value-of select="@name"/>
        <xsl:if test="position() != last()">, </xsl:if>
    </xsl:template>
</xsl:stylesheet>