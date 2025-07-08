package com.soprasteria.clinic.appointment.config;

import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.stereotype.Component;
import java.util.Map;

@Component
public class CustomInfoContributor implements InfoContributor {
    @Override
    public void contribute(Info.Builder builder) {
        builder.withDetails(Map.of(
                "name", "Clinic Appointment System",
                "description", "Manages appointment scheduling for clinics",
                "version", "1.0.0"
        ));
    }
}
