package it.unisalento.pasproject.transactionservice.controller;

import it.unisalento.pasproject.transactionservice.dto.UserYearlyDTO;
import it.unisalento.pasproject.transactionservice.exception.MissingDataException;
import it.unisalento.pasproject.transactionservice.service.AnalyticsService;
import it.unisalento.pasproject.transactionservice.service.UserCheckService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/transaction/analytics")
public class AnalyticsController {
    private final UserCheckService userCheckService;
    private final AnalyticsService analyticsService;

    @Autowired
    public AnalyticsController(UserCheckService userCheckService, AnalyticsService analyticsService) {
        this.userCheckService = userCheckService;
        this.analyticsService = analyticsService;
    }

    @GetMapping("/user/yearly")
    public List<UserYearlyDTO> getUserYearlyAnalytics() {
        String senderEmail = userCheckService.getCurrentUserEmail();
        LocalDateTime startDate = LocalDateTime.now().withDayOfYear(1).toLocalDate().atStartOfDay();
        LocalDateTime endDate = LocalDateTime.now();

        try {
            return analyticsService.getUserYearlyAnalytics(senderEmail, startDate, endDate);
        } catch (Exception e) {
            throw new MissingDataException(e.getMessage());
        }
    }
}
