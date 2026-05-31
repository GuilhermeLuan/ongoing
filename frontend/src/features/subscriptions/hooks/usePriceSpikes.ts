"use client";

import {isAxiosError} from "axios";
import {useCallback, useEffect, useState} from "react";
import {subscriptionService} from "../services/subscription.service";
import type {
    PriceSpikesFilters,
    SubscriptionPriceHistoryResponse,
} from "../types/subscription.types";

interface UsePriceSpikesResult {
    priceSpikes: SubscriptionPriceHistoryResponse[];
    filters: PriceSpikesFilters;
    draftFilters: PriceSpikesFilters;
    isLoading: boolean;
    error: string | null;
    setDraftFilter: (name: keyof PriceSpikesFilters, value: string) => void;
    applyFilters: () => void;
    refetch: () => Promise<void>;
}

const formatInputDate = (date: Date): string => {
    const year = date.getFullYear();
    const month = `${date.getMonth() + 1}`.padStart(2, "0");
    const day = `${date.getDate()}`.padStart(2, "0");

    return `${year}-${month}-${day}`;
};

export const getDefaultPriceSpikeFilters = (): PriceSpikesFilters => {
    const to = new Date();
    const from = new Date(to);
    from.setDate(to.getDate() - 30);

    return {
        from: formatInputDate(from),
        to: formatInputDate(to),
    };
};

const getErrorMessage = (error: unknown): string => {
    if (isAxiosError(error)) {
        const message = error.response?.data?.message;

        if (typeof message === "string") {
            return message;
        }
    }

    return "Não foi possível carregar o histórico de reajustes.";
};

export const usePriceSpikes = (
    initialFilters?: PriceSpikesFilters
): UsePriceSpikesResult => {
    const [filters, setFilters] = useState<PriceSpikesFilters>(
        () => initialFilters ?? getDefaultPriceSpikeFilters()
    );
    const [draftFilters, setDraftFilters] = useState<PriceSpikesFilters>(
        () => initialFilters ?? getDefaultPriceSpikeFilters()
    );
    const [priceSpikes, setPriceSpikes] = useState<SubscriptionPriceHistoryResponse[]>([]);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    const fetchPriceSpikes = useCallback(async (nextFilters: PriceSpikesFilters) => {
        setIsLoading(true);
        setError(null);

        try {
            const response = await subscriptionService.findPriceSpikes(nextFilters);
            setPriceSpikes(response);
        } catch (requestError) {
            setError(getErrorMessage(requestError));
            setPriceSpikes([]);
        } finally {
            setIsLoading(false);
        }
    }, []);

    const setDraftFilter = useCallback(
        (name: keyof PriceSpikesFilters, value: string) => {
            setDraftFilters((current) => ({
                ...current,
                [name]: value,
            }));
        },
        []
    );

    const applyFilters = useCallback(() => {
        setFilters(draftFilters);
    }, [draftFilters]);

    const refetch = useCallback(async () => {
        await fetchPriceSpikes(filters);
    }, [fetchPriceSpikes, filters]);

    useEffect(() => {
        void fetchPriceSpikes(filters);
    }, [fetchPriceSpikes, filters]);

    return {
        priceSpikes,
        filters,
        draftFilters,
        isLoading,
        error,
        setDraftFilter,
        applyFilters,
        refetch,
    };
};
