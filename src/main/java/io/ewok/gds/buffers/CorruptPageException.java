package io.ewok.gds.buffers;

import io.ewok.gds.buffers.pages.PageId;
import lombok.EqualsAndHashCode;
import lombok.Value;

/**
 * indicates a reference to an invalid page.
 *
 * @author theo
 *
 */

@Value
@EqualsAndHashCode(callSuper = false)
public class CorruptPageException extends RuntimeException {

	/**
	 *
	 */

	private static final long serialVersionUID = 1L;

	private PageId pageId;

}
