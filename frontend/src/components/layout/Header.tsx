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
        "fixed top-0 left-0 right-0 z-50 transition-all duration-300",
        isScrolled
          ? "bg-white/80 backdrop-blur-md shadow-soft py-3"
          : "bg-transparent py-4"
      )}
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
            className="md:hidden p-2 hover:bg-neutral-100 rounded-lg transition-colors"
            onClick={() => setIsMobileMenuOpen(!isMobileMenuOpen)}
            aria-label="Toggle menu"
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
          <div className="md:hidden absolute top-full left-0 right-0 bg-white shadow-elevated border-t border-neutral-100 animate-fadeIn">
            <div className="py-4 px-6 space-y-4">
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
              <div className="pt-4 border-t border-neutral-100 space-y-3">
                  <Link href="/login">
                  <Button variant="outline" className="w-full">
                    Entrar
                  </Button>
                </Link>
                  <Link href="/register">
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
