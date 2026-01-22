package dev.guilhermeluan.ongoing.subscriptions;

import dev.guilhermeluan.ongoing.subscriptions.dto.SubscriptionRequestDto;
import dev.guilhermeluan.ongoing.subscriptions.dto.SubscriptionResponseDto;
import dev.guilhermeluan.ongoing.subscriptions.entities.Subscriptions;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface SubscriptionsMapper {

    @Mapping(source = "notify", target = "notifyUser")
    SubscriptionResponseDto toSubscriptionResponse(Subscriptions subscription);

    List<SubscriptionResponseDto> toSubscriptionResponse(List<Subscriptions> subscriptions);

    @Mapping(source = "notifyUser", target = "notify")
    Subscriptions toSubscription(SubscriptionResponseDto dto);

    @Mapping(source = "notifyUser", target = "notify")
    Subscriptions toSubscription(SubscriptionRequestDto dto);

}
