package org.lexem.angmar.analyzer.memory

import org.lexem.angmar.analyzer.data.primitives.*
import org.lexem.angmar.parser.*

/**
 * Interface for an executable value.
 */
internal interface ExecutableValue {

    /**
     * The parserNode associated to the function. A null value means it is internal.
     */
    val parserNode: ParserNode?

    /**
     * The context where the function was defined. A null value means it is internal.
     */
    val parentContext: LxmReference?
}
