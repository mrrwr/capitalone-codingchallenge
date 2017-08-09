package com.capitalone.interview.codingchallenge.dto;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static java.lang.Math.min;
import static java.util.Collections.EMPTY_LIST;
import static java.util.stream.Collectors.*;
import static java.util.stream.IntStream.range;

/**
 * For data format, see https://docs.quandl.com/docs/in-depth-usage-1
 */
public class QuandlResponse
{
    // enum values match column names from Quandl API
    public enum ColumnName
    {
        ticker,
        date,
        open,
        close,
        low,
        high,
        volume
    };

    private DataTable datatable = new DataTable();

    public DataTable getDatatable()
    {
        return datatable;
    }

    public Stream<Map<ColumnName, String>> rows()
    {
        List<ColumnName> fields = getDatatable().getColumns().stream()
                .map(Column::getName)
                .collect(toList());

        // create maps of (column name, field value)
        return getDatatable().getData().stream().map(data -> zipToMap(fields, data));
    }

    private Map<ColumnName, String> zipToMap(List<ColumnName> fields, List<String> data)
    {
        return range(0, min(fields.size(), data.size()))
            .boxed()
            .collect(toMap(fields::get, data::get));
    }

    public void setDatatable(DataTable datatable)
    {
        this.datatable = datatable;
    }

    public static class DataTable
    {
        private List<List<String>> data = EMPTY_LIST;
        private List<Column> columns = EMPTY_LIST;

        public List<List<String>> getData()
        {
            return data;
        }

        public void setData(List<List<String>> data)
        {
            this.data = data;
        }

        public List<Column> getColumns()
        {
            return columns;
        }

        public void setColumns(List<Column> columns)
        {
            this.columns = columns;
        }
    }

    public static class Column
    {
        private ColumnName name;
        private String type = "";

        public ColumnName getName()
        {
            return name;
        }

        public Column setName(ColumnName name)
        {
            this.name = name;
            return this;
        }

        public String getType()
        {
            return type;
        }

        public void setType(String type)
        {
            this.type = type;
        }

    }
}
