package com.medisphere.auth.client;

import com.medisphere.auth.dto.RegisterDoctorRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "medisphere-admin-service", path = "api/v1/admin")
public interface AdminClient {

    @PostMapping("/doctors/pending")
    void registerDoctor(@RequestBody RegisterDoctorRequest request);

}
