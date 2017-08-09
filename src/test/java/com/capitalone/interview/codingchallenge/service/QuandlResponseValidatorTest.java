package com.capitalone.interview.codingchallenge.service;

import com.capitalone.interview.codingchallenge.dto.QuandlResponse;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;

public class QuandlResponseValidatorTest
{
    QuandlResponse response;
    QuandlResponseValidator validator;

    @Before
    public void setup()
    {
        validator = new QuandlResponseValidator();
        validator.tickers = asList("test-ticker");

        response = new QuandlResponse();
        response.getDatatable().setColumns(Stream.of(QuandlResponse.ColumnName.values())
                .map(name -> new QuandlResponse.Column().setName(name))
                .collect(toList()));
        List<String> columnValues = response.getDatatable().getColumns().stream()
                .map(col -> "test-" + col.getName())
                .collect(toList());
        response.getDatatable().setData(asList(columnValues));
    }

    @Test
    public void test_validResponse()
    {
        boolean valid = validator.test(response);
        assertThat(valid).isEqualTo(true);
    }

    @Test
    public void test_nullResponse()
    {
        boolean valid = validator.test(null);
        assertThat(valid).isEqualTo(false);
    }

    @Test
    public void test_noRecords()
    {
        response.getDatatable().setData(Collections.emptyList());

        boolean valid = validator.test(response);
        assertThat(valid).isEqualTo(false);
    }

    @Test
    public void test_missingTickerSymbols()
    {
        validator.tickers = asList("does not exist");

        boolean valid = validator.test(response);
        assertThat(valid).isEqualTo(false);
    }

    @Test
    public void test_dataMissingColumn()
    {
        response.getDatatable().getData().get(0).remove(QuandlResponse.ColumnName.volume.ordinal());    // not ticker

        boolean valid = validator.test(response);
        assertThat(valid).isEqualTo(false);
    }

    @Test
    public void test_invalidColumns()
    {
        response.getDatatable().getColumns().remove(QuandlResponse.ColumnName.volume.ordinal()); // not ticker

        boolean valid = validator.test(response);
        assertThat(valid).isEqualTo(false);
    }
}