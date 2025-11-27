CREATE TABLE products (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    price NUMERIC(10, 2) NOT NULL,
    quantity INT NOT NULL
);

INSERT INTO products (name, price, quantity) VALUES ('Laptop', 1200.50, 50);
INSERT INTO products (name, price, quantity) VALUES ('Mouse', 25.00, 200);
INSERT INTO products (name, price, quantity) VALUES ('Keyboard', 75.75, 150);
INSERT INTO products (name, price, quantity) VALUES ('Monitor', 300.00, 100);
INSERT INTO products (name, price, quantity) VALUES ('Webcam', 50.25, 300);
INSERT INTO products (name, price, quantity) VALUES ('Desk Chair', 150.00, 75);
INSERT INTO products (name, price, quantity) VALUES ('HDMI Cable', 15.50, 500);
INSERT INTO products (name, price, quantity) VALUES ('Docking Station', 250.00, 90);
INSERT INTO products (name, price, quantity) VALUES ('USB Hub', 30.00, 250);
INSERT INTO products (name, price, quantity) VALUES ('External Hard Drive', 120.00, 120);
INSERT INTO products (name, price, quantity) VALUES ('Laptop Stand', 45.50, 180);
INSERT INTO products (name, price, quantity) VALUES ('Wireless Charger', 40.00, 220);
INSERT INTO products (name, price, quantity) VALUES ('Bluetooth Speaker', 80.80, 130);
INSERT INTO products (name, price, quantity) VALUES ('Noise Cancelling Headphones', 220.00, 100);
INSERT INTO products (name, price, quantity) VALUES ('Microphone', 110.00, 80);
INSERT INTO products (name, price, quantity) VALUES ('Gaming Mouse', 65.00, 110);
INSERT INTO products (name, price, quantity) VALUES ('Mechanical Keyboard', 130.00, 95);
INSERT INTO products (name, price, quantity) VALUES ('4K Monitor', 700.00, 60);
INSERT INTO products (name, price, quantity) VALUES ('VR Headset', 450.00, 40);
INSERT INTO products (name, price, quantity) VALUES ('Graphics Card', 950.00, 30);
INSERT INTO products (name, price, quantity) VALUES ('CPU', 550.00, 45);
INSERT INTO products (name, price, quantity) VALUES ('Motherboard', 280.00, 55);
INSERT INTO products (name, price, quantity) VALUES ('RAM 16GB', 90.00, 150);
INSERT INTO products (name, price, quantity) VALUES ('SSD 1TB', 150.00, 140);
INSERT INTO products (name, price, quantity) VALUES ('Power Supply', 110.00, 100);
INSERT INTO products (name, price, quantity) VALUES ('PC Case', 85.00, 90);
INSERT INTO products (name, price, quantity) VALUES ('Router', 125.00, 70);
INSERT INTO products (name, price, quantity) VALUES ('Ethernet Cable', 10.00, 400);
INSERT INTO products (name, price, quantity) VALUES ('Surge Protector', 25.00, 300);
INSERT INTO products (name, price, quantity) VALUES ('Uninterruptible Power Supply (UPS)', 180.00, 65);
