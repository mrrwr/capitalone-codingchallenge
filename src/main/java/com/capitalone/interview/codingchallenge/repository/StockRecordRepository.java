package com.capitalone.interview.codingchallenge.repository;

import com.capitalone.interview.codingchallenge.dto.StockRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Standard spring-data repository to allow stock records to be saved to the underlying database,
 * and allow lookups for data validation.
 */
@Repository
public interface StockRecordRepository extends JpaRepository<StockRecord, Long>
{

    /**
     * Returns all records in the database for the given ticker symbol. If the symbol is invalid
     * or there are no records, returns an empty list.
     *
     * @param ticker
     * @return
     */
    List<StockRecord> findAllByTickerIgnoringCase(String ticker);
}
