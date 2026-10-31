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

# Interleave customers so partition routing is easier to observe.
create_order '11111111-1111-4111-8111-111111111111' 'Keyboard' 1 150.00
create_order '22222222-2222-4222-8222-222222222222' 'Mouse' 1 80.00
create_order '33333333-3333-4333-8333-333333333333' 'Monitor' 1 900.00
create_order '11111111-1111-4111-8111-111111111111' 'Headphones' 1 200.00
create_order '22222222-2222-4222-8222-222222222222' 'USB cable' 2 40.00
create_order '33333333-3333-4333-8333-333333333333' 'Mouse pad' 1 50.00
