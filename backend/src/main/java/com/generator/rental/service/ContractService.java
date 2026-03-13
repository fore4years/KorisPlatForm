package com.generator.rental.service;

import com.generator.rental.entity.Contract;

public interface ContractService {
    Contract generateContract(Long orderId);
    Contract signContract(Long orderId, String userId, String role); // role: TENANT or MERCHANT
    Contract getContractByOrderId(Long orderId);
}
