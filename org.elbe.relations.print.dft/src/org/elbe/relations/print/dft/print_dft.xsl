<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:output method="text"/>
    
    <!-- global parameter -->
    <xsl:param name="RelatedWithLbl" />
    
    <!-- entry point: write item and related -->
    <xsl:template match="/">
        <xsl:apply-templates select="Item//propertySet" />        
        <xsl:apply-templates select="Item/related" />
    </xsl:template>
    
    <!-- write item's title and text, found in the item's property set -->
    <xsl:template match="propertySet">
        <xsl:call-template name="title_formatted">
            <xsl:with-param name="title">
                <xsl:choose>
                    <xsl:when test="Title"><xsl:value-of select="normalize-space(Title)"/></xsl:when>
                    <xsl:otherwise><xsl:value-of select="normalize-space(concat(Name, ', ', Firstname))"/></xsl:otherwise>
                </xsl:choose>
            </xsl:with-param>
        </xsl:call-template>
<xsl:text>            
</xsl:text>        
        <xsl:if test="From">
            <xsl:value-of select="From"/><xsl:if test="To"> - <xsl:value-of select="To"/></xsl:if>
        </xsl:if>
        <xsl:apply-templates select="Text" />
    </xsl:template>

    <!-- write the item's related section -->
    <xsl:template match="related">
<xsl:text>
                    
</xsl:text>            
        <xsl:value-of select="$RelatedWithLbl"/>
<xsl:text>
</xsl:text>            
        <xsl:apply-templates select="related_item" />
<xsl:text>            
</xsl:text>                    
    </xsl:template>
    
    <!-- format an entry in the list of related items -->
    <xsl:template match="related_item">
        <xsl:choose>
            <xsl:when test="@type=2">
                <xsl:call-template name="related_formatted">
                    <xsl:with-param name="value"><xsl:value-of select="."/></xsl:with-param>
                    <xsl:with-param name="enclosing_start">"</xsl:with-param>
                    <xsl:with-param name="enclosing_end">"</xsl:with-param>
                </xsl:call-template>
            </xsl:when>
            <xsl:when test="@type=3">
                <xsl:call-template name="related_formatted">
                    <xsl:with-param name="value"><xsl:value-of select="."/></xsl:with-param>
                    <xsl:with-param name="enclosing_start">[</xsl:with-param>
                    <xsl:with-param name="enclosing_end">]</xsl:with-param>
                </xsl:call-template>                
            </xsl:when>
            <xsl:otherwise>
                <xsl:call-template name="related_formatted">
                    <xsl:with-param name="value"><xsl:value-of select="."/></xsl:with-param>
                </xsl:call-template>                
            </xsl:otherwise>
        </xsl:choose>
        <xsl:if test="not(position()=last())">, </xsl:if>
    </xsl:template>
    
    <!-- format the item's text field -->
    <xsl:template match="Text">
        <xsl:apply-templates />
    </xsl:template>
    
    <!-- format a paragraph -->
    <xsl:template match="para">
<xsl:text>                
</xsl:text><xsl:apply-templates />
    </xsl:template>
    
    <!-- add new line -->
    <xsl:template match="br">
<xsl:text>                
</xsl:text>
    </xsl:template>
    
    <!-- remove white space from text nodes -->
    <xsl:template match="text()">
        <xsl:value-of select="normalize-space(.)"/>
    </xsl:template>
    
    <!-- format an unordered list -->
    <xsl:template match="ul">
        <xsl:variable name="indent_space">
            <xsl:call-template name="calc_indent">
                <xsl:with-param name="indent" select="@indent" />
            </xsl:call-template>
        </xsl:variable>
        <xsl:for-each select="li">
<xsl:text>                
</xsl:text>            
            <xsl:value-of select="$indent_space"/>
            <xsl:value-of select="concat('-', '  ')"/>
            <xsl:apply-templates />
        </xsl:for-each>        
    </xsl:template>
    
    <!-- format an numbered list -->
    <xsl:template match="ol_number">
        <xsl:variable name="indent_space">
            <xsl:call-template name="calc_indent">
                <xsl:with-param name="indent" select="@indent" />
            </xsl:call-template>
        </xsl:variable>
        <xsl:for-each select="li">
