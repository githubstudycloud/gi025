create table order_number_sequences (
    sequence_name varchar(64) primary key,
    next_value bigint not null
);

insert into order_number_sequences (sequence_name, next_value)
select 'sales_order', coalesce(max(cast(substring(order_no, 4) as bigint)), 0) + 1
from sales_orders;
