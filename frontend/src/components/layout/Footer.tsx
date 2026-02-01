import { Container } from "@/components/ui";
import { Github, Twitter, Linkedin, Instagram } from "lucide-react";

const footerLinks = {
  produto: [
    { label: "Features", href: "#features" },
    { label: "Preços", href: "#pricing" },
    { label: "Integrações", href: "#" },
    { label: "Roadmap", href: "#" },
  ],
  empresa: [
    { label: "Sobre", href: "#" },
    { label: "Blog", href: "#" },
    { label: "Carreiras", href: "#" },
    { label: "Contato", href: "#" },
  ],
  legal: [
    { label: "Privacidade", href: "#" },
    { label: "Termos de uso", href: "#" },
    { label: "Cookies", href: "#" },
  ],
};

const socialLinks = [
  { icon: Twitter, href: "#", label: "Twitter" },
  { icon: Instagram, href: "#", label: "Instagram" },
  { icon: Linkedin, href: "#", label: "LinkedIn" },
  { icon: Github, href: "#", label: "GitHub" },
];

export function Footer() {
  return (
    <footer className="bg-neutral-900 text-white pt-16 pb-8">
      <Container>
        <div className="grid grid-cols-2 md:grid-cols-5 gap-8 pb-12 border-b border-neutral-800">
          {/* Brand */}
          <div className="col-span-2">
            <div className="mb-4">
              <div className="flex items-center gap-2">
                <div className="bg-gradient-to-br from-primary-500 to-primary-600 p-2 rounded-xl">
                  <svg
                    width="24"
                    height="24"
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="currentColor"
                    strokeWidth="2.5"
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    className="text-white"
                  >
                    <path d="m17 2 4 4-4 4" />
                    <path d="M3 11v-1a4 4 0 0 1 4-4h14" />
                    <path d="m7 22-4-4 4-4" />
                    <path d="M21 13v1a4 4 0 0 1-4 4H3" />
                  </svg>
                </div>
                <span className="font-display font-bold text-xl text-white">
                  Ongoing
                </span>
              </div>
            </div>
            <p className="text-neutral-400 max-w-xs mb-6">
              Gerencie todas as suas assinaturas em um só lugar. Simples,
              intuitivo e feito para brasileiros.
            </p>
            <div className="flex gap-4">
              {socialLinks.map((social) => (
                <a
                  key={social.label}
                  href={social.href}
                  className="w-10 h-10 rounded-lg bg-neutral-800 hover:bg-neutral-700 flex items-center justify-center transition-colors"
                  aria-label={social.label}
                >
                  <social.icon size={18} className="text-neutral-400" />
                </a>
              ))}
            </div>
          </div>

          {/* Links */}
          <div>
            <h4 className="font-semibold text-white mb-4">Produto</h4>
            <ul className="space-y-3">
              {footerLinks.produto.map((link) => (
                <li key={link.label}>
                  <a
                    href={link.href}
                    className="text-neutral-400 hover:text-white transition-colors"
                  >
                    {link.label}
                  </a>
                </li>
              ))}
            </ul>
          </div>

          <div>
            <h4 className="font-semibold text-white mb-4">Empresa</h4>
            <ul className="space-y-3">
              {footerLinks.empresa.map((link) => (
                <li key={link.label}>
                  <a
                    href={link.href}
                    className="text-neutral-400 hover:text-white transition-colors"
                  >
                    {link.label}
                  </a>
                </li>
              ))}
            </ul>
          </div>

          <div>
            <h4 className="font-semibold text-white mb-4">Legal</h4>
            <ul className="space-y-3">
              {footerLinks.legal.map((link) => (
                <li key={link.label}>
                  <a
                    href={link.href}
                    className="text-neutral-400 hover:text-white transition-colors"
                  >
                    {link.label}
                  </a>
                </li>
              ))}
            </ul>
          </div>
        </div>

        {/* Bottom */}
        <div className="pt-8 flex flex-col md:flex-row items-center justify-between gap-4">
          <p className="text-neutral-500 text-sm">
            © {new Date().getFullYear()} Ongoing. Todos os direitos reservados.
          </p>
          <p className="text-neutral-500 text-sm">
            Feito com 💚 no Brasil
          </p>
        </div>
      </Container>
    </footer>
  );
}
