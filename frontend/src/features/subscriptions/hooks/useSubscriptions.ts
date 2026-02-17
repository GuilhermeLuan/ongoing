"use client";

import {isAxiosError} from "axios";
import {useCallback, useEffect, useState} from "react";
import {subscriptionService} from "../services/subscription.service";
import type {
    SpringPage,
    SubscriptionFilters,
    SubscriptionRequest,
    SubscriptionResponse,
} from "../types/subscription.types";

interface UseSubscriptionsResult {
    subscriptions: SubscriptionResponse[];
    page: SpringPage<SubscriptionResponse> | null;
    isLoading: boolean;
    error: string | null;
    fetchSubscriptions: (filters?: SubscriptionFilters) => Promise<void>;
    createSubscription: (data: SubscriptionRequest) => Promise<SubscriptionResponse>;
    updateSubscription: (
        id: number,
        data: SubscriptionRequest
    ) => Promise<SubscriptionResponse>;
    deleteSubscription: (id: number) => Promise<void>;
}

const getErrorMessage = (error: unknown): string => {
    if (isAxiosError(error)) {
        const message = error.response?.data?.message;

        if (typeof message === "string") {
            return message;
        }
    }

    return "Não foi possível carregar as assinaturas.";
};

export const useSubscriptions = (
    initialFilters?: SubscriptionFilters
): UseSubscriptionsResult => {
    const [subscriptions, setSubscriptions] = useState<SubscriptionResponse[]>([]);
    const [page, setPage] = useState<SpringPage<SubscriptionResponse> | null>(null);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    const fetchSubscriptions = useCallback(async (filters: SubscriptionFilters = {}) => {
        setIsLoading(true);
        setError(null);

        try {
            const response = await subscriptionService.findAll(filters);
            setSubscriptions(response.content);
            setPage(response);
        } catch (requestError) {
            setError(getErrorMessage(requestError));
        } finally {
            setIsLoading(false);
        }
    }, []);

    const createSubscription = useCallback(async (data: SubscriptionRequest) => {
        return subscriptionService.create(data);
    }, []);

    const updateSubscription = useCallback(
        async (id: number, data: SubscriptionRequest) => {
            return subscriptionService.update(id, data);
        },
        []
    );

    const deleteSubscription = useCallback(async (id: number) => {
        await subscriptionService.remove(id);
    }, []);

    useEffect(() => {
        void fetchSubscriptions(initialFilters ?? {});
    }, [fetchSubscriptions, initialFilters]);

    return {
        subscriptions,
        page,
        isLoading,
        error,
        fetchSubscriptions,
        createSubscription,
        updateSubscription,
        deleteSubscription,
    };
};
