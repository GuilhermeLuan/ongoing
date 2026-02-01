"use client";

import { Sidebar, SidebarProvider, useSidebar } from "@/components/app";

function AppContent({ children }: { children: React.ReactNode }) {
  const { isOpen, close } = useSidebar();

  return (
    <>
      <Sidebar isOpen={isOpen} onClose={close} />
      {/* Main content - no padding on mobile, padding on desktop */}
      <div className="lg:pl-60">{children}</div>
    </>
  );
}

export default function AppLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <SidebarProvider>
      <div className="min-h-screen bg-neutral-50">
        <AppContent>{children}</AppContent>
      </div>
    </SidebarProvider>
  );
}
