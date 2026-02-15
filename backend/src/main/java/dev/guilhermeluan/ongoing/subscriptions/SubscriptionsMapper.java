package dev.guilhermeluan.ongoing.subscriptions;

import dev.guilhermeluan.ongoing.subscriptions.dto.SubscriptionRequestDto;
import dev.guilhermeluan.ongoing.subscriptions.dto.SubscriptionResponseDto;
import dev.guilhermeluan.ongoing.subscriptions.entities.Subscriptions;
import org.mapstruct.*;
import org.springframework.data.domain.Page;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface SubscriptionsMapper {

    @Mapping(source = "notify", target = "notifyUser")
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
    @Mapping(source = "categoryId", target = "category.id")
    @Mapping(source = "paymentMethodId", target = "paymentMethod.id")
    @Mapping(source = "subscriptionTypeId", target = "subscriptionType.id")
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateSubscriptionFromDto(SubscriptionRequestDto dto, @MappingTarget Subscriptions subscription);
}
