package org.lexem.angmar.errors

/**
 * Generic exception for an IO error.
 */
class AngmarIOException : AngmarException {
    constructor(message: String) : super(message)
    constructor(message: String, exception: Throwable) : super(message, exception)
}