<xsl:text>                
</xsl:text>            
            <xsl:value-of select="$indent_space"/>
            <xsl:value-of select="substring(concat(position(), '.', '   '), 1, 4)"/>
            <xsl:apply-templates />
        </xsl:for-each>
    </xsl:template>
    
    <!-- format an ordered list upper case -->
    <xsl:template match="ol_upper">
        <xsl:variable name="indent_space">
            <xsl:call-template name="calc_indent">
                <xsl:with-param name="indent" select="@indent" />
            </xsl:call-template>
        </xsl:variable>
        <xsl:for-each select="li">
<xsl:text>                
</xsl:text>            
            <xsl:value-of select="$indent_space"/>
            <xsl:call-template name="get_letter">
                <xsl:with-param name="letters" select="'ABCDEFGHIJKLMNOPQRSTUVWXYZ'" />
                <xsl:with-param name="position" select="position()" />
            </xsl:call-template>    
            <xsl:value-of select="concat(')', '  ')"/><xsl:apply-templates />
        </xsl:for-each>
    </xsl:template>
    
    <!-- format an ordered list lower case -->
    <xsl:template match="ol_lower">
        <xsl:variable name="indent_space">
            <xsl:call-template name="calc_indent">
                <xsl:with-param name="indent" select="@indent" />
            </xsl:call-template>
        </xsl:variable>
        <xsl:for-each select="li">
<xsl:text>                
</xsl:text>            
            <xsl:value-of select="$indent_space"/>
            <xsl:call-template name="get_letter">
                <xsl:with-param name="letters" select="'abcdefghijklmnopqrstuvwxyz'" />
                <xsl:with-param name="position" select="position()" />
            </xsl:call-template>    
            <xsl:value-of select="concat(')', '  ')"/>
            <xsl:apply-templates />
        </xsl:for-each>        
    </xsl:template>
    
    <!-- get the list item's letter -->
    <xsl:template name="get_letter">
        <xsl:param name="letters" />
        <xsl:param name="position" />
        <xsl:choose>
            <xsl:when test="$position &lt; 26">
                <xsl:value-of select="substring($letters, $position, 1)"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="substring($letters, 26)"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    
    <!-- caculate the list item's indent -->
    <xsl:template name="calc_indent">
        <xsl:param name="indent" />
        <xsl:variable name="indent_dft">&#160;&#160;&#160;&#160;</xsl:variable>
        <xsl:choose>
            <xsl:when test="$indent>0">
                <xsl:variable name="indent_out">
                    <xsl:call-template name="calc_indent">
                        <xsl:with-param name="indent" select="$indent - 1" />
                    </xsl:call-template>
                </xsl:variable>
                <xsl:value-of select="concat($indent_out, $indent_dft)"/>
            </xsl:when>
            <xsl:otherwise></xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    
    <!-- format the list of related items -->
    <xsl:template name="related_formatted">
        <xsl:param name="value" />
        <xsl:param name="enclosing_start" />
        <xsl:param name="enclosing_end" />
        <xsl:value-of select="$enclosing_start"/><xsl:value-of select="normalize-space($value)"/><xsl:value-of select="$enclosing_end"/>
    </xsl:template>
    
    <!-- format the item's title -->
    <xsl:template name="title_formatted">
        <xsl:param name="title" />
        <xsl:value-of select="$title"/>
<xsl:text>
</xsl:text>
        <xsl:call-template name="underscore">
            <xsl:with-param name="title" select="$title" />
        </xsl:call-template>
    </xsl:template>
    
    <!-- calucalate the title's underscore -->
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
                <xsl:value-of select="concat('-', $line)"/>
            </xsl:when>
            <xsl:otherwise>-</xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    
</xsl:stylesheet>