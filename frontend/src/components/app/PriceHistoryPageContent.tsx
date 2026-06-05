"use client";

import {AlertTriangle, CalendarDays, DollarSign, TrendingUp} from "lucide-react";
import type {FormEvent} from "react";
import {Badge, Button, Input, Skeleton} from "@/components/ui";
import {
    type SubscriptionPriceHistoryResponse,
    usePriceSpikes,
} from "@/features/subscriptions";
import {AppHeader} from "./AppHeader";
import {StatCard} from "./StatCard";

const currencyFormatter = new Intl.NumberFormat("pt-BR", {
    style: "currency",
    currency: "BRL",
});

const percentageFormatter = new Intl.NumberFormat("pt-BR", {
    minimumFractionDigits: 0,
    maximumFractionDigits: 2,
});

const formatCurrency = (value: number) => currencyFormatter.format(value);

const formatPercentage = (value: number) => `${percentageFormatter.format(value)}%`;

const formatDate = (value: string) => {
    return new Intl.DateTimeFormat("pt-BR").format(new Date(`${value.slice(0, 10)}T00:00:00`));
};

const getAbsoluteIncrease = (item: SubscriptionPriceHistoryResponse) =>
    item.newValue - item.oldValue;

const getSummary = (items: SubscriptionPriceHistoryResponse[]) => {
    const spikeCount = items.filter((item) => item.isPriceSpike).length;
    const highestPercentage = items.reduce(
        (highest, item) => Math.max(highest, item.changePercentage),
        0
    );
    const highestAbsolute = items.reduce(
        (highest, item) => Math.max(highest, getAbsoluteIncrease(item)),
        0
    );

    return {
        spikeCount,
        highestPercentage,
        highestAbsolute,
    };
};

