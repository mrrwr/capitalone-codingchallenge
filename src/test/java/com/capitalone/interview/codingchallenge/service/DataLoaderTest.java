package com.capitalone.interview.codingchallenge.service;

import com.capitalone.interview.codingchallenge.dto.QuandlResponse;
import com.capitalone.interview.codingchallenge.dto.StockRecord;
import com.capitalone.interview.codingchallenge.repository.StockRecordRepository;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;

import java.time.LocalDate;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class DataLoaderTest
{
    DataLoader dataLoader;

    @Before
    public void setup()
    {
        dataLoader = new DataLoader();
        dataLoader.status = mock(Health.Builder.class);
        dataLoader.quandlService = mock(QuandlService.class);
        dataLoader.repository = mock(StockRecordRepository.class);
        dataLoader.converter = mock(QuandlRecordConverter.class);
        dataLoader.validator = mock(QuandlResponseValidator.class);
        dataLoader.tickers = asList("TEST1", "TEST2", "TEST3");
        dataLoader.start = "2010-01-01";
        dataLoader.end = "2010-03-03";
    }

    @Test
    public void populateDatabase_success() throws Exception
    {
        QuandlResponse response = mock(QuandlResponse.class, RETURNS_MOCKS);
        Map<QuandlResponse.ColumnName, String> map1 = mock(Map.class);
        Map<QuandlResponse.ColumnName, String> map2 = mock(Map.class);
        StockRecord record1 = mock(StockRecord.class);
        StockRecord record2 = mock(StockRecord.class);

        when(dataLoader.quandlService.invoke(anyList(), any(LocalDate.class), any(LocalDate.class))).thenReturn(response);
        when(response.rows()).thenReturn(Stream.of(map1, map2));
        when(dataLoader.validator.test(any(QuandlResponse.class))).thenReturn(true);
        when(dataLoader.converter.apply(anyMap())).thenReturn(record1, record2);

        dataLoader.populateDatabase();

        InOrder inOrder = inOrder(dataLoader.status, dataLoader.validator, dataLoader.converter, dataLoader.quandlService, dataLoader.repository);
        inOrder.verify(dataLoader.status).status("REQUESTING_DATA");
        inOrder.verify(dataLoader.quandlService).invoke(eq(dataLoader.tickers), eq(LocalDate.parse("2010-01-01")), eq(LocalDate.parse("2010-03-03")));
        inOrder.verify(dataLoader.status).status("VALIDATING_RESPONSE");
        inOrder.verify(dataLoader.validator).test(response);
        inOrder.verify(dataLoader.status).status("CONVERTING_RESPONSE");
        inOrder.verify(dataLoader.converter).apply(map1);
        inOrder.verify(dataLoader.converter).apply(map2);
        inOrder.verify(dataLoader.status).status("LOADING_DATABASE");
        inOrder.verify(dataLoader.repository).save(eq(asList(record1, record2)));
        inOrder.verify(dataLoader.status).status(Status.UP);
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void populateDatabase_failedQuandlRequest()
    {
        when(dataLoader.quandlService.invoke(anyList(), any(LocalDate.class), any(LocalDate.class))).thenThrow(IllegalStateException.class);

        dataLoader.populateDatabase();

        InOrder inOrder = inOrder(dataLoader.status, dataLoader.validator, dataLoader.quandlService, dataLoader.repository);
        verify(dataLoader.status).status("REQUESTING_DATA");
        inOrder.verify(dataLoader.quandlService).invoke(eq(dataLoader.tickers), eq(LocalDate.parse("2010-01-01")), eq(LocalDate.parse("2010-03-03")));
        inOrder.verify(dataLoader.status).status(Status.DOWN);
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void populateDatabase_failedValidation()
    {
        QuandlResponse response = mock(QuandlResponse.class, RETURNS_MOCKS);
        when(dataLoader.quandlService.invoke(anyList(), any(LocalDate.class), any(LocalDate.class))).thenReturn(response);

        dataLoader.populateDatabase();

        InOrder inOrder = inOrder(dataLoader.status, dataLoader.validator, dataLoader.quandlService, dataLoader.repository);
        verify(dataLoader.status).status("REQUESTING_DATA");
        inOrder.verify(dataLoader.quandlService).invoke(eq(dataLoader.tickers), eq(LocalDate.parse("2010-01-01")), eq(LocalDate.parse("2010-03-03")));
        inOrder.verify(dataLoader.status).status("VALIDATING_RESPONSE");
        inOrder.verify(dataLoader.validator).test(response);
        inOrder.verify(dataLoader.status).status(Status.DOWN);
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void populateDatabase_failedEntityConversion()
    {
        QuandlResponse response = mock(QuandlResponse.class, RETURNS_MOCKS);
        Map<QuandlResponse.ColumnName, String> map1 = mock(Map.class);

        when(dataLoader.quandlService.invoke(anyList(), any(LocalDate.class), any(LocalDate.class))).thenReturn(response);
        when(response.rows()).thenReturn(Stream.of(map1));
        when(dataLoader.validator.test(any(QuandlResponse.class))).thenReturn(true);
        when(dataLoader.converter.apply(anyMap())).thenThrow(IllegalStateException.class);

        dataLoader.populateDatabase();

        InOrder inOrder = inOrder(dataLoader.status, dataLoader.validator, dataLoader.converter, dataLoader.quandlService, dataLoader.repository);
        inOrder.verify(dataLoader.status).status("REQUESTING_DATA");
        inOrder.verify(dataLoader.quandlService).invoke(eq(dataLoader.tickers), eq(LocalDate.parse("2010-01-01")), eq(LocalDate.parse("2010-03-03")));
        inOrder.verify(dataLoader.status).status("VALIDATING_RESPONSE");
        inOrder.verify(dataLoader.validator).test(response);
        inOrder.verify(dataLoader.status).status("CONVERTING_RESPONSE");
        inOrder.verify(dataLoader.converter).apply(map1);
        inOrder.verify(dataLoader.status).status(Status.DOWN);
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void populateDatabase_failedDatabaseSave()
    {
        QuandlResponse response = mock(QuandlResponse.class, RETURNS_MOCKS);
        Map<QuandlResponse.ColumnName, String> map1 = mock(Map.class);
        Map<QuandlResponse.ColumnName, String> map2 = mock(Map.class);
        StockRecord record1 = mock(StockRecord.class);
        StockRecord record2 = mock(StockRecord.class);

        when(dataLoader.quandlService.invoke(anyList(), any(LocalDate.class), any(LocalDate.class))).thenReturn(response);
        when(response.rows()).thenReturn(Stream.of(map1, map2));
        when(dataLoader.repository.save(anyList())).thenThrow(RuntimeException.class);
        when(dataLoader.validator.test(any(QuandlResponse.class))).thenReturn(true);
        when(dataLoader.converter.apply(anyMap())).thenReturn(record1, record2);

        dataLoader.populateDatabase();

        InOrder inOrder = inOrder(dataLoader.status, dataLoader.validator, dataLoader.converter, dataLoader.quandlService, dataLoader.repository);
        inOrder.verify(dataLoader.status).status("REQUESTING_DATA");
        inOrder.verify(dataLoader.quandlService).invoke(eq(dataLoader.tickers), eq(LocalDate.parse("2010-01-01")), eq(LocalDate.parse("2010-03-03")));
        inOrder.verify(dataLoader.status).status("VALIDATING_RESPONSE");
        inOrder.verify(dataLoader.validator).test(response);
        inOrder.verify(dataLoader.status).status("CONVERTING_RESPONSE");
        inOrder.verify(dataLoader.converter).apply(map1);
        inOrder.verify(dataLoader.converter).apply(map2);
        inOrder.verify(dataLoader.status).status("LOADING_DATABASE");
        inOrder.verify(dataLoader.repository).save(eq(asList(record1, record2)));
        inOrder.verify(dataLoader.status).status(Status.DOWN);
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void initStartsAsyncTask() throws InterruptedException
    {
        DataLoader dataLoader = spy(this.dataLoader);
        doNothing().when(dataLoader).populateDatabase();

        dataLoader.init();
        verify(dataLoader, timeout(500)).populateDatabase();
    }

    @Test
    public void health() throws Exception
    {
        DataLoader dataLoader = new DataLoader();
        assertThat(dataLoader.health().getStatus().getCode()).isEqualTo("OUT_OF_SERVICE");

        dataLoader.status.status("test-status");
        assertThat(dataLoader.health().getStatus().getCode()).isEqualTo("test-status");
    }

}