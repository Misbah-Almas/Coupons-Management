# Monk Commerce Coupons Management API - Spring Boot

Hey there! This is a REST API I built for managing discount coupons in e-commerce stores. Think of all those promo codes you use while shopping online - this handles all that behind the scenes.

## Getting Started

### What You'll Need
- Java 17 or newer
- Maven 3.8+
- Git

**Note**: You don't need to install a database! It uses H2, which runs in memory.

### Setting It Up

```bash
# Grab the code
git clone https://github.com/Misbah-Almas/Coupons-Management.git
cd Coupons-Management # Go to this project folder if not already in it.

# Build it
mvn clean install

# Fire it up
mvn spring-boot:run

# Check it out:
 Swagger UI: http://localhost:8080/api/v1/swagger-ui.html
 H2 Console: http://localhost:8080/api/v1/h2-console # on h2 console make sure the jdbc url is set to - jdbc:h2:mem:couponsdb
```

## What's Under the Hood

- **Backend**: Spring Boot 3.1.5
- **Database**: H2 (in-memory)
- **ORM**: Spring Data JPA with Hibernate
- **Validation**: Spring Validation
- **Testing**: JUnit 5, Mockito, TestContainers
- **Documentation**: SpringDoc OpenAPI (Swagger)
- **Build Tool**: Maven

## What It Can Do

### 1. Cart-wide Discounts
I've implemented discounts that apply to your entire shopping cart:
- Percentage discounts when you hit a certain total (like "10% off orders over ₹100")
- Flat discounts (like "₹50 off when you spend ₹500+")
- Maximum discount caps so businesses don't lose too much
- Minimum requirements for cart value and item count

### 2. Product-specific Discounts
These target individual products:
- Percentage or fixed discounts on specific items
- Support for multiple products in one coupon
- Minimum quantity requirements ("buy at least 2 to get the discount")
- Per-product discount limits

**Example**: "20% off Product A" or "₹30 off Product B when you buy 2 or more"

### 3. BxGy (Buy X Get Y) Deals
This was the bit complex to implement! The system handles:
- "Buy N from these products, get M from those products free"
- Repetition limits (apply the deal multiple times in one order)
- Mixed quantities in the buy list
- Smart selection of free items (the cheapest ones go free first)
- Partial fulfillment when you don't quite hit the limit
- Cross-product combinations

**Example**: "Buy 2 items from Products 1 or 2, get 1 from Products 3 or 4 free (up to 3 times per order)"

### 4. General Features
- Full CRUD operations for managing coupons
- Expiration date checks
- Enable/disable coupons on the fly
- Unique coupon codes (no duplicates)
- Find all applicable coupons for any cart
- Apply specific coupons with detailed breakdowns
- Real-time discount calculations
- Comprehensive error handling
- Request/Response validation
- Swagger documentation
- Test coverage (unit + integration tests)

## Features I Thought About But Didn't Build

### Would've Been Next on My List

#### 1. User-specific Coupons
Things like first-time user discounts, per-user limits, loyalty tiers, referral bonuses, and birthday offers. I'd need a proper user authentication system and usage tracking to pull this off right.

#### 2. Stacking Multiple Coupons
Right now, you can only use one coupon at a time. In the real world, you'd want to combine coupons with smart rules about which ones work together, which take priority, and automatic selection of the best deal.

#### 3. Category-based Discounts
Like "20% off all Electronics" or "10% off Clothing & Accessories." This would need integration with a product catalog that has proper category structures.

#### 4. Time-based Restrictions
Flash sales, happy hour deals, weekend-only coupons, specific time windows. The timezone handling alone would be pretty complex.

#### 5. Payment Method Discounts
Special offers for specific credit cards, digital wallets, bank partnerships, EMI deals. Would need to integrate with payment gateway APIs.

### Other Cool Ideas

#### 6. Tiered Discounts
The more you spend or buy, the bigger the discount. Like "Spend ₹500→10% off, ₹1000→15% off."

#### 7. Shipping Discounts
Free shipping thresholds, express delivery discounts, location-based shipping rates.

#### 8. Loyalty Points
Earning and redeeming points, point multiplier events, tiered membership benefits. This would basically be its own system.

#### 9. Location & Demographics
City-specific deals, senior citizen discounts, regional campaigns. Would need user profiles and geolocation services.

#### 10. Bulk Coupon Generation
Creating thousands of unique codes at once, templates for quick setup, QR codes for offline campaigns.

#### And Even More...
- A/B testing and analytics
- Scheduled auto-activation/deactivation
- Advanced product rules (exclusions, brands, price ranges)
- Bundle deals
- Social sharing and gamification features
- Subscription perks
- Inventory-aware discounts
- Fraud prevention systems

## Current Limitations

Let me be upfront about what this doesn't do yet:

### Authentication & Users
- No login system - anyone can access everything
- Can't track who used which coupon
- No per-user usage limits
- Cart isn't saved anywhere (you pass it with each request)

### Usage Tracking
- No history of coupon redemptions
- Coupons can be used unlimited times
- No budget caps per campaign
- No audit logs

### Product Integration
- Assumes all product IDs you send are valid
- Can't do category-level discounts
- Doesn't check if items are actually in stock
- Trusts whatever prices you send

