package com.medisphere.auth.client;

import com.medisphere.auth.dto.CreateDoctorDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "medisphere-doctor-service", path = "/doctor/api/v1")
public interface DoctorClient {

    @PostMapping("/createDoctor")
    Object createDoctor(@RequestBody CreateDoctorDTO createDoctorDTO);

    @org.springframework.web.bind.annotation.DeleteMapping("/deleteDoctor/{id}")
    Object deleteDoctor(@org.springframework.web.bind.annotation.PathVariable("id") String id, @RequestBody com.medisphere.auth.dto.DeleteDoctorDTO deleteDoctorDTO);
    @org.springframework.web.bind.annotation.GetMapping("/getMaxMsUserId/internal")
    Long getMaxMsUserId();
}
