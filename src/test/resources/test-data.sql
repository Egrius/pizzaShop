-- Очистка
DELETE FROM pizza_sizes;
DELETE FROM pizza_ingredients;
DELETE FROM pizzas;
DELETE FROM size_templates;
DELETE FROM ingredients;

-- Сброс sequence
ALTER SEQUENCE ingredients_id_seq RESTART WITH 1;
ALTER SEQUENCE size_templates_id_seq RESTART WITH 1;
ALTER SEQUENCE pizzas_id_seq RESTART WITH 1;
ALTER SEQUENCE pizza_ingredients_id_seq RESTART WITH 1;
ALTER SEQUENCE pizza_sizes_id_seq RESTART WITH 1;

ALTER TABLE pizzas ADD COLUMN IF NOT EXISTS version BIGINT DEFAULT 0;
