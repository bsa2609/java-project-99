.DEFAULT_GOAL := build-run

setup:
	./gradlew wrapper --gradle-version 8.10.2
	./gradlew clean build

clean:
	./gradlew clean

build:
	./gradlew clean build

install:
	./gradlew clean install

backend:
	./gradlew bootRun

test:
	./gradlew test

report:
	./gradlew jacocoTestReport

lint:
	./gradlew checkstyleMain

check-deps:
	./gradlew dependencyUpdates -Drevision=release

build-run: build backend

.PHONY: build