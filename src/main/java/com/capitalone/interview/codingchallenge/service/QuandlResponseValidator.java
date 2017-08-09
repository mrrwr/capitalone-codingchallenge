package com.capitalone.interview.codingchallenge.service;

import com.capitalone.interview.codingchallenge.dto.QuandlResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import static com.capitalone.interview.codingchallenge.dto.QuandlResponse.ColumnName.ticker;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toSet;

/**
 * Ensures that the response data received from Quandl is in the expected format and contains
 * all results.
 */
@Component
public class QuandlResponseValidator implements Predicate<QuandlResponse>
{
    private static final Logger log = LoggerFactory.getLogger(QuandlResponseValidator.class);

    @Value("${app.tickers}")
    List<String> tickers;

    @Override
    public boolean test(QuandlResponse response)
    {
        List<List<String>> data = Optional.ofNullable(response)
                .map(QuandlResponse::getDatatable)
                .map(QuandlResponse.DataTable::getData)
                .orElse(emptyList());

        // ensure days returned -- we can be smarter and validate that all expected days are present
        if(data.size() == 0)
        {
            log.debug("Response contains no records");
            return false;
        }
        // ensure data exists for all tickers exists -- just validate we got data for any ticker
        if(!response.rows().map(map -> map.get(ticker)).collect(toSet()).containsAll(tickers))
        {
            log.debug("Response missing expected tickers: {}", tickers);
            return false;
        }
        // ensure all expected columns are returned
        if(response.getDatatable().getColumns().size() != QuandlResponse.ColumnName.values().length)
        {
            log.debug("Response missing columns");
            return false;
        }
        // ensure at least one record contains all columns
        if(response.rows().findFirst().get().size() != QuandlResponse.ColumnName.values().length)
        {
            log.debug("Response data missing fields");
            return false;
        }
        // ensure result is not paged, we got all data -- initial project has small data size, so don't bother checking

        // can also validate column type if we cared to

        log.debug("Valid response");
        return true;
    }
}
