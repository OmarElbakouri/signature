-- Script pour nettoyer les documents sans utilisateur associé
-- À exécuter après la migration pour assurer que tous les documents sont associés à un utilisateur

-- Option 1: Suppression des documents orphelins
DELETE FROM document WHERE user_id IS NULL;

-- Option 2: Association des documents orphelins à un utilisateur admin (décommenter si préféré)
-- UPDATE document SET user_id = (SELECT id FROM users WHERE username = 'admin' LIMIT 1) WHERE user_id IS NULL;

-- Vérification des documents restants
-- SELECT COUNT(*) FROM document WHERE user_id IS NULL;
