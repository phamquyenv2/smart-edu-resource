package com.paq.service;

import java.util.Map;
import com.paq.pojo.response.ResMoMoCreateDTO;

public interface MoMoService {
    ResMoMoCreateDTO createMoMoPayment(int courseId, String username);
    void processIpnCallback(Map<String, String> ipnRequest);
    void syncReturnResult(Map<String, String> returnRequest);
}
