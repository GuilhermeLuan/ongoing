import { AppHeader, StatCard, SubscriptionCard } from "@/components/app";
import { Button } from "@/components/ui";
import {
  mockStats,
  mockSubscriptions,
  formatCurrency,
  getUpcomingBillings,
  categoryLabels,
  categoryColors,
} from "@/lib/mock-data";

export const metadata = {
  title: "Dashboard - Ongoing",
};

export default function DashboardPage() {
  const upcomingBillings = getUpcomingBillings(mockSubscriptions, 5);

  return (
    <>
      <AppHeader />
      <main className="p-6">
        {/* Welcome */}
        <div className="mb-8">
          <h1 className="text-2xl font-bold text-neutral-900 font-display">
            Olá, Guilherme
          </h1>
          <p className="text-neutral-500 mt-1">
            Aqui está um resumo das suas assinaturas.
          </p>
        </div>

        {/* Stats Grid */}
        <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4 mb-8">
          <StatCard
            title="Gasto Mensal"
            value={formatCurrency(mockStats.monthlySpending)}
            subtitle="12 assinaturas ativas"
            variant="primary"
            icon={
              <svg
                className="w-6 h-6"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1M21 12a9 9 0 11-18 0 9 9 0 0118 0z"
                />
              </svg>
            }
          />
          <StatCard
            title="Total de Assinaturas"
            value={mockStats.totalSubscriptions}
            subtitle={`${mockStats.activeSubscriptions} ativas`}
            icon={
              <svg
                className="w-6 h-6"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="M19 11H5m14 0a2 2 0 012 2v6a2 2 0 01-2 2H5a2 2 0 01-2-2v-6a2 2 0 012-2m14 0V9a2 2 0 00-2-2M5 11V9a2 2 0 012-2m0 0V5a2 2 0 012-2h6a2 2 0 012 2v2M7 7h10"
                />
              </svg>
            }
          />
          <StatCard
            title="Vencendo Esta Semana"
            value={mockStats.expiringThisWeek}
            subtitle="assinaturas"
            variant="warning"
            icon={
              <svg
                className="w-6 h-6"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z"
                />
              </svg>
            }
          />
          <StatCard
            title="Próximo Vencimento"
            value={`${mockStats.nextBillingDays} dias`}
            subtitle={mockStats.nextBillingName}
            variant="success"
            icon={
              <svg
                className="w-6 h-6"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z"
                />
              </svg>
            }
          />
        </div>

        {/* Content Grid */}
        <div className="grid gap-6 lg:grid-cols-3">
          {/* Upcoming Billings */}
          <div className="lg:col-span-2 bg-white rounded-xl border border-neutral-100 shadow-soft">
            <div className="flex items-center justify-between p-5 border-b border-neutral-100">
              <div>
                <h2 className="font-semibold text-neutral-900">
                  Próximos Vencimentos
                </h2>
                <p className="text-sm text-neutral-500 mt-0.5">
                  Suas próximas cobranças
                </p>
              </div>
              <Button variant="ghost" size="sm">
                Ver todas
              </Button>
            </div>
            <div className="p-3 space-y-2">
              {upcomingBillings.map((subscription) => (
                <SubscriptionCard
                  key={subscription.id}
                  subscription={subscription}
                  variant="compact"
                />
              ))}
            </div>
          </div>

          {/* Category Breakdown */}
          <div className="bg-white rounded-xl border border-neutral-100 shadow-soft">
            <div className="p-5 border-b border-neutral-100">
              <h2 className="font-semibold text-neutral-900">
                Gastos por Categoria
              </h2>
              <p className="text-sm text-neutral-500 mt-0.5">
                Distribuição mensal
              </p>
            </div>
            <div className="p-5 space-y-4">
              {mockStats.categoryBreakdown.map(({ category, total }) => {
                const percentage = Math.round(
                  (total / mockStats.monthlySpending) * 100
                );
                return (
                  <div key={category}>
                    <div className="flex items-center justify-between mb-1.5">
                      <span className="text-sm font-medium text-neutral-700">
                        {categoryLabels[category]}
                      </span>
                      <span className="text-sm text-neutral-500">
                        {formatCurrency(total)}
                      </span>
                    </div>
                    <div className="h-2 bg-neutral-100 rounded-full overflow-hidden">
                      <div
                        className="h-full rounded-full transition-all duration-500"
                        style={{
                          width: `${percentage}%`,
                          backgroundColor: categoryColors[category],
                        }}
                      />
                    </div>
                  </div>
                );
              })}
            </div>
          </div>
        </div>
      </main>
    </>
  );
}
