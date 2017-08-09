package com.capitalone.interview.codingchallenge.service;

import com.capitalone.interview.codingchallenge.dto.QuandlResponse;
import com.capitalone.interview.codingchallenge.dto.StockRecord;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.function.Function;

import static com.capitalone.interview.codingchallenge.dto.QuandlResponse.ColumnName.*;
import static java.lang.Double.parseDouble;
import static java.time.LocalDate.parse;

/**
 * Converts map of fields to the corresponding attribute in a StockRecord.
 */
@Component
public class QuandlRecordConverter implements Function<Map<QuandlResponse.ColumnName, String>, StockRecord>
{
    /**
     * @throws IllegalStateException if the field is missing or cannot be converted
     */
    @Override
    public StockRecord apply(Map<QuandlResponse.ColumnName, String> map)
    {
        try
        {
            StockRecord record = new StockRecord();
            record.setTicker(map.get(ticker));
            record.setDate(parse(map.get(date)));
            record.setLow(parseDouble(map.get(low)));
            record.setHigh(parseDouble(map.get(high)));
            record.setVolume(parseDouble(map.get(volume)));
            record.setOpen(parseDouble(map.get(open)));
            record.setClose(parseDouble(map.get(close)));
            return record;
        }
        catch(Exception e)
        {
            throw new IllegalStateException("Could not convert to StockRecord: " + map);
        }
    }

}
