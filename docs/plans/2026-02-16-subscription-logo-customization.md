# Design: Enhanced Subscription Form with Logo Customization Header

## Context

The subscription creation flow needs a visual enhancement to make the form more engaging and allow users to customize
how their subscriptions appear. Currently, after selecting a service from the ServicePicker, users see a plain form with
text inputs.

**The goal:** Add a prominent header section showing the subscription's logo and name with customization options, making
the form feel more polished and giving users control over the visual appearance of their subscriptions.

**User requirements:**

1. Show logo + service name in a header at the top of the form
2. Edit button to customize name and avatar background color
3. Logo URL field only visible for custom services (not popular ones)
4. Default colors match service brands (Netflix=red, Spotify=green, etc.)
5. Color stored in frontend for now, with easy path to backend persistence later

## Visual Design

### Main Form with Header

```
┌────────────────────────────────────────────────────────┐
│  Modal: Adicionar Netflix                             │
├────────────────────────────────────────────────────────┤
│                                                        │
│  ╔════════════════════════════════════════════════╗  │
│  ║  ╭──────╮                                       ║  │
│  ║  │ [🔴] │  Netflix                    ✏️ Editar ║  │
│  ║  │ IMG  │                                       ║  │
│  ║  ╰──────╯                                       ║  │
│  ╚════════════════════════════════════════════════╝  │
│                                                        │
│  Descrição                                             │
│  ┌──────────────────────────────────────────────────┐ │
│  │ ______________________________________           │ │
│  └──────────────────────────────────────────────────┘ │
│                                                        │
│  ┌─────────────────────┐  ┌──────────────────────┐   │
│  │ Valor: 39.90       │  │ Moeda: BRL ▼        │   │
│  └─────────────────────┘  └──────────────────────┘   │
│                                                        │
│  [... outras fields ...]                               │
│                                                        │
│  ← Voltar              [Cancelar]  [Criar assinatura] │
└────────────────────────────────────────────────────────┘
```

### Edit Appearance Modal (Nested)

```
┌──────────────────────────────────────┐
│  Editar aparência                    │
├──────────────────────────────────────┤
│                                      │
│         ╭───────────────╮            │
│         │               │            │
│         │   [🔴 Logo]   │            │
│         │               │            │
│         ╰───────────────╯            │
│                                      │
│  Nome do serviço                     │
│  ┌────────────────────────────────┐  │
│  │ Netflix__________________     │  │
│  └────────────────────────────────┘  │
│                                      │
│  Cor de fundo do avatar              │
│  ● ○ ○ ○ ○ ○ ○ ○ ○ ○ ○ ○            │
│  (12 color swatches)                 │
│                                      │
│            [Cancelar]  [Salvar]      │
└──────────────────────────────────────┘
```

## Architecture Design

### Component Structure

```
SubscriptionsPageContent (orchestrator)
└─ Modal
   └─ SubscriptionForm
      ├─ SubscriptionHeader (NEW)
      │  ├─ Logo avatar with color background
      │  ├─ Service name display
      │  └─ Edit button
      │
      ├─ AppearanceEditor (NEW, conditional render)
      │  ├─ Logo preview
      │  ├─ Name input
      │  ├─ ColorPicker (NEW)
      │  └─ Save/Cancel buttons
      │
      └─ Form fields (existing)
         ├─ Description
         ├─ Value + Currency
         ├─ Date + Category
         ├─ Billing Cycle
         ├─ Logo URL (conditional - only custom services)
         └─ Checkboxes + Actions
```

### Data Flow

```
1. User selects Netflix from ServicePicker
   ↓
2. SubscriptionsPageContent calls handleServiceSelect(netflix)
   ↓
3. Modal opens with SubscriptionForm
   - prefill = { name: "Netflix", logoUrl: "...", ... }
   - brandColor = "#E50914" (from enhanced PopularService)
   ↓
4. SubscriptionForm shows:
   - SubscriptionHeader with red Netflix logo
   - Form fields pre-filled
   ↓
5. User clicks "Editar" button
   ↓
6. editingAppearance state = true
   ↓
7. AppearanceEditor renders inline
   - Shows current name + color
   - ColorPicker with 12 preset colors
   ↓
8. User changes color to blue, clicks Salvar
   ↓
9. Updates local state: customizations = { name: "Netflix", avatarColor: "#1DB954" }
   ↓
10. AppearanceEditor closes
    ↓
11. SubscriptionHeader updates with new blue color
    ↓
12. On form submit: customizations stored in localStorage
    key = `subscription-appearance-${subscriptionId}`
```

