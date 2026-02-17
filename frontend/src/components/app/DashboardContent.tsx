/**
 * Dashboard Content Component
 * 
 * Client component that fetches and displays dashboard statistics.
 */

'use client';

import { AppHeader, StatCard, SubscriptionCard } from '@/components/app';
import { Button } from '@/components/ui';
import { useDashboard } from '@/features/dashboard';
import { useSubscriptions } from '@/features/subscriptions';
import { formatCurrency } from '@/lib/mock-data';
import { useAuth, getFirstName } from '@/features/auth';

export function DashboardContent() {
  const { dashboard, isLoading, error } = useDashboard();
  const { subscriptions, page } = useSubscriptions();
  const { user } = useAuth();

  // Extract first name from user's full name
  const firstName = getFirstName(user?.name);

  // Show loading state
  if (isLoading) {
    return (
      <>
        <AppHeader />
        <main className="p-6">
          <div className="flex items-center justify-center h-64">
            <div className="text-center">
              <div className="inline-block h-8 w-8 animate-spin rounded-full border-4 border-solid border-primary-600 border-r-transparent"></div>
              <p className="mt-4 text-neutral-600">Carregando dashboard...</p>
            </div>
          </div>
        </main>
      </>
    );
  }

  // Show error state
  if (error) {
    return (
      <>
        <AppHeader />
        <main className="p-6">
          <div className="bg-red-50 border border-red-200 rounded-lg p-4">
            <p className="text-red-800">
              Erro ao carregar dashboard: {error.message}
            </p>
          </div>
        </main>
      </>
    );
  }

  // No dashboard data (user has no subscriptions)
  if (!dashboard) {
    return (
      <>
        <AppHeader />
        <main className="p-6">
          <div className="text-center py-12">
            <p className="text-neutral-600">Nenhum dado disponível</p>
          </div>
        </main>
      </>
    );
  }

  // Get upcoming billings from subscriptions
  const upcomingBillings = subscriptions
    .filter((sub) => sub.active)
    .sort((a, b) => new Date(a.nextPaymentDate).getTime() - new Date(b.nextPaymentDate).getTime())
    .slice(0, 5);

  // Calculate stats from subscriptions
  const activeCount = subscriptions.filter((sub) => sub.active).length;
  const totalCount = page?.totalElements || 0;

  // Get next billing info
  const nextSubscription = upcomingBillings[0];
  const nextBillingDays = nextSubscription 
    ? Math.ceil((new Date(nextSubscription.nextPaymentDate).getTime() - Date.now()) / (1000 * 60 * 60 * 24))
    : 0;

  // Count subscriptions expiring this week
  const weekFromNow = new Date();
  weekFromNow.setDate(weekFromNow.getDate() + 7);
  const expiringThisWeek = subscriptions.filter((sub) => {
    const billingDate = new Date(sub.nextPaymentDate);
    return sub.active && billingDate <= weekFromNow && billingDate >= new Date();
  }).length;

  return (
    <>
      <AppHeader />
      <main className="p-6">
        {/* Welcome */}
        <div className="mb-8">
          <h1 className="text-2xl font-bold text-neutral-900 font-display">
            Olá, {firstName || 'Usuário'}!
          </h1>
          <p className="text-neutral-500 mt-1">
            Aqui está um resumo das suas assinaturas.
          </p>
        </div>

        {/* Stats Grid */}
        <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4 mb-8">
          <StatCard
            title="Gasto Mensal"
            value={formatCurrency(dashboard.monthlyAverage)}
            subtitle={`${activeCount} assinatura${activeCount !== 1 ? 's' : ''} ativa${activeCount !== 1 ? 's' : ''}`}
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
            value={totalCount.toString()}
            subtitle={`${activeCount} ativa${activeCount !== 1 ? 's' : ''}`}
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
            value={expiringThisWeek.toString()}
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
            value={nextBillingDays > 0 ? `${nextBillingDays} dia${nextBillingDays !== 1 ? 's' : ''}` : 'Hoje'}
            subtitle={nextSubscription?.name || 'Nenhum'}
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
              {upcomingBillings.length > 0 ? (
                upcomingBillings.map((subscription) => (
                  <SubscriptionCard
                    key={subscription.id}
                    subscription={subscription}
                    variant="compact"
                  />
                ))
              ) : (
                <div className="p-8 text-center text-neutral-500">
                  Nenhuma assinatura ativa
                </div>
              )}
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
              {dashboard.spendingByCategory.length > 0 ? (
                dashboard.spendingByCategory.map(({ categoryId, categoryName, total }) => {
                  const percentage = dashboard.monthlyAverage > 0
                    ? Math.round((total / dashboard.monthlyAverage) * 100)
                    : 0;
                  
                  // Generate a color based on category ID
                  const hue = (categoryId * 137) % 360; // Golden angle for diverse colors
                  const color = `hsl(${hue}, 70%, 50%)`;

                  return (
                    <div key={categoryId}>
                      <div className="flex items-center justify-between mb-1.5">
                        <span className="text-sm font-medium text-neutral-700">
                          {categoryName}
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
                            backgroundColor: color,
                          }}
                        />
                      </div>
                    </div>
                  );
                })
              ) : (
                <div className="text-center text-neutral-500 py-4">
                  Sem dados de categoria
                </div>
              )}
            </div>
          </div>
        </div>
      </main>
    </>
  );
}
