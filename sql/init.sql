/*
 ============================================================
  LifeShop 生活购物商城 - 数据库初始化脚本
 ============================================================
  说明：
    1. 本脚本仅包含【表结构】与【最小化演示数据】，不含任何真实用户数据。
    2. 所有演示账号密码均为占位值，请在本地环境自行修改。
    3. MySQL 8.0+ / utf8mb4
  使用：
    mysql -u root -p < init.sql
 ============================================================
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- 创建数据库
-- ----------------------------
CREATE DATABASE IF NOT EXISTS `db_life_shop`
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_0900_ai_ci;

USE `db_life_shop`;

-- ============================================================
-- 一、表结构
-- ============================================================

-- ----------------------------
-- sys_admin  ——  平台管理员表
-- ----------------------------
DROP TABLE IF EXISTS `sys_admin`;
CREATE TABLE `sys_admin`  (
  `admin_id` bigint NOT NULL AUTO_INCREMENT,
  `admin_account` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `admin_password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `real_name` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '',
  `status` tinyint NULL DEFAULT 1,
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint NULL DEFAULT 0,
  PRIMARY KEY (`admin_id`) USING BTREE,
  UNIQUE INDEX `admin_account`(`admin_account` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- biz_user  ——  用户表
-- ----------------------------
DROP TABLE IF EXISTS `biz_user`;
CREATE TABLE `biz_user`  (
  `user_id` bigint NOT NULL AUTO_INCREMENT,
  `user_account` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `user_password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `user_avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '',
  `real_name` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '',
  `gender` tinyint NULL DEFAULT 0,
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `email` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '',
  `address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '',
  `is_merchant` tinyint NULL DEFAULT 0,
  `merchant_id` bigint NULL DEFAULT NULL,
  `status` tinyint NULL DEFAULT 1,
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint NULL DEFAULT 0,
  PRIMARY KEY (`user_id`) USING BTREE,
  UNIQUE INDEX `user_account`(`user_account` ASC) USING BTREE,
  INDEX `fk_biz_user_merchant_id`(`merchant_id` ASC) USING BTREE,
  CONSTRAINT `fk_biz_user_merchant_id` FOREIGN KEY (`merchant_id`) REFERENCES `biz_merchant` (`merchant_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- biz_merchant  ——  商家表（入驻申请与审核状态）
-- ----------------------------
DROP TABLE IF EXISTS `biz_merchant`;
CREATE TABLE `biz_merchant`  (
  `merchant_id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `merchant_account` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `merchant_password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `shop_id` bigint NULL DEFAULT NULL,
  `audit_status` tinyint NULL DEFAULT 0,
  `audit_remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '',
  `merchant_status` tinyint NULL DEFAULT 1,
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint NULL DEFAULT 0,
  PRIMARY KEY (`merchant_id`) USING BTREE,
  UNIQUE INDEX `user_id`(`user_id` ASC) USING BTREE,
  UNIQUE INDEX `merchant_account`(`merchant_account` ASC) USING BTREE,
  UNIQUE INDEX `shop_id`(`shop_id` ASC) USING BTREE,
  CONSTRAINT `fk_biz_merchant_shop_id` FOREIGN KEY (`shop_id`) REFERENCES `biz_shop` (`shop_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_biz_merchant_user_id` FOREIGN KEY (`user_id`) REFERENCES `biz_user` (`user_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- biz_shop  ——  店铺表
-- ----------------------------
DROP TABLE IF EXISTS `biz_shop`;
CREATE TABLE `biz_shop`  (
  `shop_id` bigint NOT NULL AUTO_INCREMENT,
  `merchant_id` bigint NOT NULL,
  `shop_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `shop_logo` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '',
  `shop_desc` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '',
  `business_hours` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '',
  `shop_status` tinyint NULL DEFAULT 1,
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint NULL DEFAULT 0,
  PRIMARY KEY (`shop_id`) USING BTREE,
  UNIQUE INDEX `merchant_id`(`merchant_id` ASC) USING BTREE,
  CONSTRAINT `fk_biz_shop_merchant_id` FOREIGN KEY (`merchant_id`) REFERENCES `biz_merchant` (`merchant_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- biz_category  ——  商品分类表
-- ----------------------------
DROP TABLE IF EXISTS `biz_category`;
CREATE TABLE `biz_category`  (
  `category_id` bigint NOT NULL AUTO_INCREMENT,
  `category_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `category_desc` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '',
  `category_icon` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '',
  `sort` int NULL DEFAULT 0,
  `status` tinyint NULL DEFAULT 1,
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint NULL DEFAULT 0,
  PRIMARY KEY (`category_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- biz_goods  ——  商品表
-- ----------------------------
DROP TABLE IF EXISTS `biz_goods`;
CREATE TABLE `biz_goods`  (
  `goods_id` bigint NOT NULL AUTO_INCREMENT,
  `goods_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `category_id` bigint NOT NULL,
  `merchant_id` bigint NOT NULL,
  `shop_id` bigint NOT NULL,
  `goods_image` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '',
  `goods_desc` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `market_price` decimal(10, 2) NOT NULL,
  `sale_price` decimal(10, 2) NOT NULL,
  `stock` int NULL DEFAULT 0,
  `sales_count` int NOT NULL DEFAULT 0 COMMENT '销量',
  `status` tinyint NULL DEFAULT 0,
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint NULL DEFAULT 0,
  PRIMARY KEY (`goods_id`) USING BTREE,
  INDEX `fk_biz_goods_category_id`(`category_id` ASC) USING BTREE,
  INDEX `fk_biz_goods_merchant_id`(`merchant_id` ASC) USING BTREE,
  INDEX `fk_biz_goods_shop_id`(`shop_id` ASC) USING BTREE,
  CONSTRAINT `fk_biz_goods_category_id` FOREIGN KEY (`category_id`) REFERENCES `biz_category` (`category_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_biz_goods_merchant_id` FOREIGN KEY (`merchant_id`) REFERENCES `biz_merchant` (`merchant_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_biz_goods_shop_id` FOREIGN KEY (`shop_id`) REFERENCES `biz_shop` (`shop_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- biz_cart  ——  购物车表
-- ----------------------------
DROP TABLE IF EXISTS `biz_cart`;
CREATE TABLE `biz_cart`  (
  `cart_id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `goods_id` bigint NOT NULL,
  `goods_num` int NOT NULL DEFAULT 1,
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint NULL DEFAULT 0,
  PRIMARY KEY (`cart_id`) USING BTREE,
  INDEX `fk_biz_cart_user_id`(`user_id` ASC) USING BTREE,
  INDEX `fk_biz_cart_goods_id`(`goods_id` ASC) USING BTREE,
  CONSTRAINT `fk_biz_cart_goods_id` FOREIGN KEY (`goods_id`) REFERENCES `biz_goods` (`goods_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_biz_cart_user_id` FOREIGN KEY (`user_id`) REFERENCES `biz_user` (`user_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- biz_order  ——  订单主表
-- ----------------------------
DROP TABLE IF EXISTS `biz_order`;
CREATE TABLE `biz_order`  (
  `order_id` bigint NOT NULL AUTO_INCREMENT,
  `order_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `user_id` bigint NOT NULL,
  `merchant_id` bigint NOT NULL,
  `shop_id` bigint NOT NULL,
  `total_amount` decimal(10, 2) NOT NULL,
  `pay_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '',
  `receiver_name` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `receiver_phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `receiver_address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `order_status` tinyint NOT NULL DEFAULT 0,
  `logistics_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '',
  `pay_time` datetime NULL DEFAULT NULL,
  `deliver_time` datetime NULL DEFAULT NULL,
  `receive_time` datetime NULL DEFAULT NULL,
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint NULL DEFAULT 0,
  PRIMARY KEY (`order_id`) USING BTREE,
  UNIQUE INDEX `order_no`(`order_no` ASC) USING BTREE,
  INDEX `fk_biz_order_user_id`(`user_id` ASC) USING BTREE,
  INDEX `fk_biz_order_merchant_id`(`merchant_id` ASC) USING BTREE,
  INDEX `fk_biz_order_shop_id`(`shop_id` ASC) USING BTREE,
  CONSTRAINT `fk_biz_order_merchant_id` FOREIGN KEY (`merchant_id`) REFERENCES `biz_merchant` (`merchant_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_biz_order_shop_id` FOREIGN KEY (`shop_id`) REFERENCES `biz_shop` (`shop_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_biz_order_user_id` FOREIGN KEY (`user_id`) REFERENCES `biz_user` (`user_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- biz_order_item  ——  订单明细表
-- ----------------------------
DROP TABLE IF EXISTS `biz_order_item`;
CREATE TABLE `biz_order_item`  (
  `item_id` bigint NOT NULL AUTO_INCREMENT,
  `order_id` bigint NOT NULL,
  `goods_id` bigint NOT NULL,
  `goods_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `goods_image` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '',
  `goods_price` decimal(10, 2) NOT NULL,
  `goods_num` int NOT NULL DEFAULT 1,
  `total_price` decimal(10, 2) NOT NULL,
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint NULL DEFAULT 0,
  PRIMARY KEY (`item_id`) USING BTREE,
  INDEX `fk_biz_order_item_order_id`(`order_id` ASC) USING BTREE,
  INDEX `fk_biz_order_item_goods_id`(`goods_id` ASC) USING BTREE,
  CONSTRAINT `fk_biz_order_item_goods_id` FOREIGN KEY (`goods_id`) REFERENCES `biz_goods` (`goods_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_biz_order_item_order_id` FOREIGN KEY (`order_id`) REFERENCES `biz_order` (`order_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- biz_after_sale  ——  售后/退款申请表
-- ----------------------------
DROP TABLE IF EXISTS `biz_after_sale`;
CREATE TABLE `biz_after_sale`  (
  `after_sale_id` bigint NOT NULL AUTO_INCREMENT,
  `order_id` bigint NOT NULL,
  `item_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  `merchant_id` bigint NOT NULL,
  `refund_amount` decimal(10, 2) NOT NULL,
  `refund_reason` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `refund_voucher` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '',
  `audit_status` tinyint NULL DEFAULT 0,
  `audit_remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '',
  `admin_remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '',
  `escalated` tinyint NULL DEFAULT 0,
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint NULL DEFAULT 0,
  PRIMARY KEY (`after_sale_id`) USING BTREE,
  INDEX `fk_biz_after_sale_order_id`(`order_id` ASC) USING BTREE,
  INDEX `fk_biz_after_sale_item_id`(`item_id` ASC) USING BTREE,
  INDEX `fk_biz_after_sale_user_id`(`user_id` ASC) USING BTREE,
  INDEX `fk_biz_after_sale_merchant_id`(`merchant_id` ASC) USING BTREE,
  CONSTRAINT `fk_biz_after_sale_item_id` FOREIGN KEY (`item_id`) REFERENCES `biz_order_item` (`item_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_biz_after_sale_merchant_id` FOREIGN KEY (`merchant_id`) REFERENCES `biz_merchant` (`merchant_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_biz_after_sale_order_id` FOREIGN KEY (`order_id`) REFERENCES `biz_order` (`order_id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_biz_after_sale_user_id` FOREIGN KEY (`user_id`) REFERENCES `biz_user` (`user_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- biz_user_address  ——  用户收货地址表
-- ----------------------------
DROP TABLE IF EXISTS `biz_user_address`;
CREATE TABLE `biz_user_address`  (
  `address_id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `receiver_name` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `receiver_phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `receiver_address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `is_default` tinyint NULL DEFAULT 0,
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint NULL DEFAULT 0,
  PRIMARY KEY (`address_id`) USING BTREE,
  INDEX `fk_biz_user_address_user_id`(`user_id` ASC) USING BTREE,
  CONSTRAINT `fk_biz_user_address_user_id` FOREIGN KEY (`user_id`) REFERENCES `biz_user` (`user_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- biz_leave_message  ——  商品留言表
-- ----------------------------
DROP TABLE IF EXISTS `biz_leave_message`;
CREATE TABLE `biz_leave_message`  (
  `message_id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `user_account` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `user_avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '',
  `message_title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `message_content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `reply_content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint NULL DEFAULT 0,
  PRIMARY KEY (`message_id`) USING BTREE,
  INDEX `fk_biz_leave_message_user_id`(`user_id` ASC) USING BTREE,
  CONSTRAINT `fk_biz_leave_message_user_id` FOREIGN KEY (`user_id`) REFERENCES `biz_user` (`user_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- biz_message_comment  ——  留言回复表
-- ----------------------------
DROP TABLE IF EXISTS `biz_message_comment`;
CREATE TABLE `biz_message_comment`  (
  `comment_id` bigint NOT NULL AUTO_INCREMENT,
  `message_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  `user_account` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `user_avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '',
  `comment_content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint NULL DEFAULT 0,
  `is_top` int NULL DEFAULT 0 COMMENT '是否置顶 0否1是',
  PRIMARY KEY (`comment_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ============================================================
-- 二、最小化演示数据（全部为占位值，不含真实信息）
-- ============================================================

-- ----------------------------
-- 平台管理员
-- 登录账号：admin   密码：123456
-- ----------------------------
INSERT INTO `sys_admin` (`admin_id`, `admin_account`, `admin_password`, `real_name`, `status`) VALUES
  (1, 'admin', '123456', '平台管理员', 1);

-- ----------------------------
-- 演示用户
-- 登录账号：user01 / user02   密码：123456
-- 注：商家账号请通过前台「商家入驻」流程提交申请，再由管理员后台审核通过后生成，
--     以完整体验 用户 -> 入驻申请 -> 平台审核 -> 商家 的业务闭环。
-- ----------------------------
INSERT INTO `biz_user`
  (`user_id`, `user_account`, `user_password`, `user_avatar`, `real_name`, `gender`, `phone`, `email`, `address`, `is_merchant`, `merchant_id`, `status`)
VALUES
  (1, 'user01', '123456', '', '演示用户一', 1, '13800000001', 'user01@example.com', '示例省示例市示例区示例路 1 号', 0, NULL, 1),
  (2, 'user02', '123456', '', '演示用户二', 2, '13800000002', 'user02@example.com', '示例省示例市示例区示例路 2 号', 0, NULL, 1);

-- ----------------------------
-- 商品分类（基础字典数据）
-- ----------------------------
INSERT INTO `biz_category` VALUES (1, '日用清洁', '家庭清洁与卫生用品', '', 2, 1, '2026-04-27 15:23:05', '2026-05-03 16:30:07', 0);
INSERT INTO `biz_category` VALUES (2, '厨房用品', '厨房烹饪与收纳用品', '', 1, 1, '2026-04-27 15:23:05', '2026-05-03 16:30:11', 0);
INSERT INTO `biz_category` VALUES (3, '家居收纳', '家居整理与储物用品', '', 3, 1, '2026-04-27 15:23:05', '2026-04-27 15:23:05', 0);
INSERT INTO `biz_category` VALUES (4, '个护健康', '个护及健康用品', '', 4, 1, '2026-04-27 15:23:05', '2026-04-27 15:23:05', 0);
INSERT INTO `biz_category` VALUES (5, '办公文具', '办公与学习文具用品', '', 5, 1, '2026-04-27 15:23:05', '2026-04-27 15:23:05', 0);

SET FOREIGN_KEY_CHECKS = 1;
