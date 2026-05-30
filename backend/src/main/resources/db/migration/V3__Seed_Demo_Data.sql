INSERT INTO users (username, password, role_user, email, created_at)
SELECT 'admin',
       '$2a$10$fWl3OC5EjqHz6t9h2kbNN.25VyzU1X7thPRJR3h5mKbla2R6UeVJy',
       'ADMIN',
       'admin@bank.local',
       CURRENT_TIMESTAMP
WHERE NOT EXISTS (
    SELECT 1 FROM users WHERE email = 'admin@bank.local'
);

INSERT INTO services (bank_service_name, duration, description)
SELECT service_name, duration, description
FROM (VALUES
          ('Personal Account Opening', '30 min', 'Open and configure a personal current account.'),
          ('Debit Card Services', '20 min', 'Issue, replace, activate or block a debit card.'),
          ('Domestic Transfer Support', '25 min', 'Help with account-to-account transfers and payment details.'),
          ('Mortgage Consultation', '60 min', 'Consultation for mortgage options, terms and required documents.'),
          ('Investment Consultation', '45 min', 'Introductory consultation for savings and investment products.')
     ) AS demo_services(service_name, duration, description)
WHERE NOT EXISTS (
    SELECT 1 FROM services WHERE bank_service_name = demo_services.service_name
);

INSERT INTO bank_branches (bank_branch_name, latitude, longitude, city, address, country, post_code)
SELECT branch_name, latitude, longitude, city, address, country, post_code
FROM (VALUES
          ('Prague Main Branch', 50.087465, 14.421254, 'Prague', 'Na Prikope 12', 'Czech Republic', '11000'),
          ('Brno Business Center', 49.195061, 16.606837, 'Brno', 'Masarykova 8', 'Czech Republic', '60200'),
          ('Ostrava Client Hub', 49.820923, 18.262524, 'Ostrava', '28. rijna 20', 'Czech Republic', '70200')
     ) AS demo_branches(branch_name, latitude, longitude, city, address, country, post_code)
WHERE NOT EXISTS (
    SELECT 1 FROM bank_branches WHERE bank_branch_name = demo_branches.branch_name
);

INSERT INTO branch_schedule (branch_id, day, open_time, close_time)
SELECT b.id, schedule.day, schedule.open_time::time, schedule.close_time::time
FROM bank_branches b
CROSS JOIN (VALUES
                ('MONDAY', '09:00', '18:00'),
                ('TUESDAY', '09:00', '18:00'),
                ('WEDNESDAY', '09:00', '18:00'),
                ('THURSDAY', '09:00', '18:00'),
                ('FRIDAY', '09:00', '17:00')
           ) AS schedule(day, open_time, close_time)
WHERE b.bank_branch_name IN ('Prague Main Branch', 'Brno Business Center', 'Ostrava Client Hub')
  AND NOT EXISTS (
      SELECT 1
      FROM branch_schedule existing_schedule
      WHERE existing_schedule.branch_id = b.id
        AND existing_schedule.day = schedule.day
  );

INSERT INTO bank_branch_bank_service (bank_branch_id, bank_service_id)
SELECT b.id, s.id
FROM bank_branches b
JOIN services s ON (
    b.bank_branch_name = 'Prague Main Branch'
    OR (b.bank_branch_name = 'Brno Business Center'
        AND s.bank_service_name IN ('Personal Account Opening', 'Domestic Transfer Support', 'Investment Consultation'))
    OR (b.bank_branch_name = 'Ostrava Client Hub'
        AND s.bank_service_name IN ('Debit Card Services', 'Domestic Transfer Support', 'Mortgage Consultation'))
)
WHERE s.bank_service_name IN (
    'Personal Account Opening',
    'Debit Card Services',
    'Domestic Transfer Support',
    'Mortgage Consultation',
    'Investment Consultation'
)
  AND NOT EXISTS (
      SELECT 1
      FROM bank_branch_bank_service existing_link
      WHERE existing_link.bank_branch_id = b.id
        AND existing_link.bank_service_id = s.id
  );
