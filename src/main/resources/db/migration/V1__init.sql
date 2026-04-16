create table customers (
    id uuid primary key,
    customer_no varchar(32) not null unique,
    name varchar(120) not null,
    tier varchar(32) not null,
    email varchar(160) not null unique,
    country varchar(64) not null,
    status varchar(32) not null,
    created_at timestamp with time zone not null,
    updated_at timestamp with time zone not null
);

create table products (
    id uuid primary key,
    sku varchar(64) not null unique,
    name varchar(120) not null,
    category varchar(64) not null,
    price decimal(18,2) not null,
    description varchar(500) not null,
    active boolean not null,
    created_at timestamp with time zone not null,
    updated_at timestamp with time zone not null
);

create table knowledge_articles (
    id uuid primary key,
    slug varchar(120) not null unique,
    title varchar(160) not null,
    topic varchar(64) not null,
    content clob not null,
    published boolean not null,
    created_at timestamp with time zone not null,
    updated_at timestamp with time zone not null
);

create table sales_orders (
    id uuid primary key,
    order_no varchar(32) not null unique,
    customer_id uuid not null,
    status varchar(32) not null,
    total_amount decimal(18,2) not null,
    currency varchar(16) not null,
    notes varchar(1000),
    created_at timestamp with time zone not null,
    updated_at timestamp with time zone not null,
    constraint fk_sales_orders_customer foreign key (customer_id) references customers (id)
);

create table sales_order_items (
    id uuid primary key,
    order_id uuid not null,
    product_id uuid not null,
    quantity integer not null,
    unit_price decimal(18,2) not null,
    line_total decimal(18,2) not null,
    created_at timestamp with time zone not null,
    updated_at timestamp with time zone not null,
    constraint fk_sales_order_items_order foreign key (order_id) references sales_orders (id),
    constraint fk_sales_order_items_product foreign key (product_id) references products (id)
);
