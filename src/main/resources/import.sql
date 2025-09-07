INSERT INTO owner (name, email) VALUES ('Ana Pop', 'ana.pop@example.com');
INSERT INTO owner (name, email) VALUES ('Bogdan Ionescu', 'bogdan.ionescu@example.com');

INSERT INTO car (vin, make, model, year_of_manufacture, owner_id) VALUES ('VIN12345', 'Dacia', 'Logan', 2018, 1);
INSERT INTO car (vin, make, model, year_of_manufacture, owner_id) VALUES ('VIN67890', 'VW', 'Golf', 2021, 2);

INSERT INTO insurancepolicy (car_id, provider, start_date, end_date, expiration_notified) VALUES (1, 'Allianz', DATE '2024-01-01', DATE '2024-12-31', false);
INSERT INTO insurancepolicy (car_id, provider, start_date, end_date, expiration_notified) VALUES (1, 'Groupama', DATE '2025-01-01', DATE '2026-01-01', false);
INSERT INTO insurancepolicy (car_id, provider, start_date, end_date, expiration_notified) VALUES (2, 'Allianz', DATE '2025-03-01', DATE '2025-09-30', false);

INSERT INTO insuranceclaim (car_id, claim_date, description, amount, created_at) VALUES (1, DATE '2024-06-15', 'Minor fender bender in parking lot', 850.00, TIMESTAMP '2024-06-15 14:30:00');
INSERT INTO insuranceclaim (car_id, claim_date, description, amount, created_at) VALUES (2, DATE '2025-04-10', 'Windshield replacement due to stone chip', 320.00, TIMESTAMP '2025-04-10 09:15:00');