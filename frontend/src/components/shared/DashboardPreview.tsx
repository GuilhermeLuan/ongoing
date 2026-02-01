"use client";

import { cn } from "@/lib/utils";
import {
  CreditCard,
  TrendingUp,
  Bell,
  Calendar,
  MoreVertical,
} from "lucide-react";

interface DashboardPreviewProps {
  className?: string;
}

const subscriptions = [
  { name: "Netflix", price: 55.9, color: "bg-red-500", dueIn: 5 },
  { name: "Spotify", price: 21.9, color: "bg-green-500", dueIn: 12 },
  { name: "iCloud", price: 3.5, color: "bg-blue-500", dueIn: 18 },
  { name: "ChatGPT", price: 104.0, color: "bg-neutral-800", dueIn: 3 },
];

export function DashboardPreview({ className }: DashboardPreviewProps) {
  const total = subscriptions.reduce((acc, sub) => acc + sub.price, 0);

  return (
    <div
      className={cn(
        "relative bg-white rounded-2xl shadow-elevated overflow-hidden border border-neutral-100",
        className
      )}
    >
      {/* Header */}
      <div className="bg-gradient-to-r from-primary-500 to-primary-600 px-6 py-4">
        <div className="flex items-center justify-between">
          <div>
            <p className="text-primary-100 text-sm">Gasto mensal total</p>
            <p className="font-mono text-3xl font-bold text-white">
              R$ {total.toFixed(2).replace(".", ",")}
            </p>
          </div>
          <div className="w-12 h-12 rounded-xl bg-white/20 flex items-center justify-center">
            <CreditCard className="text-white" size={24} />
          </div>
        </div>
      </div>

      {/* Stats */}
      <div className="grid grid-cols-3 gap-4 px-6 py-4 border-b border-neutral-100">
        <div className="text-center">
          <div className="flex items-center justify-center gap-1 text-primary-600 mb-1">
            <TrendingUp size={14} />
            <span className="text-xs font-medium">Economia</span>
          </div>
          <p className="font-mono font-semibold text-neutral-900">
            R$ 45,00
          </p>
        </div>
        <div className="text-center border-x border-neutral-100">
          <div className="flex items-center justify-center gap-1 text-accent-600 mb-1">
            <Bell size={14} />
            <span className="text-xs font-medium">Alertas</span>
          </div>
          <p className="font-mono font-semibold text-neutral-900">2</p>
        </div>
        <div className="text-center">
          <div className="flex items-center justify-center gap-1 text-blue-600 mb-1">
            <Calendar size={14} />
            <span className="text-xs font-medium">Próximo</span>
          </div>
          <p className="font-mono font-semibold text-neutral-900">3 dias</p>
        </div>
      </div>

      {/* Subscriptions list */}
      <div className="px-6 py-4">
        <div className="flex items-center justify-between mb-3">
          <h4 className="font-semibold text-neutral-900">Suas assinaturas</h4>
          <span className="text-sm text-neutral-500">
            {subscriptions.length} ativas
          </span>
        </div>
        <div className="space-y-3">
          {subscriptions.map((sub, index) => (
            <div
              key={index}
              className="flex items-center justify-between p-3 rounded-xl bg-neutral-50 hover:bg-neutral-100 transition-colors"
            >
              <div className="flex items-center gap-3">
                <div
                  className={cn(
                    "w-10 h-10 rounded-lg flex items-center justify-center text-white font-bold text-sm",
                    sub.color
                  )}
                >
                  {sub.name[0]}
                </div>
                <div>
                  <p className="font-medium text-neutral-900">{sub.name}</p>
                  <p className="text-xs text-neutral-500">
                    Renova em {sub.dueIn} dias
                  </p>
                </div>
              </div>
              <div className="flex items-center gap-3">
                <p className="font-mono font-semibold text-neutral-900">
                  R$ {sub.price.toFixed(2).replace(".", ",")}
                </p>
                <button className="p-1 hover:bg-neutral-200 rounded-lg transition-colors">
                  <MoreVertical size={16} className="text-neutral-400" />
                </button>
              </div>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}
