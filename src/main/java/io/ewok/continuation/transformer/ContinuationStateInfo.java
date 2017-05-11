package io.ewok.continuation.transformer;

import java.util.List;

import lombok.Builder;
import lombok.Data;
import lombok.Singular;
import spoon.reflect.code.CtBlock;
import spoon.reflect.declaration.CtField;

/**
 * Represents a single state in the invocation.
 *
 * Each state has an entry code block, and a value and exceptional next state.
 *
 * The value state contains the field that the received value is placed into,
 * and the next state.
 *
 * The exceptional next state is the state that should be entered when an
 * exception is received.
 *
 * @author theo
 *
 */

@Builder
@Data
class ContinuationStateInfo {

	/**
	 * The type of state this is.
	 */

	private final ContinuationStateType type;

	/**
	 * When this state is invoked, the parameters that are expected and where
	 * they should be stored.
	 */

	@Singular
	private List<CtField<?>> parameters;

	/**
	 * The statements to execute on entering this state, which will only be
	 * called when all of the expected parameters have been received.
	 */

	private CtBlock<Void> enterBody;

	/**
	 * The state to enter when we receive a success value.
	 */

	private ContinuationStateInfo nextState;

	/**
	 * The exceptional state if we receive an exceptional value.
	 */

	private ContinuationStateInfo exceptionalState;

}
