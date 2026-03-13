package com.generator.rental.service;

import com.generator.rental.entity.Complaint;

import java.util.List;

public interface ComplaintService {
    Complaint createComplaint(Long orderId, String complainantUserId, String content);
    List<Complaint> getAllComplaints();
    void resolveComplaint(Long id, String resolution);
}
