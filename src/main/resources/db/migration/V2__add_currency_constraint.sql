ALTER TABLE tb_subscriptions
    ADD CONSTRAINT chk_currency CHECK (currency IN ('BRL', 'USD', 'EUR'));