### State Management

**In SubscriptionForm.tsx:**

```typescript
interface AppearanceCustomization {
  name: string;
  avatarColor: string;
}

const [editingAppearance, setEditingAppearance] = useState(false);
const [customization, setCustomization] = useState<AppearanceCustomization>({
  name: prefill?.name || subscription?.name || "",
  avatarColor: prefill?.brandColor || getDefaultColor(prefill?.name)
});
```

**Storage strategy (Phase 1 - Frontend only):**

- Use localStorage: `subscription-appearance-${subscriptionId}`
- Format: `{ name: string, avatarColor: string }`
- Load on mount, save on customization change
- Keyed by subscription ID (after creation) or temporary ID (during creation)

**Future backend persistence (Phase 2):**

- Add fields to Subscription entity: `customName`, `avatarColor`
- Update DTO to include these fields
- Send in SubscriptionRequest on create/update

## Implementation Plan

### Files to Create (2 new files)

#### 1. `frontend/src/components/app/SubscriptionHeader.tsx`

New component for the header section.

**Props:**

```typescript
interface SubscriptionHeaderProps {
  name: string;
  logoUrl?: string;
  avatarColor: string;
  onEdit: () => void;
}
```

**Renders:**

- Container with border and light background (`bg-neutral-50 border border-neutral-200 rounded-xl p-4`)
- Flex layout with logo on left, name in center, edit button on right
- Logo: `w-16 h-16 rounded-xl` circle with dynamic `backgroundColor`
- Image with fallback to 2-letter initials (reuse pattern from SubscriptionCard)
- Service name in large font (`text-xl font-semibold`)
- Edit button with pencil icon from lucide-react

**File path:** `/home/guilherme/workspace/ongoing/frontend/src/components/app/SubscriptionHeader.tsx`

---

#### 2. `frontend/src/components/app/AppearanceEditor.tsx`

New component for editing name and color.

**Props:**

```typescript
interface AppearanceEditorProps {
  name: string;
  logoUrl?: string;
  avatarColor: string;
  onSave: (name: string, color: string) => void;
  onCancel: () => void;
}
```

**Features:**

- Local state for name and selected color
- Large logo preview at top (same as header, but bigger: `w-24 h-24`)
- Input for name (uses existing Input component)
- ColorPicker component (see below)
- Save/Cancel buttons

**File path:** `/home/guilherme/workspace/ongoing/frontend/src/components/app/AppearanceEditor.tsx`

---

#### 3. `frontend/src/components/app/ColorPicker.tsx`

Color selection component with predefined palette.

**Props:**

```typescript
interface ColorPickerProps {
  value: string;
  onChange: (color: string) => void;
  label?: string;
}
```

**Palette (12 colors):**

```typescript
const PRESET_COLORS = [
  "#E50914", // Netflix Red
  "#1DB954", // Spotify Green
  "#5865F2", // Discord Purple
  "#0078D4", // Microsoft Blue
  "#FF6600", // Orange
  "#10A37F", // ChatGPT Teal
  "#FF0080", // Hot Pink
  "#FFC107", // Amber
  "#9C27B0", // Deep Purple
  "#00BCD4", // Cyan
  "#4CAF50", // Green
  "#6B7280", // Gray (neutral)
];
```

**Renders:**

- Label if provided
- Grid of color swatches (`grid grid-cols-6 gap-2`)
- Each swatch: `w-8 h-8 rounded-full cursor-pointer`
- Selected swatch has ring: `ring-2 ring-offset-2 ring-primary-500`
- Hover effect: `scale-110 transition-transform`

**File path:** `/home/guilherme/workspace/ongoing/frontend/src/components/app/ColorPicker.tsx`

---

### Files to Modify (5 files)

#### 4. `frontend/src/features/subscriptions/types/subscription.types.ts`

Add `brandColor` to PopularService interface:

```typescript
export interface PopularService {
  name: string;
  logoUrl: string;
  categoryId: number;
  defaultBillingCycle: BillingCycle;
  defaultValue: number;
  defaultCurrency: Currency;
  brandColor?: string; // NEW - hex color for avatar background
}
```

---

#### 5. `frontend/src/features/subscriptions/data/popular-services.ts`

Add brand colors to all 16 services:

