package com.github.fagnerlima.springspecificationtools.sample;

import java.io.Serializable;
import java.time.LocalDate;

import jakarta.persistence.Embeddable;

@Embeddable
public class Period implements Serializable {

    private static final long serialVersionUID = 1L;

    private LocalDate startDate;

    private LocalDate endDate;

    protected Period() {
    }

    public Period(LocalDate startDate, LocalDate endDate) {
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

}
