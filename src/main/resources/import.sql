insert into Customer(id, name, externalCode, country, createdOn, notes, retiredOn, active, city) values (1, 'Mario Rossi', 'IT-001', 'IT', '2026-01-10', null, null, true, 'Milano');
insert into Customer(id, name, externalCode, country, createdOn, notes, retiredOn, active, city) values (2, 'Mario Bianchi', 'FR-002', 'FR', '2026-02-12', 'preferred', '2026-06-01', true, 'Parigi');
insert into Customer(id, name, externalCode, country, createdOn, notes, retiredOn, active, city) values (3, 'Luigi Verdi', 'DE-003', 'DE', '2025-12-01', null, null, false, 'Roma');
alter sequence Customer_SEQ restart with 100;
