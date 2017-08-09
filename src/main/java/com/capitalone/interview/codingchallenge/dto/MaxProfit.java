package com.capitalone.interview.codingchallenge.dto;

import java.time.LocalDate;

public class MaxProfit
{
    private String ticker;
    private LocalDate date;
    private double profit;

    public MaxProfit()
    {
    }

    public MaxProfit(String ticker, LocalDate date, double profit)
    {
        this.ticker = ticker;
        this.date = date;
        this.profit = profit;
    }

    public String getTicker()
    {
        return ticker;
    }

    public void setTicker(String ticker)
    {
        this.ticker = ticker;
    }

    public LocalDate getDate()
    {
        return date;
    }

    public void setDate(LocalDate date)
    {
        this.date = date;
    }

    public double getProfit()
    {
        return profit;
    }

    public void setProfit(double profit)
    {
        this.profit = profit;
    }
}
