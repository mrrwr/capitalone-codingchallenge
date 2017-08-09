package com.capitalone.interview.codingchallenge.repository;

import com.capitalone.interview.codingchallenge.StockReportApplication;
import com.capitalone.interview.codingchallenge.dto.*;
import com.capitalone.interview.codingchallenge.service.DataLoader;
import org.assertj.core.util.DoubleComparator;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.annotation.DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = StockReportApplication.class)
@DirtiesContext(classMode= AFTER_EACH_TEST_METHOD)
public class StockReportRepositoryIT
{
    @Autowired
    StockReportRepository repository;

    @MockBean
    DataLoader loader;

    @Test
    @Sql("classpath:empty-data.sql")
    public void findAveragesByMonth_emptyDataset()
    {
        List<StockAverage> result = repository.findAveragesByMonth();
        assertThat(result).hasSize(0);
    }

    @Test
    @Sql("classpath:multiple-results-data.sql")
    public void findAveragesByMonth_multipleResults()
    {
        List<StockAverage> result = repository.findAveragesByMonth();
        assertThat(result)
            .usingComparatorForElementFieldsWithType(new DoubleComparator(0.01), Double.class)
            .usingFieldByFieldElementComparator()
            .contains(
                new StockAverage("GOOGL", "2017-01", 805.255, 807.89),
                new StockAverage("GOOGL", "2017-02", 820.71, 817.877),
                new StockAverage("MSFT", "2017-01", 62.635, 62.44),
                new StockAverage("MSFT", "2017-02", 63.7, 63.48)
            );
    }

    @Test
    @Sql("classpath:empty-data.sql")
    public void findMaxProfitableDayBySecurity_emptyDataset()
    {
        List<MaxProfit> result = repository.findMaxProfitableDayBySecurity();
        assertThat(result).hasSize(0);
    }

    @Test
    @Sql("classpath:multiple-results-data.sql")
    public void findMaxProfitableDayBySecurity_multipleResults()
    {
        List<MaxProfit> result = repository.findMaxProfitableDayBySecurity();
        assertThat(result)
            .usingComparatorForElementFieldsWithType(new DoubleComparator(0.01), Double.class)
            .usingFieldByFieldElementComparator()
            .contains(
                new MaxProfit("GOOGL", LocalDate.of(2017, 1, 3), 14.54),
                new MaxProfit("MSFT", LocalDate.of(2017, 2, 1), 1.15)
            );
    }

    @Test
    @Sql("classpath:tied-results-data.sql")
    public void findMaxProfitableDayBySecurity_tiedResults()
    {
        List<MaxProfit> result = repository.findMaxProfitableDayBySecurity();
        assertThat(result)
            .usingComparatorForElementFieldsWithType(new DoubleComparator(0.01), Double.class)
            .usingFieldByFieldElementComparator()
            .contains(
                new MaxProfit("GOOGL", LocalDate.of(2017, 1, 3), 3.425),
                new MaxProfit("MSFT", LocalDate.of(2017, 1, 3), 3.425)
            );
    }

    @Test
    @Sql("classpath:empty-data.sql")
    public void findBusiestDaysBySecurity_emptyDataset()
    {
        List<BusyDay> result = repository.findBusiestDaysBySecurity();
        assertThat(result).hasSize(0);
    }

    @Test
    @Sql("classpath:multiple-results-data.sql")
    public void findBusiestDaysBySecurity_multipleResults()
    {
        List<BusyDay> result = repository.findBusiestDaysBySecurity();
        assertThat(result)
            .usingComparatorForElementFieldsWithType(new DoubleComparator(0.01), Double.class)
            .usingFieldByFieldElementComparator()
            .contains(
                new BusyDay("GOOGL", LocalDate.of(2017, 2, 1), 2251047, 1788538.6),
                new BusyDay("MSFT", LocalDate.of(2017, 2, 1), 3.9671528E7, 3.1566874E7),
                new BusyDay("MSFT", LocalDate.of(2017, 2, 2), 4.5827013E7, 3.1566874E7)
            );
    }

    @Test
    @Sql("classpath:tied-results-data.sql")
    public void findBusiestDaysBySecurity_noDaysAboveAverage()
    {
        List<BusyDay> result = repository.findBusiestDaysBySecurity();
        assertThat(result).hasSize(0);
    }

    @Test
    @Sql("classpath:empty-data.sql")
    public void findSecuritiesWithMostLosingDays_emptyDataset()
    {
        BiggestLoser result = repository.findSecuritiesWithMostLosingDays();
        assertThat(result).isNull();
    }

    @Test
    @Sql("classpath:multiple-results-data.sql")
    public void findSecuritiesWithMostLosingDays_singleResult()
    {
        BiggestLoser result = repository.findSecuritiesWithMostLosingDays();
        assertThat(result)
            .isEqualToComparingFieldByField(new BiggestLoser("MSFT", 4));
    }

    @Test
    @Sql("classpath:tied-results-data.sql")
    public void findSecuritiesWithMostLosingDays_tiedResults()
    {
        BiggestLoser result = repository.findSecuritiesWithMostLosingDays();
        assertThat(result.getTicker()).isIn("MSFT", "GOOGL");
        assertThat(result.getNumberOfDays()).isEqualTo(1);
    }
}
