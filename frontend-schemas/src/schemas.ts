/**
 * ZOD validation schemas for Cooking Factory backend entities.
 * Use with React Hook Form: resolver: zodResolver(schema)
 * Or validate manually: schema.parse(data)
 *
 * Install: npm install zod
 */

import { z } from 'zod';

// =============================================================================
// Enums
// =============================================================================

export const GenderSchema = z.enum(['MALE', 'FEMALE', 'OTHER']);
export type Gender = z.infer<typeof GenderSchema>;

export const RoleSchema = z.enum(['ADMIN', 'INSTRUCTOR']);
export type Role = z.infer<typeof RoleSchema>;

export const SortDirectionSchema = z.enum(['ASC', 'DESC']).optional();
export type SortDirection = z.infer<typeof SortDirectionSchema>;

// =============================================================================
// Shared / Reusable
// =============================================================================

export const ContactDetailsInsertSchema = z.object({
  city: z.string().min(1, 'City is required'),
  street: z.string().optional(),
  streetNumber: z.string().optional(),
  postalCode: z.string().optional(),
  email: z.string().email('Invalid email').min(1, 'Email is required'),
  phoneNumber: z.string().min(1, 'Phone number is required'),
});
export type ContactDetailsInsert = z.infer<typeof ContactDetailsInsertSchema>;

// Backend ContactDetailsReadOnlyDTO uses "Email" and "PhoneNumber" (capital) in JSON
export const ContactDetailsReadOnlySchema = z.object({
  id: z.number(),
  city: z.string(),
  street: z.string().nullable(),
  streetNumber: z.string().nullable(),
  postalCode: z.string().nullable(),
  Email: z.string(),
  PhoneNumber: z.string(),
});
export type ContactDetailsReadOnly = z.infer<typeof ContactDetailsReadOnlySchema>;

export const ContactDetailsUpdateSchema = z.object({
  id: z.number(),
  city: z.string().min(1, 'City is required'),
  street: z.string().optional(),
  streetNumber: z.string().optional(),
  postalCode: z.string().optional(),
  email: z.string().email('Invalid email').min(1, 'Email is required'),
  phoneNumber: z.string().min(1, 'Phone number is required'),
});
export type ContactDetailsUpdate = z.infer<typeof ContactDetailsUpdateSchema>;

// =============================================================================
// Authentication
// =============================================================================

export const AuthenticationRequestSchema = z.object({
  username: z.string().min(1, 'Username is required'),
  password: z.string().min(1, 'Password is required'),
});
export type AuthenticationRequest = z.infer<typeof AuthenticationRequestSchema>;

export const AuthenticationResponseSchema = z.object({
  access_token: z.string(),
  vat: z.string().nullable(),
  firstname: z.string().nullable(),
  lastname: z.string().nullable(),
  role: z.string(),
});
export type AuthenticationResponse = z.infer<typeof AuthenticationResponseSchema>;

// =============================================================================
// User (nested in Instructor)
// =============================================================================

