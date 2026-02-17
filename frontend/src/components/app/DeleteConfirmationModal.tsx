"use client";

import {Button, Modal} from "@/components/ui";

interface DeleteConfirmationModalProps {
    isOpen: boolean;
    subscriptionName: string;
    onConfirm: () => void;
    onCancel: () => void;
}

export function DeleteConfirmationModal({
                                            isOpen,
                                            subscriptionName,
                                            onConfirm,
                                            onCancel,
                                        }: DeleteConfirmationModalProps) {
    return (
        <Modal
            isOpen={isOpen}
            onClose={onCancel}
            title="Excluir assinatura?"
            size="sm"
        >
            <div className="space-y-6">
                <p className="text-sm text-neutral-600">
                    Tem certeza que deseja excluir a assinatura{" "}
                    <span className="font-semibold text-neutral-900">&quot;{subscriptionName}&quot;</span>? Esta ação
                    não pode ser desfeita.
                </p>

                <div className="flex items-center justify-end gap-3">
                    <Button
                        variant="ghost"
                        size="sm"
                        onClick={onCancel}
                    >
                        Cancelar
                    </Button>
                    <Button
                        variant="outline"
                        size="sm"
                        onClick={onConfirm}
                        className="text-red-600 hover:text-red-700 border-red-200 hover:bg-red-50"
                    >
                        Excluir
                    </Button>
                </div>
            </div>
        </Modal>
    );
}
