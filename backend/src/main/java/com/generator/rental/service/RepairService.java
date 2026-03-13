package com.generator.rental.service;

import com.generator.rental.entity.RepairRequest;

import java.util.List;

public interface RepairService {
    RepairRequest createRequest(Long orderId, String description, String imageUrl);
    RepairRequest updateStatus(Long id, String status, String response);
    List<RepairRequest> getByOrder(Long orderId);
}
