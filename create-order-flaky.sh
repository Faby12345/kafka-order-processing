#!/usr/bin/env bash
set -euo pipefail

curl --fail-with-body --show-error --silent --include \
  -X POST http://localhost:8080/orders \
  -H 'Content-Type: application/json' \
  -d '{
    "customerId": "5be40f48-689e-44c8-afcd-c7461881953b",
    "product": "FLAKY",
    "quantity": 2,
    "totalPrice": 300.00
  }'
printf '\n'