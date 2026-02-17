/**
 * User Utilities
 * 
 * Helper functions for user data manipulation
 */

/**
 * Extract first name from full name
 * 
 * @param fullName - Full name (e.g., "João Gomes")
 * @returns First name only (e.g., "João")
 * 
 * @example
 * getFirstName("João Gomes") // returns "João"
 * getFirstName("Maria") // returns "Maria"
 * getFirstName("") // returns ""
 * getFirstName(null) // returns ""
 */
export function getFirstName(fullName: string | null | undefined): string {
  if (!fullName) {
    return '';
  }

  const trimmed = fullName.trim();
  const parts = trimmed.split(/\s+/); // Split by one or more whitespace characters
  
  return parts[0] || '';
}
