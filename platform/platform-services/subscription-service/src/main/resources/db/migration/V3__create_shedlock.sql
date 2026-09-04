-- Required by ShedLock (net.javacrumbs.shedlock) so the reminder/suspension jobs
-- run once even if this service ever runs with more than one replica.
CREATE TABLE shedlock (
    name       VARCHAR(64) NOT NULL PRIMARY KEY,
    lock_until TIMESTAMPTZ NOT NULL,
    locked_at  TIMESTAMPTZ NOT NULL,
    locked_by  VARCHAR(255) NOT NULL
);
