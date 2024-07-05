package it.unisalento.pasproject.transactionservice.service.Template;

import it.unisalento.pasproject.transactionservice.domain.Transaction;
import it.unisalento.pasproject.transactionservice.dto.UserYearlyDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;

import java.time.LocalDateTime;
import java.util.List;

public class UserYearlyTemplate extends AnalyticsTemplate<UserYearlyDTO> {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserYearlyTemplate.class);

    public UserYearlyTemplate(MongoTemplate mongoTemplate) {
        super(mongoTemplate);
    }

    @Override
    public List<UserYearlyDTO> getAnalyticsList(String email, LocalDateTime startDate, LocalDateTime endDate) {
        return super.getAnalyticsList(email, startDate, endDate);
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
                .and(DateOperators.dateOf("completionDate").withTimezone(DateOperators.Timezone.valueOf("UTC")).toString("%m")).as("month")
                .and(DateOperators.dateOf("completionDate").withTimezone(DateOperators.Timezone.valueOf("UTC")).toString("%Y")).as("year");
    }

    @Override
    protected GroupOperation createGroupOperation() {
        return Aggregation.group("senderEmail", "year", "month")
                .sum("amount").as("totalAmount")
                .count().as("totalTransactions")
                .avg("amount").as("averageAmount")
                .max("amount").as("maxAmount")
                .min("amount").as("minAmount");
    }

    @Override
    protected ProjectionOperation createFinalProjection() {
        return Aggregation.project()
                .andExpression("senderEmail").as("senderEmail")
                .andExpression("toInt(month)").as("month")
                .andExpression("toInt(year)").as("year")
                .andExpression("totalAmount").as("totalAmount")
                .andExpression("totalTransactions").as("totalTransactions")
                .andExpression("totalAmount / totalTransactions").as("amountPerTransaction")
                .andExpression("averageAmount").as("averageAmount")
                .andExpression("maxAmount").as("maxAmount")
                .andExpression("minAmount").as("minAmount");
    }

    @Override
    protected SortOperation createSortOperation() {
        return Aggregation.sort(Sort.by(Sort.Order.asc("year"), Sort.Order.asc("month")));
    }

    @Override
    protected String getCollectionName() {
        return this.mongoTemplate.getCollectionName(Transaction.class);
    }

    @Override
    protected Class<UserYearlyDTO> getDTOClass() {
        return UserYearlyDTO.class;
    }
}
