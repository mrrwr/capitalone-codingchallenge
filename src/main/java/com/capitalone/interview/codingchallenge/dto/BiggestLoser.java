package com.capitalone.interview.codingchallenge.dto;

public class BiggestLoser
{
    private String ticker;
    private int numberOfDays;

    public BiggestLoser()
    {
    }

    public BiggestLoser(String ticker, int numberOfDays)
    {
        this.ticker = ticker;
        this.numberOfDays = numberOfDays;
    }

    public String getTicker()
    {
        return ticker;
    }

    public void setTicker(String ticker)
    {
        this.ticker = ticker;
    }

    public int getNumberOfDays()
    {
        return numberOfDays;
    }

    public void setNumberOfDays(int numberOfDays)
    {
        this.numberOfDays = numberOfDays;
    }
}
