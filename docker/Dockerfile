FROM postgres:17-bookworm
RUN apt update && apt install -y tshark
COPY test-tables.sql /docker-entrypoint-initdb.d/
