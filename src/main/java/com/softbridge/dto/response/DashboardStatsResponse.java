package com.softbridge.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class DashboardStatsResponse {

    private long totalRequirements;
    private long pendingCount;
    private long inhouseCount;
    private long outsourceCount;

    private long totalUsers;
    private long clientCount;
    private long developerCount;

    private Map<String, Long> byProjectType;
    private Map<String, Long> byStatus;
    private Map<String, Long> byPriority;
}