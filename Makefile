.PHONY: build up down logs test clean

build:
	docker-compose up --build

up:
	docker-compose up

down:
	docker-compose down

logs:
	docker-compose logs -f

test:
	docker-compose -f docker-compose.test.yml up --build --abort-on-container-exit

clean:
	docker system prune -f
