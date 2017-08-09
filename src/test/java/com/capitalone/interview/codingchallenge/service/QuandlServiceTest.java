package com.capitalone.interview.codingchallenge.service;

import com.capitalone.interview.codingchallenge.dto.QuandlResponse;
import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.time.LocalDate;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

public class QuandlServiceTest
{
    QuandlService service;

    @Before
    public void setup()
    {
        service = new QuandlService();
        service.status = mock(Health.Builder.class);
        service.restTemplate = mock(RestTemplate.class);
        service.apiKey = "test-api-key";
        service.endpoint = URI.create("http://localhost");
        service.path = "test-path";
    }

    @Test
    public void invoke_success() throws Exception
    {
        QuandlResponse response = mock(QuandlResponse.class);
        when(service.restTemplate.getForObject(anyString(), any())).thenReturn(response);

        service.invoke(asList("a", "b", "c"), LocalDate.parse("2001-07-09"), LocalDate.parse("2013-03-03"));
        verify(service.restTemplate).getForObject("http://localhost/test-path?date.gte=2001-07-09&date.lt=2013-03-03&ticker=a,b,c&qopts.columns=ticker,date,open,close,low,high,volume&api_key=test-api-key", QuandlResponse.class);
        verify(service.status).status(Status.UP);
    }

    @Test
    public void invoke_restException()
    {
        when(service.restTemplate.getForObject(anyString(), any())).thenThrow(RestClientException.class);

        assertThatThrownBy(() -> service.invoke(asList("a", "b", "c"), LocalDate.parse("2001-07-09"), LocalDate.parse("2013-03-03")))
                .isInstanceOf(RestClientException.class);
        verify(service.restTemplate).getForObject(anyString(), eq(QuandlResponse.class));
        verifyZeroInteractions(service.status);
    }

    @Test
    public void health()
    {
        DataLoader dataLoader = new DataLoader();
        assertThat(dataLoader.health().getStatus().getCode()).isEqualTo("OUT_OF_SERVICE");

        dataLoader.status.status("test-status");
        assertThat(dataLoader.health().getStatus().getCode()).isEqualTo("test-status");
    }
}