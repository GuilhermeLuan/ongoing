# Fix Category and Payment Method Display Bug

## Context

**Problem:** The subscription details view and edit form are showing "Sem categoria" and "Não informado" instead of the
actual category and payment method names.

**Root Cause:**

1. Backend returns only `categoryId` and `paymentMethodId` (numeric IDs) in the API response
2. Category and PaymentMethod relationships use `FetchType.LAZY` without JOIN FETCH, which can cause the IDs to be null
   when not properly loaded
3. Frontend has hardcoded arrays to map IDs to names, creating a fragile client-side dependency

**User's Chosen Approach:** Backend should return complete names (`categoryName`, `paymentMethodName`) directly in the
API response, eliminating the need for frontend ID-to-name mapping.

---

## Implementation Plan

### Backend Changes

#### 1. Add JOIN FETCH to Repository Queries

**File:** `backend/src/main/java/dev/guilhermeluan/ongoing/subscriptions/SubscriptionsRepository.java`

**Purpose:** Ensure LAZY relationships are loaded to prevent LazyInitializationException when accessing
category/paymentMethod names.

**Changes:**

```java
// Update findWithFilters (line 16)
@Query("""
        SELECT s FROM Subscriptions s
        LEFT JOIN FETCH s.category
        LEFT JOIN FETCH s.paymentMethod
        LEFT JOIN FETCH s.subscriptionType
        WHERE (:name IS NULL OR s.name ILIKE CONCAT('%', CAST(:name AS string), '%'))
        AND (:active IS NULL OR s.active = :active)
        AND (:categoryId IS NULL OR s.category.id = :categoryId)
        AND (s.user.id = :userId)
        """)
Page<Subscriptions> findWithFilters(...);

// Update findAllByUserId (line 30)
@Query("""
        SELECT s FROM Subscriptions s
        LEFT JOIN FETCH s.category
        LEFT JOIN FETCH s.paymentMethod
        LEFT JOIN FETCH s.subscriptionType
        WHERE s.user.id = :userId
        """)
Page<Subscriptions> findAllByUserId(Long userId, Pageable pageable);

// Update findByIdAndUserId (line 32)
@Query("""
        SELECT s FROM Subscriptions s
        LEFT JOIN FETCH s.category
        LEFT JOIN FETCH s.paymentMethod
        LEFT JOIN FETCH s.subscriptionType
        WHERE s.id = :id AND s.user.id = :userId
        """)
Optional<Subscriptions> findByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

// Update findActiveByUserId (line 34)
@Query("""
        SELECT s FROM Subscriptions s
        LEFT JOIN FETCH s.category
        LEFT JOIN FETCH s.paymentMethod
        LEFT JOIN FETCH s.subscriptionType
        WHERE s.user.id = :userId
        AND s.active = true
        """)
List<Subscriptions> findActiveByUserId(@Param("userId") Long userId);
```

**Why LEFT JOIN?** Because category and paymentMethod are nullable - we want to return subscriptions even when they
don't have these relationships set.

#### 2. Update Response DTO

**File:** `backend/src/main/java/dev/guilhermeluan/ongoing/subscriptions/dto/SubscriptionResponseDto.java`

**Add two new fields after line 23:**

```java
public record SubscriptionResponseDto(
        Long id,
        String name,
        String description,
        BigDecimal value,
        LocalDate startDate,
        LocalDate nextPaymentDate,
        Boolean active,
        Boolean notifyUser,
        Currency currency,
        String logoUrl,
        Long categoryId,
        Long paymentMethodId,
        BillingCycle billingCycle,
        Long subscriptionTypeId,
        String categoryName,        // NEW
        String paymentMethodName    // NEW
) {
}
```

**Field Types:** Both are `String` (nullable) - will be `null` when subscription has no category/payment method.

**Why keep IDs?** For backwards compatibility and form submissions (SubscriptionForm still needs IDs for `<Select>`
component values).

#### 3. Update Mapper

**File:** `backend/src/main/java/dev/guilhermeluan/ongoing/subscriptions/SubscriptionsMapper.java`

**Add two new mappings after line 20:**

```java

@Mapping(source = "notify", target = "notifyUser")
@Mapping(source = "category.id", target = "categoryId")
@Mapping(source = "paymentMethod.id", target = "paymentMethodId")
@Mapping(source = "subscriptionType.id", target = "subscriptionTypeId")
@Mapping(source = "category.name", target = "categoryName")          // NEW
@Mapping(source = "paymentMethod.name", target = "paymentMethodName")
    // NEW
SubscriptionResponseDto toSubscriptionResponse(Subscriptions subscription);
```

