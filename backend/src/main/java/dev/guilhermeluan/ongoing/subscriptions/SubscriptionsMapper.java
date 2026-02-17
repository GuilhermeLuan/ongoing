package dev.guilhermeluan.ongoing.subscriptions;

import dev.guilhermeluan.ongoing.subscriptions.dto.SubscriptionRequestDto;
import dev.guilhermeluan.ongoing.subscriptions.dto.SubscriptionResponseDto;
import dev.guilhermeluan.ongoing.subscriptions.entities.Category;
import dev.guilhermeluan.ongoing.subscriptions.entities.PaymentMethod;
import dev.guilhermeluan.ongoing.subscriptions.entities.SubscriptionType;
import dev.guilhermeluan.ongoing.subscriptions.entities.Subscriptions;
import org.mapstruct.*;
import org.springframework.data.domain.Page;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface SubscriptionsMapper {

    @Mapping(source = "notify", target = "notifyUser")
    @Mapping(source = "category.id", target = "categoryId")
    @Mapping(source = "paymentMethod.id", target = "paymentMethodId")
    @Mapping(source = "subscriptionType.id", target = "subscriptionTypeId")
    @Mapping(source = "category.name", target = "categoryName")
    @Mapping(source = "paymentMethod.name", target = "paymentMethodName")
    SubscriptionResponseDto toSubscriptionResponse(Subscriptions subscription);

    List<SubscriptionResponseDto> toSubscriptionResponse(List<Subscriptions> subscriptions);

    List<SubscriptionResponseDto> toSubscriptionResponse(Page<Subscriptions> subscriptions);


    @Mapping(source = "notifyUser", target = "notify")
    @Mapping(source = "categoryId", target = "category.id")
    @Mapping(source = "paymentMethodId", target = "paymentMethod.id")
    @Mapping(source = "subscriptionTypeId", target = "subscriptionType.id")
    Subscriptions toSubscription(SubscriptionResponseDto dto);

    @Mapping(source = "notifyUser", target = "notify")
    @Mapping(source = "categoryId", target = "category.id")
    @Mapping(source = "paymentMethodId", target = "paymentMethod.id")
    @Mapping(source = "subscriptionTypeId", target = "subscriptionType.id")
    Subscriptions toSubscription(SubscriptionRequestDto dto);

    @Mapping(source = "notifyUser", target = "notify")
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "paymentMethod", ignore = true)
    @Mapping(target = "subscriptionType", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateSubscriptionFromDto(SubscriptionRequestDto dto, @MappingTarget Subscriptions subscription);

    @AfterMapping
    default void setRelationships(SubscriptionRequestDto dto, @MappingTarget Subscriptions subscription) {
        // Set category relationship by creating new reference
        if (dto.categoryId() != null) {
            Category category = new Category();
            category.setId(dto.categoryId());
            subscription.setCategory(category);
        }

        // Set payment method relationship by creating new reference
        if (dto.paymentMethodId() != null) {
            PaymentMethod paymentMethod = new PaymentMethod();
            paymentMethod.setId(dto.paymentMethodId());
            subscription.setPaymentMethod(paymentMethod);
        }

        // Set subscription type relationship by creating new reference
        if (dto.subscriptionTypeId() != null) {
            SubscriptionType subscriptionType = new SubscriptionType();
            subscriptionType.setId(dto.subscriptionTypeId());
            subscription.setSubscriptionType(subscriptionType);
        }
    }
}
