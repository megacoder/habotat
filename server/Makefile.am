##-----------------------------------------------------------------------------
## This package has been autoconfiscated. This Makefile.am replaces the old
## hand-built Makefile.
##
## Process this file with automake to produce Makefile.in
##-----------------------------------------------------------------------------

# classpath for compilation

CLASSPATH	= ../libs/kiwi.jar \
		  ../libs/jdom.jar \
		  ../libs/getopt.jar \
		  ../libs/smack.jar

# additional files & dirs to include in distribution

bin_SCRIPTS	= habotat.sh

# jar file to generate

JARFILE = habotat.jar

MANIFEST_FILE = manifest.mf


# packages to compile

PACKAGES	= habotat.engine \
		  habotat.api \
		  habotat.gui

# packages to document

JAVADOC_PACKAGES = habotat.api

# resource directories to include in jar file

RESOURCE_DIRS   = habotat/gui/images \
		  habotat/gui/html \
		  habotat/engine/xsl \
		  habotat/gui/locale

# javadoc window & document title

JAVADOC_TITLE	= "$(PACKAGE) v$(VERSION) API Specification"


# import the generic Java build logic

include ../java.mak

## eof
