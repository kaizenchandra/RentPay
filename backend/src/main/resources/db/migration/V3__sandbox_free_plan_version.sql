-- Preserve original baseline evidence; publish a new version reflecting the shipped sandbox setup.
INSERT INTO plan_version(version_id,audience,code,version_number,display_name,publication_state,effective_at,baseline_only,rentable_limit,manager_limit,monthly_amount,annual_amount,currency,commercial_disclosure)
SELECT '00000000-0000-4000-8000-000000000006',audience,code,2,display_name,'published',CURRENT_TIMESTAMP,true,rentable_limit,manager_limit,monthly_amount,annual_amount,currency,
'Local sandbox workspace setup is available. Rental operations are not available yet. No payment is collected.'
FROM plan_version WHERE version_id='00000000-0000-4000-8000-000000000001';
INSERT INTO catalog_audit(version_id,action,actor) VALUES('00000000-0000-4000-8000-000000000006','SANDBOX_VERSION_PUBLISHED','migration:V3');
