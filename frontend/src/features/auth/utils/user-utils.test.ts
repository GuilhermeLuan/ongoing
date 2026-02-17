/**
 * User Utilities - Tests
 */

import { getFirstName } from './user-utils';

describe('getFirstName', () => {
  it('should extract first name from full name', () => {
    expect(getFirstName('João Gomes')).toBe('João');
  });

  it('should return single name as-is', () => {
    expect(getFirstName('Maria')).toBe('Maria');
  });

  it('should handle multiple spaces', () => {
    expect(getFirstName('Pedro   Silva   Santos')).toBe('Pedro');
  });

  it('should handle leading/trailing spaces', () => {
    expect(getFirstName('  Ana Costa  ')).toBe('Ana');
  });

  it('should return empty string for null', () => {
    expect(getFirstName(null)).toBe('');
  });

  it('should return empty string for undefined', () => {
    expect(getFirstName(undefined)).toBe('');
  });

  it('should return empty string for empty string', () => {
    expect(getFirstName('')).toBe('');
  });

  it('should return empty string for whitespace-only string', () => {
    expect(getFirstName('   ')).toBe('');
  });

  it('should handle names with special characters', () => {
    expect(getFirstName('José-Paulo da Silva')).toBe('José-Paulo');
  });

  it('should handle accented characters', () => {
    expect(getFirstName('Ângela Martins')).toBe('Ângela');
  });
});