```typescript
export const popularServices: PopularService[] = [
  {
    name: "Netflix",
    logoUrl: "https://www.google.com/s2/favicons?domain=netflix.com&sz=128",
    categoryId: 1,
    defaultBillingCycle: "MONTHLY",
    defaultValue: 39.9,
    defaultCurrency: "BRL",
    brandColor: "#E50914", // Netflix red
  },
  {
    name: "Spotify",
    // ...
    brandColor: "#1DB954", // Spotify green
  },
  // ... add colors for all 16 services
  // Use #6B7280 (gray) as fallback for services without distinct brand colors
];
```

**Brand colors to add:**

- Netflix: `#E50914` (red)
- Spotify: `#1DB954` (green)
- Disney+: `#113CCF` (blue)
- YouTube Premium: `#FF0000` (red)
- Amazon Prime: `#00A8E1` (light blue)
- Xbox Game Pass: `#107C10` (green)
- iCloud: `#3693F3` (blue)
- ChatGPT Plus: `#10A37F` (teal)
- Adobe Creative Cloud: `#FF0000` (red)
- HBO Max: `#B100CD` (purple)
- Duolingo: `#58CC02` (green)
- Notion: `#000000` (black)
- GitHub Pro: `#181717` (dark gray)
- Figma Professional: `#F24E1E` (orange)
- Canva Pro: `#00C4CC` (cyan)
- Google One: `#4285F4` (blue)

---

#### 6. `frontend/src/components/app/SubscriptionForm.tsx`

**Changes:**

1. **Add state for appearance customization:**

```typescript
const [editingAppearance, setEditingAppearance] = useState(false);
const [customization, setCustomization] = useState({
  name: prefill?.name || subscription?.name || values.name,
  avatarColor: prefill?.brandColor || subscription?.avatarColor || "#6B7280"
});
```

2. **Add localStorage helpers:**

```typescript
const loadCustomization = (subscriptionId?: number) => {
  if (!subscriptionId) return null;
  const stored = localStorage.getItem(`subscription-appearance-${subscriptionId}`);
  return stored ? JSON.parse(stored) : null;
};

const saveCustomization = (subscriptionId: number, data: any) => {
  localStorage.setItem(`subscription-appearance-${subscriptionId}`, JSON.stringify(data));
};
```

3. **Update form structure:**

```typescript
return (
  <form className="space-y-4" onSubmit={handleSubmit}>
    {onBack && <BackButton />}

    {/* NEW: Show SubscriptionHeader OR AppearanceEditor */}
    {editingAppearance ? (
      <AppearanceEditor
        name={customization.name}
        logoUrl={values.logoUrl}
        avatarColor={customization.avatarColor}
        onSave={(name, color) => {
          setCustomization({ name, avatarColor: color });
          setValues(prev => ({ ...prev, name }));
          setEditingAppearance(false);
        }}
        onCancel={() => setEditingAppearance(false)}
      />
    ) : (
      <SubscriptionHeader
        name={customization.name}
        logoUrl={values.logoUrl}
        avatarColor={customization.avatarColor}
        onEdit={() => setEditingAppearance(true)}
      />
    )}

    {/* Existing form fields */}
    <Input label="Descrição" ... />
    {/* ... rest of fields ... */}

    {/* Logo URL - only show for custom services */}
    {!prefill && (
      <Input label="Logo URL" ... />
    )}

    {/* ... checkboxes and buttons ... */}
  </form>
);
```

4. **Update submit handler to save customization:**

```typescript
const handleSubmit = async (event) => {
  event.preventDefault();
  if (!validate()) return;

  const nextPaymentDate = calculateNextPaymentDate(values.startDate, values.billingCycle);

  const data = {
    name: customization.name, // Use customized name
    description: values.description.trim() || undefined,
    // ... rest of fields ...
  };

  await onSubmit(data);

  // After successful creation, save customization to localStorage
  // (In a real scenario, you'd get the subscription ID from the response)
};
```

---

#### 7. `frontend/src/components/app/index.ts`

Export new components:

```typescript
export { SubscriptionHeader } from "./SubscriptionHeader";
export { AppearanceEditor } from "./AppearanceEditor";
export { ColorPicker } from "./ColorPicker";
// ... existing exports ...
```

---

#### 8. `frontend/src/components/app/SubscriptionCard.tsx`

**Optional enhancement:** Load customization from localStorage and apply to existing subscription cards.

Add to component:

