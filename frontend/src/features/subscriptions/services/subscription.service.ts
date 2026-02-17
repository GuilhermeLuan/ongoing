import {API_ENDPOINTS} from "@/lib/constants";
import {apiClient} from "@/features/auth";
import type {
    SpringPage,
    SubscriptionFilters,
    SubscriptionRequest,
    SubscriptionResponse,
} from "../types/subscription.types";

const cleanFilters = (filters: SubscriptionFilters = {}): SubscriptionFilters => {
    return Object.fromEntries(
        Object.entries(filters).filter(([, value]) => value !== undefined && value !== "")
    ) as SubscriptionFilters;
};

const findAll = async (
    filters: SubscriptionFilters = {}
): Promise<SpringPage<SubscriptionResponse>> => {
    const response = await apiClient.get<SpringPage<SubscriptionResponse>>(
        API_ENDPOINTS.SUBSCRIPTIONS,
        {
            params: cleanFilters(filters),
        }
    );

    return response.data;
};

const findById = async (id: number): Promise<SubscriptionResponse> => {
    const response = await apiClient.get<SubscriptionResponse>(
        `${API_ENDPOINTS.SUBSCRIPTIONS}/${id}`
    );

    return response.data;
};

const create = async (
    data: SubscriptionRequest
): Promise<SubscriptionResponse> => {
    const response = await apiClient.post<SubscriptionResponse>(
        API_ENDPOINTS.SUBSCRIPTIONS,
        data
    );

    return response.data;
};

const update = async (
    id: number,
    data: SubscriptionRequest
): Promise<SubscriptionResponse> => {
    const response = await apiClient.put<SubscriptionResponse>(
        `${API_ENDPOINTS.SUBSCRIPTIONS}/${id}`,
        data
    );

    return response.data;
};

const remove = async (id: number): Promise<void> => {
    await apiClient.delete(`${API_ENDPOINTS.SUBSCRIPTIONS}/${id}`);
};

export const subscriptionService = {
    findAll,
    findById,
    create,
    update,
    remove,
};
