package com.capitalone.interview.codingchallenge;

import com.capitalone.interview.codingchallenge.dto.*;
import com.capitalone.interview.codingchallenge.repository.StockReportRepository;
import org.assertj.core.util.DoubleComparator;
import org.awaitility.Awaitility;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toSet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = StockReportApplication.class, webEnvironment = RANDOM_PORT)
public class StockReportControllerIT
{
    @Autowired
    TestRestTemplate restTemplate;

    @SpyBean
    StockReportRepository repository;

    @Before
    public void setup()
    {
        // wait for asynchronous data population to complete
        Awaitility.await().atMost(60, TimeUnit.SECONDS)
                .until(() -> restTemplate.getForEntity("/health", String.class).getStatusCode() == HttpStatus.OK);
    }

    @Test
    public void stockRecords_missingTickerThrows404()
    {
        ResponseEntity<Map> response = restTemplate.getForEntity("/v1/stock/", Map.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody())
                .containsEntry("status", 404)
                .containsEntry("path", "/v1/stock/")
                .containsKey("timestamp")
                .containsKey("message");
    }

    @Test
    public void stockRecords_invalidTickerReturnsEmptySet()
    {
        ResponseEntity<List> response = restTemplate.getForEntity("/v1/stock/ticker-does-not-exist", List.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(0);
    }

    @Test
    public void stockRecords()
    {
        ResponseEntity<StockRecord[]> response = restTemplate.getForEntity("/v1/stock/MSFT", StockRecord[].class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(125);
    }

    @Test
    public void averages()
    {
        ResponseEntity<StockAverage[]> response = restTemplate.getForEntity("/v1/averages", StockAverage[].class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody())
            .usingComparatorForElementFieldsWithType(new DoubleComparator(0.01), Double.class)
            .usingFieldByFieldElementComparator()
            .containsExactlyInAnyOrder(
                new StockAverage("COF", "2017-01", 88.29, 88.26),
                new StockAverage("COF", "2017-02", 89.85, 90.19),
                new StockAverage("COF", "2017-03", 89.26, 88.92),
                new StockAverage("COF", "2017-04", 83.41, 83.23),
                new StockAverage("COF", "2017-05", 80.64, 80.50),
                new StockAverage("COF", "2017-06", 80.09, 80.32),
                new StockAverage("GOOGL", "2017-01", 829.85, 830.24),
                new StockAverage("GOOGL", "2017-02", 836.15, 836.75),
                new StockAverage("GOOGL", "2017-03", 853.85, 853.78),
                new StockAverage("GOOGL", "2017-04", 860.07, 861.37),
                new StockAverage("GOOGL", "2017-05", 959.59, 961.65),
                new StockAverage("GOOGL", "2017-06", 975.78, 973.37),
                new StockAverage("MSFT", "2017-01", 63.18, 63.19),
                new StockAverage("MSFT", "2017-02", 64.13, 64.11),
                new StockAverage("MSFT", "2017-03", 64.76, 64.84),
                new StockAverage("MSFT", "2017-04", 66.23, 66.17),
                new StockAverage("MSFT", "2017-05", 68.82, 68.91),
                new StockAverage("MSFT", "2017-06", 70.56, 70.51)
            );
    }

    @Test
    public void maxDailyProfit()
    {
        ResponseEntity<MaxProfit[]> response = restTemplate.getForEntity("/v1/max-daily-profit", MaxProfit[].class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody())
            .usingComparatorForElementFieldsWithType(new DoubleComparator(0.01), Double.class)
            .usingFieldByFieldElementComparator()
            .containsExactlyInAnyOrder(
                new MaxProfit("COF", LocalDate.of(2017, 3, 21), 3.76),
                new MaxProfit("GOOGL", LocalDate.of(2017, 6, 9), 52.13),
                new MaxProfit("MSFT", LocalDate.of(2017, 6, 9), 3.49)
        );
    }

    @Test
    public void busiestDays()
    {
        ResponseEntity<BusyDay[]> response = restTemplate.getForEntity("/v1/busy-day", BusyDay[].class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(103);
        assertThat(Stream.of(response.getBody()).filter(day -> day.getVolume() > day.getAverageVolume()).count()).isEqualTo(103);
        assertThat(Stream.of(response.getBody()).map(day -> day.getTicker()).collect(toSet()))
            .containsExactlyInAnyOrder("MSFT", "COF", "GOOGL");
    }

    @Test
    public void biggestLoser()
    {
        ResponseEntity<BiggestLoser> response = restTemplate.getForEntity("/v1/biggest-loser", BiggestLoser.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody())
                .isEqualToComparingFieldByField(new BiggestLoser("COF", 62));
    }

    @Test
    public void databaseError_throws503()
    {
        when(repository.findAveragesByMonth()).thenThrow(new DataAccessResourceFailureException(""));

        ResponseEntity<String> response = restTemplate.getForEntity("/v1/averages", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
    }

    @Test
    public void metrics()
    {
        ResponseEntity<Map> response = restTemplate.getForEntity("/metrics", Map.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsKey("mem");
        assertThat(response.getBody().size()).isGreaterThanOrEqualTo(10);
    }

    @Test
    public void health()
    {
        ResponseEntity<String> response = restTemplate.getForEntity("/health", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}