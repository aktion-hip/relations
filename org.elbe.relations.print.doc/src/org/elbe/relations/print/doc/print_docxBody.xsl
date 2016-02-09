<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    
    <!-- global parameter -->
    <xsl:param name="TocLbl" />
    
    <xsl:template match="/">
        <w:document xmlns:wpc="http://schemas.microsoft.com/office/word/2010/wordprocessingCanvas"
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
            xmlns:wps="http://schemas.microsoft.com/office/word/2010/wordprocessingShape"
            mc:Ignorable="w14 w15 wp14">            
            <w:body>
                <w:p>
                    <w:pPr>
                        <w:pStyle w:val="Title"/>
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
                        <w:t><xsl:value-of select="docBody/docTitle"/></w:t>
                    </w:r>
                </w:p>
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
                    <w:proofErr w:type="gramStart"/>
                    <w:r>
                        <w:rPr>
                            <w:rFonts w:eastAsia="Times New Roman" w:cs="Arial"/>
                            <w:i/>
                            <w:iCs/>
                            <w:lang w:val="en-US" w:eastAsia="de-CH"/>
                        </w:rPr>
                        <w:t><xsl:value-of select="docBody/docSubTitle"/></w:t>
                    </w:r>
                </w:p>
                <w:sdt>
                    <w:sdtPr>
                        <w:id w:val="-288663922"/>
                        <w:docPartObj>
                            <w:docPartGallery w:val="Table of Contents"/>
                            <w:docPartUnique/>
                        </w:docPartObj>
                    </w:sdtPr>
                    <w:sdtEndPr>
                        <w:rPr>
                            <w:rFonts w:asciiTheme="minorHAnsi" w:eastAsiaTheme="minorHAnsi"
                                w:hAnsiTheme="minorHAnsi" w:cstheme="minorBidi"/>
                            <w:b/>
                            <w:bCs/>
                            <w:noProof/>
                            <w:color w:val="auto"/>
                            <w:sz w:val="22"/>
                            <w:szCs w:val="22"/>
                            <w:lang w:val="de-CH"/>
                        </w:rPr>
                    </w:sdtEndPr>
                    <w:sdtContent>
                        <w:p>
                            <w:pPr>
                                <w:pStyle w:val="TOCHeading"/>
                            </w:pPr>
                            <w:r>
                                <w:t><xsl:value-of select="$TocLbl"/></w:t>
                            </w:r>
                        </w:p>
                    </w:sdtContent>
                </w:sdt>                
        

            </w:body>
        </w:document>
    </xsl:template>

</xsl:stylesheet>