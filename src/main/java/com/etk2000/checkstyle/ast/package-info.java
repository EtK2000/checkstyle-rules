/**
 * AST helpers, split so a task loads only the half it needs. Routing table:
 *
 * <table>
 * <caption>Which sibling owns what</caption>
 * <tr><th>Class</th><th>Owns</th><th>Reach for it when</th></tr>
 * <tr>
 *   <td>{@link com.etk2000.checkstyle.ast.AstText}</td>
 *   <td>{@code annotationName}, {@code canonicalAnnotation}, {@code canonicalType},
 *       {@code dottedName}, {@code exprText}, {@code findNewClassName},
 *       {@code getEnclosingTypeName}, {@code getPackageName}, {@code lastIdent},
 *       {@code simpleName}, {@code typeName}, {@code typeText}; and the comment-node skip
 *       primitives {@code isCommentToken}, {@code firstRealChild}, {@code nextRealSibling}</td>
 *   <td>you need the source text a node spells, including a package or enclosing-type name; or you
 *       are walking a tree that may carry the comment nodes a {@code WITH_COMMENTS} parse weaves in
 *   </td>
 * </tr>
 * <tr>
 *   <td>{@link com.etk2000.checkstyle.ast.AstDisplay}</td>
 *   <td>{@code displayText}</td>
 *   <td>you are building a violation message that quotes an expression</td>
 * </tr>
 * <tr>
 *   <td>{@link com.etk2000.checkstyle.ast.AstQuery}</td>
 *   <td>navigation ({@code findNodeAt}, {@code firstLine}, {@code lastLine},
 *       {@code firstColumn}, the {@code collect*} family, the {@code unwrap*} family) and
 *       predicates that need no type ({@code isPureExpression}, {@code isSideEffectFree},
 *       {@code isEmptyBody}, {@code hasModifier}, {@code hasSuppressWarnings}, ...)</td>
 *   <td>you are walking or classifying nodes without asking what type anything is</td>
 * </tr>
 * <tr>
 *   <td>{@link com.etk2000.checkstyle.ast.AstResolve}</td>
 *   <td>{@code resolveVariableType}, {@code getReceiverTypeName},
 *       {@code resolveSameFileFieldType}, {@code resolveSameFileMethodReturnType},
 *       {@code sameFileClassDef}, {@code sameFileTypeBody}, {@code supertypeBodies}</td>
 *   <td>you need to know what type a name has. Everything here is a best effort, so a fixer must
 *       not gate a rewrite on it; {@code config/checkstyle/import-control.xml} enforces that</td>
 * </tr>
 * </table>
 *
 * <p>Dependencies run one way only: {@code AstResolve -> AstQuery -> AstText} and
 * {@code AstDisplay -> AstText}. Nothing points back up, so a sibling never drags in the ones
 * above it. {@code AstResolve} is the only one that touches
 * {@code ReflectionUtil}; {@code AstText} and {@code AstQuery} depend on nothing outside
 * checkstyle's own API.
 *
 * <p>That direction is why the comment-node primitives sit in {@code AstText} rather than beside
 * the rest of the navigation family in {@code AstQuery}: {@code AstText} and {@code AstDisplay}
 * both need them and both sit below {@code AstQuery}, so siting them there would cost either two
 * private copies or an {@code AstDisplay -> AstQuery} edge. At the bottom of the chain they cost
 * no new edge at all, and {@code com.etk2000.checkstyle.format} reaches them too.
 */
package com.etk2000.checkstyle.ast;