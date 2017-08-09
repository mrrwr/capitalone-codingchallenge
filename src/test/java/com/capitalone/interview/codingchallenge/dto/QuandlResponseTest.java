package com.capitalone.interview.codingchallenge.dto;

import org.junit.Test;

import java.util.*;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;

public class QuandlResponseTest
{
    @Test
    public void map_streamsMappedDataset()
    {
        QuandlResponse.Column column1 = new QuandlResponse.Column().setName(QuandlResponse.ColumnName.ticker);
        QuandlResponse.Column column2 = new QuandlResponse.Column().setName(QuandlResponse.ColumnName.date);

        QuandlResponse response = new QuandlResponse();
        response.getDatatable().setColumns(asList(column1, column2));
        response.getDatatable().setData(asList(asList("a", "b"), asList("c", "d")));

        List<Map<QuandlResponse.ColumnName, String>> rows = response.rows().collect(toList());
        assertThat(rows).hasSize(2);
        assertThat(rows.get(0))
                .hasSize(2)
                .containsEntry(column1.getName(), "a")
                .containsEntry(column2.getName(), "b");
        assertThat(rows.get(1))
                .hasSize(2)
                .containsEntry(column1.getName(), "c")
                .containsEntry(column2.getName(), "d");
    }

    @Test
    public void map_excludesFieldsWhenColumnsDoNotMatch()
    {
        QuandlResponse response = new QuandlResponse();
        response.getDatatable().setColumns(Collections.emptyList());
        response.getDatatable().setData(asList(asList("a", "b"), asList("c", "d")));

        List<Map<QuandlResponse.ColumnName, String>> rows = response.rows().collect(toList());
        assertThat(rows).hasSize(2);
        assertThat(rows.get(0)).hasSize(0); // no columns
        assertThat(rows.get(1)).hasSize(0); // no columns
    }
}