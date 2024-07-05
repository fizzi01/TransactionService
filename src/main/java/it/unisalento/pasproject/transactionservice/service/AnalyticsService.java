package it.unisalento.pasproject.transactionservice.service;

import it.unisalento.pasproject.transactionservice.dto.UserYearlyDTO;
import it.unisalento.pasproject.transactionservice.service.Template.UserYearlyTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AnalyticsService {
    private final UserYearlyTemplate userYearlyTemplate;

    private static final Logger LOGGER = LoggerFactory.getLogger(AnalyticsService.class);

    @Autowired
    public AnalyticsService(MongoTemplate mongoTemplate) {
        this.userYearlyTemplate = new UserYearlyTemplate(mongoTemplate);
    }

    public List<UserYearlyDTO> getUserYearlyAnalytics(String email, LocalDateTime startDate, LocalDateTime endDate) {
        return userYearlyTemplate.getAnalyticsList(email, startDate, endDate);
    }
}