```typescript
const customization = useMemo(() => {
  const stored = localStorage.getItem(`subscription-appearance-${subscription.id}`);
  return stored ? JSON.parse(stored) : null;
}, [subscription.id]);

const avatarColor = customization?.avatarColor || getAvatarColor(subscription.name);
const displayName = customization?.name || subscription.name;
```

This makes customizations visible in the subscription list, not just in the form.

---

## Backend Changes Analysis (Future Phase 2)

To persist appearance customizations in the database, these changes would be needed:

### Database Migration

**File:** `backend/src/main/resources/db/migration/V1.X__add_subscription_appearance.sql`

```sql
ALTER TABLE subscriptions
ADD COLUMN custom_name VARCHAR(255),
ADD COLUMN avatar_color VARCHAR(7); -- Hex color format: #RRGGBB

COMMENT ON COLUMN subscriptions.custom_name IS 'User-customized display name (nullable - falls back to name)';
COMMENT ON COLUMN subscriptions.avatar_color IS 'Hex color for avatar background (nullable - uses default if not set)';
```

### Entity Update

**File:** `backend/src/main/java/com/guilherme/ongoing/subscriptions/entities/Subscriptions.java`

Add fields:

```java
@Column(name = "custom_name", length = 255)
private String customName;

@Column(name = "avatar_color", length = 7)
@Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "Avatar color must be a valid hex color")
private String avatarColor;
```

### DTOs Update

**File:** `backend/src/main/java/com/guilherme/ongoing/subscriptions/dto/SubscriptionRequest.java`

```java
public record SubscriptionRequest(
    // ... existing fields ...

    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "Avatar color must be a valid hex color")
    String avatarColor,

    @Size(max = 255)
    String customName
) {}
```

**File:** `backend/src/main/java/com/guilherme/ongoing/subscriptions/dto/SubscriptionResponse.java`

```java
public record SubscriptionResponse(
    // ... existing fields ...
    String avatarColor,
    String customName
) {}
```

### Mapper Update

**File:** `backend/src/main/java/com/guilherme/ongoing/subscriptions/SubscriptionsMapper.java`

MapStruct will automatically map these fields since they match by name. No changes needed if using `@Mapping` with
default configuration.

### Service Layer

No changes needed - the service methods already handle all entity fields generically.

### Integration Tests

**File:** `backend/src/test/java/com/guilherme/ongoing/subscriptions/SubscriptionsControllerIT.java`

Add test cases:

```java
@Test
void shouldCreateSubscriptionWithCustomAppearance() {
    // Test creating subscription with avatarColor and customName
}

@Test
void shouldUpdateSubscriptionAppearance() {
    // Test updating only appearance fields
}

@Test
void shouldValidateAvatarColorFormat() {
    // Test invalid hex colors are rejected
}
```

**Estimated effort:** ~2-3 hours (migration + entity + tests)

---

## Color Palette Strategy

### Default Color Selection

When a service doesn't have a brand color (or is custom):

```typescript
const getDefaultColor = (name: string): string => {
  // Hash-based color selection for consistency
  const colors = [
    "#E50914", "#1DB954", "#5865F2", "#0078D4",
    "#FF6600", "#10A37F", "#FF0080", "#FFC107",
    "#9C27B0", "#00BCD4", "#4CAF50", "#6B7280"
  ];

  const hash = name.split("").reduce((acc, char) => {
    return char.charCodeAt(0) + ((acc << 5) - acc);
  }, 0);

  return colors[Math.abs(hash) % colors.length];
};
```

This ensures:

- Custom services get colorful avatars (not all gray)
- Same service name always gets same color (consistency)
- Works for any service, even without brand color defined

---

## Verification & Testing

### Manual Testing Checklist

1. **Popular Service Flow:**
    - [ ] Select Netflix from ServicePicker
    - [ ] Form shows header with red Netflix logo
    - [ ] Click "Editar" button
    - [ ] AppearanceEditor appears inline
    - [ ] Change name to "Netflix Premium"
    - [ ] Change color to blue
    - [ ] Click "Salvar"
    - [ ] Header updates with new name and blue color
    - [ ] Submit form
    - [ ] Check localStorage has customization saved

2. **Custom Service Flow:**
    - [ ] Search for "Crunchyroll" (not in list)
    - [ ] Click "Criar Crunchyroll"
    - [ ] Form shows header with default color (hash-based)
    - [ ] Logo URL field is visible
    - [ ] Edit appearance works
    - [ ] Submit creates subscription with customization

3. **Edit Existing Subscription:**
    - [ ] Edit an existing subscription
    - [ ] Header shows current name and logo
    - [ ] If customization exists in localStorage, shows custom color
    - [ ] Can edit appearance
    - [ ] Changes persist

