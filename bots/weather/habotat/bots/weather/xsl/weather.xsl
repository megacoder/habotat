<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet version="1.0"
  exclude-result-prefixes="rdf purl"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
  xmlns:purl="http://purl.org/rss/1.0/">
  
  <xsl:output method="html" omit-xml-declaration="yes"/>

  <xsl:template match="rss">
    <html>
      <xsl:apply-templates select="channel"/>
    </html>
  </xsl:template>

  <xsl:template match="channel">
    <b><a><xsl:attribute name="href"><xsl:value-of select="link"/></xsl:attribute><xsl:value-of select="title"/></a></b><br/>
    <xsl:apply-templates select="item"/>
  </xsl:template>
  
  <xsl:template match="item">
    <xsl:if test="position() &lt;= 8">
      <u><xsl:value-of select="title"/></u><br/>
      <xsl:value-of select="normalize-space(description)"/><br/>
    </xsl:if>
  </xsl:template>
    
</xsl:stylesheet>