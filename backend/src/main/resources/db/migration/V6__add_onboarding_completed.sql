-- Add onboarding_completed column to tb_users table
ALTER TABLE tb_users
ADD COLUMN onboarding_completed BOOLEAN NOT NULL DEFAULT FALSE;

-- Add comment for documentation
COMMENT ON COLUMN tb_users.onboarding_completed IS 'Tracks whether the user has completed the onboarding wizard';
