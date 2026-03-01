#!/bin/sh
# wait-for.sh

set -e

host="$1"
shift
port="$1"
shift
cmd="$@"

# Vérifier que pg_isready est disponible
if ! command -v pg_isready >/dev/null 2>&1; then
  echo "pg_isready not found. Make sure postgresql-client is installed."
  exit 1
fi

echo "Waiting for PostgreSQL at $host:$port ..."

# Attendre que PostgreSQL soit prêt
until pg_isready -h "$host" -p "$port" -U "$POSTGRES_USER"; do
  >&2 echo "PostgreSQL is unavailable - sleeping"
  sleep 2
done

echo "PostgreSQL is up - executing command"
exec $cmd
