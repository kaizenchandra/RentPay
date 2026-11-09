ALTER TABLE document_scan_job ADD CONSTRAINT document_scan_job_subject_unique UNIQUE(id,account_id);
ALTER TABLE document_scan_command ADD CONSTRAINT document_scan_command_subject_fk FOREIGN KEY(job_id,account_id) REFERENCES document_scan_job(id,account_id);
