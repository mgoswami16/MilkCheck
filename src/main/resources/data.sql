-- Roles
INSERT IGNORE INTO roles (name) VALUES ('ROLE_CUSTOMER'), ('ROLE_ADMIN');

-- Categories
INSERT IGNORE INTO categories (name, description, image_url) VALUES
('Milk',           'Fresh farm milk in every variety',        '/images/categories/milk.jpg'),
('Butter & Ghee',  'Rich, creamy butter and pure ghee',       '/images/categories/butter.jpg'),;

-- Products
INSERT IGNORE INTO products (name, description, price, stock_quantity, image_url, unit, featured, active, category_id) VALUES
('Farm Fresh Full Cream Milk', 'Pasteurised full cream milk, rich in calcium and protein. Delivered fresh from local farms.', 3.20, 120, '/images/products/full-cream-milk.jpg', '1 L', TRUE, TRUE, (SELECT id FROM categories WHERE name='Milk')),
('Toned Milk',                 'Light and healthy toned milk for everyday use.', 2.80, 150, '/images/products/toned-milk.jpg', '1 L', FALSE, TRUE, (SELECT id FROM categories WHERE name='Milk')),
('Organic A2 Milk',            'Certified organic A2 milk from grass-fed desi cows.', 4.90, 60, '/images/products/a2-milk.jpg', '1 L', TRUE, TRUE, (SELECT id FROM categories WHERE name='Milk')),
('Almond Flavoured Milk',      'Chilled milk with real almond flavour. Perfect for kids.', 1.50, 80, '/images/products/almond-milk.jpg', '200 ml', FALSE, TRUE, (SELECT id FROM categories WHERE name='Milk')),
('Belgian Chocolate Ice Cream','Decadent dark chocolate ice cream, extra creamy.', 6.20, 30, '/images/products/chocolate-icecream.jpg', '500 ml', TRUE, TRUE, (SELECT id FROM categories WHERE name='Butter & Ghee'));