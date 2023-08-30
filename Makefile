JFLAGS = -d src

SRC := $(wildcard src/$(PACKAGE_PATH)/*.java)

all: compile

compile:
	javac $(JFLAGS) -cp $(LOGGING) $(SRC)

run: compile
	java -cp $(LOGGING):src $(CALCULATOR_SERVER) &
	sleep 2
	java -cp src $(CALCULATOR_CLIENT)

# test: compile
# 	java -cp ./lib/*:./src:./test/resources:./test org.junit.runner.JUnitCore $(PACKAGE).$(UNIT_TEST) $(PACKAGE).$(INTEGRATION_TEST) $(PACKAGE).$(CLIENT_TEST)

# clean:
# 	$(RM) -r src/$(PACKAGE_PATH)/*.class test/$(PACKAGE_PATH)/*.class