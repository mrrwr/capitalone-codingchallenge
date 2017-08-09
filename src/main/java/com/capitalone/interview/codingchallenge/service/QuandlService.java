package com.capitalone.interview.codingchallenge.service;

import com.capitalone.interview.codingchallenge.dto.QuandlResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.*;
import org.springframework.boot.actuate.health.Health.Builder;
import org.springframework.retry.annotation.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;

import static java.lang.String.join;
import static java.util.stream.Collectors.joining;
import static org.springframework.boot.actuate.health.Status.DOWN;
import static org.springframework.util.CollectionUtils.isEmpty;

@Service
public class QuandlService implements HealthIndicator
{
    private static final Logger log = LoggerFactory.getLogger(QuandlService.class);

    Health.Builder status = new Builder(Status.OUT_OF_SERVICE);

    @Autowired
    RestTemplate restTemplate;

    @Value("${app.quandl.url}")
    URI endpoint;

    @Value("${app.quandl.dataset}")
    String path;

    @Value("${app.quandl.api_key}")
    String apiKey;

    @Recover
    public QuandlResponse retryAttemptsExceeded(RestClientException e) throws IllegalStateException
    {
        status.status(DOWN);
        throw new IllegalStateException("Could not invoke Quandl service", e);
    }

    /**
     * Invokes remote Quandl service for the requested tickers and date range. All parameters are
     * required. If invoking the service fails for any reason, retry up 3 times before quitting.
     *
     * @param tickers
     * @param start
     * @param end
     * @return
     * @throws IllegalStateException if service is unavailable or response is invalid
     */
    //
    // Don't retry client-side 4xx errors or invalid state
    @Retryable(maxAttempts = 3,
            backoff = @Backoff(delay = 10000, multiplier = 1),
            include = { RestClientException.class  },
            exclude = { HttpClientErrorException.class,IllegalStateException.class })
    public QuandlResponse invoke(List<String> tickers, LocalDate start, LocalDate end)
    {
        if(isEmpty(tickers) || start == null || end == null)
        {
            throw new IllegalStateException("Invalid parameters");
        }

        String columns = Stream.of(QuandlResponse.ColumnName.values())
                .map(QuandlResponse.ColumnName::name)
                .collect(joining(","));

        String uri = UriComponentsBuilder.fromUri(endpoint)
                .path(path)
                .queryParam("date.gte", start)
                .queryParam("date.lt", end)
                .queryParam("ticker", join(",", tickers))
                .queryParam("qopts.columns", columns)
                .queryParam("api_key", apiKey)
                .toUriString();

        log.info("Calling {}", uri);
        QuandlResponse response = restTemplate.getForObject(uri, QuandlResponse.class);
        status.status(Status.UP);
        return response;
    }

    @Override
    public Health health()
    {
        return status.build();
    }

    public void setEndpoint(URI endpoint)
    {
        this.endpoint = endpoint;
    }

    public void setApiKey(String apiKey)
    {
        this.apiKey = apiKey;
    }
}
