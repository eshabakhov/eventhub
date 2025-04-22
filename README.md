# Eventhub

Eventhub - интерактивная платформа, на которой в публичном доступе отображается определенная информация о мероприятиях в указанных сферах

## Требования к работе

1. Презентация находится в job-requirements/Eventhub.pdf - презентация в формате .pdf
2. Видео доступно по ссылке на RuTube - https://rutube.ru/video/492d6df200998227c4a3115ca398a70c/

## Требования к окружению

1. jdk v21.*
2. maven v2.*
3. node v20.*
4. docker compose v2.*

## Запуск приложения

1. Для сборки приложения использовать команду:
```
mvn clean package -f ./backend/eventhub/pom.xml -DskipTests  
```
Для сборки необходим запущенный докер, так как в нём поднимается тестовая БД, в которой применяются миграции и генерируются jooq-классы.

2. Для запуска приложения (бэкенд, фронтенд) и БД в контейнерах:
```
docker-compose up -d --build 
```

## Подключение к платформе

http://localhost:3000 - после запуска приложения

## Авторизация
В приложении созданы тестовые учетные записи, под которыми можно авторизоваться
### Участники: 
1. member-test-1
2. member-test-2
3. member-test-3
4. member-test-4
5. member-test-5
### Организаторы:
1. organizer-test-1
2. organizer-test-2
3. organizer-test-3
4. organizer-test-4
5. organizer-test-5
6. organizer-test-6
7. organizer-test-7
8. organizer-test-8
9. organizer-test-9
10. organizer-test-10
11. organizer-test-11
12. organizer-test-12
13. organizer-test-13
14. organizer-test-14
15. organizer-test-15
16. organizer-test-16
17. organizer-test-17
18. organizer-test-18
19. organizer-test-19
20. organizer-test-20
### Модератор:
moderator-test-2
### Модератор-админ:
moderator-test-1

### Пароль на все тестовые учетные записи: 1234
## Спецификация API backend
http://localhost:9500/api/swagger-ui/index.html после запуска приложения
