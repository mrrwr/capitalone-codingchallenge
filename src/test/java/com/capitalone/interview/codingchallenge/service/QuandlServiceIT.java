package com.capitalone.interview.codingchallenge.service;

import com.capitalone.interview.codingchallenge.StockReportApplication;
import com.capitalone.interview.codingchallenge.dto.QuandlResponse;
import com.capitalone.interview.codingchallenge.dto.QuandlResponse.ColumnName;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Status;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.*;

import java.time.LocalDate;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.*;
import static org.hamcrest.Matchers.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.annotation.DirtiesContext.MethodMode.BEFORE_METHOD;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = StockReportApplication.class)
@DirtiesContext(methodMode = BEFORE_METHOD)
public class QuandlServiceIT
{
    @Autowired
    QuandlService service;

    @SpyBean
    RestTemplate restTemplate;

    @MockBean
    DataLoader dataLoader;

    @Test
    public void invoke_successful() throws Exception
    {
        QuandlResponse response = service.invoke(asList("MSFT"), LocalDate.of(2017, 1, 3), LocalDate.of(2017, 1, 4));
        assertThat(response.getDatatable()).isNotNull();
        assertThat(response.getDatatable().getData()).hasSize(1);
        assertThat(response.getDatatable().getData().get(0)).hasSize(ColumnName.values().length);
        assertThat(response.getDatatable().getColumns()).hasSize(ColumnName.values().length);
        assertThat(response.getDatatable().getColumns()).extracting("name").containsAll(asList(ColumnName.values()));
        assertThat(service.health().getStatus()).isEqualTo(Status.UP);
    }

    @Test
    public void invoke_serverResponseError()
    {
        doThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR)).when(restTemplate).getForObject(anyString(), any());
        assertThatThrownBy(() -> service.invoke(asList("MSFT"), LocalDate.of(2017, 1, 3), LocalDate.of(2017, 1, 4)))
                .isInstanceOf(IllegalStateException.class);
        verify(restTemplate, times(3)).getForObject(argThat(containsString("MSFT")), eq(QuandlResponse.class));
        assertThat(service.health().getStatus()).isEqualTo(Status.DOWN);
    }

    @Test
    public void invoke_clientResponseError()
    {
        doThrow(new HttpClientErrorException(HttpStatus.UNAUTHORIZED)).when(restTemplate).getForObject(anyString(), any());

        // can force this by having an incorrect api key
        assertThatThrownBy(() -> service.invoke(asList("MSFT"), LocalDate.of(2017, 1, 3), LocalDate.of(2017, 1, 4)))
                .isInstanceOf(IllegalStateException.class);
        // does not retry
        verify(restTemplate, times(1)).getForObject(argThat(containsString("MSFT")), eq(QuandlResponse.class));
        assertThat(service.health().getStatus()).isEqualTo(Status.DOWN);
    }

    @Test
    public void invoke_networkException()
    {
        doThrow(new ResourceAccessException("")).when(restTemplate).getForObject(anyString(), any());

        assertThatThrownBy(() -> service.invoke(asList("MSFT"), LocalDate.of(2017, 1, 3), LocalDate.of(2017, 1, 4)))
                .isInstanceOf(IllegalStateException.class);
        verify(restTemplate, times(3)).getForObject(argThat(containsString("MSFT")), eq(QuandlResponse.class));
        assertThat(service.health().getStatus()).isEqualTo(Status.DOWN);
    }

    @Test
    public void invoke_retriesFailedAttempts()
    {
        doThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR))
                .doReturn(mock(QuandlResponse.class))
                .when(restTemplate).getForObject(anyString(), any());

        QuandlResponse response = service.invoke(asList("MSFT"), LocalDate.of(2017, 1, 3), LocalDate.of(2017, 1, 4));
        assertThat(response).isNotNull();
        assertThat(service.health().getStatus()).isEqualTo(Status.UP);
        verify(restTemplate, times(2)).getForObject(argThat(containsString("MSFT")), eq(QuandlResponse.class));
    }
}
