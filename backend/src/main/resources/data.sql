INSERT INTO category (id, label, sort_order)
VALUES
    ('all', '全部餐品', 1),
    ('signature', '招牌推荐', 2),
    ('staple', '主食饭面', 3),
    ('snack', '轻食小吃', 4),
    ('drink', '清爽饮品', 5)
ON DUPLICATE KEY UPDATE
    label = VALUES(label),
    sort_order = VALUES(sort_order);

INSERT INTO dish (id, name, category_id, price, rating, calories, description, highlight, image_url, available, stock, deleted)
VALUES
    (1, '南瓜鸡肉能量碗', 'signature', 32.00, 4.9, 486, '低油烤鸡胸搭配南瓜泥与时蔬，适合作为饱腹正餐。', '暖胃又有满足感', NULL, 1, -1, 0),
    (2, '香草牛肉意面', 'staple', 38.00, 4.8, 532, '番茄香草底酱搭配慢炖牛肉，风味浓郁。', '牛肉香气突出', NULL, 1, -1, 0),
    (3, '柑橘鲜虾沙拉', 'snack', 26.00, 4.7, 268, '生菜、牛油果与鲜虾组合，口感清爽。', '低负担高蛋白', NULL, 1, -1, 0),
    (4, '菌菇烤饭拼盘', 'signature', 29.00, 4.8, 418, '菌菇搭配烤饭与轻酱汁，适合午间套餐。', '工作日午餐首选', NULL, 1, -1, 0),
    (5, '海盐拿铁', 'drink', 18.00, 4.6, 190, '奶香与轻微海盐回甘结合，适合配餐。', '顺滑不腻', NULL, 1, -1, 0),
    (6, '白桃气泡茶', 'drink', 16.00, 4.8, 122, '白桃风味结合轻气泡，适合夏日饮用。', '清爽低糖', NULL, 1, -1, 0),
    (7, '芝士玉米饼', 'snack', 14.00, 4.7, 214, '酥脆外皮包裹芝士玉米馅，适合加餐分享。', '外酥里软', NULL, 1, -1, 0),
    (8, '温泉蛋肥牛饭', 'staple', 35.00, 4.9, 544, '肥牛与温泉蛋拌饭组合，口感浓郁。', '复购率很高', NULL, 1, -1, 0),
    (9, '黑椒鸡腿焗饭', 'staple', 36.00, 4.7, 498, '黑椒风味鸡腿与焗饭组合，适合晚餐。', '香浓有层次', NULL, 1, -1, 0),
    (10, '芒果酸奶碗', 'snack', 22.00, 4.8, 236, '芒果、酸奶和坚果混合，适合下午茶。', '颜值与口感兼具', NULL, 1, -1, 0),
    (11, '青柠气泡水', 'drink', 12.00, 4.5, 88, '青柠清香结合细腻气泡，适合解腻。', '轻盈解腻', NULL, 1, -1, 0),
    (12, '焦糖布丁', 'snack', 15.00, 4.9, 184, '顺滑布丁搭配焦糖层，适合餐后甜品。', '细腻香甜', NULL, 1, -1, 0)
ON DUPLICATE KEY UPDATE
    name = VALUES(name),
    category_id = VALUES(category_id),
    price = VALUES(price),
    rating = VALUES(rating),
    calories = VALUES(calories),
    description = VALUES(description),
    highlight = VALUES(highlight),
    image_url = VALUES(image_url),
    available = VALUES(available),
    stock = VALUES(stock),
    deleted = VALUES(deleted);