export function PriceHistoryPageContent() {
    const {
        priceSpikes,
        draftFilters,
        isLoading,
        error,
        setDraftFilter,
        applyFilters,
    } = usePriceSpikes();
    const summary = getSummary(priceSpikes);

    const handleSubmit = (event: FormEvent<HTMLFormElement>) => {
        event.preventDefault();
        applyFilters();
    };

    return (
        <>
            <AppHeader
                title="Histórico"
                subtitle="Acompanhe reajustes relevantes nas suas assinaturas"
            />

            <main className="space-y-6 p-4 sm:p-6">
                <div>
                    <h1 className="font-display text-2xl font-bold text-neutral-900">
                        Histórico de reajustes
                    </h1>
                    <p className="mt-1 text-neutral-500">
                        Veja aumentos detectados no período selecionado.
                    </p>
                </div>

                <form
                    className="rounded-xl border border-neutral-100 bg-white p-4 shadow-soft"
                    onSubmit={handleSubmit}
                >
                    <div className="grid gap-4 md:grid-cols-[1fr_1fr_auto] md:items-end">
                        <Input
                            id="price-history-from"
                            label="De"
                            type="date"
                            value={draftFilters.from}
                            max={draftFilters.to}
                            onChange={(event) => setDraftFilter("from", event.target.value)}
                        />
                        <Input
                            id="price-history-to"
                            label="Até"
                            type="date"
                            value={draftFilters.to}
                            min={draftFilters.from}
                            onChange={(event) => setDraftFilter("to", event.target.value)}
                        />
                        <Button
                            type="submit"
                            className="h-[46px] w-full md:w-auto"
                            disabled={!draftFilters.from || !draftFilters.to}
                        >
                            Aplicar filtro
                        </Button>
                    </div>
                </form>

                {error && (
                    <div className="rounded-lg border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700">
                        {error}
                    </div>
                )}

                <div className="grid gap-4 md:grid-cols-3">
                    <StatCard
                        title="Spikes no período"
                        value={isLoading ? "-" : summary.spikeCount}
                        subtitle="Reajustes marcados como relevantes"
                        icon={<AlertTriangle className="h-5 w-5" />}
                        variant="warning"
                    />
                    <StatCard
                        title="Maior reajuste"
                        value={isLoading ? "-" : formatPercentage(summary.highestPercentage)}
                        subtitle="Maior variação percentual"
                        icon={<TrendingUp className="h-5 w-5" />}
                        variant="primary"
                    />
                    <StatCard
                        title="Maior aumento"
                        value={isLoading ? "-" : formatCurrency(summary.highestAbsolute)}
                        subtitle="Diferença em valor absoluto"
                        icon={<DollarSign className="h-5 w-5" />}
                        variant="success"
                    />
                </div>

                <section className="overflow-hidden rounded-xl border border-neutral-100 bg-white shadow-soft">
                    <div className="flex flex-col gap-1 border-b border-neutral-100 px-4 py-4 sm:px-5">
                        <h2 className="font-display text-lg font-semibold text-neutral-900">
                            Reajustes encontrados
                        </h2>
                        <p className="text-sm text-neutral-500">
                            Valores antigos e novos registrados por assinatura.
                        </p>
                    </div>

                    {isLoading ? (
                        <div
                            className="space-y-3 p-4 sm:p-5"
                            aria-label="Carregando histórico de reajustes"
                        >
                            {[...Array(5)].map((_, index) => (
                                <Skeleton key={index} className="h-14 w-full" />
                            ))}
                        </div>
                    ) : priceSpikes.length === 0 ? (
                        <div className="px-4 py-12 text-center sm:px-5">
                            <div className="mx-auto mb-4 flex h-16 w-16 items-center justify-center rounded-full bg-neutral-100">
                                <CalendarDays className="h-8 w-8 text-neutral-400" />
                            </div>
                            <h3 className="text-lg font-medium text-neutral-900">
                                Nenhum reajuste encontrado
                            </h3>
                            <p className="mt-1 text-neutral-500">
                                Tente ajustar o período para ver outros registros.
                            </p>
                        </div>
                    ) : (
                        <div className="overflow-x-auto">
                            <table className="min-w-[760px] w-full text-left">
                                <thead className="bg-neutral-50 text-xs uppercase tracking-wide text-neutral-500">
                                <tr>
                                    <th className="px-5 py-3 font-semibold">Assinatura</th>
                                    <th className="px-5 py-3 font-semibold">Valor antigo</th>
                                    <th className="px-5 py-3 font-semibold">Valor novo</th>
                                    <th className="px-5 py-3 font-semibold">Diferença</th>
                                    <th className="px-5 py-3 font-semibold">Percentual</th>
                                    <th className="px-5 py-3 font-semibold">Data</th>
                                    <th className="px-5 py-3 font-semibold">Status</th>
                                </tr>
                                </thead>
                                <tbody className="divide-y divide-neutral-100">
                                {priceSpikes.map((item) => (
                                    <tr key={item.id} className="text-sm text-neutral-700">
                                        <td className="px-5 py-4 font-medium text-neutral-900">
                                            #{item.subscriptionId}
                                        </td>
                                        <td className="px-5 py-4">{formatCurrency(item.oldValue)}</td>
                                        <td className="px-5 py-4">{formatCurrency(item.newValue)}</td>
                                        <td className="px-5 py-4 font-medium text-neutral-900">
                                            {formatCurrency(getAbsoluteIncrease(item))}
                                        </td>
                                        <td className="px-5 py-4 text-primary-600 font-semibold">
                                            {formatPercentage(item.changePercentage)}
                                        </td>
                                        <td className="px-5 py-4">{formatDate(item.changedAt)}</td>
                                        <td className="px-5 py-4">
                                            {item.isPriceSpike ? (
                                                <Badge variant="accent">Price spike</Badge>
                                            ) : (
                                                <Badge variant="neutral">Reajuste</Badge>
                                            )}
                                        </td>
                                    </tr>
                                ))}
                                </tbody>
                            </table>
                        </div>
                    )}
                </section>
            </main>
        </>
    );
}
