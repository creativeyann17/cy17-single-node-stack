FROM node:18.1-alpine as builder

WORKDIR /tmp/ui

COPY ui/ .
ARG ENV=dev
COPY ui/.env.$ENV .env

RUN npm install
RUN npm run build

FROM nginx:1.24.0-alpine as server

COPY --from=builder /tmp/ui/dist /usr/share/nginx/html/

COPY ui/nginx-ui.conf /etc/nginx/conf.d/default.conf

ENTRYPOINT [""]
CMD ["nginx", "-g", "daemon off;"]
