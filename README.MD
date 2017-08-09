# Coding challenge
This is a service that displays monthly averages of securities. It is built as a spring-boot service to query the Quandl dataset asynchronously and store the results locally in an embedded database. It's intended to operate as a materialized view service, where clients remain insulated from intermittent backend connectivity problems, and always get a valid response object of the current view state. This isn't the canonical source of the data, since it does not updates and and records can get stale.   

Data from Quandl is stored in an embedded database, and is not refreshed or retried if the initial load fails. Errors in the async tasks are logged, and reported via the [/health](http://localhost:8080/health) endpoint.

Main logic is in:
- [StockReportController](src/main/java/com/capitalone/interview/codingchallenge/StockReportController.java)
- [DataLoader](src/main/java/com/capitalone/interview/codingchallenge/service/DataLoader.java)
- [QuandlService](src/main/java/com/capitalone/interview/codingchallenge/service/QuandlService.java)
- [application.yml](src/main/resources/application.yml) - contains SQL queries

Main integration tests are in:
 - [StockReportRepositoryIT](src/test/java/com/capitalone/interview/codingchallenge/repository/StockReportRepositoryIT.java)
 - [QuandlServiceIT](src/test/java/com/capitalone/interview/codingchallenge/service/QuandlServiceIT.java)
 - [StockReportControllerIT](src/test/java/com/capitalone/interview/codingchallenge/StockReportControllerIT.java)

## Running the app
Build and run with docker:

    docker build -t coding-challenge .
    docker run -it -p 8080:8080 coding-challenge
    
Or build and run with maven and jdk8:
     
     mvn spring-boot:run
          
The service will be accessible at http://localhost:8080.

## Endpoints
- [/swagger-ui.html](http://localhost:8080/swagger-ui.html) - Swagger documentation and UI
- [/v1/averages](http://localhost:8080/v1/averages) - monthly average open and closing price for each security
- [/v1/max-daily-profit](http://localhost:8080/v1/max-daily-profit) - days which would see the most profit if bought at low and sold at high 
- [/v1/busy-day](http://localhost:8080/v1/busy-day) - days with above-average volume for the security
- [/v1/biggest-loser](http://localhost:8080/v1/biggest-loser) - security with most days where closing price < opening price 
- [/health](http://localhost:8080/health) - health checks, status of asynchronous Quandl queries
- [/metrics](http://localhost:8080/metrics) - internal application metrics

## Tests

To run unit and integration tests in docker, use:
    
    docker build -t coding-challenge .
    docker run -it coding-challenge clean verify
    
Or run unit and integration tests with maven:

    mvn clean verify
    