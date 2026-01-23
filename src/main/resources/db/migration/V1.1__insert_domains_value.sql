INSERT INTO tb_categories (name)
VALUES ('Video Streaming'),
       ('Music Streaming'),
       ('Gaming'),
       ('Software / SaaS'),
       ('Education'),
       ('Health & Fitness'),
       ('Utilities'),
       ('Insurance'),
       ('Other');

INSERT INTO tb_payment_methods (name)
VALUES ('Credit Card'),
       ('Debit Card'),
       ('PIX'),
       ('Boleto'),
       ('PayPal'),
       ('Direct Debit');

INSERT INTO tb_billing_cycles (name)
VALUES ('Monthly'),
       ('Quarterly'),
       ('Semi-Annual'),
       ('Annual'),
       ('Weekly'),
       ('Bi-weekly');

INSERT INTO tb_subscription_types (name)
VALUES ('Paid'),
       ('Trial');