"use client";

import {useEffect, useState} from "react";
import Link from "next/link";
import {cn} from "@/lib/utils";
import {Button, Container} from "@/components/ui";
import {Logo} from "@/components/shared";
import {Menu, X} from "lucide-react";

const navLinks = [
  { label: "Features", href: "#features" },
  { label: "Como funciona", href: "#how-it-works" },
  { label: "Preços", href: "#pricing" },
];

export function Header() {
  const [isScrolled, setIsScrolled] = useState(false);
  const [isMobileMenuOpen, setIsMobileMenuOpen] = useState(false);

  useEffect(() => {
    const handleScroll = () => {
      setIsScrolled(window.scrollY > 10);
    };

    window.addEventListener("scroll", handleScroll);
    return () => window.removeEventListener("scroll", handleScroll);
  }, []);

  return (
    <header
      className={cn(
          "fixed top-0 left-0 right-0 z-50 transition-[background-color,box-shadow,padding] duration-300",
          isScrolled || isMobileMenuOpen
              ? "bg-white py-3 shadow-soft md:bg-white/80 md:backdrop-blur-md"
              : "bg-white py-4 md:bg-transparent"
      )}
      style={{
          paddingTop: isScrolled || isMobileMenuOpen
              ? "max(0.75rem, env(safe-area-inset-top))"
              : "max(1rem, env(safe-area-inset-top))",
          paddingRight: "env(safe-area-inset-right)",
          paddingLeft: "env(safe-area-inset-left)",
      }}
    >
      <Container>
        <nav className="flex items-center justify-between">
          <Logo />

          {/* Desktop Navigation */}
          <div className="hidden md:flex items-center gap-8">
            {navLinks.map((link) => (
              <a
                key={link.href}
                href={link.href}
                className="text-neutral-600 hover:text-neutral-900 font-medium transition-colors"
              >
                {link.label}
              </a>
            ))}
          </div>

          {/* Desktop CTA */}
          <div className="hidden md:flex items-center gap-4">
              <Link href="/login">
              <Button variant="ghost" size="sm">
                Entrar
              </Button>
            </Link>
              <Link href="/register">
                  <Button variant="primary" size="sm">
                      Começar grátis
                  </Button>
              </Link>
          </div>

          {/* Mobile Menu Button */}
          <button
              type="button"
            className="md:hidden p-2 hover:bg-neutral-100 rounded-lg transition-colors"
            onClick={() => setIsMobileMenuOpen(!isMobileMenuOpen)}
            aria-label="Toggle menu"
              aria-expanded={isMobileMenuOpen}
          >
            {isMobileMenuOpen ? (
              <X size={24} className="text-neutral-900" />
            ) : (
              <Menu size={24} className="text-neutral-900" />
            )}
          </button>
        </nav>

        {/* Mobile Menu */}
        {isMobileMenuOpen && (
            <div
                className="md:hidden mt-3 rounded-2xl border border-neutral-100 bg-white shadow-elevated animate-fadeIn">
                <div className="space-y-4 px-6 pb-[max(1rem,env(safe-area-inset-bottom))] pt-4">
              {navLinks.map((link) => (
                <a
                  key={link.href}
                  href={link.href}
                  className="block text-neutral-600 hover:text-neutral-900 font-medium py-2"
                  onClick={() => setIsMobileMenuOpen(false)}
                >
                  {link.label}
                </a>
              ))}
                    <div className="flex flex-col gap-3 border-t border-neutral-100 pt-4">
                        <Link href="/login" className="block" onClick={() => setIsMobileMenuOpen(false)}>
                  <Button variant="outline" className="w-full">
                    Entrar
                  </Button>
                </Link>
                        <Link href="/register" className="block" onClick={() => setIsMobileMenuOpen(false)}>
                            <Button variant="primary" className="w-full">
                                Começar grátis
                            </Button>
                        </Link>
              </div>
            </div>
          </div>
        )}
      </Container>
    </header>
  );
}
