package io.ewok.continuation.transformer;

import java.util.List;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import io.ewok.continuation.AbstractContinuationImpl;
import io.ewok.continuation.Continuation;
import io.ewok.continuation.ContinuationResultReceiver;
import io.ewok.continuations.api.async;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtNewClass;
import spoon.reflect.code.CtReturn;
import spoon.reflect.code.CtVariableAccess;
import spoon.reflect.declaration.CtAnnotation;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtField;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtParameter;
import spoon.reflect.declaration.ModifierKind;
import spoon.reflect.reference.CtTypeReference;

public class AsyncProcessor extends AbstractProcessor<CtMethod<?>> {

	@Override
	public void process(CtMethod<?> originalMethod) {

		if (originalMethod.getAnnotation(async.class) == null) {
			// method isn't async.
			return;
		}

		// create a new method (same name + Async) that will be used to generate
		// our continuation.
		final CtMethod<Continuation> asyncGenerator = this.getFactory().createMethod();

		asyncGenerator.setParent(originalMethod.getDeclaringType());
		asyncGenerator.setSimpleName(originalMethod.getSimpleName() + "AsyncFactory");
		asyncGenerator.setModifiers(originalMethod.getModifiers());

		// the return value is the Continuation that has been generated.
		asyncGenerator.setType(this.continuationType());

		// the parameters for the method are the same as the original, with the
		// caller context/value receiver as the first param.
		final List<CtParameter<?>> params = Lists.newArrayList(originalMethod.getParameters());

		final CtParameter<?> rev = this.getFactory().createParameter(
				asyncGenerator,
				this.callerTypeRef(),
				"$$$receiver");

		params.add(0, rev);
		asyncGenerator.setParameters(params);

		// generate the class that represents the state machine of the original
		// method.
		final CtClass<Object> impl = this.generateInner(originalMethod);

		// create a method body which returns a new instance
		final CtBlock<Object> body = this.getFactory().createBlock();

		// the expression which generates instance of the continuation.
		final CtNewClass<AbstractContinuationImpl> generator = this.getFactory().createNewClass(
				this.abstractContinuationTypeRef(),
				impl,
				this.getFactory().createVariableRead(rev.getReference(), false));

		// originalMethod.getDeclaringType().addNestedType(impl);

		// generate the return expression
		final CtReturn<AbstractContinuationImpl> ret = this.getFactory().createReturn();
		ret.setReturnedExpression(generator);

		// add to the body
		body.addStatement(ret);

		// set the body of the new generator
		asyncGenerator.setBody(body);

		// add the new method to the declaring class.
		originalMethod.getDeclaringType().addMethod(asyncGenerator);

	}

	/**
	 * The base class of the state
	 */

	private CtTypeReference<AbstractContinuationImpl> abstractContinuationTypeRef() {
		return this.getFactory().Type()
				.createReference(AbstractContinuationImpl.class);
	}

	/**
	 * Generates the type passed into a method.
	 */

	private CtTypeReference<ContinuationResultReceiver> callerTypeRef() {
		return this.getFactory().Type()
				.createReference(ContinuationResultReceiver.class);
	}

	/**
	 * The type of a continuation instance.
	 */

	private CtTypeReference<Continuation> continuationType() {
		return this.getFactory().Type()
				.createReference(Continuation.class);
	}

	/**
	 * Generates an inner class that is used to hold an async state machine
	 * implementation of this method.
	 *
	 * There is a field defined for each variable needed for execution, a set of
	 * labels, and then the map to jump. this keeps code size down as the
	 * abstract class handles the execution logic.
	 *
	 * Each field is assigned an index, which is used to perform a direct Unsafe
	 * call to get/set the value.
	 *
	 */

	private CtClass<Object> generateInner(CtMethod<?> m) {

		final CtClass<Object> anon = this.getFactory().createClass();

		anon.addModifier(ModifierKind.FINAL);
		anon.setSimpleName(m.getSimpleName());

		// add each of the parameters as fields of the class

		// for (final CtParameter<?> param : m.getParameters()) {
		// anon.addField(this.generateFieldForParam(param));
		// }

		// add 'invoke' method, which starts off the execution.
		// it returns a boolean indicating if the invocation completed
		// immediately.
		final CtMethod<?> invokeMethod = this.getFactory().Method().create(
				anon,
				Sets.newHashSet(ModifierKind.PUBLIC, ModifierKind.FINAL),
				this.getFactory().Type().BOOLEAN_PRIMITIVE,
				"invoke",
				null,
				null);

		invokeMethod.addAnnotation(this.getOverrideAnnotation());

		// we run through each statement in the body, and convert it to methods
		// and states. exception blocks also get their own states and blocks.

		final CtBlock<?> block = m.getBody().clone();


		// perform the code conversion
		//		final Expander expander = new Expander(this.getFactory(), anon);
		//		expander.scan(block);


		// set the body
		invokeMethod.setBody(block);

		invokeMethod.setParent(anon);

		new ExtractAsyncCallsVisitor().scan(block);

		anon.addMethod(invokeMethod);

		System.err.println("Finishing");
		//expander.finish();

		return anon;

	}

	private CtAnnotation getOverrideAnnotation() {
		return this.getFactory().createAnnotation(this.getFactory().Annotation().createReference(Override.class));
	}

	private <T> CtField<T> generateFieldForParam(CtParameter<T> param) {
		final CtField<T> field = this.getFactory().createCtField("param_" + param.getSimpleName(), param.getType(),
				null, ModifierKind.PRIVATE,
				ModifierKind.FINAL);
		final CtVariableAccess<T> expr = this.getFactory().createVariableRead(param.getReference(), false);
		field.setDefaultExpression(expr);
		return field;
	}

}
