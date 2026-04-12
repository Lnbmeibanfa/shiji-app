INSERT INTO food_nutrition
(food_item_id, nutrient_basis, calories, protein, fat, carbohydrate, fiber, sugar, sodium, data_source, version_no, created_at, updated_at)
VALUES

((SELECT id FROM food_item WHERE food_code = 'FOOD_RICE_WHITE'), 'per_100g', 116.00, 2.60, 0.30, 25.90, 0.30, 0.10, 2.00, 'seed_data', 1, NOW(6), NOW(6)),
((SELECT id FROM food_item WHERE food_code = 'FOOD_NOODLE_PLAIN'), 'per_100g', 137.00, 4.50, 1.80, 25.00, 1.20, 0.50, 5.00, 'seed_data', 1, NOW(6), NOW(6)),
((SELECT id FROM food_item WHERE food_code = 'FOOD_BREAD_WHITE'), 'per_100g', 265.00, 8.00, 3.20, 49.00, 2.70, 5.00, 490.00, 'seed_data', 1, NOW(6), NOW(6)),
((SELECT id FROM food_item WHERE food_code = 'FOOD_CONGEE'), 'per_100g', 46.00, 1.10, 0.20, 9.90, 0.10, 0.10, 2.00, 'seed_data', 1, NOW(6), NOW(6)),

((SELECT id FROM food_item WHERE food_code = 'FOOD_EGG_BOILED'), 'per_piece', 78.00, 6.30, 5.30, 0.60, 0.00, 0.20, 62.00, 'seed_data', 1, NOW(6), NOW(6)),
((SELECT id FROM food_item WHERE food_code = 'FOOD_EGG_FRIED'), 'per_piece', 95.00, 6.50, 7.00, 0.80, 0.00, 0.20, 90.00, 'seed_data', 1, NOW(6), NOW(6)),
((SELECT id FROM food_item WHERE food_code = 'FOOD_CHICKEN_BREAST'), 'per_100g', 165.00, 31.00, 3.60, 0.00, 0.00, 0.00, 74.00, 'seed_data', 1, NOW(6), NOW(6)),
((SELECT id FROM food_item WHERE food_code = 'FOOD_BEEF_STIR'), 'per_100g', 220.00, 26.00, 12.00, 2.00, 0.00, 0.50, 75.00, 'seed_data', 1, NOW(6), NOW(6)),
((SELECT id FROM food_item WHERE food_code = 'FOOD_PORK_BRAISED'), 'per_100g', 395.00, 14.00, 37.00, 2.50, 0.00, 1.50, 320.00, 'seed_data', 1, NOW(6), NOW(6)),
((SELECT id FROM food_item WHERE food_code = 'FOOD_TOFU'), 'per_100g', 76.00, 8.00, 4.80, 1.90, 0.30, 0.60, 7.00, 'seed_data', 1, NOW(6), NOW(6)),

((SELECT id FROM food_item WHERE food_code = 'FOOD_TOMATO_EGG'), 'per_100g', 110.00, 5.00, 7.00, 6.00, 0.80, 2.50, 140.00, 'seed_data', 1, NOW(6), NOW(6)),
((SELECT id FROM food_item WHERE food_code = 'FOOD_FRIED_VEG'), 'per_100g', 60.00, 2.00, 3.00, 6.00, 2.00, 1.50, 120.00, 'seed_data', 1, NOW(6), NOW(6)),
((SELECT id FROM food_item WHERE food_code = 'FOOD_BROCCOLI'), 'per_100g', 35.00, 2.80, 0.40, 6.60, 2.60, 1.70, 33.00, 'seed_data', 1, NOW(6), NOW(6)),
((SELECT id FROM food_item WHERE food_code = 'FOOD_CUCUMBER'), 'per_100g', 16.00, 0.80, 0.20, 3.60, 0.50, 1.70, 2.00, 'seed_data', 1, NOW(6), NOW(6)),

((SELECT id FROM food_item WHERE food_code = 'FOOD_MILK'), 'per_100ml', 54.00, 3.30, 3.20, 4.80, 0.00, 4.80, 50.00, 'seed_data', 1, NOW(6), NOW(6)),
((SELECT id FROM food_item WHERE food_code = 'FOOD_SOYMILK'), 'per_100ml', 31.00, 2.90, 1.60, 1.70, 0.30, 1.20, 35.00, 'seed_data', 1, NOW(6), NOW(6)),
((SELECT id FROM food_item WHERE food_code = 'FOOD_COLA'), 'per_100ml', 43.00, 0.00, 0.00, 10.60, 0.00, 10.60, 4.00, 'seed_data', 1, NOW(6), NOW(6)),
((SELECT id FROM food_item WHERE food_code = 'FOOD_MILK_TEA'), 'per_100ml', 70.00, 1.20, 2.00, 12.00, 0.00, 10.00, 25.00, 'seed_data', 1, NOW(6), NOW(6)),

((SELECT id FROM food_item WHERE food_code = 'FOOD_APPLE'), 'per_100g', 52.00, 0.30, 0.20, 14.00, 2.40, 10.00, 1.00, 'seed_data', 1, NOW(6), NOW(6)),
((SELECT id FROM food_item WHERE food_code = 'FOOD_BANANA'), 'per_100g', 89.00, 1.10, 0.30, 22.80, 2.60, 12.20, 1.00, 'seed_data', 1, NOW(6), NOW(6)),

((SELECT id FROM food_item WHERE food_code = 'FOOD_FRIED_CHICKEN'), 'per_100g', 290.00, 18.00, 20.00, 12.00, 1.00, 0.50, 500.00, 'seed_data', 1, NOW(6), NOW(6)),
((SELECT id FROM food_item WHERE food_code = 'FOOD_HAMBURGER'), 'per_piece', 320.00, 14.00, 14.00, 33.00, 2.00, 6.00, 620.00, 'seed_data', 1, NOW(6), NOW(6)),
((SELECT id FROM food_item WHERE food_code = 'FOOD_FRENCH_FRIES'), 'per_100g', 312.00, 3.40, 15.00, 41.00, 3.80, 0.40, 210.00, 'seed_data', 1, NOW(6), NOW(6)),
((SELECT id FROM food_item WHERE food_code = 'FOOD_CAKE'), 'per_100g', 350.00, 4.50, 15.00, 50.00, 1.00, 30.00, 260.00, 'seed_data', 1, NOW(6), NOW(6));