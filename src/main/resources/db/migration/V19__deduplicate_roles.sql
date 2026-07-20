-- Keep one canonical row for each role name and repoint existing users.
UPDATE `user` u
JOIN `roles` r ON r.id = u.role_id
JOIN (
    SELECT name, MIN(id) AS keep_id
    FROM `roles`
    GROUP BY name
) canonical ON canonical.name = r.name
SET u.role_id = canonical.keep_id
WHERE u.role_id <> canonical.keep_id;

DELETE duplicate_role
FROM `roles` duplicate_role
JOIN (
    SELECT name, MIN(id) AS keep_id
    FROM `roles`
    GROUP BY name
) canonical ON canonical.name = duplicate_role.name
WHERE duplicate_role.id <> canonical.keep_id;

ALTER TABLE `roles`
    MODIFY `name` VARCHAR(255) NOT NULL,
    ADD CONSTRAINT `uk_roles_name` UNIQUE (`name`);
