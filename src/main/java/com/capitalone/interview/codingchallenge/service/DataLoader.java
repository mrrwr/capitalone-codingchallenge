package com.capitalone.interview.codingchallenge.service;

import com.capitalone.interview.codingchallenge.dto.QuandlResponse;
import com.capitalone.interview.codingchallenge.dto.StockRecord;
import com.capitalone.interview.codingchallenge.repository.StockRecordRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.*;
import org.springframework.boot.actuate.health.Health.Builder;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static java.time.LocalDate.parse;
import static java.util.stream.Collectors.toList;
import static org.springframework.boot.actuate.health.Status.*;

@Component
public class DataLoader implements HealthIndicator
{
    private static final Logger log = LoggerFactory.getLogger(DataLoader.class);

    Health.Builder status = new Builder(Status.OUT_OF_SERVICE);

    @Autowired
    QuandlService quandlService;

    @Autowired
    StockRecordRepository repository;

    @Value("${app.tickers}")
    List<String> tickers;

    @Value("${app.start_date}")
    String start;

    @Value("${app.end_date}")
    String end;

    @Autowired
    QuandlRecordConverter converter;

    @Autowired
    QuandlResponseValidator validator;

    /**
     * Asynchronously initialize the database.
     */
    @PostConstruct
    public void init()
    {
        CompletableFuture.<Void>supplyAsync(() ->
        {
            populateDatabase();
            return null;
        });
    }

    /**
     * Request data from quandl service, convert DTO response to entity instances, and save to the database.
     * Log status at each step and publish status to the /health endpoint.
     */
    public void populateDatabase()
    {
        try
        {
            log.debug("Retrieving data for tickers: {}, start: {}, end: {}", tickers, start, end);
            status.status("REQUESTING_DATA");
            QuandlResponse stockData = quandlService.invoke(tickers, parse(start), parse(end));

            log.debug("Validating response");
            status.status("VALIDATING_RESPONSE");
            if(!validator.test(stockData))
            {
                throw new IllegalStateException("Invalid response: " + stockData);
            }

            log.debug("Attempting to convert {} records to StockRecord instances.", stockData.getDatatable().getData().size());
            status.status("CONVERTING_RESPONSE");
            List<StockRecord> records = stockData.rows().map(converter).collect(toList());

            log.debug("Attempting to save {} StockRecord instances.", records.size());
            status.status("LOADING_DATABASE");
            repository.save(records);

            log.debug("Initialization successful.");
            status.status(UP);
        }
        catch(Exception e)
        {
            log.error("Could not complete initial database population", e);
            status.status(DOWN);
        }
    }

    @Override
    public Health health()
    {
        return status.build();
    }
}
