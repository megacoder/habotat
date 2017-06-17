##-----------------------------------------------------------------------------
## Process this file with automake to produce Makefile.in
##-----------------------------------------------------------------------------

# classpath for compilation

CLASSPATH	= ../../server/habotat.jar \
		  ../../libs/daim.jar \
		  ../../libs/kiwi.jar

# additional files & dirs to include in distribution

EXTRA_FILES	= 

# jar file to generate

JARFILE = aim.jar

JAR_SUBDIR = adapters

# packages to compile

PACKAGES	= habotat.adapters.aim

# resource directories to include in jar file

RESOURCE_DIRS   = habotat/adapters/aim/images

# javadoc window & document title

JAVADOC_TITLE	= "$(PACKAGE)"

MANIFEST_FILE = manifest.mf

# import the generic Java build logic

include ../../java.mak

## eof
