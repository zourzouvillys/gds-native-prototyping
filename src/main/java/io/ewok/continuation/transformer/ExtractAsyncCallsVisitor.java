package io.ewok.continuation.transformer;

import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import io.ewok.continuations.api.async;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtFieldRead;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtStatement;
import spoon.reflect.code.CtStatementList;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtField;
import spoon.reflect.declaration.CtType;
import spoon.reflect.declaration.ModifierKind;
import spoon.reflect.factory.Factory;
import spoon.reflect.visitor.CtScanner;

/**
 * Runs through each statement and extracts the async calls.
 *
 * @author theo
 */

public class ExtractAsyncCallsVisitor extends CtScanner {

	@Override
	protected void enter(CtElement e) {
		if (e instanceof CtStatement) {
			System.err.println("STMT");
		}
	}

	/**
	 * An invocation can either be a statement on its own, or an expression.
	 * need to treat differently, depending - as using it as a statement without
	 * assigning it's value doesn't need a field to be assigned.
	 */

	@Override
	public <T> void visitCtInvocation(final CtInvocation<T> invocation) {

		if (invocation.getExecutable().getActualMethod().getAnnotation(async.class) == null) {
			// not async. no change.
			System.err.println("Not async: " + invocation.getExecutable());
			super.visitCtInvocation(invocation);
			return;
		}


		final CtStatement stmt = this.getStatement(invocation);
		final Factory f = invocation.getFactory();
		final boolean valueUsed = !(invocation.getParent() instanceof CtStatementList);

		System.err.println("-----------: " + valueUsed);
		System.err.println(invocation);
		System.err.println(invocation.getClass());
		System.err.println(invocation.getParent().getClass());





		// insert the dispatch just before the usage.

		final CtInvocation<Boolean> dispatch = (CtInvocation<Boolean>) invocation.clone();

		//
		final ArrayList<CtExpression<?>> args = Lists.newArrayList(dispatch.getArguments());
		args.add(0, f.createSuperAccess());
		dispatch.setArguments(args);
		dispatch.getExecutable().setSimpleName(dispatch.getExecutable().getSimpleName() + "AsyncFactory");

		//final CtIf ifstmt = f.createIf();
		//ifstmt.setCondition(dispatch);
		//ifstmt.setThenStatement(f.createReturn().setReturnedExpression(read));


		//

		if (!invocation.getType().equals(f.Type().VOID_PRIMITIVE) && valueUsed) {

			stmt.insertBefore(dispatch);

			// field as placeholder for value
			final CtField<T> field = f.Field().create(
					Objects.requireNonNull(invocation.getParent(CtType.class)),
					Sets.newHashSet(ModifierKind.PRIVATE),
					Objects.requireNonNull(invocation.getType()),
					"future_" + Long.toHexString(ThreadLocalRandom.current().nextLong()));

			// add the field.
			invocation.getParent(CtType.class).addField(field);

			//

			// now replace the invocation with a reference to the field instead.
			final CtFieldRead read = f.createFieldRead();

			read.setParent(invocation.getParent());
			read.setType(invocation.getType());
			read.setVariable(field.getReference());

			// now replace the invocation with a reference to the field instead.
			invocation.replace(read);

		} else {

			// for a call to a void type, we define a state but don't assign.

			invocation.replace(dispatch);

		}

		// need to assign state.
		System.err.println("-----------");

	}

	private CtStatement getStatement(CtElement potential) {

		do {

			if (potential.getParent() instanceof CtStatementList && potential instanceof CtStatement) {
				return (CtStatement) potential;
			}

			potential = potential.getParent();

		} while (potential != null);

		throw new RuntimeException();

	}

}