### Business Logic
- Only one coupon at a time
- Doesn't auto-suggest the best deal
- Returns all applicable coupons but doesn't rank them
- No margin protection (could theoretically discount below cost)

### BxGy Specifics
- Uses a greedy algorithm (might not always be mathematically optimal)
- Always picks the cheapest items to make free
- Free products must already be in the cart
- Doesn't handle fractional quantities
- Assumes same product = same price throughout cart

### Technical Stuff
- No caching (everything hits the database)
- No API rate limiting
- No pagination for listing coupons
- Permanent deletes (no soft delete)
- Single currency only

## Assumptions I Made

### Pricing
- Everything's in the same currency (eg. INR)
- All prices are positive
- Prices represent per-unit cost
- Tax not included
- Two decimal places max

### Carts
- Cart data comes with every request (no server-side storage because we don't have complete required schema)
- Product IDs are valid
- Quantities are positive whole numbers
- Each product appears only once per cart
- Same product = same price in one cart

### Coupons
- Coupon codes are unique
- Case-sensitive ("DEAL10" ≠ "deal10")
- Manual activation/deactivation
- System doesn't auto-delete expired ones
- One coupon per request
- Valid from creation time (no future start dates)

### BxGy Logic
- Must have exact quantity from the "buy" list
- Free products need to be in cart already
- Always discounts the cheapest eligible items
- Whole units only (for example 2 but not 2.5 items)
- Counts complete sets only
- Any combo from buy array works (2 of Product X + 1 of Product Y = 3 total)

### API
- JSON only
- UTF-8 encoding
- No authentication required
- Following standard REST conventions

## How to Use the API

### Base URL
```
http://localhost:8080/api/v1
```

### Creating a Coupon
```http
POST /api/v1/coupons
Content-Type: application/json

{
  "code": "SAVE10",
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

### Getting All Coupons
```http
GET /api/v1/coupons
```

### Getting One Coupon
```http
GET /api/v1/coupons/{id}
```

### Updating a Coupon
```http
PUT /api/v1/coupons/{id}
Content-Type: application/json

{
  "isActive": false,
  "description": "Updated description"
}
```

### Deleting a Coupon
```http
DELETE /api/v1/coupons/{id}
```

### Finding Applicable Coupons
```http
POST /api/v1/applicable-coupons
Content-Type: application/json

{
  "cart": {
    "items": [
      {
        "productId": 1,
        "quantity": 2,
        "price": 50.0
      },
      {
        "productId": 2,
        "quantity": 1,
        "price": 30.0
      }
    ]
  }
}
```

### Applying a Coupon
```http
POST /api/v1/apply-coupon/{id}
Content-Type: application/json

{
  "cart": {
    "items": [
      {
        "productId": 1,
        "quantity": 2,
        "price": 50.0
      }
    ]
  }
}
```

## Example Coupon Configs

### Percentage Off Cart
```json
{
  "code": "CART10",
  "type": "CART_WISE",
  "description": "10% off on orders above ₹100",
  "details": {
    "threshold": 100.0,
    "discount": 10.0,
    "discountType": "PERCENTAGE",
    "maxDiscount": 50.0,
    "minItems": 2
  },
  "expirationDate": "2025-12-31T23:59:59",
  "isActive": true
}
```

### Flat Discount
```json
{
  "code": "FLAT50",
  "type": "CART_WISE",
  "description": "₹50 flat off on orders above ₹500",
  "details": {
    "threshold": 500.0,
    "discount": 50.0,
    "discountType": "FIXED"
  },
  "expirationDate": "2025-12-31T23:59:59",
  "isActive": true
}
```

### Product Discount
```json
{
  "code": "PRODUCT20",
  "type": "PRODUCT_WISE",
  "description": "20% off on Product 1",
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

### Buy 2, Get 1 Free
```json
{
  "code": "B2G1FREE",
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

## Testing

### Run All Tests
```bash
mvn test
```

### Check Coverage
```bash
mvn test jacoco:report
# Open target/site/jacoco/index.html to see the report
```

### Run Specific Tests
```bash
mvn test -Dtest=CouponServiceTest
```

### Run Integration Tests
```bash
mvn verify
```

I aimed for pretty good coverage:
- Unit Tests: >80%
- Integration Tests: All endpoints
- Service Layer: >90%

## Project Structure

The code is organized pretty straightforwardly - controllers handle requests, services contain business logic, repositories talk to the database, and models define data structures. Tests mirror the main code structure.

## If I Had More Time

### Right Away
1. Add proper user authentication (Spring Security + JWT)
2. Track coupon usage in a dedicated table
3. Add Redis caching for better performance
4. Implement database indexing
5. Add API rate limiting

### Soon After
1. Category-based discounts
2. Coupon stacking with smart rules
3. Analytics dashboard
4. Bulk coupon generation
5. Export usage reports

## Documentation

Once you've got it running, check out:
- **Swagger UI**: http://localhost:8080/api/v1/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/api/v1/v3/api-docs

## Questions?

Feel free to reach out:
**Email**: misbah.almas@hotmail.com  
**Author**: Misbah Almas