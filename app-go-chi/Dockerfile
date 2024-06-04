FROM golang:1.22.3-alpine AS build
WORKDIR /build

COPY go.mod go.sum ./
RUN go mod download

COPY . .
RUN CGO_ENABLED=0 GOOS=linux go build -ldflags="-s -w" -a -installsuffix cgo -o app .
RUN apk add --no-cache upx && upx --best app

FROM alpine
WORKDIR /app
COPY --from=build /build/app .
RUN apk add --no-cache curl
RUN mkdir logs

ENTRYPOINT ["./app"]
