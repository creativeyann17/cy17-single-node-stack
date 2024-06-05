FROM golang:1.22.3-alpine AS build
WORKDIR /build

COPY go.mod ./
COPY go.sum ./
RUN go mod download

COPY . .
RUN CGO_ENABLED=0 GOOS=linux GARCH=amd64 go build -ldflags="-s -w" -installsuffix cgo -o app .
#RUN apk add --no-cache upx && upx --best app

FROM alpine
WORKDIR /app
COPY --from=build /build/app .
RUN apk add --no-cache curl dumb-init
# dumb-init fix Prefork error if PID = 1
ENTRYPOINT ["/usr/bin/dumb-init", "--"]
CMD ["./app"]
