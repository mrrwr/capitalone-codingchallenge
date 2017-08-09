package com.capitalone.interview.codingchallenge;

import com.capitalone.interview.codingchallenge.dto.*;
import com.capitalone.interview.codingchallenge.repository.StockRecordRepository;
import com.capitalone.interview.codingchallenge.repository.StockReportRepository;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

public class StockReportControllerTest
{
    private StockReportController controller;

    @Before
    public void setup()
    {
        controller = new StockReportController();
        controller.reports = mock(StockReportRepository.class);
        controller.records = mock(StockRecordRepository.class);
    }
    
    @Test
    public void stockRecords() throws Exception
    {
        List<StockRecord> result = asList(mock(StockRecord.class));
        when(controller.records.findAllByTickerIgnoringCase(anyString())).thenReturn(result);

        List<StockRecord> response = controller.stockRecords("test-ticker");
        assertThat(response).isEqualTo(result);
        verify(controller.records).findAllByTickerIgnoringCase("test-ticker");
        verifyNoMoreInteractions(controller.records, controller.reports);
    }

    @Test
    public void averages() throws Exception
    {
        List<StockAverage> result = asList(mock(StockAverage.class));
        when(controller.averages()).thenReturn(result);

        List<StockAverage> response = controller.averages();
        assertThat(response).isEqualTo(result);
        verify(controller.reports).findAveragesByMonth();
        verifyNoMoreInteractions(controller.records, controller.reports);
    }

    @Test
    public void maxDailyProfit() throws Exception
    {
        List<MaxProfit> result = asList(mock(MaxProfit.class));
        when(controller.maxDailyProfit()).thenReturn(result);

        List<MaxProfit> response = controller.maxDailyProfit();
        assertThat(response).isEqualTo(result);
        verify(controller.reports).findMaxProfitableDayBySecurity();
        verifyNoMoreInteractions(controller.records, controller.reports);
    }

    @Test
    public void busiestDays() throws Exception
    {
        List<BusyDay> result = asList(mock(BusyDay.class));
        when(controller.busiestDays()).thenReturn(result);

        List<BusyDay> response = controller.busiestDays();
        assertThat(response).isEqualTo(result);
        verify(controller.reports).findBusiestDaysBySecurity();
        verifyNoMoreInteractions(controller.records, controller.reports);
    }

    @Test
    public void biggestLoser() throws Exception
    {
        BiggestLoser result = mock(BiggestLoser.class);
        when(controller.biggestLoser()).thenReturn(result);

        BiggestLoser response = controller.biggestLoser();
        assertThat(response).isEqualTo(result);
        verify(controller.reports).findSecuritiesWithMostLosingDays();
        verifyNoMoreInteractions(controller.records, controller.reports);
    }

}