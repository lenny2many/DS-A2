# Variables
JFLAGS = -cp ".:./src/main/java/:./lib/logging/*:../bin"
SRCDIR = src/main/java
BINDIR = ./bin
COMMON = au/edu/adelaide

# Target: all
all: compile

# Compile targets
compile: create_bin compile_as compile_cs compile_client compile_common

create_bin:
	mkdir -p $(BINDIR)

compile_as: create_bin
	cd AS && javac $(JFLAGS) -d .$(BINDIR) $(SRCDIR)/$(COMMON)/aggregationserver/AggregationServer.java

compile_cs: create_bin
	cd CS && javac $(JFLAGS) -d .$(BINDIR) $(SRCDIR)/$(COMMON)/contentserver/ContentServer.java

compile_client: create_bin
	cd GET && javac $(JFLAGS) -d .$(BINDIR) $(SRCDIR)/$(COMMON)/client/GETClient.java

compile_common: create_bin
	javac $(JFLAGS) -d $(BINDIR) ./common/http/*.java
	javac $(JFLAGS) -d $(BINDIR) ./common/http/messages/*.java
	javac $(JFLAGS) -d $(BINDIR) ./common/util/*.java

run_as:
	cd AS && java $(JFLAGS) $(COMMON).aggregationserver.AggregationServer --default

run_cs:
	cd CS && java $(JFLAGS) $(COMMON).contentserver.ContentServer --default

run_client:
	cd GET && java $(JFLAGS) $(COMMON).client.GETClient --default

# Clean targets
clean:
	rm -rf $(BINDIR)
