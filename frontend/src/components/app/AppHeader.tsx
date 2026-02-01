"use client";

import { Input } from "@/components/ui";
import { useSidebar } from "./SidebarContext";
import { Menu } from "lucide-react";

interface AppHeaderProps {
  title?: string;
  subtitle?: string;
}

export function AppHeader({ title, subtitle }: AppHeaderProps) {
  const { open } = useSidebar();

  return (
    <header className="sticky top-0 z-30 bg-white/80 backdrop-blur-sm border-b border-neutral-100">
      <div className="flex items-center justify-between h-16 px-4 sm:px-6">
        {/* Left side - Menu button & Title */}
        <div className="flex items-center gap-3">
          {/* Mobile menu button */}
          <button
            onClick={open}
            className="lg:hidden p-2 -ml-2 text-neutral-600 hover:text-neutral-900 hover:bg-neutral-100 rounded-lg transition-colors"
            aria-label="Abrir menu"
          >
            <Menu size={22} />
          </button>

          {/* Title */}
          <div className="hidden sm:block">
            {title && (
              <h1 className="text-lg font-semibold text-neutral-900">{title}</h1>
            )}
            {subtitle && (
              <p className="text-sm text-neutral-500">{subtitle}</p>
            )}
          </div>
        </div>

        {/* Search & Actions */}
        <div className="flex items-center gap-2 sm:gap-4">
          {/* Search - hidden on small mobile, icon only on mobile */}
          <div className="hidden sm:block sm:w-48 md:w-72">
            <Input
              placeholder="Buscar assinaturas..."
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

          {/* Search button - mobile only */}
          <button className="sm:hidden p-2 text-neutral-400 hover:text-neutral-600 hover:bg-neutral-100 rounded-lg transition-colors">
            <svg
              className="w-5 h-5"
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
          </button>

          {/* Notifications */}
          <button className="relative p-2 text-neutral-400 hover:text-neutral-600 hover:bg-neutral-100 rounded-lg transition-colors">
            <svg
              className="w-5 h-5"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth={2}
                d="M15 17h5l-1.405-1.405A2.032 2.032 0 0118 14.158V11a6.002 6.002 0 00-4-5.659V5a2 2 0 10-4 0v.341C7.67 6.165 6 8.388 6 11v3.159c0 .538-.214 1.055-.595 1.436L4 17h5m6 0v1a3 3 0 11-6 0v-1m6 0H9"
              />
            </svg>
            <span className="absolute top-1.5 right-1.5 w-2 h-2 bg-red-500 rounded-full" />
          </button>

          {/* User avatar - mobile only */}
          <button className="lg:hidden p-1">
            <div className="w-8 h-8 rounded-full bg-gradient-to-br from-primary-400 to-accent-500 flex items-center justify-center">
              <span className="text-white font-semibold text-xs">GS</span>
            </div>
          </button>
        </div>
      </div>
    </header>
  );
}
