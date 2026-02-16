"use client";

import {useMemo, useState} from "react";
import {AppHeader, ServicePicker, SubscriptionForm, SubscriptionList,} from "@/components/app";
import {Button, Modal} from "@/components/ui";
import {
    type PopularService,
    type SubscriptionFilters,
    type SubscriptionRequest,
    type SubscriptionResponse,
    useSubscriptions,
} from "@/features/subscriptions";

const INITIAL_FILTERS: SubscriptionFilters = {
    page: 0,
    size: 12,
};

type ModalStep = "picker" | "form";

export function SubscriptionsPageContent() {
    const [filters, setFilters] = useState<SubscriptionFilters>(INITIAL_FILTERS);
    const [isModalOpen, setIsModalOpen] = useState(false);
    const [editingSubscription, setEditingSubscription] =
        useState<SubscriptionResponse | null>(null);
    const [isSubmitting, setIsSubmitting] = useState(false);

    // Two-step modal state
    const [modalStep, setModalStep] = useState<ModalStep>("picker");
    const [selectedService, setSelectedService] = useState<PopularService | null>(null);
    const [customName, setCustomName] = useState<string | null>(null);

    const {
        subscriptions,
        page,
        isLoading,
        error,
        fetchSubscriptions,
        createSubscription,
        updateSubscription,
        deleteSubscription,
    } = useSubscriptions(INITIAL_FILTERS);

    const closeModal = () => {
        setIsModalOpen(false);
        setEditingSubscription(null);
        setSelectedService(null);
        setCustomName(null);
        setModalStep("picker");
    };

    const refreshCurrentPage = async () => {
        await fetchSubscriptions(filters);
    };

    const handleCreate = () => {
        setEditingSubscription(null);
        setSelectedService(null);
        setCustomName(null);
        setModalStep("picker");
        setIsModalOpen(true);
    };

    const handleEdit = (subscription: SubscriptionResponse) => {
        setEditingSubscription(subscription);
        setSelectedService(null);
        setCustomName(null);
        setModalStep("form"); // Skip picker when editing
        setIsModalOpen(true);
    };

    const handleServiceSelect = (service: PopularService) => {
        setSelectedService(service);
        setCustomName(null);
        setModalStep("form");
    };

    const handleCreateCustom = (name: string) => {
        setSelectedService(null);
        setCustomName(name);
        setModalStep("form");
    };

    const handleBackToPicker = () => {
        setSelectedService(null);
        setCustomName(null);
        setModalStep("picker");
    };

    const handleDelete = async (subscription: SubscriptionResponse) => {
        const confirmed = window.confirm(
            `Deseja realmente excluir a assinatura "${subscription.name}"?`
        );

        if (!confirmed) {
            return;
        }

        await deleteSubscription(subscription.id);
        await refreshCurrentPage();
    };

    const handleSubmit = async (data: SubscriptionRequest) => {
        setIsSubmitting(true);

        try {
            if (editingSubscription) {
                await updateSubscription(editingSubscription.id, data);
            } else {
                await createSubscription(data);
            }

            closeModal();
            await refreshCurrentPage();
        } finally {
            setIsSubmitting(false);
        }
    };

    const handleFilterChange = async (next: Partial<SubscriptionFilters>) => {
        const nextFilters: SubscriptionFilters = {
            ...filters,
            ...next,
            page: 0,
        };

        setFilters(nextFilters);
        await fetchSubscriptions(nextFilters);
    };

    const handlePageChange = async (nextPage: number) => {
        const nextFilters: SubscriptionFilters = {
            ...filters,
            page: nextPage - 1,
        };

        setFilters(nextFilters);
        await fetchSubscriptions(nextFilters);
    };

    // Dynamic modal title
    const modalTitle = useMemo(() => {
        if (editingSubscription) {
            return "Editar assinatura";
        }

        if (modalStep === "picker") {
            return "Adicionar assinatura";
        }

        // Step is "form"
        if (selectedService) {
            return `Adicionar ${selectedService.name}`;
        }

        if (customName) {
            return `Adicionar ${customName}`;
        }

        return "Nova assinatura";
    }, [editingSubscription, modalStep, selectedService, customName]);

    return (
        <>
            <AppHeader
                title="Assinaturas"
                subtitle={`${page?.totalElements ?? subscriptions.length} assinaturas cadastradas`}
            />

            <main className="p-6 space-y-6">
                <div className="flex items-center justify-between">
                    <div>
                        <h1 className="text-2xl font-bold text-neutral-900 font-display">
                            Minhas Assinaturas
                        </h1>
                        <p className="text-neutral-500 mt-1">
                            Gerencie todas as suas assinaturas em um só lugar.
                        </p>
                    </div>

                    <Button onClick={handleCreate}>
                        <svg
                            className="w-5 h-5 mr-2"
                            fill="none"
                            stroke="currentColor"
                            viewBox="0 0 24 24"
                        >
                            <path
                                strokeLinecap="round"
                                strokeLinejoin="round"
                                strokeWidth={2}
                                d="M12 4v16m8-8H4"
                            />
                        </svg>
                        Adicionar
                    </Button>
                </div>

                {error && (
                    <div className="rounded-lg border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700">
                        {error}
                    </div>
                )}

                <SubscriptionList
                    subscriptions={subscriptions}
                    isLoading={isLoading}
                    currentPage={(page?.number ?? 0) + 1}
                    totalPages={page?.totalPages ?? 1}
                    onPageChange={handlePageChange}
                    onFilterChange={handleFilterChange}
                    onEdit={handleEdit}
                    onDelete={handleDelete}
                />
            </main>

            <Modal
                isOpen={isModalOpen}
                onClose={closeModal}
                title={modalTitle}
                size="lg"
            >
                {modalStep === "picker" ? (
                    <ServicePicker
                        onSelectService={handleServiceSelect}
                        onCreateCustom={handleCreateCustom}
                    />
                ) : (
                    <SubscriptionForm
                        subscription={editingSubscription ?? undefined}
                        prefill={
                            selectedService
                                ? selectedService
                                : customName
                                    ? {
                                        name: customName,
                                        logoUrl: "",
                                        categoryId: 9, // "Other" category
                                        defaultBillingCycle: "MONTHLY",
                                        defaultValue: 0,
                                        defaultCurrency: "BRL",
                                    }
                                    : undefined
                        }
                        onSubmit={handleSubmit}
                        onCancel={closeModal}
                        onBack={!editingSubscription ? handleBackToPicker : undefined}
                        isSubmitting={isSubmitting}
                    />
                )}
            </Modal>
        </>
    );
}
