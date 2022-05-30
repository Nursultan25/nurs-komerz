delete from users;
delete from users_clients;
delete from clients;

insert into users values (1, '$2a$12$o2NehtaHr7SOvu7bUkfwxenZ2S.5I09E31jF.bDyQYn.nAOcfKraO', 'USER_ROLE', 'erlan'),
                            (2, '$2a$12$o2NehtaHr7SOvu7bUkfwxenZ2S.5I09E31jF.bDyQYn.nAOcfKraO', 'USER_ROLE', 'dastan'),
                            (3, '$2a$12$o2NehtaHr7SOvu7bUkfwxenZ2S.5I09E31jF.bDyQYn.nAOcfKraO', 'ADMIN_ROLE', 'nurs');

insert into clients values (1, '0999565656'),
                           (2, '0999343434');

insert into users_clients values (1, 1),
                                 (2, 2);