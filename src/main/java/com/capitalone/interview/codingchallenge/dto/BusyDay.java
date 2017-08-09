package com.capitalone.interview.codingchallenge.dto;

import java.time.LocalDate;

public class BusyDay
{
    private String ticker;
    private LocalDate date;
    private double volume;
    private double averageVolume;

    public BusyDay(String ticker, LocalDate date, double volume, double averageVolume)
    {
        this.ticker = ticker;
        this.date = date;
        this.volume = volume;
        this.averageVolume = averageVolume;
    }

    public BusyDay()
    {

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

    public double getVolume()
    {
        return volume;
    }

    public void setVolume(double volume)
    {
        this.volume = volume;
    }

    public double getAverageVolume()
    {
        return averageVolume;
    }

    public void setAverageVolume(double averageVolume)
    {
        this.averageVolume = averageVolume;
    }
}
