#!/usr/bin/env bash

mvn clean install
if [ "$?" -ne 0 ]; then echo "maven failed"; exit 1; fi
