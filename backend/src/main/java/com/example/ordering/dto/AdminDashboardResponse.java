package com.example.ordering.dto;

import java.math.BigDecimal;

public class AdminDashboardResponse {

    private BigDecimal totalRevenue;
    private BigDecimal todayRevenue;
    private Integer todayOrderCount;
    private Long availableDishCount;
    private Long unavailableDishCount;

    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(BigDecimal totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public BigDecimal getTodayRevenue() {
        return todayRevenue;
    }

    public void setTodayRevenue(BigDecimal todayRevenue) {
        this.todayRevenue = todayRevenue;
    }

    public Integer getTodayOrderCount() {
        return todayOrderCount;
    }

    public void setTodayOrderCount(Integer todayOrderCount) {
        this.todayOrderCount = todayOrderCount;
    }

    public Long getAvailableDishCount() {
        return availableDishCount;
    }

    public void setAvailableDishCount(Long availableDishCount) {
        this.availableDishCount = availableDishCount;
    }

    public Long getUnavailableDishCount() {
        return unavailableDishCount;
    }

    public void setUnavailableDishCount(Long unavailableDishCount) {
        this.unavailableDishCount = unavailableDishCount;
    }

}
