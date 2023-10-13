# Variables
JFLAGS = -cp ".:./src/main/java/:../lib/*:./lib/*:../bin/src/main/java/:../bin/test:./test:./bin/test/:./bin/src/main/java/"
SRCDIR = src/main/java
TESTDIR = test
BINDIR = ../bin
PACKAGE = au/edu/adelaide
COMMON = ../common/src/main/java/*

# Target: all
all: compile

# Compile targets
compile: create_bin compile_as compile_cs compile_client compile_common

create_bin:
	mkdir -p ./bin
	mkdir -p ./bin/$(SRCDIR)
	mkdir -p ./bin/test

compile_as: create_bin
	cd AS && cp -r $(COMMON) $(SRCDIR) && javac $(JFLAGS) -d $(BINDIR)/$(SRCDIR) $(SRCDIR)/$(PACKAGE)/aggregationserver/AggregationServer.java

compile_cs: create_bin
	cd CS && cp -r $(COMMON) $(SRCDIR) && javac $(JFLAGS) -d $(BINDIR)/$(SRCDIR) $(SRCDIR)/$(PACKAGE)/contentserver/ContentServer.java

compile_client: create_bin
	cd GET && cp -r $(COMMON) $(SRCDIR) && javac $(JFLAGS) -d $(BINDIR)/$(SRCDIR) $(SRCDIR)/$(PACKAGE)/client/GETClient.java

compile_common: create_bin
	# compile src
	cd common && javac $(JFLAGS) -d $(BINDIR)/$(SRCDIR) ./$(SRCDIR)/http/*.java 
	cd common && javac $(JFLAGS) -d $(BINDIR)/$(SRCDIR) ./$(SRCDIR)/http/messages/*.java
	cd common && javac $(JFLAGS) -d $(BINDIR)/$(SRCDIR) ./$(SRCDIR)/util/*.java
	
	# compile tests
	cd common && javac $(JFLAGS) -d $(BINDIR)/test ./$(TESTDIR)/http/*.java
	cd common && javac $(JFLAGS) -d $(BINDIR)/test ./$(TESTDIR)/http/messages/*.java

run_as:
	cd AS && java $(JFLAGS) $(PACKAGE).aggregationserver.AggregationServer --default

run_cs:
	cd CS && java $(JFLAGS) $(PACKAGE).contentserver.ContentServer --default

run_client:
	cd GET && java $(JFLAGS) $(PACKAGE).client.GETClient --default

test_as:
	cd AS && java $(JFLAGS) $(PACKAGE).aggregationserver.AggregationServer --test

test_cs:
	cd CS && java $(JFLAGS) $(PACKAGE).contentserver.ContentServer --test

test_client:
	cd GET && java $(JFLAGS) $(PACKAGE).client.GETClient --test

test_common_http:
	java $(JFLAGS) org.junit.runner.JUnitCore http.HTTPConnectionTest

test_common_http_messages:
	java $(JFLAGS) org.junit.runner.JUnitCore http.messages.HTTPMessageTypeTest
	java $(JFLAGS) org.junit.runner.JUnitCore http.messages.HTTPMessageConnectionTest
	java $(JFLAGS) org.junit.runner.JUnitCore http.messages.HTTPMessageHeaderTest
	java $(JFLAGS) org.junit.runner.JUnitCore http.messages.HTTPMessageBodyTest
	java $(JFLAGS) org.junit.runner.JUnitCore http.messages.HTTPRequestTest
	java $(JFLAGS) org.junit.runner.JUnitCore http.messages.HTTPResponseTest

test_common_util:
	java $(JFLAGS) common.util.CLI
	java $(JFLAGS) common.util.IOUtility
	java $(JFLAGS) common.util.JSONObject
	java $(JFLAGS) common.util.LamportClock
	java $(JFLAGS) common.util.Math
	java $(JFLAGS) common.util.TimeZoneConverter

test_common: test_common_http test_common_http_messages test_common_util

test_all: test_as test_cs test_client test_common

# Clean targets
clean:
	rm -rf ./bin
