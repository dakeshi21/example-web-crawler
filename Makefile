clean:
	./gradlew clean

test: clean
	./gradlew test

crawl:
ifndef BASE_URL
	@echo "BASE_URL is undefined. Default value from the config file will be used instead."
endif
ifndef DOMAIN
	@echo "DOMAIN is undefined. Default value from the config file will be used instead."
endif
ifndef MAX_LEVEL
	@echo "MAX_LEVEL is undefined. Default value from the config file will be used instead."
endif
	./gradlew run --args='BASE_URL=${BASE_URL} DOMAIN=${DOMAIN}'