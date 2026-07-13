# Explore-With-Me

## Архитектура

Состоит из трех групп модулей:

- `core` — доменные сервисы и внутренний API для взаимодействия.
- `ewm-stats-service` — сервис и клиент статистики.
- `infra` — инфраструктурные компоненты (конфигурации, discovery, gateway).

---

## Сервисы и взаимодействие

### Модуль `core`
Содержит прикладные сервисы:

- `event-service` — обработка эндпойнтов событий, категорий и подборок.
- `request-service` — обработка эндпойнтов запросов на участие в событии.
- `subscription-service` — управление подписками на пользователей.
- `user-service` — управление пользователями.
- `interaction-api` — контракты и dto для взаимодействия сервисов.

### Модуль `ewm-stats-service`
Отвечает за статистику:

- `stats-server` — сервер статистики.
- `stats-client` — Feign клиент для обращения к `stats-server` из других микросервисов.
- `stats-sto` — слой доступа к данным статистики (dto).

### Модуль `infra`
Инфраструктура для запуска и интеграции сервисов:

- `config-server` — централизованная конфигурация для сервисов.
- `discovery-server` — сервис-дискавери (регистрация/поиск).
- `gateway-server` — точка входа, роутинг.

---

## Конфигурации сервисов

Все конфигурации сервисов вынесены хранятся в:

- `infra/config-server`

Каждый сервис имеет `application.yaml`, в котором указан импорт конфигурации из `config-server`, местоположение которого определяется с помощью Eureka.

---

## Внутренний API

Взаимодействие между сервисами осуществляется через внутренний API в модуле `core/interaction-api`

- содержит контракты (DTO/эндпоинты) для обмена данными между `event-service`, `request-service`, `subscription-service`, `user-service`;
- согласованный формат вызовов/сообщений при интеграции сервисов.

Статистика:
- сервисы `core` обращаются к статистике через `ewm-stats-service/stats-client`,
- предоставляет данные `ewm-stats-service/stats-server`,
- доступ к бд статистики выполняется через `ewm-stats-service/stats-sto`.

---

## Внешний API

Спецификация внешнего API находится в групповом проекте:

- [Ссылка API для сервисов событий](<https://github.com/yandex-praktikum/java-plus-graduation/blob/main/ewm-main-service-spec.json>)
- [Ссылка API для статистики](<https://github.com/yandex-praktikum/java-plus-graduation/blob/main/ewm-stats-service-spec.json>)
