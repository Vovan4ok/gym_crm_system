create sequence training_seq start with 1 increment by 1;
create sequence user_seq start with 1 increment by 1;

create table trainee_trainers (
                                  trainee_id bigint not null,
                                  trainer_id bigint not null,
                                  primary key (trainee_id, trainer_id)
);

create table trainees (
                          address varchar(255),
                          date_of_birth date,
                          user_id bigint not null,
                          primary key (user_id)
);

create table trainers (
                          user_id bigint not null,
                          training_type_id bigint,
                          primary key (user_id)
);

create table training_types (
                                training_type_id bigint not null,
                                training_type_name varchar(255) not null,
                                primary key (training_type_id)
);

create table trainings (
                           training_id bigint not null,
                           training_date date not null,
                           training_duration_in_minutes integer not null,
                           training_name varchar(255) not null,
                           trainee_id bigint not null,
                           trainer_id bigint not null,
                           training_type_id bigint not null,
                           primary key (training_id)
);

create table users (
                       user_id bigint not null,
                       first_name varchar(255) not null,
                       is_active boolean not null,
                       last_name varchar(255) not null,
                       password varchar(255) not null,
                       username varchar(255) not null,
                       primary key (user_id)
);

alter table users
    add constraint UK_r43af9ap4edm43mmtq01oddj6 unique (username);

alter table trainee_trainers
    add constraint FKpdgw7we6i8vmfqjjyj03a15o1
        foreign key (trainer_id)
            references trainers;

alter table trainee_trainers
    add constraint FKojciab1uqv39r1adcof31wji8
        foreign key (trainee_id)
            references trainees;

alter table trainees
    add constraint FKcy6e52861i61gaajadiq70lnt
        foreign key (user_id)
            references users;

alter table trainers
    add constraint FKju63khn641fkoxn0aani2jey9
        foreign key (training_type_id)
            references training_types;

alter table trainers
    add constraint FKmkxcvfr0uu3pwv772aurye5w7
        foreign key (user_id)
            references users;

alter table trainings
    add constraint FK4btmw0yu4tbogiw8qi0ewba74
        foreign key (trainee_id)
            references trainees;

alter table trainings
    add constraint FKbqwj5wievnuonuxv60wg6rr48
        foreign key (trainer_id)
            references trainers;

alter table trainings
    add constraint FKovn6ewpej7cxhjx55tovddhnj
        foreign key (training_type_id)
            references training_types;
