# Projekt StopChocolate

## Cel projektu

Projekt StopChocolate to aplikacja mająca na celu wsparcie użytkowników chcących prowadzić zdrowy styl życia. System jest utworzony w celu wsparcia w ograniczaniu spożycia słodyczy, docelowo użytkownicy będą w stanie notować i zapisywać w kalendarzu swoje postępy, z opcjonalną możliwością synchronizacji i udostępniania ich za pośrednictwem stworzonego w systemie konta. Dane są przetwarzane zgodnie z RODO. System oferuje funkcje rejestracji, logowania i zarządzania kontem.

## Stan obecny

- **Backend**: Aplikacja oparta na Spring Boot, skonfigurowana do pracy z bazą danych PostgreSQL oraz integracją z Keycloak jako serwerem autoryzacji i zarządzania użytkownikami. Moduł "auth" pozwala na zarejestrowanie nowego użytkownika, logowanie się, zmianę danych konta i przywrócenie hasła, działając jako pośrednik względem Keycloaka.
- **Frontend**: TODO
- **Autoryzacja**: Wdrożony Keycloak z własną konfiguracją realm, obsługą ról i polityką haseł. Import realm realizowany jest automatycznie przy starcie kontenera.
- **Baza danych**: Baza danych PostgreSQL, inicjalizowana skryptem SQL.
- **Obsługa e-mail**: System przygotowany do wysyłania maili (np. reset hasła) z wykorzystaniem zewnętrznego serwera SMTP.
- **Docker Compose**: Całość uruchamiana w środowisku kontenerowym, zdefiniowanym w pliku `compose.yaml`.

## Zamierzenia

- Rozwinięcie Rest API o funkcjonalności tematyczne.
- Przygotowanie frontendu dostosowanego do RWD, z wykorzystaniem sprawdzonej biblioteki komponentów (Angular Material / PrimeNG).
- Ulepszenie logowania informacji przez aplikację.
- Wdrożenie testów automatycznych oraz CI/CD.
- Wgranie aplikacji na serwis chmurowy i połączenie z domeną internetową.

## Pliki konfiguracyjne

- `.env` – zmienne środowiskowe dla wszystkich usług.
- `compose.yaml` – konfiguracja usług Docker.
- `keycloak/realms/stop-chocolate.json` – definicja realm Keycloak.

## Uruchomienie

1. Skonfiguruj plik `.env` na podstawie `.env.example`.
2. Uruchom projekt poleceniem:
   ```
   docker compose up --build
   ```
3. Aplikacja backendowa będzie dostępna na porcie określonym w `.env` (`BACKEND_PORT`), Keycloak na porcie `KEYCLOAK_PORT`, baza danych na `DB_PORT`.

## Autorzy

- Pawel Marcinowski

---

Projekt w fazie rozwojowej. Wszelkie uwagi i propozycje mile widziane.
