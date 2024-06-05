SHELL:=/bin/bash
PROJECT_NAME=app-go-fiber
GO_BUILD_ENV=CGO_ENABLED=0 GOOS=linux GOARCH=amd64
GO_FILES=$(shell go list ./... | grep -v /vendor/)

BUILD_VERSION=$(shell cat VERSION)
BUILD_TAG=$(BUILD_VERSION)
DOCKER_IMAGE=$(PROJECT_NAME):$(BUILD_TAG)

.SILENT:

install-swagger:
	go get -u github.com/swaggo/swag/cmd/swag ;\
	go install github.com/swaggo/swag/cmd/swag ;\

download:
	go mod download

build: download generate
	$(GO_BUILD_ENV) go build -v -ldflags="-s -w" -installsuffix cgo -o $(PROJECT_NAME) .

format:
	go vet $(GO_FILES) ;\
	go fmt $(GO_FILES) ;\
	go mod tidy ;\

dev:
	air

test:
	go test $(GO_FILES) -cover

integration_test:
	go test -tags=integration $(GO_FILES)

docker: build
	docker build -t $(DOCKER_IMAGE) .;\
  rm -f $(PROJECT_NAME).bin 2> /dev/null; \

generate: install-swagger
	go generate ;\
