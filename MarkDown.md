# 🛠 Helpdesk

Простая система заявок на Java. Без баз данных и внешних библиотек.

Полное описание требований: [TZ.md](TZ.md)

## Что умеет

- Создавать заявки (имя, тема, описание)
- Показывать список заявок
- Менять статус: **Новая → В работе → Готово**
- Фильтровать заявки по статусу
- Хранить заявки в файле `tickets.txt`

## Как запустить

Нужен установленный **JDK 11 или новее** (проверка: `javac -version`).

**Linux / macOS:**
```bash
./run.sh
```

**Windows:**
```bat
run.bat
```

Откройте в браузере: <http://localhost:8080>

### Через Docker
```bash
docker build -t helpdesk .
docker run -p 8080:8080 helpdesk
```

### Настройки (необязательно)

| Переменная | По умолчанию | Что означает |
|---|---|---|
| `PORT` | `8080` | Порт сервера |
| `DATA_FILE` | `tickets.txt` | Файл с заявками |
| `WEB_DIR` | `web` | Папка с сайтом |

## Как запустить тесты

```bash
./run-tests.sh      # Linux / macOS
run-tests.bat       # Windows
```

## Структура проекта

```
helpdesk/
├── MarkDown.md              <- этот файл
├── TZ.md                    <- техническое задание
├── Dockerfile               <- DevOps
├── run.sh / run.bat         <- DevOps: запуск
├── run-tests.sh / .bat      <- DevOps + тестировщик: запуск тестов
├── src/
│   ├── Main.java            <- Backend: сервер и API
│   ├── Ticket.java          <- Backend: модель заявки
│   └── TicketStore.java     <- Backend: хранение в файле
├── web/
│   ├── index.html           <- Frontend: страница
│   ├── app.js               <- Frontend: логика
│   └── style.css            <- Дизайнер: оформление
└── test/
    └── TicketStoreTest.java <- Тестировщик: автотесты
```

## Кто за что отвечает

| Роль | Зона ответственности |
|---|---|
| 🎨 Дизайнер | `web/style.css` |
| 💻 Frontend | `web/index.html`, `web/app.js` |
| ⚙️ Backend | `src/` |
| 🚀 DevOps | `run.sh`, `run.bat`, `Dockerfile` |
| 🧪 Тестировщик | `test/`, ручные тесты из `TZ.md` |

## API

| Метод | Адрес | Описание |
|---|---|---|
| GET | `/api/tickets` | Список заявок |
| POST | `/api/tickets` | Создать заявку (`author`, `title`, `description`) |
| POST | `/api/tickets/{id}/status` | Сменить статус (`NEW`, `IN_PROGRESS`, `DONE`) |

## Идеи для развития

- Вход для сотрудников поддержки
- Комментарии к заявкам
- Приоритеты заявок
- База данных вместо файла
