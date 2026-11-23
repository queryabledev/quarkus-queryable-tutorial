insert into teams(uuid, name, tags) values ('team-1', 'primo', 'java,angular');
insert into teams(uuid, name, tags) values ('team-2', 'secondo', 'typescript,react');

insert into developers(uuid, name, surname, team_uuid, hiredOn, notes, active) values ('dev-1', 'fiorenzo', 'pizza', 'team-1', '2026-01-10', null, true);
insert into developers(uuid, name, surname, team_uuid, hiredOn, notes, active) values ('dev-2', 'mario', 'rossi', 'team-2', '2026-02-12', 'senior', true);
insert into developers(uuid, name, surname, team_uuid, hiredOn, notes, active) values ('dev-3', 'luigi', 'verdi', 'team-2', '2025-12-01', null, false);

insert into projects(uuid, name, budget) values ('proj-1', 'queryable', 100000);
insert into projects(uuid, name, budget) values ('proj-2', 'legacy', 5000);

insert into project_developers(project_uuid, developer_uuid) values ('proj-1', 'dev-1');
insert into project_developers(project_uuid, developer_uuid) values ('proj-1', 'dev-2');
insert into project_developers(project_uuid, developer_uuid) values ('proj-2', 'dev-3');
