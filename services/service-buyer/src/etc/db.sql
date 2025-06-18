CREATE TABLE `shop-demo`.`Shop_User` (
  `id` BIGINT(9) NOT NULL AUTO_INCREMENT,
  `user_name` VARCHAR(60) NOT NULL DEFAULT 'DemoUser',
  `address` VARCHAR(100) NULL,
  `updated_at` TIMESTAMP,
  PRIMARY KEY (`id`));

CREATE TABLE `shop-demo`.`User_Account` (
  `id` BIGINT(9) NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT(9) NOT NULL,
  `balance` DECIMAL(10,2) NULL,
  `updated_at` TIMESTAMP,
  PRIMARY KEY (`id`),
  INDEX `userIdIndex` (`user_id` ASC) VISIBLE);


CREATE TABLE `shop-demo`.`User_Order` (
  `id` BIGINT(9) NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT(9) NOT NULL,
  `sku` BIGINT(9) NOT NULL,
  `qty` INT NOT NULL,
  `updated_at` TIMESTAMP,
  PRIMARY KEY (`id`),
  INDEX `userIdx` (`user_id` ASC) VISIBLE);

  CREATE TABLE `shop-demo`.`Product_Store` (
    `sku` BIGINT(9) NOT NULL AUTO_INCREMENT,
    `init_qty` INT NOT NULL,
    `qty_balance` INT NOT NULL,
    `price` DECIMAL(10,2) NULL,
    `updated_at` TIMESTAMP,
    PRIMARY KEY (`sku`));

  CREATE TABLE `shop-demo`.`Seller_Account` (
    `id` BIGINT(9) NOT NULL AUTO_INCREMENT,
    `balance` DECIMAL(10,2) NULL,
    `updated_at` TIMESTAMP,
   PRIMARY KEY (`id`));

   insert into `shop-demo`.Seller_Account values (1001, 0, current_timestamp());

