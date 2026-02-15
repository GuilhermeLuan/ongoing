-- Migration: Convert BillingCycle from entity to enum
-- Migrates tb_billing_cycles table data to billing_cycle VARCHAR column

-- Step 1: Add new billing_cycle column as VARCHAR
ALTER TABLE tb_subscriptions
    ADD COLUMN billing_cycle VARCHAR(20);

-- Step 2: Migrate data from tb_billing_cycles to enum values
-- Maps legacy names to enum constant names
UPDATE tb_subscriptions s
SET billing_cycle = CASE bc.name
                        WHEN 'Monthly' THEN 'MONTHLY'
                        WHEN 'Quarterly' THEN 'QUARTERLY'
                        WHEN 'Semi-Annual' THEN 'SEMI_ANNUAL'
                        WHEN 'Annual' THEN 'YEARLY'
                        WHEN 'Weekly' THEN 'WEEKLY'
                        WHEN 'Bi-weekly' THEN 'BIWEEKLY'
    END FROM tb_billing_cycles bc
WHERE s.id_billing_cycle = bc.id;

-- Step 3: Make billing_cycle NOT NULL after data migration
-- (only if there are existing subscriptions without billing cycle)
UPDATE tb_subscriptions
SET billing_cycle = 'MONTHLY'
WHERE billing_cycle IS NULL;

ALTER TABLE tb_subscriptions
    ALTER COLUMN billing_cycle SET NOT NULL;

-- Step 4: Remove foreign key constraint
ALTER TABLE tb_subscriptions
    DROP CONSTRAINT IF EXISTS tb_subscriptions_id_billing_cycle_fkey;

-- Step 5: Drop old id_billing_cycle column
ALTER TABLE tb_subscriptions
    DROP COLUMN id_billing_cycle;

-- Step 6: Drop tb_billing_cycles table (no longer needed)
DROP TABLE IF EXISTS tb_billing_cycles;

-- Step 7: Add comment for documentation
COMMENT ON COLUMN tb_subscriptions.billing_cycle IS 'Billing cycle frequency (enum): MONTHLY, QUARTERLY, SEMI_ANNUAL, YEARLY, WEEKLY, BIWEEKLY';
