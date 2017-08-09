package com.capitalone.interview.codingchallenge.repository;

import com.capitalone.interview.codingchallenge.dto.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class StockReportRepository
{
    @Autowired
    JdbcTemplate jdbcTemplate;

    @Value("${query.find_averages_by_month}")
    String findAveragesByMonthQuery;

    @Value("${query.find_biggest_loser}")
    String findBiggestLoserQuery;

    @Value("${query.find_max_profitable_day}")
    String findMaxProfitableDaysQuery;

    @Value("${query.find_busiest_days}")
    String findBusiestDaysQuery;

    /**
     * Calculates the average open/close values for each security for each month. If there are no
     * securities, returns an empty list.
     *
     * @return
     */
    public List<StockAverage> findAveragesByMonth()
    {
        return jdbcTemplate.query(findAveragesByMonthQuery, new BeanPropertyRowMapper<>(StockAverage.class));
    }

    /**
     * Calculates most profitable day for each security. If there are no securities, returns an empty
     * list. If there are multiple days with the same profitability, returns them all.
     *
     * @return
     */
    public List<MaxProfit> findMaxProfitableDayBySecurity()
    {
        return jdbcTemplate.query(findMaxProfitableDaysQuery, new BeanPropertyRowMapper<>(MaxProfit.class));
    }

    /**
     * Calculates all days with a high trading volume for each security. If there are no securities,
     * returns an empty list.
     *
     * @return
     */
    public List<BusyDay> findBusiestDaysBySecurity()
    {
        return jdbcTemplate.query(findBusiestDaysQuery, new BeanPropertyRowMapper<>(BusyDay.class));
    }

    /**
     * Calculates the security with the most losing days in the dataset. If there are no entries in
     * the database or multiple securities with the same number of losing days, returns null.
     *
     * @return
     */
    public BiggestLoser findSecuritiesWithMostLosingDays()
    {
        try
        {
            return jdbcTemplate.queryForObject(findBiggestLoserQuery, new BeanPropertyRowMapper<>(BiggestLoser.class));
        }
        catch(EmptyResultDataAccessException e)
        {
            // no records in the database, so return default
            return null;
        }
    }
}
