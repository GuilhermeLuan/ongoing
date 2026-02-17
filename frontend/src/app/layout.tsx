import type {Metadata, Viewport} from "next";
import {Inter, JetBrains_Mono, Plus_Jakarta_Sans} from "next/font/google";
import "./globals.css";
import {AuthProvider} from "@/features/auth";

const plusJakarta = Plus_Jakarta_Sans({
  subsets: ["latin"],
  variable: "--font-plus-jakarta",
  display: "swap",
});

const inter = Inter({
  subsets: ["latin"],
  variable: "--font-inter",
  display: "swap",
});

const jetbrainsMono = JetBrains_Mono({
  subsets: ["latin"],
  variable: "--font-jetbrains",
  display: "swap",
});

export const metadata: Metadata = {
  title: "Ongoing - Gerencie suas assinaturas de forma inteligente",
  description:
    "Controle todas as suas assinaturas em um só lugar. Visualize gastos, receba alertas de renovação e economize dinheiro com o Ongoing.",
  keywords: [
    "assinaturas",
    "subscription tracker",
    "gerenciador de assinaturas",
    "controle financeiro",
    "streaming",
    "Netflix",
    "Spotify",
  ],
  authors: [{ name: "Ongoing" }],
  openGraph: {
    title: "Ongoing - Gerencie suas assinaturas de forma inteligente",
    description:
      "Controle todas as suas assinaturas em um só lugar. Visualize gastos, receba alertas de renovação e economize dinheiro.",
    type: "website",
    locale: "pt_BR",
  },
};

export const viewport: Viewport = {
    width: "device-width",
    initialScale: 1,
    viewportFit: "cover",
    themeColor: "#ffffff",
    colorScheme: "light",
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
      <html lang="pt-BR">
      <body
        className={`${plusJakarta.variable} ${inter.variable} ${jetbrainsMono.variable} font-body antialiased`}
      >
      <AuthProvider>{children}</AuthProvider>
      </body>
    </html>
  );
}
