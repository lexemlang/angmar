package org.lexem.angmar.errors

/**
 * Generic exception to specify internal unimplemented errors.
 */
class AngmarUnreachableException : AngmarException("Unreachable path. This error must never happened.")
