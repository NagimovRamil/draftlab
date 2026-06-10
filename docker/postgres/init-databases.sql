SELECT format('CREATE DATABASE %I', database_name)
FROM (
    VALUES
        ('orders_db'),
        ('payments_db'),
        ('ledger_db'),
        ('notifications_db'),
        ('sagas_db')
) AS required_databases(database_name)
WHERE NOT EXISTS (
    SELECT 1
    FROM pg_database
    WHERE datname = required_databases.database_name
)
\gexec