const passwordRegex = /^(?=.*?[a-z])(?=.*?[A-Z])(?=.*?\d)(?=.*?[@#$!%&*]).{8,}$/;

export const UserInsertSchema = z.object({
  isActive: z.boolean(),
  username: z.string().min(1, 'Username is required'),
  password: z.string().min(1, 'Password is required').regex(passwordRegex, 'Password must have 8+ chars, upper, lower, digit, special'),
  role: RoleSchema,
  vat: z.string().regex(/^\d{9}$/, 'VAT must be 9 digits'),
});
export type UserInsert = z.infer<typeof UserInsertSchema>;

export const UserReadOnlySchema = z.object({
  id: z.number(),
  username: z.string(),
  role: RoleSchema,
  vat: z.string(),
});
export type UserReadOnly = z.infer<typeof UserReadOnlySchema>;

export const UserUpdateSchema = z.object({
  id: z.number(),
  isActive: z.boolean(),
  username: z.string().min(1, 'Username is required'),
  password: z
    .string()
    .regex(passwordRegex, 'Password must have 8+ chars, upper, lower, digit, special')
    .optional()
    .nullable(),
  role: RoleSchema,
  vat: z.string().regex(/^\d{9}$/, 'VAT must be 9 digits'),
});
export type UserUpdate = z.infer<typeof UserUpdateSchema>;

// =============================================================================
// Course
// =============================================================================

export const CourseInsertSchema = z.object({
  isActive: z.boolean(),
  name: z.string().min(1, 'Course name is required'),
  description: z.string().min(1, 'Description is required'),
  instructorId: z.number().nullable().optional(),
});
export type CourseInsert = z.infer<typeof CourseInsertSchema>;

export const CourseReadOnlySchema = z.object({
  id: z.number(),
  isActive: z.boolean(),
  name: z.string(),
  description: z.string(),
  instructorId: z.number().nullable(),
});
export type CourseReadOnly = z.infer<typeof CourseReadOnlySchema>;

export const CourseUpdateSchema = z.object({
  id: z.number(),
  isActive: z.boolean(),
  name: z.string().min(1, 'Course name is required'),
  description: z.string().min(1, 'Description is required'),
  instructorId: z.number().nullable().optional(),
});
export type CourseUpdate = z.infer<typeof CourseUpdateSchema>;

// =============================================================================
// Instructor
// =============================================================================

export const InstructorInsertSchema = z.object({
  isActive: z.boolean(),
  firstname: z.string().min(1, 'First name is required'),
  lastname: z.string().min(1, 'Last name is required'),
  identityNumber: z.string().min(1, 'Identity number is required'),
  gender: GenderSchema,
  userInsertDTO: UserInsertSchema,
  contactDetailsInsertDTO: ContactDetailsInsertSchema,
});
export type InstructorInsert = z.infer<typeof InstructorInsertSchema>;

export const InstructorReadOnlySchema = z.object({
  id: z.number(),
  uuid: z.string(),
  isActive: z.boolean(),
  firstname: z.string(),
  lastname: z.string(),
  identityNumber: z.string(),
  gender: GenderSchema,
  userReadOnlyDTO: UserReadOnlySchema,
  contactDetailsReadOnlyDTO: ContactDetailsReadOnlySchema,
});
export type InstructorReadOnly = z.infer<typeof InstructorReadOnlySchema>;

export const InstructorUpdateSchema = z.object({
  id: z.number(),
  uuid: z.string(),
  isActive: z.boolean(),
  firstname: z.string().min(1, 'First name is required'),
  lastname: z.string().min(1, 'Last name is required'),
  identityNumber: z.string().min(1, 'Identity number is required'),
  gender: GenderSchema,
  userUpdateDTO: UserUpdateSchema,
  contactDetailsUpdateDTO: ContactDetailsUpdateSchema,
});
export type InstructorUpdate = z.infer<typeof InstructorUpdateSchema>;

// =============================================================================
// Student
// =============================================================================

const vat9DigitsSchema = z.string().regex(/^\d{9}$/, 'VAT must be 9 digits');

export const StudentInsertSchema = z.object({
  isActive: z.boolean(),
  firstname: z.string().min(1, 'Firstname is required'),
  lastname: z.string().min(1, 'Lastname is required'),
  dateOfBirth: z.string().regex(/^\d{4}-\d{2}-\d{2}$/, 'Date must be yyyy-MM-dd'),
  vat: vat9DigitsSchema,
  identityNumber: z.string().min(1, 'Identity number is required'),
  gender: GenderSchema,
  contactDetailsInsertDTO: ContactDetailsInsertSchema,
  courseIds: z.array(z.number()).optional().nullable(), // can be omitted, null, or empty
});
export type StudentInsert = z.infer<typeof StudentInsertSchema>;

export const StudentReadOnlySchema = z.object({
  id: z.number(),
  uuid: z.string(),
  isActive: z.boolean(),
  firstname: z.string(),
  lastname: z.string(),
  dateOfBirth: z.string(),
  vat: z.string(),
  identityNumber: z.string(),
  gender: GenderSchema,
  contactDetailsReadOnlyDTO: ContactDetailsReadOnlySchema,
  courseIds: z.array(z.number()).nullable(),
});
export type StudentReadOnly = z.infer<typeof StudentReadOnlySchema>;

export const StudentUpdateSchema = z.object({
  id: z.number(),
  uuid: z.string(),
  isActive: z.boolean(),
  firstname: z.string().min(1, 'Firstname is required'),
  lastname: z.string().min(1, 'Lastname is required'),
  dateOfBirth: z.string().regex(/^\d{4}-\d{2}-\d{2}$/, 'Date must be yyyy-MM-dd'),
  vat: vat9DigitsSchema,
  identityNumber: z.string().min(1, 'Identity number is required'),
  gender: GenderSchema,
  contactDetailsUpdateDTO: ContactDetailsUpdateSchema,
  courseIds: z.array(z.number()).optional().nullable(),
});
export type StudentUpdate = z.infer<typeof StudentUpdateSchema>;

// =============================================================================
// Filters (for POST /search endpoints)
// =============================================================================

const baseFilterSchema = z.object({
  page: z.number().int().min(0).optional(),
  pageSize: z.number().int().min(1).optional(),
  sortBy: z.string().optional(),
  sortDirection: SortDirectionSchema.optional(),
}).passthrough(); // allow additional filter fields

export const CourseFiltersSchema = baseFilterSchema.extend({
  id: z.number().optional().nullable(),
  name: z.string().optional().nullable(),
  instructorId: z.number().optional().nullable(),
});
export type CourseFilters = z.infer<typeof CourseFiltersSchema>;

export const InstructorFiltersSchema = baseFilterSchema.extend({
  uuid: z.string().optional().nullable(),
  lastname: z.string().optional().nullable(),
});
export type InstructorFilters = z.infer<typeof InstructorFiltersSchema>;

export const StudentFiltersSchema = baseFilterSchema.extend({
  lastname: z.string().optional().nullable(),
  dateOfBirth: z.string().optional().nullable(),
  courseId: z.number().optional().nullable(),
  instructorUuid: z.string().optional().nullable(),
});
export type StudentFilters = z.infer<typeof StudentFiltersSchema>;

// =============================================================================
// Paginated response
// =============================================================================

export function paginatedSchema<T extends z.ZodType>(itemSchema: T) {
  return z.object({
    data: z.array(itemSchema),
    currentPage: z.number(),
    pageSize: z.number(),
    totalPages: z.number(),
    numberOfElements: z.number(),
    totalElements: z.number(),
  });
}

export const PaginatedCourseSchema = paginatedSchema(CourseReadOnlySchema);
export const PaginatedInstructorSchema = paginatedSchema(InstructorReadOnlySchema);
export const PaginatedStudentSchema = paginatedSchema(StudentReadOnlySchema);

export type PaginatedCourse = z.infer<typeof PaginatedCourseSchema>;
export type PaginatedInstructor = z.infer<typeof PaginatedInstructorSchema>;
export type PaginatedStudent = z.infer<typeof PaginatedStudentSchema>;

// =============================================================================
// Error / Response message
// =============================================================================

export const ResponseMessageSchema = z.object({
  code: z.string(),
  description: z.string().optional(),
});
export type ResponseMessage = z.infer<typeof ResponseMessageSchema>;
