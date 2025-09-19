# Frontend estático servido por nginx con posibilidad de inyectar host:port del API Gateway
FROM nginx:alpine

LABEL authors="Xiomara"

# build args para inyectar la URL del gateway en la config nginx
ARG API_GATEWAY_HOST=api-gateway
ARG API_GATEWAY_PORT=8080

# Exponer variables de entorno para envsubst
ENV API_GATEWAY_HOST=${API_GATEWAY_HOST}
ENV API_GATEWAY_PORT=${API_GATEWAY_PORT}

# Copiamos el frontend
COPY ./ /usr/share/nginx/html/

# Copiamos plantilla de config nginx (la convertimos en la config final con envsubst)
COPY nginx/default.conf.template /etc/nginx/templates/default.conf.template

# Instalamos gettext para envsubst (alpine)
RUN apk add --no-cache gettext \
 && envsubst '\$API_GATEWAY_HOST \$API_GATEWAY_PORT' < /etc/nginx/templates/default.conf.template > /etc/nginx/conf.d/default.conf \
 && rm /etc/nginx/templates/default.conf.template

EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
