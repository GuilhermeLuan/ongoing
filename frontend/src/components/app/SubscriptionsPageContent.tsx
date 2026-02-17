"use client";

import {useMemo, useState} from "react";
import {
    AppHeader,
    DeleteConfirmationModal,
    ServicePicker,
    SubscriptionDetailsView,
    SubscriptionForm,
    SubscriptionList,
} from "@/components/app";
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

type ModalStep = "picker" | "form" | "details";

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

    // Details view state
    const [viewingSubscription, setViewingSubscription] = useState<SubscriptionResponse | null>(null);

    // Delete confirmation modal state
    const [isDeleteModalOpen, setIsDeleteModalOpen] = useState(false);
    const [subscriptionToDelete, setSubscriptionToDelete] = useState<SubscriptionResponse | null>(null);

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
        setViewingSubscription(null);
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

    const handleViewDetails = (subscription: SubscriptionResponse) => {
        setViewingSubscription(subscription);
        setEditingSubscription(null);
        setSelectedService(null);
        setCustomName(null);
        setModalStep("details");
        setIsModalOpen(true);
    };

    const handleEditFromDetails = () => {
        if (!viewingSubscription) return;

        setEditingSubscription(viewingSubscription);
        setViewingSubscription(null);
        setModalStep("form");
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

    const handleDelete = (subscription: SubscriptionResponse) => {
        setSubscriptionToDelete(subscription);
        setIsDeleteModalOpen(true);
    };

    const handleConfirmDelete = async () => {
        if (!subscriptionToDelete) return;

        await deleteSubscription(subscriptionToDelete.id);
        setIsDeleteModalOpen(false);
        setSubscriptionToDelete(null);
        closeModal(); // Close details modal too
        await refreshCurrentPage();
    };

    const handleToggleActive = async () => {
        if (!viewingSubscription) return;

        const updatedData: SubscriptionRequest = {
            name: viewingSubscription.name,
            description: viewingSubscription.description ?? undefined,
            value: viewingSubscription.value,
            startDate: viewingSubscription.startDate,
            nextPaymentDate: viewingSubscription.nextPaymentDate,
            active: !viewingSubscription.active, // Toggle the active status
            notifyUser: viewingSubscription.notifyUser,
            currency: viewingSubscription.currency,
            logoUrl: viewingSubscription.logoUrl ?? undefined,
            categoryId: viewingSubscription.categoryId ?? undefined,
            paymentMethodId: viewingSubscription.paymentMethodId ?? undefined,
            billingCycle: viewingSubscription.billingCycle,
            subscriptionTypeId: viewingSubscription.subscriptionTypeId ?? undefined,
        };

        await updateSubscription(viewingSubscription.id, updatedData);

        // Update the viewingSubscription state to reflect the change
        setViewingSubscription({
            ...viewingSubscription,
            active: !viewingSubscription.active,
        });

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
        if (modalStep === "details") {
            return "Detalhes da assinatura";
        }

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

            <main className="space-y-6 p-4 sm:p-6">
                <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
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
                    onClick={handleViewDetails}
                    onEdit={handleEdit}
                    onDelete={handleDelete}
                />
            </main>

            <Modal
                isOpen={isModalOpen}
                onClose={closeModal}
                title={modalTitle}
                size="md"
            >
                {modalStep === "details" && viewingSubscription ? (
                    <SubscriptionDetailsView
                        subscription={viewingSubscription}
                        onEdit={handleEditFromDetails}
                        onToggleActive={handleToggleActive}
                        onDelete={() => handleDelete(viewingSubscription)}
                        onClose={closeModal}
                    />
                ) : modalStep === "picker" ? (
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

            <DeleteConfirmationModal
                isOpen={isDeleteModalOpen}
                subscriptionName={subscriptionToDelete?.name ?? ""}
                onConfirm={handleConfirmDelete}
                onCancel={() => {
                    setIsDeleteModalOpen(false);
                    setSubscriptionToDelete(null);
                }}
            />
        </>
    );
}
