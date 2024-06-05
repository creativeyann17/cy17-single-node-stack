FROM golang:1.22.3-alpine AS build
WORKDIR /build
RUN apk add --no-cache make bash upx

COPY . .
RUN make build
RUN apk add --no-cache upx && upx --best app-go-fiber

FROM alpine
WORKDIR /app
COPY --from=build /build/app-go-fiber .
COPY --from=build /build/docs ./docs
RUN apk add --no-cache curl dumb-init
# dumb-init fix Prefork error if PID = 1
ENTRYPOINT ["/usr/bin/dumb-init", "--"]
CMD ["./app-go-fiber"]
