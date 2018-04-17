#!/bin/bash

PASS_PHRASE=qwerty
EXPORT_PASS=browser 

NGINX_PATH=nginx*

## Create self-signed Certificate Authority (CA) certificate
openssl genrsa -des3 -passout pass:$PASS_PHRASE -out ca.key 4096
openssl req -new -x509 -days 365 -key ca.key -out ca.crt -subj "/C=FI/ST=Uusimaa/L=Espoo/O=$HOSTNAME/OU=Admin/CN=$HOSTNAME" -passin pass:$PASS_PHRASE

## Create client side SSL certificate and self-signed it using the CA certificate
openssl genrsa -des3 -passout pass:$PASS_PHRASE -out client.key 4096
openssl req -new -key client.key -out client.csr -subj "/C=FI/ST=Uusimaa/L=Espoo/O=$HOSTNAME/OU=Admin/CN=Asad Javed/emailAddress=measad2009@yahoo.com" -passin pass:$PASS_PHRASE
openssl x509 -req -days 365 -in client.csr -CA ca.crt -CAkey ca.key -set_serial 01 -out client.crt -passin pass:$PASS_PHRASE

## Covert client certificate to the PKCS format so that it can be installed in different browsers
openssl pkcs12 -export -clcerts -in client.crt -inkey client.key -out client.p12 -passin pass:$PASS_PHRASE -passout pass:$EXPORT_PASS

## Create server side certificate and self-signed it using the CA certificate.
openssl genrsa -des3 -passout pass:$PASS_PHRASE -out server.key 4096
openssl req -new -key server.key -out server.csr -subj "/C=FI/ST=Uusimaa/L=Espoo/O=localhost/OU=Admin/CN=localhost" -passin pass:$PASS_PHRASE
openssl x509 -req -days 365 -in server.csr -CA ca.crt -CAkey ca.key -set_serial 02 -out server.crt -passin pass:$PASS_PHRASE

## Remove password from the server certificate 
openssl rsa -in server.key -out server.key -passin pass:$PASS_PHRASE

## Copy certificates to the approriate directory
cp server.crt $NGINX_PATH/conf/
cp server.key $NGINX_PATH/conf/
cp ca.crt $NGINX_PATH/conf/
