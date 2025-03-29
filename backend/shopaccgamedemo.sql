

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";

CREATE TABLE IF NOT EXISTS `card_deposit_order` (
  `id` bigint(20) NOT NULL,
  `card_deposit_status` enum('PENDING','REJECTED','SUCCESS') DEFAULT NULL,
  `network_provider` enum('MOBIFONE','VIETTEL','VINAPHONE') DEFAULT NULL,
  `value` bigint(20) NOT NULL,
  `user_id` bigint(20) DEFAULT NULL,
  `code` varchar(255) DEFAULT NULL,
  `serial` varchar(255) DEFAULT NULL,
  `time_of_depositing` datetime(6) DEFAULT NULL,
  `actually_receive` bigint(20) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;



CREATE TABLE IF NOT EXISTS `deposit_payment_info` (
  `id` bigint(20) NOT NULL,
  `mobifone_tradecost` int(11) NOT NULL,
  `qr_code_momo` varchar(255) NOT NULL,
  `viettel_tradecost` int(11) NOT NULL,
  `vinaphone_tradecost` int(11) NOT NULL,
  `qr_code_viettel_pay` varchar(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO `deposit_payment_info` (`id`, `mobifone_tradecost`, `qr_code_momo`, `viettel_tradecost`, `vinaphone_tradecost`, `qr_code_viettel_pay`) VALUES
(1, 30, '/assets/images/qrcode-momo.png', 40, 20, '/assets/images/qrcode-viettelpay.png');



CREATE TABLE IF NOT EXISTS `fifa_account` (
  `id` bigint(20) NOT NULL,
  `game_account_status` enum('SELLING','SOLD') DEFAULT NULL,
  `description` varchar(255) DEFAULT NULL,
  `discount` int(11) NOT NULL,
  `email` varchar(255) DEFAULT NULL,
  `password` varchar(255) DEFAULT NULL,
  `phonenumber` varchar(255) DEFAULT NULL,
  `price` bigint(20) NOT NULL,
  `username` varchar(255) DEFAULT NULL,
  `bp` bigint(20) NOT NULL,
  `fc` int(11) NOT NULL,
  `valueteam` bigint(20) NOT NULL,
  `images` text DEFAULT NULL,
  `game_account_type` enum('FIFA','LOL','LQ') DEFAULT NULL,
  `time_of_listing` datetime DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;



CREATE TABLE IF NOT EXISTS `game_account_transaction` (
  `id` bigint(20) NOT NULL,
  `account_description` varchar(255) NOT NULL,
  `account_id` bigint(20) NOT NULL,
  `email_account` varchar(255) NOT NULL,
  `game_account_type` enum('FIFA','LOL','LQ') NOT NULL,
  `password_account` varchar(255) NOT NULL,
  `phone_number_account` varchar(255) NOT NULL,
  `price` bigint(20) NOT NULL,
  `transaction_date` datetime(6) NOT NULL,
  `username_account` varchar(255) NOT NULL,
  `user_id` bigint(20) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


CREATE TABLE IF NOT EXISTS `giftcode` (
  `id` bigint(20) NOT NULL,
  `code` varchar(255) DEFAULT NULL,
  `value` bigint(20) NOT NULL,
  `giftcode_info` varchar(255) DEFAULT NULL,
  `status` enum('AVAILABLE','USED') DEFAULT NULL,
  `time_of_listing` datetime NOT NULL DEFAULT current_timestamp(),
  `time_of_use` datetime DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;



CREATE TABLE IF NOT EXISTS `lolaccount` (
  `id` bigint(20) NOT NULL,
  `game_account_status` enum('SELLING','SOLD') DEFAULT NULL,
  `description` varchar(255) DEFAULT NULL,
  `discount` int(11) NOT NULL,
  `email` varchar(255) DEFAULT NULL,
  `password` varchar(255) DEFAULT NULL,
  `phonenumber` varchar(255) DEFAULT NULL,
  `price` bigint(20) NOT NULL,
  `username` varchar(255) DEFAULT NULL,
  `champ` int(11) NOT NULL,
  `rank` varchar(255) DEFAULT NULL,
  `rp` int(11) NOT NULL,
  `skin` int(11) NOT NULL,
  `tinhhoalam` int(11) NOT NULL,
  `images` text DEFAULT NULL,
  `game_account_type` enum('FIFA','LOL','LQ') DEFAULT NULL,
  `time_of_listing` datetime DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;



CREATE TABLE IF NOT EXISTS `lqmaccount` (
  `id` bigint(20) NOT NULL,
  `game_account_status` enum('SELLING','SOLD') DEFAULT NULL,
  `description` varchar(255) DEFAULT NULL,
  `discount` int(11) NOT NULL,
  `email` varchar(255) DEFAULT NULL,
  `password` varchar(255) DEFAULT NULL,
  `phonenumber` varchar(255) DEFAULT NULL,
  `price` bigint(20) NOT NULL,
  `username` varchar(255) DEFAULT NULL,
  `champ` int(11) NOT NULL,
  `rank` varchar(255) DEFAULT NULL,
  `skin` int(11) NOT NULL,
  `images` text DEFAULT NULL,
  `game_account_type` enum('FIFA','LOL','LQ') DEFAULT NULL,
  `time_of_listing` datetime DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;



CREATE TABLE IF NOT EXISTS `user` (
  `id` bigint(20) NOT NULL,
  `time_create_at` datetime(6) DEFAULT NULL,
  `balance` bigint(20) NOT NULL,
  `email` varchar(255) DEFAULT NULL,
  `fullname` varchar(255) DEFAULT NULL,
  `password` varchar(255) DEFAULT NULL,
  `provider` varchar(255) DEFAULT NULL,
  `provider_id` varchar(255) DEFAULT NULL,
  `status` enum('ACTIVE','LOCKED') DEFAULT NULL,
  `username` varchar(255) DEFAULT NULL,
  `totaldeposit` bigint(20) NOT NULL,
  `role` enum('ADMIN','AGENCY','USER') NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


INSERT INTO `user` (`id`, `time_create_at`, `balance`, `email`, `fullname`, `password`, `provider`, `provider_id`, `status`, `username`, `totaldeposit`, `role`) VALUES (1, '2025-03-25 15:52:39.000000', '4019300', 'quangsinhk42331@gmail.com', 'Quang sinh', '$2a$10$Kk.ah8O3oSCT6N0XjudtM.W7k2zmcuDtsh46rTNRM5RujGpmlMhq2', NULL, NULL, 'ACTIVE', 'quangsinhtestgmail', '210000', 'ADMIN');


CREATE TABLE IF NOT EXISTS `vnpay_transaction` (
  `id` bigint(20) NOT NULL,
  `amount` bigint(20) DEFAULT NULL,
  `time_of_depositing` datetime(6) NOT NULL,
  `status` varchar(255) DEFAULT NULL,
  `transaction_id` varchar(255) DEFAULT NULL,
  `txn_ref` varchar(255) DEFAULT NULL,
  `user_id` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;






CREATE TABLE IF NOT EXISTS `voucher` (
  `id` bigint(20) NOT NULL,
  `code` varchar(255) DEFAULT NULL,
  `value` bigint(20) NOT NULL,
  `voucher_expire_date` datetime(6) DEFAULT NULL,
  `status` enum('AVAILABLE','USED') DEFAULT NULL,
  `time_of_listing` datetime NOT NULL DEFAULT current_timestamp(),
  `time_of_use` datetime DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;





CREATE TABLE IF NOT EXISTS `withdrawal_order` (
  `id` bigint(20) NOT NULL,
  `amount` bigint(20) NOT NULL,
  `withdraw_description` varchar(255) DEFAULT NULL,
  `Withdrawal_method` tinyint(4) NOT NULL,
  `user_id` bigint(20) DEFAULT NULL,
  `withdrawal_status` enum('FAILED','PENDING','SUCCESS') NOT NULL,
  `time_of_depositing` datetime(6) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


ALTER TABLE `card_deposit_order`
  ADD PRIMARY KEY (`id`),
  ADD KEY `FK618o2ua3472cm7wkgua4hy07n` (`user_id`);


ALTER TABLE `deposit_payment_info`
  ADD PRIMARY KEY (`id`);

ALTER TABLE `fifa_account`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `uk_usernameaccount` (`username`),
  ADD KEY `index_price` (`price`),
  ADD KEY `index_status` (`game_account_status`);


ALTER TABLE `game_account_transaction`
  ADD PRIMARY KEY (`id`),
  ADD KEY `FK7y9bkidx6pge0cb3o53jgxb8h` (`user_id`);


ALTER TABLE `giftcode`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `uk_code` (`code`);

ALTER TABLE `lolaccount`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `uk_usernameaccount` (`username`),
  ADD KEY `index_price` (`price`),
  ADD KEY `index_status` (`game_account_status`);

ALTER TABLE `lqmaccount`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `uk_usernameaccount` (`username`),
  ADD KEY `index_price` (`price`),
  ADD KEY `index_status` (`game_account_status`);

ALTER TABLE `user`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `uk_username` (`username`),
  ADD UNIQUE KEY `uk_email` (`email`);

ALTER TABLE `vnpay_transaction`
  ADD PRIMARY KEY (`id`),
  ADD KEY `FK7habj2u90698r9v69ot651xgb` (`user_id`);

ALTER TABLE `voucher`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `uk_code` (`code`);

ALTER TABLE `withdrawal_order`
  ADD PRIMARY KEY (`id`),
  ADD KEY `FK9o3749iuyeifaw4mfwfvtiir9` (`user_id`);


ALTER TABLE `card_deposit_order`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=38;


ALTER TABLE `deposit_payment_info`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=2;


ALTER TABLE `fifa_account`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=29;


ALTER TABLE `game_account_transaction`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=9;

ALTER TABLE `giftcode`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=25;


ALTER TABLE `lolaccount`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=21;


ALTER TABLE `lqmaccount`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=5;

ALTER TABLE `user`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=28;

ALTER TABLE `vnpay_transaction`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=18;

ALTER TABLE `voucher`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=31;


ALTER TABLE `withdrawal_order`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT;

ALTER TABLE `card_deposit_order`
  ADD CONSTRAINT `FK618o2ua3472cm7wkgua4hy07n` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`);

ALTER TABLE `game_account_transaction`
  ADD CONSTRAINT `FK7y9bkidx6pge0cb3o53jgxb8h` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`);

ALTER TABLE `vnpay_transaction`
  ADD CONSTRAINT `FK7habj2u90698r9v69ot651xgb` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`);

ALTER TABLE `withdrawal_order`
  ADD CONSTRAINT `FK9o3749iuyeifaw4mfwfvtiir9` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`),
  ADD CONSTRAINT `FKmlr0iyuv8yhroymmg7fc8t54e` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`);
COMMIT;