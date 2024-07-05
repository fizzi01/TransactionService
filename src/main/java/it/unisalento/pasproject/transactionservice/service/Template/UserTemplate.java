package it.unisalento.pasproject.transactionservice.service.Template;

import it.unisalento.pasproject.transactionservice.domain.Transaction;
import it.unisalento.pasproject.transactionservice.dto.UserDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;

import java.time.LocalDateTime;
import java.util.List;

public class UserTemplate extends AnalyticsTemplate<UserDTO> {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserTemplate.class);

    public UserTemplate(MongoTemplate mongoTemplate) {
        super(mongoTemplate);
    }

    @Override
    public List<UserDTO> getAnalyticsList(String email, LocalDateTime startDate, LocalDateTime endDate, String granularity) {
        return super.getAnalyticsList(email, startDate, endDate, granularity);
    }

    @Override
    protected MatchOperation createMatchOperation(String email, LocalDateTime startDate, LocalDateTime endDate) {
        MatchOperation matchOperation;

        if (startDate != null && endDate != null) {
            matchOperation = Aggregation.match(
                    Criteria.where("senderEmail").is(email)
                            .andOperator(Criteria.where("completionDate").gte(startDate).lte(endDate))
                            .and("isCompleted").is(true)
            );
        } else if (startDate != null) {
            matchOperation = Aggregation.match(
                    Criteria.where("senderEmail").is(email)
                            .and("creationDate").gte(startDate)
                            .and("isCompleted").is(true)
            );
        } else if (endDate != null) {
            matchOperation = Aggregation.match(
                    Criteria.where("senderEmail").is(email)
                            .and("creationDate").lte(endDate)
                            .and("isCompleted").is(true)
            );
        } else {
            matchOperation = Aggregation.match(
                    Criteria.where("senderEmail").is(email)
                            .and("isCompleted").is(true)
            );
        }

        return matchOperation;
    }

    @Override
    protected List<AggregationOperation> getAdditionalOperations() {
        return List.of();
    }

    @Override
    protected ProjectionOperation createProjectionOperation() {
        return Aggregation.project()
                .andInclude(
                        "senderEmail",
                        "amount",
                        "completionDate",
                        "isCompleted"
                )
                .and(DateOperators.dateOf("completionDate").withTimezone(DateOperators.Timezone.valueOf("UTC")).toString("%d")).as("day")
                .and(DateOperators.dateOf("completionDate").withTimezone(DateOperators.Timezone.valueOf("UTC")).toString("%m")).as("month")
                .and(DateOperators.dateOf("completionDate").withTimezone(DateOperators.Timezone.valueOf("UTC")).toString("%Y")).as("year");
    }

    @Override
    protected GroupOperation createGroupOperation(String granularity) {
        return switch (granularity) {
            case "day" -> Aggregation.group("senderEmail", "year", "month", "day")
                    .sum("amount").as("totalAmount")
                    .count().as("totalTransactions")
                    .avg("amount").as("averageAmount")
                    .max("amount").as("maxAmount")
                    .min("amount").as("minAmount");
            case "month" -> Aggregation.group("senderEmail", "year", "month")
                    .sum("amount").as("totalAmount")
                    .count().as("totalTransactions")
                    .avg("amount").as("averageAmount")
                    .max("amount").as("maxAmount")
                    .min("amount").as("minAmount");
            case "year" -> Aggregation.group("senderEmail", "year")
                    .sum("amount").as("totalAmount")
                    .count().as("totalTransactions")
                    .avg("amount").as("averageAmount")
                    .max("amount").as("maxAmount")
                    .min("amount").as("minAmount");
            default -> null;
        };
    }

    @Override
    protected ProjectionOperation createFinalProjection(String granularity) {
        ProjectionOperation projectionOperation = Aggregation.project()
                .andExpression("senderEmail").as("senderEmail")
                .andExpression("totalAmount").as("totalAmount")
                .andExpression("totalTransactions").as("totalTransactions")
                .andExpression("totalAmount / totalTransactions").as("amountPerTransaction")
                .andExpression("averageAmount").as("averageAmount")
                .andExpression("maxAmount").as("maxAmount")
                .andExpression("minAmount").as("minAmount");

        projectionOperation = switch (granularity) {
            case "day" -> projectionOperation
                    .andExpression("toInt(day)").as("day")
                    .andExpression("toInt(month)").as("month")
                    .andExpression("toInt(year)").as("year");
            case "month" -> projectionOperation
                    .andExpression("toInt(month)").as("month")
                    .andExpression("toInt(year)").as("year");
            case "year" -> projectionOperation
                    .andExpression("toInt(year)").as("year");
            default -> projectionOperation;
        };

        return projectionOperation;
    }

    @Override
    protected SortOperation createSortOperation(String granularity) {
        return switch (granularity) {
            case "day" -> Aggregation.sort(Sort.by(Sort.Order.asc("year"), Sort.Order.asc("month"), Sort.Order.asc("day")));
            case "month" -> Aggregation.sort(Sort.by(Sort.Order.asc("year"), Sort.Order.asc("month")));
            case "year" -> Aggregation.sort(Sort.by(Sort.Order.asc("year")));
            default -> null;
        };
    }

    @Override
    protected String getCollectionName() {
        return this.mongoTemplate.getCollectionName(Transaction.class);
    }

    @Override
    protected Class<UserDTO> getDTOClass() {
        return UserDTO.class;
    }
}

