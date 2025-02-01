package org.checkerframework.checker.dividebyzero;

// import com.sun.source.tree.*;
import java.lang.annotation.Annotation;
import java.util.EnumSet;
import java.util.Set;
import javax.lang.model.type.TypeKind;
import org.checkerframework.checker.dividebyzero.qual.*;
import org.checkerframework.common.basetype.BaseTypeChecker;
import org.checkerframework.common.basetype.BaseTypeVisitor;

import com.sun.source.tree.BinaryTree;
import com.sun.source.tree.CompoundAssignmentTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.Tree;


public class DivByZeroVisitor extends BaseTypeVisitor<DivByZeroAnnotatedTypeFactory> {

  /** Set of operators we care about */
  private static final Set<Tree.Kind> DIVISION_OPERATORS =
      EnumSet.of(
          /* x /  y */ Tree.Kind.DIVIDE,
          /* x /= y */ Tree.Kind.DIVIDE_ASSIGNMENT,
          /* x %  y */ Tree.Kind.REMAINDER,
          /* x %= y */ Tree.Kind.REMAINDER_ASSIGNMENT);

  /**
   * Determine whether to report an error at the given binary AST node. The error text is defined in
   * the messages.properties file.
   *
   * @param node the AST node to inspect
   * @return true if an error should be reported, false otherwise
   */
  private boolean errorAt(BinaryTree node) {
    // A BinaryTree can represent any binary operator, including + or -.
    // TODO

    if (node == null) {
      return false;
    }

    // Only DIVISION_OPERATORS have potential to throw a DB0 error
    if (!DIVISION_OPERATORS.contains(node.getKind())) {
      return false;
    }

    ExpressionTree rightOperand = node.getRightOperand();

    // If denominator is not an integer type, return false
    if (!isInt(rightOperand)) {
      return false;
    }

    // TODO : Recursively check both sides for an error????

    // If the annotation isn't of Non-Zero class, it is an error
    return !hasAnnotation(rightOperand, NonZero.class);
  }

  /**
   * Determine whether to report an error at the given compound assignment AST node. The error text
   * is defined in the messages.properties file.
   *
   * @param node the AST node to inspect
   * @return true if an error should be reported, false otherwise
   */
  private boolean errorAt(CompoundAssignmentTree node) {
    // A CompoundAssignmentTree represents any binary operator combined with an assignment,
    // such as "x += 10".
    // TODO

    if (node == null) {
      return false;
    }

    // Only DIVISION_OPERATORS have potential to throw a DB0 error
    if (!DIVISION_OPERATORS.contains(node.getKind())) {
      return false;
    }

    ExpressionTree expression = node.getExpression();

    // If expression is not an integer type, return false
    if (!isInt(expression)) {
      return false;
    }

    return !hasAnnotation(expression, NonZero.class);
  }

  // ========================================================================
  // Useful helpers

  private static final Set<TypeKind> INT_TYPES = EnumSet.of(TypeKind.INT, TypeKind.LONG);

  private boolean isInt(Tree node) {
    return INT_TYPES.contains(atypeFactory.getAnnotatedType(node).getKind());
  }

  private boolean hasAnnotation(Tree node, Class<? extends Annotation> c) {
    return atypeFactory.getAnnotatedType(node).hasPrimaryAnnotation(c);
  }

  // ========================================================================
  // Checker Framework plumbing

  public DivByZeroVisitor(BaseTypeChecker c) {
    super(c);
  }

  @Override
  public Void visitBinary(BinaryTree node, Void p) {
    if (isInt(node)) {
      if (errorAt(node)) {
        checker.reportError(node, "divide.by.zero");
      }
    }
    return super.visitBinary(node, p);
  }

  @Override
  public Void visitCompoundAssignment(CompoundAssignmentTree node, Void p) {
    if (isInt(node.getExpression())) {
      if (errorAt(node)) {
        checker.reportError(node, "divide.by.zero");
      }
    }
    return super.visitCompoundAssignment(node, p);
  }
}
