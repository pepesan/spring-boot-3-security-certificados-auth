# Aplicación de ejemplo de uso de certificados para autenticación mutua

Este proyecto es una aplicación de ejemplo que demuestra cómo utilizar certificados para la autenticación mutua entre un cliente y un servidor. La autenticación mutua es un proceso en el cual tanto el cliente como el servidor verifican la identidad del otro mediante certificados digitales.
## Requisitos previos
- Java 17+
- Spring Boot 3.x
- Dependencias: Spring Web y Spring Security

## Genera la CA, el certificado de servidor y el certificado de cliente
Para generar los certificados necesarios, puedes utilizar OpenSSL. A continuación se muestran los comandos para crear una Autoridad Certificadora (CA), un certificado de servidor y un certificado de cliente.
Crea la carpeta `certs` y navega a ella:
```bash
mkdir certs
cd certs
```
### CA (autoridad certificadora)
```bash
openssl genrsa -out ca.key 4096
openssl req -x509 -new -nodes -key ca.key -sha256 -days 3650 \
  -subj "/CN=Demo-CA" -out ca.crt
```
### Servidor (clave y CSR con SAN=localhost)
```bash
openssl genrsa -out server.key 2048
openssl req -new -key server.key -subj "/CN=localhost" -out server.csr
```
### Extensiones para el servidor (crea server.ext con este contenido):
```bash

cat > server.ext <<'EOF'
authorityKeyIdentifier=keyid,issuer
basicConstraints=CA:FALSE
keyUsage=digitalSignature,keyEncipherment
extendedKeyUsage=serverAuth
subjectAltName=@alt_names
[alt_names]
DNS.1=localhost
EOF
```
### Firmar el servidor con la CA
```bash
openssl x509 -req -in server.csr -CA ca.crt -CAkey ca.key -CAcreateserial \
  -out server.crt -days 825 -sha256 -extfile server.ext
```
### Cliente (CN=alice)
```bash
openssl genrsa -out client.key 2048
openssl req -new -key client.key -subj "/CN=alice" -out client.csr
```

# Extensiones para el cliente (crea client.ext con este contenido):
```bash
cat > client.ext <<'EOF'
basicConstraints=CA:FALSE
keyUsage=digitalSignature,keyEncipherment
extendedKeyUsage=clientAuth
EOF
```
### Firmar el cliente con la CA
```bash
openssl x509 -req -in client.csr -CA ca.crt -CAkey ca.key -CAcreateserial \
  -out client.crt -days 825 -sha256 -extfile client.ext

```
### Empaquetar en PKCS12 para Spring Boot
```bash
#   - Keystore del servidor (contiene clave privada del servidor + su certificado + cadena)
openssl pkcs12 -export -inkey server.key -in server.crt -certfile ca.crt \
  -name server -out server.p12 -password pass:changeit

#   - Truststore para el servidor (con la CA que firmó a los clientes)
keytool -importcert -alias ca -file ca.crt -keystore ca.p12 \
  -storetype PKCS12 -storepass changeit -noprompt
``` 
### Empaquetar en PKCS12 para el cliente
```bash
openssl pkcs12 -export \
  -inkey client.key \
  -in client.crt \
  -certfile ca.crt \
  -name client \
  -out client.p12 \
  -password pass:changeit
```

## Prueba de la autenticación mutua
### Iniciar el servidor
Asegúrate de que los archivos `server.p12`, `client.p12` y `ca.p12` estén en el directorio `src/main/resources` de tu proyecto Spring Boot. Luego, inicia la aplicación Spring Boot. El servidor escuchará en `https://localhost:8443`.

Métete en directorio certs...
### Probar con curl de manera autenticada
Usa el siguiente comando `curl` para probar la autenticación mutua. Asegúrate de que los archivos `client.crt` y `client.key` estén en el directorio actual.
```bash
curl --cert client.p12:changeit --cert-type P12 \
     --cacert ca.crt https://localhost:8443/hello
```

### Prueba con curl de manera indirecta con HTTPClient
```bash
curl \
  --cert client.p12:changeit --cert-type P12 \
  --cacert ca.crt \
  https://localhost:8443/client-hello

```

### Prueba con curl de manera indirecta con Webflux
```bash
curl \
  --cert client.p12:changeit --cert-type P12 \
  --cacert ca.crt \
  https://localhost:8443/webclient-hello

```