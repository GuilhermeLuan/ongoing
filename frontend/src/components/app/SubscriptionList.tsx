"use client";

import {useState} from "react";
import {Input, Select} from "@/components/ui";
import {type SubscriptionFilters, type SubscriptionResponse,} from "@/features/subscriptions";
import {SubscriptionCard} from "./SubscriptionCard";

interface SubscriptionListProps {
    subscriptions: SubscriptionResponse[];
  isLoading?: boolean;
    currentPage: number;
    totalPages: number;
    onPageChange: (page: number) => void;
    onFilterChange: (filters: Partial<SubscriptionFilters>) => void;
    onClick?: (subscription: SubscriptionResponse) => void;
    onEdit: (subscription: SubscriptionResponse) => void;
    onDelete: (subscription: SubscriptionResponse) => void;
}

const statusOptions = [
  { value: "", label: "Todos os status" },
    {value: "true", label: "Ativas"},
    {value: "false", label: "Inativas"},
];

export function SubscriptionList({
  subscriptions,
  isLoading = false,
                                     currentPage,
                                     totalPages,
                                     onPageChange,
                                     onFilterChange,
                                     onClick,
                                     onEdit,
                                     onDelete,
}: SubscriptionListProps) {
  const [search, setSearch] = useState("");
  const [statusFilter, setStatusFilter] = useState("");

    const handleSearchChange = (value: string) => {
        setSearch(value);
        onFilterChange({name: value || undefined});
    };

    const handleStatusChange = (value: string) => {
        setStatusFilter(value);

        if (value === "") {
            onFilterChange({active: undefined});
            return;
        }

        onFilterChange({active: value === "true"});
    };

  if (isLoading) {
    return (
      <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
        {[...Array(6)].map((_, i) => (
            <div key={i} className="h-48 rounded-xl bg-neutral-100 animate-pulse"/>
        ))}
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row gap-4">
        <div className="flex-1">
          <Input
            placeholder="Buscar assinatura..."
            value={search}
            onChange={(event) => handleSearchChange(event.target.value)}
            icon={
              <svg
                className="w-4 h-4"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"
                />
              </svg>
            }
          />
        </div>

        <div className="w-full sm:w-40">
          <Select
            options={statusOptions}
            value={statusFilter}
            onChange={(event) => handleStatusChange(event.target.value)}
          />
        </div>
      </div>

        {subscriptions.length === 0 ? (
        <div className="text-center py-12">
          <div className="w-16 h-16 mx-auto mb-4 rounded-full bg-neutral-100 flex items-center justify-center">
            <svg
              className="w-8 h-8 text-neutral-400"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth={2}
                d="M9.172 16.172a4 4 0 015.656 0M9 10h.01M15 10h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"
              />
            </svg>
          </div>
          <h3 className="text-lg font-medium text-neutral-900">
            Nenhuma assinatura encontrada
          </h3>
          <p className="text-neutral-500 mt-1">
            Tente ajustar os filtros ou adicione uma nova assinatura.
          </p>
        </div>
      ) : (
            <>
                <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
                    {subscriptions.map((subscription) => (
                        <SubscriptionCard
                            key={subscription.id}
                            subscription={subscription}
                            onClick={onClick ? () => onClick(subscription) : undefined}
                            onEdit={onEdit}
                            onDelete={onDelete}
                        />
                    ))}
                </div>

                <div className="flex items-center justify-between">
                    <button
                        type="button"
                        className="px-3 py-2 rounded-lg border border-neutral-200 text-sm text-neutral-700 hover:bg-neutral-50 disabled:opacity-50 disabled:cursor-not-allowed"
                        onClick={() => onPageChange(currentPage - 1)}
                        disabled={currentPage <= 1}
                    >
                        Anterior
                    </button>

                    <p className="text-sm text-neutral-500">
                        Página {currentPage} de {Math.max(totalPages, 1)}
                    </p>

                    <button
                        type="button"
                        className="px-3 py-2 rounded-lg border border-neutral-200 text-sm text-neutral-700 hover:bg-neutral-50 disabled:opacity-50 disabled:cursor-not-allowed"
                        onClick={() => onPageChange(currentPage + 1)}
                        disabled={currentPage >= Math.max(totalPages, 1)}
                    >
                        Próximo
                    </button>
                </div>
            </>
      )}
    </div>
  );
}
