package it.unisalento.pasproject.transactionservice.service.Template;

import it.unisalento.pasproject.transactionservice.domain.Transaction;
import it.unisalento.pasproject.transactionservice.dto.MemberAnalyticsDTO;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;

import java.time.LocalDateTime;
import java.util.List;

public class MemberTemplate extends AnalyticsTemplate<MemberAnalyticsDTO> {
    public MemberTemplate(MongoTemplate mongoTemplate) {
        super(mongoTemplate);
    }

    @Override
    public List<MemberAnalyticsDTO> getAnalyticsList(String email, LocalDateTime startDate, LocalDateTime endDate, String granularity) {
        return super.getAnalyticsList(email, startDate, endDate, granularity);
    }

    @Override
    protected MatchOperation createMatchOperation(String email, LocalDateTime startDate, LocalDateTime endDate) {
        MatchOperation matchOperation;

        if (startDate != null && endDate != null) {
            matchOperation = Aggregation.match(
                    Criteria.where("receiverEmail").is(email)
                            .andOperator(Criteria.where("completionDate").gte(startDate).lte(endDate))
                            .and("isCompleted").is(true)
            );
        } else if (startDate != null) {
            matchOperation = Aggregation.match(
                    Criteria.where("receiverEmail").is(email)
                            .and("creationDate").gte(startDate)
                            .and("isCompleted").is(true)
            );
        } else if (endDate != null) {
            matchOperation = Aggregation.match(
                    Criteria.where("receiverEmail").is(email)
                            .and("creationDate").lte(endDate)
                            .and("isCompleted").is(true)
            );
        } else {
            matchOperation = Aggregation.match(
                    Criteria.where("receiverEmail").is(email)
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
                        "receiverEmail",
                        "amount",
                        "completionDate",
                        "isCompleted"
                )
                .and(ConvertOperators.ToInt.toInt(DateOperators.dateOf("completionDate").withTimezone(DateOperators.Timezone.valueOf("UTC")).toString("%d"))).as("day")
                .and(ConvertOperators.ToInt.toInt(DateOperators.dateOf("completionDate").withTimezone(DateOperators.Timezone.valueOf("UTC")).toString("%m"))).as("month")
                .and(ConvertOperators.ToInt.toInt(DateOperators.dateOf("completionDate").withTimezone(DateOperators.Timezone.valueOf("UTC")).toString("%Y"))).as("year");
    }

    @Override
    protected GroupOperation createGroupOperation(String granularity) {
        return switch (granularity) {
            case "day" -> Aggregation.group("receiverEmail", "year", "month", "day")
                    .sum("amount").as("totalAmount")
                    .count().as("totalTransactions")
                    .avg("amount").as("averageAmount")
                    .max("amount").as("maxAmount")
                    .min("amount").as("minAmount");
            case "month" -> Aggregation.group("receiverEmail", "year", "month")
                    .sum("amount").as("totalAmount")
                    .count().as("totalTransactions")
                    .avg("amount").as("averageAmount")
                    .max("amount").as("maxAmount")
                    .min("amount").as("minAmount");
            case "year" -> Aggregation.group("receiverEmail", "year")
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
                .andExpression("receiverEmail").as("receiverEmail")
                .andExpression("totalAmount").as("totalAmount")
                .andExpression("totalTransactions").as("totalTransactions")
                .andExpression("totalAmount / totalTransactions").as("amountPerTransaction")
                .andExpression("averageAmount").as("averageAmount")
                .andExpression("maxAmount").as("maxAmount")
                .andExpression("minAmount").as("minAmount");

        projectionOperation = switch (granularity) {
            case "day" -> projectionOperation
                    .andExpression("day").as("day")
                    .andExpression("month").as("month")
                    .andExpression("year").as("year");
            case "month" -> projectionOperation
                    .andExpression("month").as("month")
                    .andExpression("year").as("year");
            case "year" -> projectionOperation
                    .andExpression("year").as("year");
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
    protected Class<MemberAnalyticsDTO> getDTOClass() {
        return MemberAnalyticsDTO.class;
    }
}
