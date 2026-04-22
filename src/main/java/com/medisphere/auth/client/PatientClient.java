package com.medisphere.auth.client;

import com.medisphere.auth.dto.PatientDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "medisphere-patient-service", path = "/patient/api/v1")
public interface PatientClient {

    @PostMapping("/createPatient/internal")
    Object createPatient(@RequestBody PatientDto patientDto);

    @DeleteMapping("/deletePatient/internal/{msUserId}")
    Object deletePatient(@PathVariable("msUserId") String msUserId);

    @org.springframework.web.bind.annotation.GetMapping("/getMaxMsUserId/internal")
    Long getMaxMsUserId();
}
