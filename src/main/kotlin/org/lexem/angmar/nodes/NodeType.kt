package org.lexem.angmar.nodes

/**
 * Enum with all parser node types.
 */
enum class NodeType {
    // Commons
    Whitespace,
    WhitespaceUntilEOL,
    WhitespaceWithoutEOL,
    CommentSingleLine,
    CommentMultiline,
    Identifier,
    QuotedIdentifier,
    Escape,
    UnicodeEscape,

    // Literals
    Nil,
    Boolean,
    Number,

    // Expressions
    Expression,
    ExpressionList,
    AccessExpression,
    ExpressionElement,
    ExpressionElementWithPostfix,
    SemanticIdentifierList,

    // Expressions Operators
    GraphicOperator,
    InfixOperator,
    PrefixOperator,
    PostfixOperator
}