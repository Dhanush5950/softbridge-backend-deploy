package com.softbridge.service;

import com.softbridge.dto.request.DecisionRequest;
import com.softbridge.dto.request.RequirementRequest;
import com.softbridge.dto.request.UpdateRequirementRequest;
import com.softbridge.dto.response.DashboardStatsResponse;
import com.softbridge.dto.response.PageResponse;
import com.softbridge.dto.response.RequirementResponse;
import com.softbridge.enums.RequirementStatus;

public interface RequirementService {

    // CLIENT
    RequirementResponse      submit(RequirementRequest request, Long clientId);
    PageResponse<RequirementResponse> getMyRequirements(Long clientId, int page, int size);
    RequirementResponse      getMyRequirementById(String id, Long clientId);

    // ADMIN — Read
    PageResponse<RequirementResponse> getAll(int page, int size, String status, String query);
    RequirementResponse      getById(String id);

    // ADMIN — Write
    RequirementResponse      update(String id, UpdateRequirementRequest request);
    RequirementResponse      makeDecision(String id, DecisionRequest request);
    void                     delete(String id);

    // Stats
    DashboardStatsResponse   getDashboardStats();
}
