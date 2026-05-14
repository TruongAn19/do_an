-- V9__add_recurring_fields_to_booking.sql
ALTER TABLE `booking` 
ADD COLUMN `days_of_week` VARCHAR(255) NULL AFTER `recurring_end_date`,
ADD COLUMN `duration_months` INT NULL AFTER `days_of_week`;
