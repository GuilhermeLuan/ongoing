"use client";

import { useState, useMemo } from "react";
import { Input, Select } from "@/components/ui";
import { SubscriptionCard } from "./SubscriptionCard";
import {
  Subscription,
  categoryLabels,
  statusLabels,
} from "@/lib/mock-data";

interface SubscriptionListProps {
  subscriptions: Subscription[];
  isLoading?: boolean;
}

const categoryOptions = [
  { value: "", label: "Todas as categorias" },
  ...Object.entries(categoryLabels).map(([value, label]) => ({ value, label })),
];

const statusOptions = [
  { value: "", label: "Todos os status" },
  ...Object.entries(statusLabels).map(([value, label]) => ({ value, label })),
];

export function SubscriptionList({
  subscriptions,
  isLoading = false,
}: SubscriptionListProps) {
  const [search, setSearch] = useState("");
  const [categoryFilter, setCategoryFilter] = useState("");
  const [statusFilter, setStatusFilter] = useState("");

  const filteredSubscriptions = useMemo(() => {
    return subscriptions.filter((sub) => {
      const matchesSearch = sub.name
        .toLowerCase()
        .includes(search.toLowerCase());
      const matchesCategory =
        !categoryFilter || sub.category === categoryFilter;
      const matchesStatus = !statusFilter || sub.status === statusFilter;
      return matchesSearch && matchesCategory && matchesStatus;
    });
  }, [subscriptions, search, categoryFilter, statusFilter]);

  if (isLoading) {
    return (
      <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
        {[...Array(6)].map((_, i) => (
          <div
            key={i}
            className="h-48 rounded-xl bg-neutral-100 animate-pulse"
          />
        ))}
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {/* Filters */}
      <div className="flex flex-col sm:flex-row gap-4">
        <div className="flex-1">
          <Input
            placeholder="Buscar assinatura..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
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
        <div className="w-full sm:w-48">
          <Select
            options={categoryOptions}
            value={categoryFilter}
            onChange={(e) => setCategoryFilter(e.target.value)}
          />
        </div>
        <div className="w-full sm:w-40">
          <Select
            options={statusOptions}
            value={statusFilter}
            onChange={(e) => setStatusFilter(e.target.value)}
          />
        </div>
      </div>

      {/* List */}
      {filteredSubscriptions.length === 0 ? (
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
        <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {filteredSubscriptions.map((subscription) => (
            <SubscriptionCard
              key={subscription.id}
              subscription={subscription}
            />
          ))}
        </div>
      )}
    </div>
  );
}
