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
('Cheddar Cheese Block',       'Aged cheddar with a sharp, nutty flavour.', 6.50, 45, '/images/products/cheddar.jpg', '250 g', TRUE, TRUE, (SELECT id FROM categories WHERE name='Cheese')),
('Mozzarella Cheese',          'Stretchy, melt-in-the-mouth mozzarella, ideal for pizzas.', 5.40, 70, '/images/products/mozzarella.jpg', '200 g', FALSE, TRUE, (SELECT id FROM categories WHERE name='Cheese')),
('Salted Butter',              'Classic creamy salted butter made from fresh cream.', 3.90, 90, '/images/products/butter.jpg', '200 g', TRUE, TRUE, (SELECT id FROM categories WHERE name='Butter & Ghee')),
('Pure Cow Ghee',              'Traditionally churned golden ghee with rich aroma.', 9.80, 40, '/images/products/ghee.jpg', '500 ml', TRUE, TRUE, (SELECT id FROM categories WHERE name='Butter & Ghee')),
('Greek Yogurt',               'Thick, high-protein Greek yogurt with no added sugar.', 2.60, 100, '/images/products/greek-yogurt.jpg', '400 g', TRUE, TRUE, (SELECT id FROM categories WHERE name='Yogurt & Curd')),
('Fresh Set Curd',             'Homestyle set curd with live cultures.', 1.90, 110, '/images/products/curd.jpg', '500 g', FALSE, TRUE, (SELECT id FROM categories WHERE name='Yogurt & Curd')),
('Soft Paneer',                'Fresh, soft paneer made daily from full-fat milk.', 4.20, 55, '/images/products/paneer.jpg', '250 g', TRUE, TRUE, (SELECT id FROM categories WHERE name='Paneer & Cream')),
('Fresh Cream',                'Light, whippable cream for desserts and cooking.', 2.30, 0, '/images/products/cream.jpg', '200 ml', FALSE, TRUE, (SELECT id FROM categories WHERE name='Paneer & Cream')),
('Vanilla Bean Ice Cream',     'Premium vanilla ice cream made with real vanilla beans.', 5.90, 35, '/images/products/vanilla-icecream.jpg', '500 ml', FALSE, TRUE, (SELECT id FROM categories WHERE name='Ice Cream')),
('Belgian Chocolate Ice Cream','Decadent dark chocolate ice cream, extra creamy.', 6.20, 30, '/images/products/chocolate-icecream.jpg', '500 ml', TRUE, TRUE, (SELECT id FROM categories WHERE name='Ice Cream'));