-- 索引优化迁移（对已有数据库执行）
-- 运行: mysql -u root -p123456 campus_market < migration_index.sql

USE campus_market;

-- 先加新索引
ALTER TABLE product ADD INDEX idx_status_created (status, created_at);
ALTER TABLE product ADD INDEX idx_status_cat_created (status, category_id, created_at);
ALTER TABLE product ADD INDEX idx_seller_created (seller_id, created_at);

-- 再删旧索引（IGNORE 防止索引不存在时报错）
ALTER TABLE product DROP INDEX idx_category;
ALTER TABLE product DROP INDEX idx_seller;
ALTER TABLE product DROP INDEX idx_status;

-- 商品图片表
ALTER TABLE product_image ADD INDEX idx_product_sort (product_id, sort_order);
ALTER TABLE product_image DROP INDEX idx_product;
