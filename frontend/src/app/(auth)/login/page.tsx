import type {Metadata} from "next";
import {LoginForm} from "@/components/auth";

export const metadata: Metadata = {
    title: "Entrar | Ongoing",
    description: "Entre na sua conta Ongoing para gerenciar suas assinaturas.",
};

export default function LoginPage() {
    return <LoginForm/>;
}
