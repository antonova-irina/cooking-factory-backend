# Cooking Factory – ZOD Validation Schemas

ZOD schemas aligned with the backend DTOs for use in a React + TypeScript frontend.

## Setup

```bash
cd frontend-schemas
npm install
```

Or copy `src/schemas.ts` into your React project and add `zod`:

```bash
npm install zod
```

## Usage

### React Hook Form

```tsx
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { CourseInsertSchema, type CourseInsert } from './schemas';

const form = useForm<CourseInsert>({
  resolver: zodResolver(CourseInsertSchema),
  defaultValues: { isActive: true, name: '', description: '', instructorId: null },
});
```

### Manual validation

```ts
import { StudentInsertSchema, AuthenticationRequestSchema } from './schemas';

const result = StudentInsertSchema.safeParse(formData);
if (result.success) {
  await api.createStudent(result.data);
} else {
  console.error(result.error.flatten());
}
```

### Parse API responses

```ts
import { PaginatedStudentSchema, AuthenticationResponseSchema } from './schemas';

const authData = AuthenticationResponseSchema.parse(await response.json());
const page = PaginatedStudentSchema.parse(await searchResponse.json());
```

## Schemas overview

| Schema | Use case |
|--------|----------|
| `AuthenticationRequestSchema` | Login form |
| `AuthenticationResponseSchema` | Parse auth response |
| `CourseInsertSchema` / `CourseUpdateSchema` | Create/update course forms |
| `InstructorInsertSchema` / `InstructorUpdateSchema` | Create/update instructor forms |
| `StudentInsertSchema` / `StudentUpdateSchema` | Create/update student forms |
| `CourseFiltersSchema` / `InstructorFiltersSchema` / `StudentFiltersSchema` | Search request body |
| `PaginatedCourseSchema` / `PaginatedInstructorSchema` / `PaginatedStudentSchema` | Search response |

## Notes

- **Date format**: `dateOfBirth` uses `yyyy-MM-dd`.
- **ContactDetailsReadOnly**: Backend returns `Email` and `PhoneNumber` (capital letters).
- **Filters**: `page`, `pageSize`, `sortBy`, `sortDirection` are optional; backend uses defaults.
