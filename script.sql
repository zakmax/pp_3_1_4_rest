create table roles
(
    id        bigint auto_increment
        primary key,
    name_role varchar(255) not null,
    constraint name_role
        unique (name_role)
);

create table users
(
    id       bigint auto_increment
        primary key,
    name     varchar(255) not null,
    password varchar(255) not null,
    age      int          null
);

CREATE TABLE user_roles (
                            user_id BIGINT NOT NULL,
                            role_id BIGINT NOT NULL,
                            PRIMARY KEY (user_id, role_id),
                            FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
                            FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
);




INSERT INTO roles (name_role) VALUES
                                  ('admin'),
                                  ('user');

INSERT INTO users (name, age, password) VALUES
    ('admin', 30, '$2a$12$3UJ1dGGafNpm9VG6aTQL8.0sEPLSLsf.Ww6No6ZmPilM0VeSfL05O');


INSERT INTO user_roles (user_id, role_id) VALUES
    (1, 1);