'use client';

import { useEffect, useState } from 'react';

export default function ConfettiAnimation() {
    const [particles, setParticles] = useState<Array<{
        id: number;
        left: number;
        delay: number;
        duration: number;
        color: string;
    }>>([]);

    useEffect(() => {
        // Generate random confetti particles
        const colors = ['#10B981', '#8B5CF6', '#F59E0B', '#EF4444', '#3B82F6'];
        const newParticles = Array.from({ length: 50 }, (_, i) => ({
            id: i,
            left: Math.random() * 100,
            delay: Math.random() * 0.5,
            duration: 2 + Math.random() * 1,
            color: colors[Math.floor(Math.random() * colors.length)],
        }));
        setParticles(newParticles);

        // Remove particles after animation
        const timer = setTimeout(() => {
            setParticles([]);
        }, 3500);

        return () => clearTimeout(timer);
    }, []);

    return (
        <div className="fixed inset-0 pointer-events-none overflow-hidden z-50">
            {particles.map((particle) => (
                <div
                    key={particle.id}
                    className="absolute top-0 w-2 h-2 rounded-full animate-confetti"
                    style={{
                        left: `${particle.left}%`,
                        backgroundColor: particle.color,
                        animationDelay: `${particle.delay}s`,
                        animationDuration: `${particle.duration}s`,
                    }}
                />
            ))}
        </div>
    );
}
