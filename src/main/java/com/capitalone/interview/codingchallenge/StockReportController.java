package com.capitalone.interview.codingchallenge;

import com.capitalone.interview.codingchallenge.dto.*;
import com.capitalone.interview.codingchallenge.repository.StockRecordRepository;
import com.capitalone.interview.codingchallenge.repository.StockReportRepository;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping(value = "/v1/", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
public class StockReportController
{
    private static final Logger log = LoggerFactory.getLogger(StockReportController.class);

    @Autowired
    StockRecordRepository records;

    @Autowired
    StockReportRepository reports;

    /**
     * Return all records for the given security. If none are found or the ticker is invalid, return
     * an empty list.
     *
     * @param ticker
     * @return
     */
    @ApiOperation("Find all records for the given ticker symbol.")
    @RequestMapping(value = "stock/{ticker}")
    public List<StockRecord> stockRecords(@PathVariable String ticker)
    {
        return records.findAllByTickerIgnoringCase(ticker);
    }

    /**
     * Displays the Average Monthly Open and Close prices for each security for each month of data in
     * the data set. The securities to use are: COF, GOOGL, and MSFT.  Perform this analysis for Jan -
     * June of 2017 Output the data in the below format, or optionally in a prettier format if you see
     * fit.
     * <p>
     * <p>
     * {"GOOGL": {"month":"2017-01", "average_open": "815.43", "average_close": "$818.34"},
     * {"month":"2017-02", "average_open": "825.87", "average_close": "$822.73"}, ...
     * {"month":"2017-05", "average_open": "945.24", "average_close": "$951.52"}, {"month":"2017-06",
     * "average_open": "975.37", "average_close": "$977.11"}}
     */
    @ApiOperation("Finds the average monthly open and close values for all securities in the dataset.")
    @RequestMapping("averages")
    public List<StockAverage> averages()
    {
        return reports.findAveragesByMonth();
    }

    /**
     * We’d like to know which day in our data set would provide the highest amount of profit for
     * each security if purchased at the day’s low and sold at the day’s high.
     * Please display the ticker symbol, date, and the amount of profit.
     */
    @ApiOperation("Finds the day which would give the maximum profit for each security, if bought at the day's low and sold at the day's high.")
    @RequestMapping("max-daily-profit")
    public List<MaxProfit> maxDailyProfit()
    {
        return reports.findMaxProfitableDayBySecurity();
    }

    /**
     * We’d like to know which days generated unusually high activity for our securities.  Please
     * display the ticker symbol, date, and volume for each day where the volume was more than 10%
     * higher than the security’s average volume (Note: You’ll need to calculate the average volume,
     * and should display that somewhere too).
     */
    @ApiOperation("Finds the days which had above-average trading volume for each security.")
    @RequestMapping("busy-day")
    public List<BusyDay> busiestDays()
    {
        return reports.findBusiestDaysBySecurity();
    }

    /**
     * Which security had the most days where the closing price was lower than the opening price.
     * Please display the ticker symbol and the number of days that security’s closing price was lower
     * than that day’s opening price.
     */
    @ApiOperation("Finds the security which had the most days where closing price is lower than the opening price.")
    @RequestMapping("biggest-loser")
    public BiggestLoser biggestLoser()
    {
        return reports.findSecuritiesWithMostLosingDays();
    }

    @ExceptionHandler(DataAccessException.class)
    @ResponseStatus(value = HttpStatus.SERVICE_UNAVAILABLE)
    protected String handleDatabaseException(HttpServletRequest request, Exception ex)
    {
        log.error("Uncaught database exception", ex);
        return "Database unavailable";
    }
}
