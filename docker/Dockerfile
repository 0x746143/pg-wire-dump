FROM postgres:17-bookworm
COPY test-tables.sql /docker-entrypoint-initdb.d/
RUN apt update && apt install -y tshark
