#!/usr/bin/env bash
set -euo pipefail

# Optional first argument: API base URL, e.g. http://localhost:8082
base_url="${1:-http://localhost:8080}"

create_order() {
  local customer_id="$1"
  local product="$2"
  local quantity="$3"
  local total_price="$4"

  printf '\nCreating order for customer %s (%s)\n' "$customer_id" "$product"
  curl --fail-with-body --silent --show-error --include \
    -X POST "${base_url%/}/orders" \
    -H 'Content-Type: application/json' \
    -d "{\"customerId\":\"$customer_id\",\"product\":\"$product\",\"quantity\":$quantity,\"totalPrice\":$total_price}"
  printf '\n'
}

# Same id ot test idempotency
create_order '11111111-1111-4111-8111-111111111111' 'Keyboard' 1 150.00
create_order '11111111-1111-4111-8111-111111111111' 'Mouse' 1 80.00
