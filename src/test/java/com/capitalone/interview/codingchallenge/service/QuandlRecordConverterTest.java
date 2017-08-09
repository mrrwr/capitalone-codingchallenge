package com.capitalone.interview.codingchallenge.service;

import com.capitalone.interview.codingchallenge.dto.QuandlResponse;
import com.capitalone.interview.codingchallenge.dto.StockRecord;
import org.junit.Test;

import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class QuandlRecordConverterTest
{
    @Test
    public void createRecord()
    {

        Map<QuandlResponse.ColumnName, String> record1 = new HashMap<>();
        record1.put(QuandlResponse.ColumnName.ticker, "test-record1");
        record1.put(QuandlResponse.ColumnName.date, "2000-07-07");
        record1.put(QuandlResponse.ColumnName.open, "17");
        record1.put(QuandlResponse.ColumnName.close, "19");
        record1.put(QuandlResponse.ColumnName.low, "27");
        record1.put(QuandlResponse.ColumnName.high, "31");
        record1.put(QuandlResponse.ColumnName.volume, "37");

        QuandlRecordConverter converter = new QuandlRecordConverter();
        StockRecord record = converter.apply(record1);
        assertThat(record.getTicker()).isEqualTo("test-record1");
        assertThat(record.getDate()).isEqualTo(LocalDate.parse("2000-07-07"));
        assertThat(record.getOpen()).isEqualTo(17);
        assertThat(record.getClose()).isEqualTo(19);
        assertThat(record.getLow()).isEqualTo(27);
        assertThat(record.getHigh()).isEqualTo(31);
        assertThat(record.getVolume()).isEqualTo(37);
    }

    @Test
    public void createRecord_invalidConversionThrowsException()
    {
        Map<QuandlResponse.ColumnName, String> record1 = new HashMap<>();
        record1.put(QuandlResponse.ColumnName.ticker, "test-record1");
        record1.put(QuandlResponse.ColumnName.date, "2000-07-07");
        record1.put(QuandlResponse.ColumnName.open, "17");
        record1.put(QuandlResponse.ColumnName.close, "19");
        record1.put(QuandlResponse.ColumnName.low, "27");
        record1.put(QuandlResponse.ColumnName.high, "31");
        // all valid except this
        record1.put(QuandlResponse.ColumnName.volume, "not-a-number");

        QuandlRecordConverter converter = new QuandlRecordConverter();
        assertThatThrownBy(() -> converter.apply(record1)).isInstanceOf(IllegalStateException.class);
    }

    @Test
    public void createRecord_missingEntryThrowsException()
    {
        QuandlRecordConverter converter = new QuandlRecordConverter();
        assertThatThrownBy(() -> converter.apply(Collections.emptyMap())).isInstanceOf(IllegalStateException.class);
    }

}