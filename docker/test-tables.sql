create table basic_types_table (
	integer_column integer null,
	varchar_column varchar null
);

insert into basic_types_table values(0, 'varchar_data_0');
insert into basic_types_table values(1, 'varchar_data_1');

create table filter_query_table (
	integer_data_col integer,
	varchar_data_col varchar,
	integer_filter_col integer,
	varchar_filter_col varchar
);

insert into filter_query_table values(0, 'varchar_data_0', 0, 'varchar_filter_0');
insert into filter_query_table values(1, 'varchar_data_1', 0, 'varchar_filter_0');
insert into filter_query_table values(2, 'varchar_data_2', 0, 'varchar_filter_1');
insert into filter_query_table values(3, 'varchar_data_3', 0, 'varchar_filter_1');
insert into filter_query_table values(4, 'varchar_data_4', 1, 'varchar_filter_0');
insert into filter_query_table values(5, 'varchar_data_5', 1, 'varchar_filter_0');
insert into filter_query_table values(6, 'varchar_data_6', 1, 'varchar_filter_1');
insert into filter_query_table values(7, 'varchar_data_7', 1, 'varchar_filter_1');