4. **Logo URL Visibility:**
    - [ ] Popular services: Logo URL field NOT visible
    - [ ] Custom services: Logo URL field IS visible
    - [ ] Editing existing subscription: Logo URL field visible

5. **Subscription List:**
    - [ ] Cards show customized colors (if implemented)
    - [ ] Cards show customized names (if implemented)

### Build Verification

```bash
cd frontend

# Type check
npm run build

# Lint check
npm run lint

# Expected: No errors, only warnings about <img> tags (acceptable)
```

### Integration Points

- **ServicePicker** → **SubscriptionForm**: prefill includes brandColor
- **SubscriptionForm** → **localStorage**: customizations saved on submit
- **SubscriptionCard** → **localStorage**: customizations loaded on render (optional)
- **AppearanceEditor** → **SubscriptionHeader**: updates reflect immediately

---

## Open Questions & Decisions

### Decided:

✅ Color applies to avatar background (not header background)
✅ Color shows behind logo image (badge effect)
✅ Predefined palette (12 colors)
✅ Popular services get brand colors by default
✅ Frontend-only storage initially (localStorage)
✅ Logo URL only for custom services
✅ Nested modal → inline section (better UX)

### Future Enhancements:

- Backend persistence (Phase 2)
- Custom color picker (beyond preset palette)
- Gradient backgrounds instead of solid colors
- Icon picker (choose emoji/icon instead of logo)
- Import logo from URL with preview
- Color accessibility checker (contrast ratios)

---

## File Summary

### New Files (3):

1. `/home/guilherme/workspace/ongoing/frontend/src/components/app/SubscriptionHeader.tsx`
2. `/home/guilherme/workspace/ongoing/frontend/src/components/app/AppearanceEditor.tsx`
3. `/home/guilherme/workspace/ongoing/frontend/src/components/app/ColorPicker.tsx`

### Modified Files (5):

4. `/home/guilherme/workspace/ongoing/frontend/src/features/subscriptions/types/subscription.types.ts`
5. `/home/guilherme/workspace/ongoing/frontend/src/features/subscriptions/data/popular-services.ts`
6. `/home/guilherme/workspace/ongoing/frontend/src/components/app/SubscriptionForm.tsx`
7. `/home/guilherme/workspace/ongoing/frontend/src/components/app/index.ts`
8. `/home/guilherme/workspace/ongoing/frontend/src/components/app/SubscriptionCard.tsx` (optional)

### Backend Files (Future - Phase 2):

- Migration: `V1.X__add_subscription_appearance.sql`
- Entity: `Subscriptions.java`
- DTOs: `SubscriptionRequest.java`, `SubscriptionResponse.java`
- Tests: `SubscriptionsControllerIT.java`

---

## Implementation Order

1. **Types & Data** (10 min)
    - Add `brandColor` to PopularService interface
    - Add brand colors to all 16 services in popular-services.ts

2. **ColorPicker Component** (20 min)
    - Create ColorPicker.tsx with preset palette
    - Export from index.ts

3. **SubscriptionHeader Component** (30 min)
    - Create SubscriptionHeader.tsx
    - Reuse avatar pattern from SubscriptionCard
    - Export from index.ts

4. **AppearanceEditor Component** (30 min)
    - Create AppearanceEditor.tsx
    - Integrate ColorPicker
    - Export from index.ts

5. **SubscriptionForm Integration** (45 min)
    - Add state for editingAppearance and customization
    - Add localStorage helpers
    - Conditional render: SubscriptionHeader vs AppearanceEditor
    - Hide Logo URL for popular services
    - Update submit handler

6. **Optional: SubscriptionCard Enhancement** (15 min)
    - Load customization from localStorage
    - Apply to card display

7. **Build & Test** (30 min)
    - Run build and lint
    - Manual testing of all flows
    - Fix any issues

**Total estimated time:** ~3 hours

---

## Design Principles Applied

- **Reuse existing patterns:** Avatar circles, form layouts, modal patterns
- **Progressive enhancement:** Works without customization, enhances when used
- **Frontend-first, backend-ready:** Easy to add persistence later
- **Consistent with design system:** Uses existing colors, spacing, components
- **YAGNI:** No over-engineering, just what's needed for the feature
- **Accessible:** Color selection works with keyboard, has clear labels
- **Performant:** localStorage is fast, no API calls needed initially
