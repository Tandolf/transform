#!/usr/bin/env bash
docker rm myDatabase
docker run --name myDatabase -p 5432:5432 -e POSTGRES_PASSWORD=mysecretpassword postgres
