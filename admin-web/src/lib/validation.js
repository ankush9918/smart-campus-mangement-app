/**
 * Standard email validation utilities.
 * Validates against standard email pattern: user@domain.tld
 * Rejects invalid formats such as 'ankush', 'ankush@gmail', 'test123'.
 * Accepts valid formats such as 'ankush@gmail.com', 'student@vvit.edu'.
 */

export const EMAIL_REGEX = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;

export const isValidEmail = (email) => {
  if (!email || typeof email !== 'string') return false;
  return EMAIL_REGEX.test(email.trim());
};
