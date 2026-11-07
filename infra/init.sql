-- Local-only credentials; bind the database port to loopback. Never deploy these to production.
CREATE ROLE lmt_runtime LOGIN PASSWORD 'lmt_local_runtime';
GRANT CONNECT ON DATABASE lmt TO lmt_runtime;
GRANT USAGE ON SCHEMA public TO lmt_runtime;
