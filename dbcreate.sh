#!/usr/bin/env bash
#
# PGPASSWORD=password dbcreate.sh [-n] host port database user [password]
#
# Create database tables

new="n"
if [ "$1" = "-n" ]; then
    new="y"
    shift
fi

if [ -z "$4" ]; then
    echo "Usage: [PGPASSWORD=<password>] $0 [-n] <host> <port> <database> <user> [<password>]"
    exit 1
fi

host=$1
port=$2
database=$3
user=$4
if [ -n "$5" ]; then
    PGPASSWORD="$5" ; export PGPASSWORD
fi

psql --no-password -h $host -d $database -p $port -U $user -f ./sql/create_tables.sql
if [ "$new" = "n" ]; then
    psql --no-password -h $host -d $database -p $port -U $user -f ./sql/create_triggers.sql
fi