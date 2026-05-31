import {fireEvent, render, screen, waitFor} from "@testing-library/react";
import {afterEach, beforeEach, describe, expect, it, vi} from "vitest";
import {subscriptionService} from "@/features/subscriptions";
import {SidebarProvider} from "./SidebarContext";
import {PriceHistoryPageContent} from "./PriceHistoryPageContent";

vi.mock("@/features/subscriptions/services/subscription.service", () => ({
    subscriptionService: {
        findPriceSpikes: vi.fn(),
    },
}));

vi.mock("./AppHeader", () => ({
    AppHeader: ({title}: { title?: string }) => <header>{title}</header>,
}));

const renderPage = () => {
    const result = render(
        <SidebarProvider>
            <PriceHistoryPageContent />
        </SidebarProvider>
    );
    vi.useRealTimers();

    return result;
};

const priceSpikes = [
    {
        id: 1,
        subscriptionId: 10,
        oldValue: 40,
        newValue: 50,
        changePercentage: 25,
        isPriceSpike: true,
        changedAt: "2026-05-20",
    },
    {
        id: 2,
        subscriptionId: 11,
        oldValue: 20,
        newValue: 24,
        changePercentage: 20,
        isPriceSpike: false,
        changedAt: "2026-05-22",
    },
];

describe("PriceHistoryPageContent", () => {
    beforeEach(() => {
        vi.useFakeTimers({now: new Date("2026-05-31T12:00:00")});
        vi.mocked(subscriptionService.findPriceSpikes).mockResolvedValue([]);
    });

    afterEach(() => {
        vi.clearAllMocks();
        vi.useRealTimers();
    });

    it("renders the loading state", () => {
        vi.mocked(subscriptionService.findPriceSpikes).mockReturnValue(
            new Promise(() => {
            })
        );

        renderPage();

        expect(
            screen.getByLabelText("Carregando histórico de reajustes")
        ).toBeInTheDocument();
    });

    it("calls the service with the default 30-day date range", async () => {
        renderPage();

        await waitFor(() => {
            expect(subscriptionService.findPriceSpikes).toHaveBeenCalledWith({
                from: "2026-05-01",
                to: "2026-05-31",
            });
        });
    });

    it("renders a list with price spikes", async () => {
        vi.mocked(subscriptionService.findPriceSpikes).mockResolvedValue(priceSpikes);

        renderPage();

        expect(await screen.findByText("#10")).toBeInTheDocument();
        expect(screen.getByText("#11")).toBeInTheDocument();
        expect(screen.getByText("Price spike")).toBeInTheDocument();
        expect(screen.getAllByText("25%").length).toBeGreaterThan(0);
        expect(screen.getByText("20/05/2026")).toBeInTheDocument();
    });

    it("renders the empty state", async () => {
        renderPage();

        expect(await screen.findByText("Nenhum reajuste encontrado")).toBeInTheDocument();
        expect(
            screen.getByText("Tente ajustar o período para ver outros registros.")
        ).toBeInTheDocument();
    });

    it("calls the service with selected dates after applying filters", async () => {
        vi.mocked(subscriptionService.findPriceSpikes).mockResolvedValue(priceSpikes);

        renderPage();

        await waitFor(() => {
            expect(subscriptionService.findPriceSpikes).toHaveBeenCalledTimes(1);
        });

        fireEvent.change(screen.getByLabelText("De"), {
            target: {value: "2026-04-01"},
        });
        fireEvent.change(screen.getByLabelText("Até"), {
            target: {value: "2026-04-30"},
        });
        fireEvent.click(screen.getByRole("button", {name: "Aplicar filtro"}));

        await waitFor(() => {
            expect(subscriptionService.findPriceSpikes).toHaveBeenLastCalledWith({
                from: "2026-04-01",
                to: "2026-04-30",
            });
        });
    });
});
