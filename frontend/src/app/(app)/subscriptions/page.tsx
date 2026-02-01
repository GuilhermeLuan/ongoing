import { AppHeader, SubscriptionList } from "@/components/app";
import { Button } from "@/components/ui";
import { mockSubscriptions } from "@/lib/mock-data";

export const metadata = {
  title: "Assinaturas - Ongoing",
};

export default function SubscriptionsPage() {
  return (
    <>
      <AppHeader
        title="Assinaturas"
        subtitle={`${mockSubscriptions.length} assinaturas cadastradas`}
      />
      <main className="p-6">
        {/* Header */}
        <div className="flex items-center justify-between mb-6">
          <div>
            <h1 className="text-2xl font-bold text-neutral-900 font-display">
              Minhas Assinaturas
            </h1>
            <p className="text-neutral-500 mt-1">
              Gerencie todas as suas assinaturas em um só lugar.
            </p>
          </div>
          <Button>
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

        {/* List */}
        <SubscriptionList subscriptions={mockSubscriptions} />
      </main>
    </>
  );
}
