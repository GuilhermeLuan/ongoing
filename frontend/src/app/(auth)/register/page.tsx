import type {Metadata} from "next";
import {RegisterForm} from "@/components/auth";

export const metadata: Metadata = {
    title: "Registrar | Ongoing",
    description:
        "Crie sua conta Ongoing e comece a gerenciar suas assinaturas.",
};

export default function RegisterPage() {
    return <RegisterForm/>;
}
