package it.unisalento.pasproject.transactionservice.service;

import it.unisalento.pasproject.transactionservice.dto.MemberAnalyticsDTO;
import it.unisalento.pasproject.transactionservice.dto.UserAnalyticsDTO;
import it.unisalento.pasproject.transactionservice.service.Template.MemberTemplate;
import it.unisalento.pasproject.transactionservice.service.Template.UserTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AnalyticsService {
    private final UserTemplate userTemplate;
    private final MemberTemplate memberTemplate;

    private static final Logger LOGGER = LoggerFactory.getLogger(AnalyticsService.class);

    @Autowired
    public AnalyticsService(MongoTemplate mongoTemplate) {
        this.userTemplate = new UserTemplate(mongoTemplate);
        this.memberTemplate = new MemberTemplate(mongoTemplate);
    }

    public List<UserAnalyticsDTO> getUserAnalytics(String email, LocalDateTime startDate, LocalDateTime endDate, String granularity) {
        return userTemplate.getAnalyticsList(email, startDate, endDate, granularity);
    }

    public List<MemberAnalyticsDTO> getMemberAnalytics(String email, LocalDateTime startDate, LocalDateTime endDate, String granularity) {
        return memberTemplate.getAnalyticsList(email, startDate, endDate, granularity);
    }
}
