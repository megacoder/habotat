
##-----------------------------------------------------------------------------
## This package has been autoconfiscated. This Makefile.am replaces the old
## hand-built Makefile.
##
## Process this file with automake to produce Makefile.in
##-----------------------------------------------------------------------------

# classpath for compilation

CLASSPATH	= ../../libs/kiwi.jar \
		  ../../libs/jdom.jar \
		  ../../server/habotat.jar

# additional files & dirs to include in distribution

EXTRA_FILES	= 

# jar file to generate

JARFILE = weatherbot.jar

JAR_SUBDIR = plugins

# packages to compile

PACKAGES	= habotat.bots.weather

# resource directories to include in jar file

RESOURCE_DIRS   = habotat/bots/weather/images \
		  habotat/bots/weather/xsl

# javadoc window & document title

JAVADOC_TITLE	= "$(PACKAGE)"

MANIFEST_FILE = manifest.mf

# import the generic Java build logic

include ../../java.mak

## eof
