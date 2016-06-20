<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    
    <!-- global parameter -->
    <xsl:param name="TocLbl" />
    
    <xsl:template match="/">
        <office:document-content xmlns:office="urn:oasis:names:tc:opendocument:xmlns:office:1.0"
            xmlns:style="urn:oasis:names:tc:opendocument:xmlns:style:1.0"
            xmlns:text="urn:oasis:names:tc:opendocument:xmlns:text:1.0"
            xmlns:table="urn:oasis:names:tc:opendocument:xmlns:table:1.0"
            xmlns:draw="urn:oasis:names:tc:opendocument:xmlns:drawing:1.0"
            xmlns:fo="urn:oasis:names:tc:opendocument:xmlns:xsl-fo-compatible:1.0"
            xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:dc="http://purl.org/dc/elements/1.1/"
            xmlns:meta="urn:oasis:names:tc:opendocument:xmlns:meta:1.0"
            xmlns:number="urn:oasis:names:tc:opendocument:xmlns:datastyle:1.0"
            xmlns:svg="urn:oasis:names:tc:opendocument:xmlns:svg-compatible:1.0"
            xmlns:chart="urn:oasis:names:tc:opendocument:xmlns:chart:1.0"
            xmlns:dr3d="urn:oasis:names:tc:opendocument:xmlns:dr3d:1.0"
            xmlns:math="http://www.w3.org/1998/Math/MathML"
            xmlns:form="urn:oasis:names:tc:opendocument:xmlns:form:1.0"
            xmlns:script="urn:oasis:names:tc:opendocument:xmlns:script:1.0"
            xmlns:ooo="http://openoffice.org/2004/office" xmlns:ooow="http://openoffice.org/2004/writer"
            xmlns:oooc="http://openoffice.org/2004/calc" xmlns:dom="http://www.w3.org/2001/xml-events"
            xmlns:xforms="http://www.w3.org/2002/xforms" xmlns:xsd="http://www.w3.org/2001/XMLSchema"
            xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" office:version="1.2">
            <office:scripts/>
            <office:font-face-decls>
                <style:font-face style:name="Tahoma1" svg:font-family="Tahoma"/>
                <style:font-face style:name="Lucida Sans Unicode"
                    svg:font-family="&apos;Lucida Sans Unicode&apos;" style:font-pitch="variable"/>
                <style:font-face style:name="Tahoma" svg:font-family="Tahoma" style:font-pitch="variable"/>
                <style:font-face style:name="Times New Roman"
                    svg:font-family="&apos;Times New Roman&apos;" style:font-family-generic="roman"
                    style:font-pitch="variable"/>
                <style:font-face style:name="Arial" svg:font-family="Arial"
                    style:font-family-generic="swiss" style:font-pitch="variable"/>
                <style:font-face style:name="Arial1" svg:font-family="Arial" style:font-adornments="Regular"
                    style:font-family-generic="swiss" style:font-pitch="variable"/>
            </office:font-face-decls>
            <office:automatic-styles>
                <style:style style:name="P1" style:family="paragraph" style:parent-style-name="Standard">
                    <style:text-properties style:font-name="Arial" fo:font-size="11pt"
                        fo:font-style="italic"/>
                </style:style>
                <style:style style:name="P2" style:family="paragraph"
                    style:parent-style-name="Contents_20_1">
                    <style:paragraph-properties>
                        <style:tab-stops/>
                    </style:paragraph-properties>
                    <style:text-properties fo:font-style="italic" style:font-style-asian="italic"
                        style:font-style-complex="italic"/>
                </style:style>
                <style:style style:name="P3" style:family="paragraph"
                    style:parent-style-name="Contents_20_3">
                    <style:paragraph-properties>
                        <style:tab-stops>
                            <style:tab-stop style:position="16.999cm" style:type="right"
                                style:leader-style="dotted" style:leader-text="."/>
                        </style:tab-stops>
                    </style:paragraph-properties>
                </style:style>
                <style:style style:name="P4" style:family="paragraph" style:parent-style-name="Standard"
                    style:list-style-name="L1">
                    <style:text-properties fo:font-weight="normal" style:font-weight-asian="normal"
                        style:font-weight-complex="normal"/>
                </style:style>                
                <style:style style:name="P5" style:family="paragraph" style:parent-style-name="Standard"
                    style:list-style-name="L2">
                    <style:text-properties fo:font-weight="normal" style:font-weight-asian="normal"
                        style:font-weight-complex="normal"/>
                </style:style>
                <style:style style:name="P6" style:family="paragraph" style:parent-style-name="Standard"
                    style:list-style-name="L3">
                    <style:text-properties fo:font-weight="normal" style:font-weight-asian="normal"
                        style:font-weight-complex="normal"/>
                </style:style>
                <style:style style:name="P7" style:family="paragraph" style:parent-style-name="Standard"
                    style:list-style-name="L4">
                    <style:text-properties fo:font-weight="normal" style:font-weight-asian="normal"
                        style:font-weight-complex="normal"/>
                </style:style>
                
                <style:style style:name="T1" style:family="text">
                    <style:text-properties fo:font-weight="bold"/>
                </style:style>
                <style:style style:name="T2" style:family="text">
                    <style:text-properties fo:font-style="italic"/>
                </style:style>
                <style:style style:name="T3" style:family="text">
                    <style:text-properties style:text-underline-style="solid"
                        style:text-underline-width="auto" style:text-underline-color="font-color"/>
                </style:style>                
                <text:list-style style:name="L1">
                    <text:list-level-style-bullet text:level="1" text:style-name="Bullet_20_Symbols"
                        style:num-suffix="." text:bullet-char="•">
                        <style:list-level-properties text:min-label-width="0.65cm"/>
                        <style:text-properties style:font-name="StarSymbol"/>
                    </text:list-level-style-bullet>
                    <text:list-level-style-number text:level="2" text:style-name="Numbering_20_Symbols"
                        style:num-suffix="." style:num-format="1">
                        <style:list-level-properties text:min-label-width="0.65cm"/>
                    </text:list-level-style-number>                                      
                </text:list-style>                
                <text:list-style style:name="L2">
                    <text:list-level-style-number text:level="1" text:style-name="Numbering_20_Symbols"
                        style:num-suffix="." style:num-format="1">
                        <style:list-level-properties text:min-label-width="0.65cm"/>
                    </text:list-level-style-number>
                    <text:list-level-style-number text:level="2" style:num-suffix=")" style:num-format="a">
                        <style:list-level-properties text:space-before="0.65cm"
                            text:min-label-width="0.65cm"/>
                    </text:list-level-style-number>                    
                </text:list-style>                
                <text:list-style style:name="L3">
                    <text:list-level-style-number text:level="1" style:num-suffix=")" style:num-format="A">
                        <style:list-level-properties text:min-label-width="0.65cm"/>
                    </text:list-level-style-number>
                    <text:list-level-style-number text:level="2" style:num-suffix=")" style:num-format="a">
                        <style:list-level-properties text:space-before="0.65cm"
                            text:min-label-width="0.65cm"/>
                    </text:list-level-style-number>                    
                </text:list-style>                
                <text:list-style style:name="L4">
                    <text:list-level-style-number text:level="1" style:num-suffix=")" style:num-format="a">
                        <style:list-level-properties text:min-label-width="0.65cm"/>
                    </text:list-level-style-number>
                    <text:list-level-style-number text:level="2" text:style-name="Numbering_20_Symbols"
                        style:num-suffix="." style:num-format="1">
                        <style:list-level-properties text:min-label-width="0.65cm"/>
                    </text:list-level-style-number>
                </text:list-style>                
                
                <style:style style:name="Sect1" style:family="section">
                    <style:section-properties fo:background-color="transparent" style:editable="false">
                        <style:columns fo:column-count="1" fo:column-gap="0cm"/>
                        <style:background-image/>
                    </style:section-properties>
                </style:style>
                <style:style style:name="Sect2" style:family="section">
                    <style:section-properties style:editable="false">
                        <style:columns fo:column-count="1" fo:column-gap="0cm"/>
                    </style:section-properties>
                </style:style>
            </office:automatic-styles>
            <office:body>
                <office:text>
                    <text:sequence-decls>
                        <text:sequence-decl text:display-outline-level="0" text:name="Illustration"/>
                        <text:sequence-decl text:display-outline-level="0" text:name="Table"/>
                        <text:sequence-decl text:display-outline-level="0" text:name="Text"/>
                        <text:sequence-decl text:display-outline-level="0" text:name="Drawing"/>
                    </text:sequence-decls>
                    <text:h text:style-name="Heading_20_1" text:outline-level="1"><xsl:value-of select="docBody/docTitle"/></text:h>
                    <text:p text:style-name="P1"><xsl:value-of select="docBody/docSubTitle"/></text:p>
                    <text:p text:style-name="P1"/>
                    <text:table-of-content text:style-name="Sect1" text:protected="true"
                        text:name="Table of Contents1">
                        <text:table-of-content-source text:outline-level="10">
                            <text:index-title-template text:style-name="Contents_20_Heading">Content</text:index-title-template>
                            <text:table-of-content-entry-template text:outline-level="1"
                                text:style-name="Contents_20_1"/>
                            <text:table-of-content-entry-template text:outline-level="2"
                                text:style-name="Contents_20_2">
                                <text:index-entry-chapter/>
                                <text:index-entry-text/>
                                <text:index-entry-tab-stop style:type="right" style:leader-char="."/>
                                <text:index-entry-page-number/>
                            </text:table-of-content-entry-template>
                            <text:table-of-content-entry-template text:outline-level="3"
                                text:style-name="Contents_20_3">
                                <text:index-entry-chapter/>
                                <text:index-entry-text/>
                                <text:index-entry-tab-stop style:type="right" style:leader-char="."/>
                                <text:index-entry-page-number/>
                            </text:table-of-content-entry-template>
                        </text:table-of-content-source>
                        <text:index-body>
                            <text:index-title text:style-name="Sect2" text:name="Table of Contents1_Head">
                                <text:p text:style-name="Contents_20_Heading"><xsl:value-of select="$TocLbl"/></text:p>
                            </text:index-title>
                        </text:index-body>
                    </text:table-of-content>  
                </office:text>
            </office:body>
        </office:document-content>
    </xsl:template>

</xsl:stylesheet>