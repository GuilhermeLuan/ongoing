package dev.guilhermeluan.ongoing.subscriptions;

import dev.guilhermeluan.ongoing.subscriptions.dto.SubscriptionRequestDto;
import dev.guilhermeluan.ongoing.subscriptions.dto.SubscriptionResponseDto;
import dev.guilhermeluan.ongoing.subscriptions.entitites.Subscriptions;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface SubscriptionsMapper {

    SubscriptionResponseDto toSubscriptionResponse(Subscriptions subscription);

    List<SubscriptionResponseDto> toSubscriptionResponse(List<Subscriptions> subscriptions);

    Subscriptions toSubscription(SubscriptionResponseDto dto);

    Subscriptions toSubscription(SubscriptionRequestDto dto);

}
