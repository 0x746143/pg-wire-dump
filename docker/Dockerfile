FROM postgres:17-bookworm
RUN apt update && apt install -y tshark netcat-traditional
COPY test-tables.sql /docker-entrypoint-initdb.d/
