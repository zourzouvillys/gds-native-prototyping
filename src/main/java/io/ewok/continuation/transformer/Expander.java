package io.ewok.continuation.transformer;

import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;

import io.ewok.continuations.api.async;
import lombok.Getter;
import spoon.reflect.code.CtAssignment;
import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtCatch;
import spoon.reflect.code.CtConditional;
import spoon.reflect.code.CtFieldRead;
import spoon.reflect.code.CtIf;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtLocalVariable;
import spoon.reflect.code.CtReturn;
import spoon.reflect.code.CtTry;
import spoon.reflect.code.CtVariableRead;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtField;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.ModifierKind;
import spoon.reflect.factory.Factory;
import spoon.reflect.reference.CtFieldReference;
import spoon.reflect.reference.CtLocalVariableReference;
import spoon.reflect.reference.CtParameterReference;
import spoon.reflect.visitor.CtScanner;

/**
 * performs static analysis on an async method to generate a set of states,
 * registers, and invocations.
 *
 * the first stage is to detect any calls to async functions, and lift them into
 * variables, replacing the call site with a variable ref.
 *
 * the next step is to run through and calculate the states.
 *
 * @author theo
 *
 */

public class Expander extends CtScanner {

	private final Factory factory;
	private final CtClass<Object> klass;
	private final CtMethod<Boolean> invokeMethod;

	/**
	 * each of the states for this continuation.
	 */

	@Getter
	private final List<ContinuationStateInfo> states = new LinkedList<>();

	/**
	 *
	 */

	@Getter
	private final Stack<ContinuationStateInfo> stack = new Stack<>();

	/**
	 *
	 */

	public Expander(Factory factory, CtClass<Object> klass) {

		this.factory = factory;
		this.klass = klass;

		this.invokeMethod = factory.Method().create(
				klass,
				Sets.newHashSet(ModifierKind.PUBLIC, ModifierKind.FINAL),
				factory.Type().BOOLEAN_PRIMITIVE,
				"invoke",
				null,
				null);

		this.invokeMethod.addAnnotation(
				factory.createAnnotation(this.factory.Annotation().createReference(Override.class)));

		final ContinuationStateInfo starting = ContinuationStateInfo.builder()
				.type(ContinuationStateType.Initial)
				.enterBody(factory.createBlock())
				.build();

		this.states.add(starting);
		this.stack.push(starting);

	}

	/**
	 *
	 * @param reference
	 */

	private void append() {

	}

	/**
	 *
	 * @param reference
	 */
	public <T> void XXX_visitCtParameterReference(final CtParameterReference<T> reference) {

		if (true) {
			super.visitCtParameterReference(reference);
			return;
		}

		final CtFieldReference<T> field = this.factory.Field().createReference(
				this.klass.getReference(),
				reference.getType(),
				"param_" + reference.getSimpleName());

		final CtFieldRead<T> read = this.factory.createFieldRead();

		read.setVariable(field);
		read.setTarget(this.factory.createThisAccess());

		reference.getParent().getParent().replace(read);

		// super.visitCtParameterReference(reference);
	}

	@Override
	public <T> void visitCtVariableRead(final CtVariableRead<T> variableRead) {
		super.visitCtVariableRead(variableRead);
	}

	@Override
	public <T> void visitCtLocalVariableReference(final CtLocalVariableReference<T> reference) {
		super.visitCtLocalVariableReference(reference);
	}

	@Override
	public <T> void visitCtFieldRead(final CtFieldRead<T> fieldRead) {
		super.visitCtFieldRead(fieldRead);
	}

	// ---

	@Override
	public <T> void visitCtLocalVariable(final CtLocalVariable<T> localVariable) {
		super.visitCtLocalVariable(localVariable);
	}

	@Override
	public <T, A extends T> void visitCtAssignment(final CtAssignment<T, A> assignement) {
		System.err.println("XXXXX: " + assignement);
		super.visitCtAssignment(assignement);
	}

	/**
	 * Each invocation is checked to see if it is async.
	 *
	 * If the target is not async, then it is left as is.
	 *
	 * If it is async and returns a value, the variable is changed to a field,
	 * and a state assigned. The invocation is moved to to its own statement
	 * just before this expression.
	 *
	 * If it returns void, then it assigned a state, but not a field.
	 *
	 */

	@Override
	public <T> void visitCtInvocation(final CtInvocation<T> invocation) {


		if (true) {
			super.visitCtInvocation(invocation);
			return;
		}

		if (invocation.getExecutable().getActualMethod().getAnnotation(async.class) == null) {
			// not async. no change.
			System.err.println("Not async: " + invocation.getExecutable());
			super.visitCtInvocation(invocation);
			return;
		}

		// insert the dispatch just before the usage.

		// final CtIf ifstmt = this.factory.createIf();
		// ifstmt.setCondition(this.factory.createLiteral(true));
		// ifstmt.setThenStatement(invocation.clone());
		// invocation.getParent(CtStatement.class).insertBefore(ifstmt);


		if (!invocation.getType().equals(this.factory.Type().VOID_PRIMITIVE)) {

			// lift the invocation to a field.

			final CtField<T> field = this.factory.Field().create(
					this.klass,
					Sets.newHashSet(ModifierKind.PRIVATE),
					invocation.getType(),
					"future_cxx");

			// add the field.
			this.klass.addField(field);

			// now replace the invocation with a reference to the field instead.
			final CtFieldRead<T> read = this.factory.createFieldRead();

			read.setType(invocation.getType());
			read.setVariable(field.getReference());

			invocation.replace(read);

		} else {

			// for a call to a void type, we define a state but don't assign.

		}

		// need to assign state.

	}

	// ---

	@Override
	public <R> void visitCtBlock(final CtBlock<R> block) {
		super.visitCtBlock(block);
	}

	@Override
	public void visitCtIf(final CtIf ifElement) {
		System.err.println(ifElement);
		super.visitCtIf(ifElement);
	}

	@Override
	public <T> void visitCtConditional(final CtConditional<T> conditional) {
		super.visitCtConditional(conditional);
	}

	/**
	 *
	 */

	@Override
	public void visitCtTry(final CtTry tryBlock) {
		super.visitCtTry(tryBlock);
	}

	/**
	 *
	 */

	@Override
	public void visitCtCatch(final CtCatch catchBlock) {
		super.visitCtCatch(catchBlock);
	}

	/**
	 * a return statement in the original block gets converted into calling the
	 * result receiver, then returning the result of the returnValue call.
	 */

	@Override
	public <R> void visitCtReturn(final CtReturn<R> stmt) {

		if (true) {
			super.visitCtReturn(stmt);
			return;
		}

		final CtInvocation<Object> invoke = this.factory.createInvocation();

		invoke.setTarget(this.factory.createSuperAccess());
		invoke.setExecutable(
				this.factory.Executable().createReference(
						this.klass.getReference(),
						this.klass.getReference(),
						"returnValue"));

		invoke.addArgument(stmt.getReturnedExpression());

		final CtReturn<Object> ret = this.factory.createReturn();

		ret.setReturnedExpression(invoke);

		stmt.replace(ret);

	}

	/**
	 * once we have processed all statements in the blocking function, we should
	 * have our set of states. Write them out!
	 */

	public void finish() {

		Preconditions.checkState(this.stack.size() == 1, this.stack.size());

		for (final ContinuationStateInfo state : this.states) {
			System.err.println("STATE: " + state);
		}

	}

}
