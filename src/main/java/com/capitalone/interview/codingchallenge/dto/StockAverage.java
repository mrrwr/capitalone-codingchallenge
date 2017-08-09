package com.capitalone.interview.codingchallenge.dto;

public class StockAverage
{
    private String ticker;
    private String month;
    private double averageOpen;
    private double averageClose;

    public StockAverage()
    {
    }

    public StockAverage(String ticker, String month, double averageOpen, double averageClose)
    {
        this.ticker = ticker;
        this.month = month;
        this.averageOpen = averageOpen;
        this.averageClose = averageClose;
    }

    public String getTicker()
    {
        return ticker;
    }

    public void setTicker(String ticker)
    {
        this.ticker = ticker;
    }

    public double getAverageOpen()
    {
        return averageOpen;
    }

    public void setAverageOpen(double averageOpen)
    {
        this.averageOpen = averageOpen;
    }

    public double getAverageClose()
    {
        return averageClose;
    }

    public void setAverageClose(double averageClose)
    {
        this.averageClose = averageClose;
    }

    public String getMonth()
    {
        return month;
    }

    public void setMonth(String month)
    {
        this.month = month;
    }
}
