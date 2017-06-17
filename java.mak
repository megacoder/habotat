##----------------------------------------------------------------------------
## This makefile fragment provides some convenience logic for building Java
## code. Simply include it from your Makefile.am. This file does not need to
## be modified on a per-project basis.
##
## Hey, what can I say? Jakarta Ant sucks.
##
## Mark Lindner - 12/11/2003
##----------------------------------------------------------------------------

.PHONY = javadoc jar docdist #resindex

JAVADOC_STAMP   = .javadoc_uptodate
#RESINDEX_STAMP  = .resindex_uptodate

x =
space = $x $x

AM_JAVACFLAGS = -deprecation -classpath .:$(subst $(space),:,$(strip $(CLASSPATH)))

SRCDIR_LIST = $(subst .,/,$(strip $(PACKAGES)))

dist_noinst_JAVA = $(foreach dir,$(SRCDIR_LIST),$(wildcard $(dir)/*.java))

SOURCE_FILES = $(foreach dir,$(SRCDIR_LIST),$(wildcard $(dir)/*.java))

JAR = $(JAVA_HOME)/bin/jar
JAVADOC = $(JAVA_HOME)/bin/javadoc

JAVADOC_DIR = ./docs
JAVADOC_TAR = docs.tar

JDK5_JAVADOC_URL = "http://java.sun.com/j2se/1.5/docs/api"
JDOM_JAVADOC_URL = "http://www.jdom.org/docs/apidocs"

find_files = $(shell find $(dir) -name '.svn' -prune -o -type f -print)

JAVAROOT = .

RESOURCE_FILES := $(foreach dir,$(RESOURCE_DIRS),$(find_files))

JAVADOC_FLAGS = -classpath .:$(subst $(space),:,$(strip $(CLASSPATH))) \
		-d $(JAVADOC_DIR) -doctitle $(JAVADOC_TITLE) \
		-windowtitle $(JAVADOC_TITLE) \
		-splitindex -version -author \
		-linkoffline $(JDK5_JAVADOC_URL) $(JDK5_JAVADOC_URL) \
		-linkoffline $(JDOM_JAVADOC_URL) $(JDOM_JAVADOC_URL)

$(JAVADOC_STAMP): $(SOURCE_FILES)
	if [ -z "$(JAVADOC_PACKAGES)" ]; \
	then \
		$(JAVADOC) $(JAVADOC_FLAGS) $(PACKAGES); \
	else \
		$(JAVADOC) $(JAVADOC_FLAGS) $(JAVADOC_PACKAGES); \
	fi
	@touch $(JAVADOC_STAMP)

jar: $(JARFILE)

foo:
	echo $(RESOURCE_DIRS)

$(JARFILE): $(SOURCE_FILES) $(RESOURCE_FILES) $(MANIFEST_FILE)
	rm -f $(JARFILE)
	touch $(JARFILE)
	find . -name '*.class' | xargs $(JAR) -uvf $(JARFILE)
	test -z "$(RESOURCE_DIRS)" || \
	for i in $(RESOURCE_DIRS); do \
		find $$i -name '.svn' -prune -o -type f -print \
			| xargs	$(JAR) -uvf $(JARFILE); \
	done
	test -z "$(MANIFEST_FILE)" || \
		$(JAR) -uvfm $(JARFILE) $(MANIFEST_FILE)

javadoc: $(JAVADOC_STAMP)

docdist: $(JAVADOC_TAR)

$(JAVADOC_TAR): javadoc
	rm -f $(JAVADOC_TAR)
	touch $(JAVADOC_TAR)
	find $(JAVADOC_DIR) -follow -name '.svn' -prune -o -type f -print \
		| xargs tar huvf $(JAVADOC_TAR)

EXTRA_DIST	:= $(RESOURCE_FILES) $(JARFILE) $(JAVADOC_TAR) \
		   $(MANIFEST_FILE) \
		   $(foreach dir,$(EXTRA_FILES),$(find_files))

# resindex: $(RESINDEX_STAMP)

#$(RESINDEX_STAMP): $(RESOURCE_FILES)
#	@test -z "$(RESOURCE_DIRS)" || for i in $(RESOURCE_DIRS); \
#	do \
#	  perl resindex.pl $$i; \
#	done
#	@touch $(RESINDEX_STAMP)

CLEANFILES = $(JARFILE) $(JAVADOC_TAR) $(RESINDEX_STAMP) $(JAVADOC_STAMP)

all-local: jar

clean-local:
	find . -name '*.class' | xargs rm -f

install-exec-local:
	mkdir -p $(pkglibdir)/$(JAR_SUBDIR)
	$(INSTALL) -m 644 $(JARFILE) $(pkglibdir)/$(JAR_SUBDIR)

##----------------------------------------------------------------------------
## eof
