# Example API Payloads

This document contains ready-to-use example payloads for testing the Coupons Management API.

## Create Coupons

### 1. Cart-wise Coupon (Percentage Discount)

```js
POST /api/v1/coupons
Content-Type: application/json

{
  "code": "CART10",
  "type": "CART_WISE",
  "description": "10% off on orders above ₹100",
  "details": {
    "threshold": 100.0,
    "discount": 10.0,
    "discountType": "PERCENTAGE",
    "maxDiscount": 50.0
  },
  "expirationDate": "2025-12-31T23:59:59",
  "isActive": true
}
```

### 2. Cart-wise Coupon (Fixed Discount)

```js
POST /api/v1/coupons

{
  "code": "FLAT50",
  "type": "CART_WISE",
  "description": "₹50 flat discount on orders above ₹500",
  "details": {
    "threshold": 500.0,
    "discount": 50.0,
    "discountType": "FIXED"
  },
  "expirationDate": "2025-12-31T23:59:59",
  "isActive": true
}
```

### 3. Cart-wise with Minimum Items

```js
POST /api/v1/coupons

{
  "code": "BULK15",
  "type": "CART_WISE",
  "description": "15% off on orders with min 5 items",
  "details": {
    "threshold": 200.0,
    "discount": 15.0,
    "discountType": "PERCENTAGE",
    "minItems": 5,
    "maxDiscount": 100.0
  },
  "expirationDate": "2025-12-31T23:59:59",
  "isActive": true
}
```

### 4. Product-wise Coupon (Percentage)

```js
POST /api/v1/coupons

{
  "code": "PRODUCT20",
  "type": "PRODUCT_WISE",
  "description": "20% off on Product ID 1",
  "details": {
    "productId": 1,
    "discount": 20.0,
    "discountType": "PERCENTAGE",
    "minQuantity": 1,
    "maxDiscount": 100.0
  },
  "expirationDate": "2025-12-31T23:59:59",
  "isActive": true
}
```

### 5. Product-wise Coupon (Fixed per Unit)

```js
POST /api/v1/coupons

{
  "code": "PRODUCT30OFF",
  "type": "PRODUCT_WISE",
  "description": "₹30 off per unit on Product ID 2",
  "details": {
    "productId": 2,
    "discount": 30.0,
    "discountType": "FIXED",
    "minQuantity": 2
  },
  "expirationDate": "2025-12-31T23:59:59",
  "isActive": true
}
```

### 6. BxGy Coupon (Buy 2 Get 1)

```js
POST /api/v1/coupons

{
  "code": "B2G1",
  "type": "BXGY",
  "description": "Buy 2 get 1 free",
  "details": {
    "buyProducts": [
      {"productId": 1, "quantity": 2},
      {"productId": 2, "quantity": 2}
    ],
    "getProducts": [
      {"productId": 3, "quantity": 1},
      {"productId": 4, "quantity": 1}
    ],
    "repetitionLimit": 3
  },
  "expirationDate": "2025-12-31T23:59:59",
  "isActive": true
}
```

### 7. BxGy Coupon (Buy 3 Get 2)

```js
POST /api/v1/coupons

{
  "code": "B3G2FREE",
  "type": "BXGY",
  "description": "Buy 3 items from X,Y,Z and get 2 from A,B free",
  "details": {
    "buyProducts": [
      {"productId": 1, "quantity": 1},
      {"productId": 2, "quantity": 1},
      {"productId": 3, "quantity": 1}
    ],
    "getProducts": [
      {"productId": 4, "quantity": 1},
      {"productId": 5, "quantity": 1}
    ],
    "repetitionLimit": 2
  },
  "expirationDate": "2025-12-31T23:59:59",
  "isActive": true
}
```

## Get Applicable Coupons

### Example 1: Simple Cart

```js
POST /api/v1/applicable-coupons
Content-Type: application/json

{
  "cart": {
    "items": [
      {
        "product_id": 1,
        "quantity": 2,
        "price": 60.0
      },
      {
        "product_id": 2,
        "quantity": 1,
        "price": 30.0
      }
    ]
  }
}
```

**Expected Response:**
```js
{
  "applicable_coupons": [
    {
      "coupon_id": 1,
      "code": "CART10",
      "type": "CART_WISE",
      "discount": 15.0,
      "description": "10% off on orders above ₹100"
    },
    {
      "coupon_id": 4,
      "code": "PRODUCT20",
      "type": "PRODUCT_WISE",
      "discount": 24.0,
      "description": "20% off on Product ID 1"
    }
  ]
}
```

### Example 2: Large Cart with Multiple Products

```js
POST /api/v1/applicable-coupons

{
  "cart": {
    "items": [
      {
        "product_id": 1,
        "quantity": 6,
        "price": 50.0
      },
      {
        "product_id": 2,
        "quantity": 3,
        "price": 30.0
      },
      {
        "product_id": 3,
        "quantity": 2,
        "price": 25.0
      }
    ]
  }
}
```

### Example 3: BxGy Eligible Cart

```js
POST /api/v1/applicable-coupons

{
  "cart": {
    "items": [
      {
        "product_id": 1,
        "quantity": 4,
        "price": 100.0
      },
      {
        "product_id": 2,
        "quantity": 2,
        "price": 80.0
      },
      {
        "product_id": 3,
        "quantity": 3,
        "price": 50.0
      },
      {
        "product_id": 4,
        "quantity": 2,
        "price": 40.0
      }
    ]
  }
}
```

