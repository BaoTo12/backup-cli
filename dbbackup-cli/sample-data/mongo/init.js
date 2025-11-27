use('testdb');

db.customers.insertMany([
  { "name": "John Doe", "email": "john.doe@example.com", "totalSpent": 1225.50, "tags": ["premium", "tech"] },
  { "name": "Jane Smith", "email": "jane.smith@example.com", "totalSpent": 75.75, "tags": ["new", "tech"] },
  { "name": "Alice Johnson", "email": "alice.j@example.com", "totalSpent": 650.25, "tags": ["frequent", "home"] },
  { "name": "Bob Brown", "email": "b.brown@example.com", "totalSpent": 15.50, "tags": ["new"] },
  { "name": "Charlie Davis", "email": "charlie.d@example.com", "totalSpent": 800.00, "tags": ["premium", "gaming"] },
  { "name": "Diana Miller", "email": "diana.m@example.com", "totalSpent": 30.00, "tags": ["new"] },
  { "name": "Ethan Wilson", "email": "ethan.w@example.com", "totalSpent": 240.00, "tags": ["tech"] },
  { "name": "Fiona Garcia", "email": "f.garcia@example.com", "totalSpent": 90.50, "tags": ["home"] },
  { "name": "George Martinez", "email": "george.m@example.com", "totalSpent": 330.00, "tags": ["frequent"] },
  { "name": "Hannah Rodriguez", "email": "hannah.r@example.com", "totalSpent": 1950.00, "tags": ["premium", "gaming", "tech"] },
  { "name": "Ian Lopez", "email": "ian.l@example.com", "totalSpent": 550.00, "tags": ["tech"] },
  { "name": "Julia Hernandez", "email": "julia.h@example.com", "totalSpent": 280.00, "tags": ["home"] },
  { "name": "Kevin Gonzalez", "email": "kevin.g@example.com", "totalSpent": 180.00, "tags": ["frequent"] },
  { "name": "Laura Perez", "email": "laura.p@example.com", "totalSpent": 300.00, "tags": ["tech"] },
  { "name": "Mason Sanchez", "email": "mason.s@example.com", "totalSpent": 125.00, "tags": ["home"] },
  { "name": "Nora Ramirez", "email": "nora.r@example.com", "totalSpent": 10.00, "tags": ["new"] },
  { "name": "Oscar Torres", "email": "oscar.t@example.com", "totalSpent": 25.00, "tags": ["new"] },
  { "name": "Penelope Flores", "email": "penelope.f@example.com", "totalSpent": 180.00, "tags": ["frequent"] }
]);
