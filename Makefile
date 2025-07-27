SBT ?= sbt

compile:
	$(SBT) compile

reload:
	$(SBT) reload

test: reload fmt
	$(SBT) test

run:
	$(SBT) run

run-linear:
	$(SBT) "project examples" "runMain rcflow.examples.LinearRegression"

run-mg:
	$(SBT) "project examples" "runMain rcflow.examples.MackeyGlassForecast"

bench:
	$(SBT) rcflow-bench/Jmh/run

docs:
	$(SBT) doc

fmt:
	$(SBT) scalafmtAll

lint:
	$(SBT) scalafmtCheckAll

clean:
	$(SBT) clean

package:
	$(SBT) package

ci:
	$(MAKE) lint && $(MAKE) test