## Apply Coupon to Cart

### Example 1: Apply Cart-wise Coupon

```js
POST /api/v1/apply-coupon/1
Content-Type: application/json

{
  "cart": {
    "items": [
      {
        "product_id": 1,
        "quantity": 2,
        "price": 60.0
      },
      {
        "product_id": 2,
        "quantity": 1,
        "price": 30.0
      }
    ]
  }
}
```

**Expected Response:**
```js
{
  "updatedCart": {
    "items": [
      {
        "product_id": 1,
        "quantity": 2,
        "price": 60.0,
        "total_discount": 9.6
      },
      {
        "product_id": 2,
        "quantity": 1,
        "price": 30.0,
        "total_discount": 3.4
      }
    ],
    "total_price": 150.0,
    "total_discount": 15.0,
    "final_price": 135.0
  }
}
```

### Example 2: Apply Product-wise Coupon

```js
POST /api/v1/apply-coupon/4

{
  "cart": {
    "items": [
      {
        "product_id": 1,
        "quantity": 3,
        "price": 100.0
      },
      {
        "product_id": 2,
        "quantity": 2,
        "price": 50.0
      }
    ]
  }
}
```

**Expected Response:**
```js
{
  "updatedCart": {
    "items": [
      {
        "product_id": 1,
        "quantity": 3,
        "price": 100.0,
        "total_discount": 60.0
      },
      {
        "product_id": 2,
        "quantity": 2,
        "price": 50.0,
        "total_discount": 0.0
      }
    ],
    "total_price": 400.0,
    "total_discount": 60.0,
    "final_price": 340.0
  }
}
```

### Example 3: Apply BxGy Coupon

```js
POST /api/v1/apply-coupon/6

{
  "cart": {
    "items": [
      {
        "product_id": 1,
        "quantity": 4,
        "price": 100.0
      },
      {
        "product_id": 2,
        "quantity": 2,
        "price": 80.0
      },
      {
        "product_id": 3,
        "quantity": 3,
        "price": 50.0
      }
    ]
  }
}
```

**Expected Response:**
```js
{
  "updatedCart": {
    "items": [
      {
        "product_id": 1,
        "quantity": 4,
        "price": 100.0,
        "total_discount": 0.0
      },
      {
        "product_id": 2,
        "quantity": 2,
        "price": 80.0,
        "total_discount": 0.0
      },
      {
        "product_id": 3,
        "quantity": 3,
        "price": 50.0,
        "total_discount": 100.0
      }
    ],
    "total_price": 710.0,
    "total_discount": 100.0,
    "final_price": 610.0
  }
}
```

## Update Coupon

### Example: Deactivate Coupon

```js
PUT /api/v1/coupons/1
Content-Type: application/json

{
  "isActive": false
}
```

### Example: Update Expiration Date

```js
PUT /api/v1/coupons/2

{
  "expirationDate": "2026-01-31T23:59:59"
}
```

### Example: Update Description and Details

```js
PUT /api/v1/coupons/3

{
  "description": "Updated: 25% off on orders above ₹200",
  "details": {
    "threshold": 200.0,
    "discount": 25.0,
    "discountType": "PERCENTAGE",
    "maxDiscount": 100.0
  }
}
```

## Error Scenarios

### Coupon Not Found
```js
GET /api/v1/coupons/999

Response: 404 Not Found
{
  "timestamp": "2025-11-01T10:00:00",
  "status": 404,
  "error": "Not Found",
  "message": "Coupon not found with id: 999"
}
```

### Duplicate Coupon Code
```js
POST /api/v1/coupons
{
  "code": "SAVE10",
  ...
}

Response: 409 Conflict
{
  "timestamp": "2025-11-01T10:00:00",
  "status": 409,
  "error": "Conflict",
  "message": "Coupon with code 'SAVE10' already exists"
}
```

### Coupon Not Applicable
```js
POST /api/v1/apply-coupon/1
{
  "cart": {
    "items": [
      {
        "product_id": 1,
        "quantity": 1,
        "price": 50.0
      }
    ]
  }
}

Response: 400 Bad Request
{
  "timestamp": "2025-11-01T10:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Cart total ₹50.0 is below threshold ₹100.0"
}
```

### Validation Error
```js
POST /api/v1/coupons
{
  "code": "",
  "type": null
}

Response: 400 Bad Request
{
  "timestamp": "2025-11-01T10:00:00",
  "status": 400,
  "error": "Validation Failed",
  "message": "Input validation failed",
  "validationErrors": {
    "code": "Coupon code is required",
    "type": "Coupon type is required"
  }
}
```

## Testing with cURL

### Create Coupon
```bash
curl -X POST http://localhost:8080/api/v1/coupons \
  -H "Content-Type: application/json" \
  -d '{
    "code": "SAVE10",
    "type": "CART_WISE",
    "description": "10% off",
    "details": {
      "threshold": 100.0,
      "discount": 10.0,
      "discountType": "PERCENTAGE"
    },
    "isActive": true
  }'
```

### Get All Coupons
```bash
curl http://localhost:8080/api/v1/coupons
```

### Apply Coupon
```bash
curl -X POST http://localhost:8080/api/v1/apply-coupon/1 \
  -H "Content-Type: application/json" \
  -d '{
    "cart": {
      "items": [
        {
          "product_id": 1,
          "quantity": 2,
          "price": 60.0
        }
      ]
    }
  }'
```

---