**Null Safety:** MapStruct automatically handles null-safe navigation (`category?.name`).

#### 4. Update Integration Tests

**File:** `backend/src/test/java/dev/guilhermeluan/ongoing/subscriptions/SubscriptionsControllerIT.java`

**Add assertions to verify new fields:**

```java
// In findAll_ShouldReturnAllSubscriptions test (after line 90)
assertThatJson(response).

node("content[0].categoryName").

isEqualTo("Video Streaming");

assertThatJson(response).

node("content[0].paymentMethodName").

isEqualTo("Credit Card");

// In findById_ShouldReturnOneSubscriptionById test (after line 107)
assertThatJson(response).

node("categoryName").

isNotNull();

assertThatJson(response).

node("paymentMethodName").

isNotNull();
```

### Frontend Changes

#### 5. Update TypeScript Types

**File:** `frontend/src/features/subscriptions/types/subscription.types.ts`

**Add two new fields to SubscriptionResponse interface after line 25:**

```typescript
export interface SubscriptionResponse {
    id: number;
    name: string;
    description: string | null;
    value: number;
    startDate: string;
    nextPaymentDate: string;
    active: boolean;
    notifyUser: boolean;
    currency: Currency;
    logoUrl: string | null;
    categoryId: number | null;
    paymentMethodId: number | null;
    billingCycle: BillingCycle;
    subscriptionTypeId: number | null;
    categoryName: string | null;        // NEW
    paymentMethodName: string | null;   // NEW
}
```

#### 6. Update Utility Functions

**File:** `frontend/src/features/subscriptions/utils/subscription.utils.ts`

**Replace getCategoryName function (lines 145-149):**

```typescript
/**
 * Get category name from subscription.
 * Prefers categoryName from backend, falls back to ID lookup for backwards compatibility.
 */
export const getCategoryName = (subscription: { categoryId: number | null; categoryName?: string | null }): string => {
        // Prefer backend-provided name (source of truth)
        if (subscription.categoryName) return subscription.categoryName;

        // Fallback to ID lookup (backwards compatibility)
        if (!subscription.categoryId) return "Sem categoria";
        const category = categoryOptions.find(c => c.value === subscription.categoryId.toString());
        return category?.label ?? "Sem categoria";
    };
```

**Replace getPaymentMethodName function (lines 155-159):**

```typescript
/**
 * Get payment method name from subscription.
 * Prefers paymentMethodName from backend, falls back to ID lookup for backwards compatibility.
 */
export const getPaymentMethodName = (subscription: {
        paymentMethodId: number | null;
        paymentMethodName?: string | null
    }): string => {
        // Prefer backend-provided name (source of truth)
        if (subscription.paymentMethodName) return subscription.paymentMethodName;

        // Fallback to ID lookup (backwards compatibility)
        if (!subscription.paymentMethodId) return "Não informado";
        const method = paymentMethodOptions.find(m => m.value === subscription.paymentMethodId.toString());
        return method?.label ?? "Não informado";
    };
```

**Strategy:** Accept full subscription object instead of just ID. Prefer backend-provided names but fall back to ID
lookup for transition period.

#### 7. Update SubscriptionDetailsView Component

**File:** `frontend/src/features/subscriptions/components/SubscriptionDetailsView.tsx`

**Update function calls to pass full subscription object:**

```typescript
// Line 123 (Category card)
{
    getCategoryName(subscription)
}

// Line 151 (Category detail row)
{
    getCategoryName(subscription)
}

// Line 164 (Payment method detail row)
{
    getPaymentMethodName(subscription)
}
```

**Search for other usages:**

```bash
cd frontend
grep -rn "getCategoryName\|getPaymentMethodName" src/
```

Update any other components (SubscriptionCard, etc.) to pass full subscription object.

---

## Verification Steps

### Backend Verification

1. **Rebuild backend to regenerate MapStruct implementation:**
   ```bash
   cd backend
   ./mvnw clean compile
   ```

2. **Verify generated mapper includes new fields:**
   ```bash
   cat target/generated-sources/annotations/dev/guilhermeluan/ongoing/subscriptions/SubscriptionsMapperImpl.java | grep -A5 "categoryName\|paymentMethodName"
   ```

3. **Run tests:**
   ```bash
   ./mvnw verify
   ```

