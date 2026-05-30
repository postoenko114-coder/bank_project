
CREATE INDEX IF NOT EXISTS idx_accounts_account_number ON accounts(account_number);

CREATE INDEX IF NOT EXISTS idx_accounts_user_id ON accounts(user_id);

CREATE INDEX IF NOT EXISTS idx_cards_card_number ON cards(card_number);

CREATE INDEX IF NOT EXISTS idx_cards_user_id ON cards(user_id);

CREATE INDEX IF NOT EXISTS idx_transactions_created_at ON transactions(created_at DESC);

CREATE INDEX IF NOT EXISTS idx_transactions_account_from ON transactions(account_from_id);

CREATE INDEX IF NOT EXISTS idx_transactions_account_to ON transactions(account_to_id);

CREATE INDEX IF NOT EXISTS idx_notifications_user_id ON notifications(user_id);

CREATE INDEX IF NOT EXISTS idx_notifications_status ON notifications(status_notification);

CREATE INDEX IF NOT EXISTS idx_reservations_user_id ON reservations(user_id);

CREATE INDEX IF NOT EXISTS idx_reservations_branch_time ON reservations(bank_branch_id, start_reservation);
