-- Remove legacy duplicate memberships before enforcing one membership per user/post.
DELETE mp1 FROM match_participants mp1
INNER JOIN match_participants mp2
    ON mp1.match_post_id = mp2.match_post_id
   AND mp1.user_id = mp2.user_id
   AND mp1.id > mp2.id;

ALTER TABLE match_participants
    ADD CONSTRAINT uk_match_participant_post_user
    UNIQUE (match_post_id, user_id);