4. **Start backend and test endpoint:**
   ```bash
   ./mvnw spring-boot:run
   ```

   Then test: `GET http://localhost:6969/api/v1/subscriptions`

   **Expected JSON:**
   ```json
   {
     "content": [{
       "id": 1,
       "name": "Netflix",
       "categoryId": 1,
       "categoryName": "Video Streaming",
       "paymentMethodId": 1,
       "paymentMethodName": "Credit Card",
       ...
     }]
   }
   ```

### Frontend Verification

5. **Rebuild frontend:**
   ```bash
   cd frontend
   npm run build
   ```

6. **Start frontend dev server:**
   ```bash
   npm run dev
   ```

7. **Manual browser testing:**
    - Open browser DevTools (Network tab)
    - Navigate to subscriptions page
    - Verify API response contains `categoryName` and `paymentMethodName` fields
    - Click on a subscription to view details
    - **Expected:** Shows "Video Streaming" instead of "Sem categoria"
    - **Expected:** Shows "Credit Card" instead of "Não informado"

8. **Test null handling:**
    - Create a subscription without category/payment method
    - **Expected:** Still shows "Sem categoria" and "Não informado" (graceful fallback)

9. **Test form functionality:**
    - Create new subscription → verify it saves and displays correctly
    - Edit existing subscription → verify category/payment method update correctly

---

## Design Considerations

### Why This Approach?

1. **Single Source of Truth:** Database is the authoritative source for names, not frontend hardcoded arrays
2. **No Language Mismatch:** Frontend currently has Portuguese payment method names while database has English - sending
   from backend eliminates this
3. **Easier Maintenance:** Adding new categories/payment methods only requires database insert, not frontend code
   changes
4. **Performance:** Small overhead (2 extra string fields), but eliminates potential bugs from ID mismatches

### Backwards Compatibility

- Keep both ID and name fields in response
- Frontend prefers names but falls back to ID lookup
- No breaking changes to existing API consumers
- Form submissions still use IDs (unchanged)

### Null Handling

- When `category` is null → both `categoryId` and `categoryName` are null
- When `paymentMethod` is null → both `paymentMethodId` and `paymentMethodName` are null
- Frontend shows appropriate fallback messages

---

## Critical Files Modified

**Backend (4 files):**

1. `backend/src/main/java/dev/guilhermeluan/ongoing/subscriptions/SubscriptionsRepository.java` - Add JOIN FETCH to
   prevent LAZY loading issues
2. `backend/src/main/java/dev/guilhermeluan/ongoing/subscriptions/dto/SubscriptionResponseDto.java` - Add name fields to
   DTO
3. `backend/src/main/java/dev/guilhermeluan/ongoing/subscriptions/SubscriptionsMapper.java` - Map names from entities
4. `backend/src/test/java/dev/guilhermeluan/ongoing/subscriptions/SubscriptionsControllerIT.java` - Update test
   assertions

**Frontend (3 files):**

1. `frontend/src/features/subscriptions/types/subscription.types.ts` - Add name fields to interface
2. `frontend/src/features/subscriptions/utils/subscription.utils.ts` - Update utility functions to use names
3. `frontend/src/features/subscriptions/components/SubscriptionDetailsView.tsx` - Pass full subscription object

---

## Potential Pitfalls

### 1. MapStruct Not Regenerating

**Symptom:** Fields are null even after adding mappings

**Solution:**

```bash
cd backend
./mvnw clean compile  # Force regeneration
```

### 2. LazyInitializationException

**Symptom:** `could not initialize proxy - no Session`

**Solution:** Ensure JOIN FETCH is added to all repository queries (already in plan)

### 3. TypeScript Compilation Errors

**Symptom:** Type errors when passing subscription object

**Solution:** Verify backend is running with updated code, restart Next.js dev server, clear browser cache

### 4. Frontend Shows English Names Instead of Portuguese

**Context:** Database has English names ("Credit Card") but UI might want Portuguese

**Short-term:** Accept English names (minimal change)

**Long-term options:**

- Update database migration to use Portuguese names
- Add `name_pt` column for translations
- Frontend translation layer (not recommended - defeats purpose of this solution)

---

## Success Criteria

✅ Backend API response includes `categoryName` and `paymentMethodName` fields
✅ All backend integration tests pass
✅ Frontend TypeScript compiles without errors
✅ Subscription details view shows actual category name (not "Sem categoria")
✅ Subscription details view shows actual payment method name (not "Não informado")
✅ Null handling works: subscriptions without category/payment still show fallback messages
✅ Form functionality unchanged: creating and editing subscriptions still works
✅ No console errors in browser
