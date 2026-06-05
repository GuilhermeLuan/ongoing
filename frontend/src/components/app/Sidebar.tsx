"use client";

import Link from "next/link";
import {usePathname} from "next/navigation";
import {cn} from "@/lib/utils";
import {History, Home, WalletCards, X} from "lucide-react";

const navigation = [
  {
    name: "Dashboard",
    href: "/dashboard",
    icon: <Home className="h-5 w-5" />,
  },
  {
    name: "Assinaturas",
    href: "/subscriptions",
    icon: <WalletCards className="h-5 w-5" />,
  },
  {
    name: "Histórico",
    href: "/subscriptions/price-history",
    icon: <History className="h-5 w-5" />,
  },
];

interface SidebarProps {
  isOpen?: boolean;
  onClose?: () => void;
}

export function Sidebar({ isOpen = false, onClose }: SidebarProps) {
  const pathname = usePathname();

  const handleLinkClick = () => {
    if (onClose) {
      onClose();
    }
  };

  return (
    <>
      {/* Mobile Overlay */}
      {isOpen && (
        <div
          className="fixed inset-0 z-40 bg-black/50 lg:hidden"
          onClick={onClose}
        />
      )}

      {/* Sidebar */}
      <aside
        className={cn(
            "fixed left-0 top-0 z-50 h-screen w-60 border-r border-neutral-200 bg-white pb-[env(safe-area-inset-bottom)] pt-[env(safe-area-inset-top)] transition-transform duration-300 ease-in-out",
          // Mobile: hidden by default, shown when isOpen
          "lg:translate-x-0",
          isOpen ? "translate-x-0" : "-translate-x-full"
        )}
      >
        <div className="flex h-full flex-col">
          {/* Logo */}
          <div className="flex h-16 items-center justify-between px-6 border-b border-neutral-100">
            <Link href="/dashboard" className="flex items-center gap-2">
              <div className="w-8 h-8 rounded-lg bg-gradient-to-br from-primary-500 to-accent-500 flex items-center justify-center">
                <span className="text-white font-bold text-sm">O</span>
              </div>
              <span className="font-display font-bold text-xl text-neutral-900">
                Ongoing
              </span>
            </Link>
            {/* Close button - mobile only */}
            <button
              onClick={onClose}
              className="lg:hidden p-2 -mr-2 text-neutral-400 hover:text-neutral-600 transition-colors"
              aria-label="Fechar menu"
            >
              <X size={20} />
            </button>
          </div>

          {/* Navigation */}
          <nav className="flex-1 px-3 py-4 space-y-1 overflow-y-auto">
            {navigation.map((item) => {
              const isActive = pathname === item.href;
              return (
                <Link
                  key={item.name}
                  href={item.href}
                  onClick={handleLinkClick}
                  className={cn(
                      "flex items-center gap-3 rounded-lg px-3 py-2.5 text-sm font-medium transition-colors duration-200",
                    isActive
                      ? "bg-primary-50 text-primary-600"
                      : "text-neutral-600 hover:bg-neutral-50 hover:text-neutral-900"
                  )}
                >
                  <span
                    className={cn(
                      isActive ? "text-primary-500" : "text-neutral-400"
                    )}
                  >
                    {item.icon}
                  </span>
                  {item.name}
                </Link>
              );
            })}
          </nav>

          {/* Footer */}
          <div className="p-4 border-t border-neutral-100">
            <div className="flex items-center gap-3 px-3 py-2">
              <div className="w-9 h-9 rounded-full bg-gradient-to-br from-primary-400 to-accent-500 flex items-center justify-center">
                <span className="text-white font-semibold text-sm">GS</span>
              </div>
              <div className="flex-1 min-w-0">
                <p className="text-sm font-medium text-neutral-900 truncate">
                  Guilherme
                </p>
                <p className="text-xs text-neutral-500 truncate">
                  guilherme@email.com
                </p>
              </div>
            </div>
          </div>
        </div>
      </aside>
    </>
  );
}
